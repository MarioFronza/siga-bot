package br.com.mariofronza.sigabot.usecases.identity.port

import br.com.mariofronza.sigabot.entity.institution.Institution
import br.com.mariofronza.sigabot.entity.institution.InstitutionCode

interface InstitutionRepository {

    fun findByCode(code: InstitutionCode): Institution?
}
