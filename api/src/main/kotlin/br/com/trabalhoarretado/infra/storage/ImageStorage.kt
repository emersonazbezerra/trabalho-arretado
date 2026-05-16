package br.com.trabalhoarretado.infra.storage

interface ImageStorage {
    suspend fun upload(
        bytes: ByteArray,
        contentType: String,
        key: String,
    ): String
}
