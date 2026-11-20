package br.com.mariofronza.sigabot.entity.user

@JvmInline
value class Email private constructor(val value: String) {

    override fun toString() = value

    companion object {
        fun of(raw: String): Email {
            val normalized = raw.trim().lowercase()
            require('@' in normalized) { "Email must have an @" }
            return Email(normalized)
        }
    }
}
