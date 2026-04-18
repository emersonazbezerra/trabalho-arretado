package br.com.trabalhoarretado.domain

sealed class DomainException(
    message: String,
) : RuntimeException(message)

class EmailAlreadyExistsException : DomainException("Email já está em uso")

class InvalidCredentialsException : DomainException("Email ou senha inválidos")

class NotFoundException(
    resource: String,
) : DomainException("$resource não encontrado")

class ForbiddenException : DomainException("Acesso negado")

class ValidationException(
    message: String,
) : DomainException(message)
