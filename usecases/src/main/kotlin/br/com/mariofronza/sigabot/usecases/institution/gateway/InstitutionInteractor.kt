package br.com.mariofronza.sigabot.usecases.institution.gateway

import br.com.mariofronza.sigabot.entity.institution.Institution

interface InstitutionInteractor {

    fun findByCode(code: String): Institution?
}
