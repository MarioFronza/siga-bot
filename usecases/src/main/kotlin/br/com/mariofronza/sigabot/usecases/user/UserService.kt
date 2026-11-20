package br.com.mariofronza.sigabot.usecases.user

import br.com.mariofronza.sigabot.entity.institution.Institution
import br.com.mariofronza.sigabot.entity.user.AcademicAccount
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.usecases.credential.gateway.PasswordHasher
import br.com.mariofronza.sigabot.usecases.credential.gateway.SecretCipher
import br.com.mariofronza.sigabot.usecases.user.dto.CreateUserInput
import br.com.mariofronza.sigabot.usecases.user.dto.SigaCredentialRequest
import br.com.mariofronza.sigabot.usecases.user.dto.UserOutput
import br.com.mariofronza.sigabot.usecases.institution.gateway.InstitutionInteractor
import br.com.mariofronza.sigabot.usecases.user.gateway.AcademicAccountInteractor
import br.com.mariofronza.sigabot.usecases.user.gateway.UserInteractor
import org.springframework.stereotype.Service
import br.com.mariofronza.sigabot.entity.user.Cpf
import br.com.mariofronza.sigabot.entity.user.Email
import br.com.mariofronza.sigabot.entity.user.UserId

@Service
class UserService(
    val userInteractor: UserInteractor,
    val roleService: RoleService,
    val institutionInteractor: InstitutionInteractor,
    val academicAccountInteractor: AcademicAccountInteractor,
    val passwordHasher: PasswordHasher,
    val secretCipher: SecretCipher,
) {

    fun findAllActiveUsers(): List<User> {
        return userInteractor.findAllActive()
    }

    fun findById(id: UserId): User = userInteractor.findById(id) ?: throw UserNotFoundException(id)

    fun createUser(request: CreateUserInput): UserOutput {
        val email = Email.of(request.email)
        val cpf = Cpf.of(request.sigaCredential.cpf)

        if (userInteractor.findByEmail(email) != null) {
            throw EmailAlreadyRegisteredException()
        }

        val institution = institutionInteractor.findByCode(Institution.UDESC)
            ?: throw Exception("Institution ${Institution.UDESC} not found")

        if (academicAccountInteractor.existsByLoginDigest(institution, secretCipher.digest(cpf.value))) {
            throw AcademicAccountAlreadyRegisteredException()
        }

        val role = roleService.findByName(STUDENT_ROLE) ?: throw Exception("Role $STUDENT_ROLE not found")

        val user = userInteractor.save(
            User(
                email = email,
                isActive = true,
                passwordHash = passwordHasher.hash(request.password),
                roles = listOf(role),
                academicAccounts = listOf(encryptAcademicAccount(institution, request.sigaCredential)),
            )
        )

        return UserOutput.from(user)
    }

    private fun encryptAcademicAccount(institution: Institution, request: SigaCredentialRequest): AcademicAccount {
        return AcademicAccount(
            institution = institution,
            loginCiphertext = secretCipher.encrypt(request.cpf),
            loginDigest = secretCipher.digest(Cpf.of(request.cpf).value),
            passwordCiphertext = secretCipher.encrypt(request.password)
        )
    }

    companion object {
        const val STUDENT_ROLE = "STUDENT"
    }
}
