package br.com.mariofronza.sigabot.usecases.institution.gateway

import br.com.mariofronza.sigabot.entity.institution.Institution
import br.com.mariofronza.sigabot.entity.institution.InstitutionCode

interface InstitutionInteractor {

    fun findByCode(code: InstitutionCode): Institution?
}
