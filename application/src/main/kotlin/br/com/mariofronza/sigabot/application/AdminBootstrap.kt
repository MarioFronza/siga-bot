package br.com.mariofronza.sigabot.application

import br.com.mariofronza.sigabot.usecases.identity.GrantAdmins
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class AdminBootstrap(
    private val grantAdmins: GrantAdmins,
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        grantAdmins()
    }
}
