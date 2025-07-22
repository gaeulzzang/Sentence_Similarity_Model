package com.sentence.similarity.presentation

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ml.shubham0204.sentence_embeddings.SentenceEmbedding
import com.sentence.similarity.type.ChunkVector
import com.sentence.similarity.type.SearchResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import javax.inject.Inject
import kotlin.math.pow
import kotlin.math.sqrt

@HiltViewModel
class SentenceViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val llamaChunks = readPageContentsFromJsonl("llama4.jsonl")
    private val qwenChunks = readPageContentsFromJsonl("qwen3.jsonl")
    private val vectorDB = mutableStateListOf<ChunkVector>()

    private lateinit var sentenceEmbedding: SentenceEmbedding

    private val _initializedState = MutableStateFlow(false)
    val initializedState: StateFlow<Boolean> = _initializedState

    private val _searchResultsState = MutableStateFlow<List<SearchResult>>(emptyList())
    val searchResultsState: StateFlow<List<SearchResult>> = _searchResultsState

    private val _searchTimeState = MutableStateFlow(0L)
    val searchTimeState: StateFlow<Long> = _searchTimeState

    init {
        initializeEmbedding()
    }

    private fun initializeEmbedding() {
        viewModelScope.launch(Dispatchers.IO) {
            copyAssetsIfNeeded(context)
            val modelFile = File(context.filesDir, "snowflake-quantized-model.onnx")
            val tokenizerFile = File(context.filesDir, "snowflake-tokenizer.json")

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
            _initializedState.value = true
            buildAndStoreVectorDB()
        }
    }

    private suspend fun copyAssetsIfNeeded(context: Context) {
        withContext(Dispatchers.IO) {
            val filenames = listOf("snowflake-quantized-model.onnx", "snowflake-tokenizer.json")
            for (name in filenames) {
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
    }

    private fun buildAndStoreVectorDB() {
        viewModelScope.launch(Dispatchers.IO) {
            val allChunks = llamaChunks + qwenChunks
            val result = buildVectorDB(sentenceEmbedding, allChunks)
            vectorDB.clear()
            vectorDB.addAll(result)
        }
    }

    private suspend fun buildVectorDB(
        embedding: SentenceEmbedding,
        chunks: List<String>
    ): List<ChunkVector> {
        return chunks.mapIndexed { index, text ->
            val vector = embedding.encode(text)
            ChunkVector(
                id = index,
                content = text,
                embedding = vector
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
            val sim = cosineSimilarity(queryVector, it.embedding)
            SearchResult(id = it.id, content = it.content, similarity = sim)
        }.sortedByDescending { it.similarity }
            .take(topN)

        val elapsed = System.nanoTime() - startTime
        return results to elapsed
    }

    private fun cosineSimilarity(
        x1: FloatArray,
        x2: FloatArray
    ): Float {
        var mag1 = 0.0f
        var mag2 = 0.0f
        var product = 0.0f
        for (i in x1.indices) {
            mag1 += x1[i].pow(2)
            mag2 += x2[i].pow(2)
            product += x1[i] * x2[i]
        }
        mag1 = sqrt(mag1)
        mag2 = sqrt(mag2)
        return product / (mag1 * mag2)
    }

    private fun readPageContentsFromJsonl(filename: String): List<String> {
        val result = mutableListOf<String>()

        context.assets.open(filename).bufferedReader().useLines { lines ->
            lines.forEach { line ->
                if (line.isNotBlank()) {
                    try {
                        val jsonObject = JSONObject(line)
                        val content = jsonObject.getString("page_content")
                        result.add(content)
                    } catch (e: Exception) {
                        // 파싱 실패한 줄 로그 출력
                        e.printStackTrace()
                    }
                }
            }
        }

        return result
    }
}
