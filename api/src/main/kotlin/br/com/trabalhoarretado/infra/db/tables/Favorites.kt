package br.com.trabalhoarretado.infra.db.tables

import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.datetime.timestamp

object Favorites : UUIDTable("favorites") {
    val clientId = reference("client_id", Users)
    val professionalId = reference("professional_id", Users)
    val createdAt = timestamp("created_at")

    init {
        uniqueIndex(clientId, professionalId)
    }
}
