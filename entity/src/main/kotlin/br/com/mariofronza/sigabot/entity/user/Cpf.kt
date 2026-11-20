package br.com.mariofronza.sigabot.entity.user

@JvmInline
value class Cpf private constructor(val value: String) {

    companion object {
        fun of(raw: String): Cpf {
            val digits = raw.filter(Char::isDigit)
            require(digits.isNotEmpty()) { "CPF must have digits" }
            return Cpf(digits)
        }
    }
}
