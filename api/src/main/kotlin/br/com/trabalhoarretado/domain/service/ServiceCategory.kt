package br.com.trabalhoarretado.domain.service

enum class ServiceCategory(
    val label: String,
) {
    MASONRY("Alvenaria"),
    ELECTRICAL("Eletricidade"),
    PLUMBING("Hidráulica"),
    PAINTING("Pintura"),
    CLEANING("Limpeza"),
    GARDENING("Jardinagem"),
    CARPENTRY("Marcenaria"),
    MECHANICS("Mecânica"),
}
