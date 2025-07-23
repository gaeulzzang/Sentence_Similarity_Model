package com.sentence.similarity.presentation

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ml.shubham0204.sentence_embeddings.SentenceEmbedding
import com.sentence.similarity.proto.ProtoVectorStorage
import com.sentence.similarity.type.ChunkVector
import com.sentence.similarity.type.SearchResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import javax.inject.Inject
import kotlin.math.pow
import kotlin.math.sqrt

@HiltViewModel
class SentenceViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val protoVectorStorage: ProtoVectorStorage
) : ViewModel() {

    private val llamaChunks = readPageContentsFromJsonl(LLAMA_JSON)
    private val qwenChunks = readPageContentsFromJsonl(QWEN_JSON)
    private val vectorDB = mutableStateListOf<ChunkVector>()

    private lateinit var sentenceEmbedding: SentenceEmbedding

    private val _initializedState = MutableStateFlow(false)
    val initializedState: StateFlow<Boolean> = _initializedState

    private val _searchResultsState = MutableStateFlow<List<SearchResult>>(emptyList())
    val searchResultsState: StateFlow<List<SearchResult>> = _searchResultsState

    private val _searchTimeState = MutableStateFlow(0L)
    val searchTimeState: StateFlow<Long> = _searchTimeState

    init {
        initialize()
    }

    private fun initialize() {
        viewModelScope.launch(Dispatchers.IO) {
            prepareEmbeddingModel()
            initializeEmbedding()
            _initializedState.value = true
        }
    }

    private fun prepareEmbeddingModel() {
        val requiredFiles = listOf(MODEL_FILE, TOKENIZER_FILE)

        requiredFiles.forEach { name ->
            val outFile = File(context.filesDir, name)
            if (!outFile.exists()) {
                context.assets.open(name).use { input ->
                    outFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }
    }

    private suspend fun initializeEmbedding() {
        val modelFile = File(context.filesDir, MODEL_FILE)
        val tokenizerFile = File(context.filesDir, TOKENIZER_FILE)
        val tokenizerBytes = tokenizerFile.readBytes()

        val embedding = SentenceEmbedding()
        embedding.init(
            modelFilepath = modelFile.absolutePath,
            tokenizerBytes = tokenizerBytes,
            useTokenTypeIds = true,
            outputTensorName = "sentence_embedding",
            useFP16 = false,
            useXNNPack = false,
            normalizeEmbeddings = true
        )

        sentenceEmbedding = embedding
        loadOrBuildVectorDB()
    }

    private fun loadOrBuildVectorDB() {
        viewModelScope.launch(Dispatchers.IO) {
            val cached = protoVectorStorage.load()
            if (cached != null) {
                vectorDB.clear()
                vectorDB.addAll(cached)
                return@launch
            }

            val allChunks = llamaChunks + qwenChunks
            val result = buildVectorDB(allChunks)
            vectorDB.clear()
            vectorDB.addAll(result)
            protoVectorStorage.save(result)
        }
    }

    private suspend fun buildVectorDB(chunks: List<String>): List<ChunkVector> {
        return chunks.mapIndexed { index, text ->
            ChunkVector(
                id = index,
                content = text,
                embedding = sentenceEmbedding.encode(text)
            )
        }
    }

    fun search(query: String, topN: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val (results, elapsed) = searchTopN(query, topN)
            _searchResultsState.value = results
            _searchTimeState.value = elapsed / 1_000_000 // ns → ms
        }
    }

    private suspend fun searchTopN(query: String, topN: Int): Pair<List<SearchResult>, Long> {
        val startTime = System.nanoTime()

        val queryVector = sentenceEmbedding.encode(query)
        val results = vectorDB.map {
            val similarity = cosineSimilarity(queryVector, it.embedding)
            SearchResult(id = it.id, content = it.content, similarity = similarity)
        }.sortedByDescending { it.similarity }
            .take(topN)

        val elapsed = System.nanoTime() - startTime
        return results to elapsed
    }

    private fun cosineSimilarity(x1: FloatArray, x2: FloatArray): Float {
        var mag1 = 0.0f
        var mag2 = 0.0f
        var dot = 0.0f
        for (i in x1.indices) {
            mag1 += x1[i].pow(2)
            mag2 += x2[i].pow(2)
            dot += x1[i] * x2[i]
        }
        return dot / (sqrt(mag1) * sqrt(mag2))
    }

    private fun readPageContentsFromJsonl(filename: String): List<String> {
        val result = mutableListOf<String>()
        context.assets.open(filename).bufferedReader().useLines { lines ->
            lines.forEach { line ->
                if (line.isNotBlank()) {
                    try {
                        val json = JSONObject(line)
                        val content = json.getString(JSON_KEY)
                        result.add(content)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        return result
    }

    companion object {
        private const val JSON_KEY = "page_content"
        private const val MODEL_FILE = "snowflake-quantized-model.onnx"
        private const val TOKENIZER_FILE = "snowflake-tokenizer.json"
        private const val LLAMA_JSON = "llama4.jsonl"
        private const val QWEN_JSON = "qwen3.jsonl"
    }
}
