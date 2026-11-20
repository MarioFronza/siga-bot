package br.com.mariofronza.sigabot.usecases.identity.port

import br.com.mariofronza.sigabot.entity.user.User

fun interface AccessTokenIssuer {

    fun issue(user: User): String
}
