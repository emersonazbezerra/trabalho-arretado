package br.com.trabalhoarretado

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform