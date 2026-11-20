package br.com.mariofronza.sigabot.entity.user

import br.com.mariofronza.sigabot.entity.institution.Institution

class AcademicAccount(
    val institution: Institution,
    val loginCiphertext: String,
    val loginDigest: ByteArray,
    val passwordCiphertext: String,
)
