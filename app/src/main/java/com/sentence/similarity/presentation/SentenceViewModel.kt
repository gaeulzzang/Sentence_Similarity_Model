package com.sentence.similarity.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sentence.similarity.di.EmbeddingManager
import com.sentence.similarity.di.ProtoVectorStorage
import com.sentence.similarity.type.ChunkVector
import com.sentence.similarity.type.SearchResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject
import kotlin.math.pow
import kotlin.math.sqrt

@HiltViewModel
class SentenceViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val embeddingManager: EmbeddingManager,
    private val protoVectorStorage: ProtoVectorStorage
) : ViewModel() {

    private val llamaChunks = readPageContentsFromJsonl(LLAMA_JSON)
    private val qwenChunks = readPageContentsFromJsonl(QWEN_JSON)

    private val _vectorDBState = MutableStateFlow<List<ChunkVector>>(emptyList())

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
            embeddingManager.initialize()
            _initializedState.value = true
            loadOrBuildVectorDB()
        }
    }

    private fun loadOrBuildVectorDB() {
        viewModelScope.launch(Dispatchers.IO) {
            val cached = protoVectorStorage.load()
            if (cached != null) {
                _vectorDBState.value = cached
                return@launch
            }

            val allChunks = llamaChunks + qwenChunks
            val result = buildVectorDB(allChunks)
            _vectorDBState.value = result
            protoVectorStorage.save(result)
        }
    }

    private suspend fun buildVectorDB(chunks: List<String>): List<ChunkVector> {
        return chunks.mapIndexed { index, text ->
            ChunkVector(
                id = index,
                content = text,
                embedding = embeddingManager.encode(text)
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

        val queryVector = embeddingManager.encode(query)
        val results = _vectorDBState.value.map {
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
        private const val LLAMA_JSON = "llama4.jsonl"
        private const val QWEN_JSON = "qwen3.jsonl"
    }
}
