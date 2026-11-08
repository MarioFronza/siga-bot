package br.com.mariofronza.sigabot.usecases.user

import br.com.mariofronza.sigabot.entity.user.SigaCredential
import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.usecases.misc.cryptograpy.CryptographyService
import br.com.mariofronza.sigabot.usecases.misc.filter.UserDetailsImpl
import br.com.mariofronza.sigabot.usecases.user.dto.CreateUserInput
import br.com.mariofronza.sigabot.usecases.user.dto.CreateUserOutput
import br.com.mariofronza.sigabot.usecases.user.dto.SigaCredentialRequest
import br.com.mariofronza.sigabot.usecases.user.dto.UserPeriodsOutput
import br.com.mariofronza.sigabot.usecases.user.gateway.SigaCredentialInteractor
import br.com.mariofronza.sigabot.usecases.user.gateway.UserInteractor
import org.springframework.security.core.context.SecurityContextHolder
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
        return userInteractor.findAllActiveUsers()
    }

    fun findById(id: Long): User {
        return userInteractor.findById(id) ?: throw Exception("User not found")
    }

    fun findUserPeriods(): UserPeriodsOutput {
        val user = getCurrentUser()

        return UserPeriodsOutput(
            username = user.username,
            periods = user.groups
                .map { it.period }
                .sortedWith(compareBy({ it.year }, { it.term }))
                .reversed()
                .toSet()
        )
    }

    fun createUser(request: CreateUserInput): CreateUserOutput {
        val email = request.email
        val cpf = request.sigaCredential.cpf

        if (userInteractor.findByEmail(email) != null) {
            throw Exception("User already exists")
        }

        if (sigaCredentialInteractor.findByCpf(cpf) != null) {
            throw Exception("Siga credential already exists")
        }

        val role = roleService.findByName(STUDENT_ROLE) ?: roleService.createRole(STUDENT_ROLE)

        val sigaCredential = generateEncryptedSigaCredential(request.sigaCredential)

        val user = userInteractor.save(
            User(
                email = request.email,
                isActive = true,
                passwordHash = bCryptPasswordEncoder.encode(request.password)!!,
                roles = listOf(role),
                sigaCredential = sigaCredential,
            )
        )

        return CreateUserOutput(
            id = user.id!!,
            username = user.username,
            email = user.email
        )
    }

    private fun generateEncryptedSigaCredential(sigaCredentialRequest: SigaCredentialRequest): SigaCredential {
        return SigaCredential(
            cpfHash = cryptographyService.encrypt(sigaCredentialRequest.cpf),
            passwordHash = cryptographyService.encrypt(sigaCredentialRequest.password)
        )
    }

    private fun getCurrentUser(): User {
        val currentUser = SecurityContextHolder.getContext().authentication!!.principal as UserDetailsImpl
        return currentUser.toUser()
    }

    companion object {
        const val STUDENT_ROLE = "STUDENT"
    }
}
