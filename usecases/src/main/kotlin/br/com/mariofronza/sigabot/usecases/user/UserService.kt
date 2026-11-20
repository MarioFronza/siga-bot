package br.com.mariofronza.sigabot.usecases.user

import br.com.mariofronza.sigabot.entity.user.SigaCredential
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.usecases.misc.cryptograpy.CryptographyService
import br.com.mariofronza.sigabot.usecases.user.dto.CreateUserInput
import br.com.mariofronza.sigabot.usecases.user.dto.CreateUserOutput
import br.com.mariofronza.sigabot.usecases.user.dto.SigaCredentialRequest
import br.com.mariofronza.sigabot.usecases.user.gateway.SigaCredentialInteractor
import br.com.mariofronza.sigabot.usecases.user.gateway.UserInteractor
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
    val userInteractor: UserInteractor,
    val roleService: RoleService,
    val sigaCredentialInteractor: SigaCredentialInteractor,
    val bCryptPasswordEncoder: BCryptPasswordEncoder,
    val cryptographyService: CryptographyService
) {

    fun findAllActiveUsers(): List<User> {
        return userInteractor.findAllActive()
    }

    fun findById(id: Long): User {
        return userInteractor.findById(id) ?: throw Exception("User not found")
    }

    fun createUser(request: CreateUserInput): CreateUserOutput {
        val email = request.email.lowercase()
        val cpf = request.sigaCredential.cpf

        if (userInteractor.findByEmail(email) != null) {
            throw Exception("User already exists")
        }

        if (sigaCredentialInteractor.existsByCpfDigest(cpfDigest(cpf))) {
            throw Exception("Siga credential already exists")
        }

        val role = roleService.findByName(STUDENT_ROLE) ?: throw Exception("Role $STUDENT_ROLE not found")

        val user = userInteractor.save(
            User(
                email = email,
                isActive = true,
                passwordHash = bCryptPasswordEncoder.encode(request.password)!!,
                roles = listOf(role),
                sigaCredential = encryptSigaCredential(request.sigaCredential),
            )
        )

        return CreateUserOutput(
            id = user.id!!,
            username = user.username,
            email = user.email
        )
    }

    private fun encryptSigaCredential(sigaCredentialRequest: SigaCredentialRequest): SigaCredential {
        return SigaCredential(
            cpfCiphertext = cryptographyService.encrypt(sigaCredentialRequest.cpf),
            cpfDigest = cpfDigest(sigaCredentialRequest.cpf),
            passwordCiphertext = cryptographyService.encrypt(sigaCredentialRequest.password)
        )
    }

    private fun cpfDigest(cpf: String) = cryptographyService.digest(cpf.filter(Char::isDigit))

    companion object {
        const val STUDENT_ROLE = "STUDENT"
    }
}
