package com.sentence.similarity.type

data class ChunkVector(
    val id: Int,
    val content: String,
    val embedding: FloatArray
)
