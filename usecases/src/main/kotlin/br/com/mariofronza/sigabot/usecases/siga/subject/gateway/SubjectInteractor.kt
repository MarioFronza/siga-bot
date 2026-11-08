package br.com.mariofronza.sigabot.usecases.siga.subject.gateway

import br.com.mariofronza.sigabot.entity.subject.Subject

interface SubjectInteractor {

    fun findByName(name: String): Subject?

    fun save(subject: Subject): Subject
}
