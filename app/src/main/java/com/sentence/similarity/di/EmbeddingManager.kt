package com.sentence.similarity.di

import android.content.Context
import com.ml.shubham0204.sentence_embeddings.SentenceEmbedding
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmbeddingManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var embedding: SentenceEmbedding? = null

    suspend fun initialize() {
        if (embedding != null) return

        prepareEmbeddingModel()

        val modelFile = File(context.filesDir, MODEL_FILE)
        val tokenizerFile = File(context.filesDir, TOKENIZER_FILE)
        val tokenizerBytes = tokenizerFile.readBytes()

        val instance = SentenceEmbedding()
        instance.init(
            modelFilepath = modelFile.absolutePath,
            tokenizerBytes = tokenizerBytes,
            useTokenTypeIds = true,
            outputTensorName = "sentence_embedding",
            useFP16 = false,
            useXNNPack = false,
            normalizeEmbeddings = true
        )

        embedding = instance
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

    suspend fun encode(text: String): FloatArray {
        if (embedding == null) {
            initialize()
        }

        return embedding?.encode(text) ?: throw IllegalStateException("Embedding not initialized")
    }

    companion object {
        private const val MODEL_FILE = "snowflake-quantized-model.onnx"
        private const val TOKENIZER_FILE = "snowflake-tokenizer.json"
    }
}
