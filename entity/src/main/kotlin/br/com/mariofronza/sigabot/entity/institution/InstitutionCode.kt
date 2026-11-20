package br.com.mariofronza.sigabot.entity.institution

@JvmInline
value class InstitutionCode(val value: String) {

    init {
        require(value.isNotBlank()) { "Institution code cannot be blank" }
    }

    override fun toString() = value

    companion object {
        val UDESC = InstitutionCode("udesc")
    }
}
