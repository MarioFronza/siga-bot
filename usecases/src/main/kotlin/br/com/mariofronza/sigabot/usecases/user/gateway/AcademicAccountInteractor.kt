package br.com.mariofronza.sigabot.usecases.user.gateway

import br.com.mariofronza.sigabot.entity.institution.InstitutionCode

interface AcademicAccountInteractor {

    fun existsByLoginDigest(institution: InstitutionCode, loginDigest: ByteArray): Boolean
}
