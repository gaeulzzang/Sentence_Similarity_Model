package com.sentence.similarity.type

data class ChunkVector(
    val id: Int,
    val content: String,
    val embedding: FloatArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChunkVector

        if (id != other.id) return false
        if (content != other.content) return false
        if (!embedding.contentEquals(other.embedding)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + content.hashCode()
        result = 31 * result + embedding.contentHashCode()
        return result
    }
}
