package br.com.mariofronza.sigabot.entity.enrollment

@JvmInline
value class Grade(val value: Double) {

    init {
        require(value >= 0) { "Grade $value cannot be negative" }
    }
}
