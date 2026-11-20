package br.com.mariofronza.sigabot.usecases.user.gateway

import br.com.mariofronza.sigabot.entity.institution.Institution

interface AcademicAccountInteractor {

    fun existsByLoginDigest(institution: Institution, loginDigest: ByteArray): Boolean
}
