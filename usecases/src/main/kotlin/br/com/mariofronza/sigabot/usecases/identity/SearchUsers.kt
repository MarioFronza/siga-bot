package br.com.mariofronza.sigabot.usecases.identity

import br.com.mariofronza.sigabot.entity.user.User
import br.com.mariofronza.sigabot.usecases.identity.port.UserRepository
import br.com.mariofronza.sigabot.usecases.shared.Page
import br.com.mariofronza.sigabot.usecases.shared.PageRequest

class SearchUsers(private val users: UserRepository) {

    operator fun invoke(query: String, request: PageRequest): Page<User> = users.search(query.trim(), request)
}
