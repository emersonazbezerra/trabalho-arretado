package br.com.trabalhoarretado.infra.db.tables

import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.datetime.timestamp

object Users : UUIDTable("users") {
    val name = varchar("name", 255)
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val role = varchar("role", 20)
    val city = varchar("city", 100).nullable()
    val state = varchar("state", 2).default("PB")
    val phone = varchar("phone", 20).nullable()
    val avatarUrl = varchar("avatar_url", 500).nullable()
    val createdAt = timestamp("created_at")
}
