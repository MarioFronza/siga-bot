package br.com.mariofronza.sigabot.persistence.user.gateway

import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.persistence.misc.toJPA
import br.com.mariofronza.sigabot.persistence.user.repository.UserRepository
import br.com.mariofronza.sigabot.usecases.user.gateway.UserInteractor
import org.springframework.stereotype.Component

@Component
class UserRepositoryInteractor(
    private val userRepository: UserRepository
) : UserInteractor {

    override fun findAll(): List<User> {
        return userRepository.findAll().map { it.toEntity() }
    }

    override fun findAllActiveUsers(): List<User> {
        return userRepository.findByIsActiveTrue().map { it.toEntity() }
    }

    override fun findById(id: Long): User? {
        return userRepository.findById(id).orElse(null).toEntity()
    }

    override fun findByUsername(username: String): User {
        return userRepository.findByUsername(username).toEntity()
    }

    override fun findByEmail(email: String): User? {
        return userRepository.findByEmail(email)?.toEntity()
    }

    override fun save(user: User): User {
        return userRepository.save(user.toJPA()).toEntity()
    }
}
