package br.com.mariofronza.sigabot.entity.user

class SigaCredential(
    val cpfCiphertext: String,
    val cpfDigest: ByteArray,
    val passwordCiphertext: String,
)
