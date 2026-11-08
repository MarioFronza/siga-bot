package br.com.mariofronza.sigabot.entity.user

class SigaCredential(
    val id: Long? = null,
    val cpfHash: String,
    val passwordHash: String,
)
