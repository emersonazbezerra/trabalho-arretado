package br.com.trabalhoarretado.infra.db.tables

import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.datetime.timestamp

object Reviews : UUIDTable("reviews") {
    val clientId = reference("client_id", Users)
    val professionalId = reference("professional_id", Users)
    val rating = integer("rating")
    val comment = text("comment").nullable()
    val createdAt = timestamp("created_at")

    init {
        uniqueIndex(clientId, professionalId)
    }
}
