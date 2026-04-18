package br.com.trabalhoarretado.infra.db.tables

import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.datetime.timestamp

object Services : UUIDTable("services") {
    val professionalId = reference("professional_id", Users)
    val title = varchar("title", 255)
    val description = text("description").nullable()
    val estimatedPrice = decimal("estimated_price", 10, 2).nullable()
    val category = varchar("category", 100)
    val createdAt = timestamp("created_at")
}
