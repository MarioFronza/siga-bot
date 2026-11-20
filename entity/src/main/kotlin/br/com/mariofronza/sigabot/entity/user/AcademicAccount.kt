package br.com.mariofronza.sigabot.entity.user

import br.com.mariofronza.sigabot.entity.institution.InstitutionCode

class AcademicAccount(
    val institution: InstitutionCode,
    val loginCiphertext: String,
    val loginDigest: ByteArray,
    val passwordCiphertext: String,
)
