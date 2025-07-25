package com.sentence.similarity.di

import android.content.Context
import chunk.ChunkVector.ChunkVectorList
import chunk.ChunkVector.ChunkVectorProto
import com.sentence.similarity.type.ChunkVector
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

class ProtoVectorStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val file = File(context.filesDir, VECTOR_DB_FILE)

    fun save(vectorDB: List<ChunkVector>) {
        val protoList = ChunkVectorList.newBuilder()
        for (chunk in vectorDB) {
            val proto = ChunkVectorProto.newBuilder()
                .setId(chunk.id)
                .setContent(chunk.content)
                .addAllEmbedding(chunk.embedding.toList())
                .build()
            protoList.addVectors(proto)
        }
        file.outputStream().use {
            protoList.build().writeTo(it)
        }
    }

    fun load(): List<ChunkVector>? {
        if (!file.exists()) return null

        return try {
            val protoList = ChunkVectorList.parseFrom(file.inputStream())
            protoList.vectorsList.map {
                ChunkVector(
                    id = it.id,
                    content = it.content,
                    embedding = it.embeddingList.map { f -> f.toFloat() }.toFloatArray()
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun exists(): Boolean = file.exists()

    companion object {
        private const val VECTOR_DB_FILE = "vector_db.pb"
    }
}
