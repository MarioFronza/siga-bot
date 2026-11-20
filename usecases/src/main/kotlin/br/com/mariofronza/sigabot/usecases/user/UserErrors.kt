package br.com.mariofronza.sigabot.usecases.user

import java.util.UUID

class EmailAlreadyRegisteredException : RuntimeException("User already exists")

class AcademicAccountAlreadyRegisteredException : RuntimeException("Siga credential already exists")

class UserNotFoundException(externalId: UUID) : RuntimeException("User $externalId not found")
