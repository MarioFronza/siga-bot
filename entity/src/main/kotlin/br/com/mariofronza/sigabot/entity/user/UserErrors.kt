package br.com.mariofronza.sigabot.entity.user

import br.com.mariofronza.sigabot.entity.institution.InstitutionCode

sealed class UserException(message: String) : RuntimeException(message)

class EmailAlreadyRegisteredException : UserException("User already exists")

class AcademicAccountAlreadyRegisteredException : UserException("Siga credential already exists")

class UserNotFoundException(id: UserId) : UserException("User $id not found")

class NoAcademicAccountException(id: UserId, institution: InstitutionCode) :
    UserException("User $id has no academic account at $institution")
