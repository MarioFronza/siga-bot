package br.com.mariofronza.sigabot.usecases.user

import br.com.mariofronza.sigabot.entity.user.UserId

class EmailAlreadyRegisteredException : RuntimeException("User already exists")

class AcademicAccountAlreadyRegisteredException : RuntimeException("Siga credential already exists")

class UserNotFoundException(id: UserId) : RuntimeException("User $id not found")
