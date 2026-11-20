package br.com.mariofronza.sigabot.usecases.user

import br.com.mariofronza.sigabot.entity.institution.Institution
import br.com.mariofronza.sigabot.entity.user.AcademicAccount
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.usecases.misc.cryptograpy.CryptographyService
import br.com.mariofronza.sigabot.usecases.user.dto.CreateUserInput
import br.com.mariofronza.sigabot.usecases.user.dto.SigaCredentialRequest
import br.com.mariofronza.sigabot.usecases.user.dto.UserOutput
import br.com.mariofronza.sigabot.usecases.institution.gateway.InstitutionInteractor
import br.com.mariofronza.sigabot.usecases.user.gateway.AcademicAccountInteractor
import br.com.mariofronza.sigabot.usecases.user.gateway.UserInteractor
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserService(
    val userInteractor: UserInteractor,
    val roleService: RoleService,
    val institutionInteractor: InstitutionInteractor,
    val academicAccountInteractor: AcademicAccountInteractor,
    val bCryptPasswordEncoder: BCryptPasswordEncoder,
    val cryptographyService: CryptographyService
) {

    fun findAllActiveUsers(): List<User> {
        return userInteractor.findAllActive()
    }

    fun findByExternalId(externalId: UUID): User {
        return userInteractor.findByExternalId(externalId) ?: throw UserNotFoundException(externalId)
    }

    fun createUser(request: CreateUserInput): UserOutput {
        val email = request.email.lowercase()
        val cpf = request.sigaCredential.cpf

        if (userInteractor.findByEmail(email) != null) {
            throw EmailAlreadyRegisteredException()
        }

        val institution = institutionInteractor.findByCode(Institution.UDESC)
            ?: throw Exception("Institution ${Institution.UDESC} not found")

        if (academicAccountInteractor.existsByLoginDigest(institution, cpfDigest(cpf))) {
            throw AcademicAccountAlreadyRegisteredException()
        }

        val role = roleService.findByName(STUDENT_ROLE) ?: throw Exception("Role $STUDENT_ROLE not found")

        val user = userInteractor.save(
            User(
                email = email,
                isActive = true,
                passwordHash = bCryptPasswordEncoder.encode(request.password)!!,
                roles = listOf(role),
                academicAccounts = listOf(encryptAcademicAccount(institution, request.sigaCredential)),
            )
        )

        return UserOutput.from(user)
    }

    private fun encryptAcademicAccount(institution: Institution, request: SigaCredentialRequest): AcademicAccount {
        return AcademicAccount(
            institution = institution,
            loginCiphertext = cryptographyService.encrypt(request.cpf),
            loginDigest = cpfDigest(request.cpf),
            passwordCiphertext = cryptographyService.encrypt(request.password)
        )
    }

    private fun cpfDigest(cpf: String) = cryptographyService.digest(cpf.filter(Char::isDigit))

    companion object {
        const val STUDENT_ROLE = "STUDENT"
    }
}
