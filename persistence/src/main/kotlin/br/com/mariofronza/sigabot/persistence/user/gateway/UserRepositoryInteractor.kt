package br.com.mariofronza.sigabot.persistence.user.gateway

import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.persistence.institution.repository.InstitutionRepository
import br.com.mariofronza.sigabot.persistence.user.entity.AcademicAccountJPA
import br.com.mariofronza.sigabot.persistence.user.entity.UserJPA
import br.com.mariofronza.sigabot.persistence.user.repository.RoleRepository
import br.com.mariofronza.sigabot.persistence.user.repository.UserRepository
import br.com.mariofronza.sigabot.usecases.user.gateway.UserInteractor
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
@Transactional
class UserRepositoryInteractor(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val institutionRepository: InstitutionRepository,
) : UserInteractor {

    override fun findAllActive(): List<User> = userRepository.findByActiveTrue().map { it.toEntity() }

    override fun findByExternalId(externalId: UUID): User? = userRepository.findByExternalId(externalId)?.toEntity()

    override fun findByEmail(email: String): User? = userRepository.findByEmail(email)?.toEntity()

    override fun save(user: User): User {
        val saved = user.id
            ?.let { userRepository.findById(it).orElseThrow().apply { update(user) } }
            ?: userRepository.save(create(user))
        return user.copy(id = saved.id, externalId = saved.externalId)
    }

    private fun UserJPA.update(user: User) {
        email = user.email
        passwordHash = user.passwordHash
        fullName = user.fullName
        active = user.isActive
    }

    private fun create(user: User) = UserJPA(
        email = user.email,
        passwordHash = user.passwordHash,
        fullName = user.fullName,
        active = user.isActive,
        roles = user.roles.map { roleRepository.getReferenceById(it.id!!) }.toMutableSet(),
    ).also { entity ->
        user.academicAccounts.mapTo(entity.academicAccounts) {
            AcademicAccountJPA(
                user = entity,
                institution = institutionRepository.getReferenceById(it.institution.id!!),
                loginCiphertext = it.loginCiphertext,
                loginDigest = it.loginDigest,
                passwordCiphertext = it.passwordCiphertext,
            )
        }
    }
}
