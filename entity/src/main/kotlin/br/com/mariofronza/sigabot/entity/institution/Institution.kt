package br.com.mariofronza.sigabot.entity.institution

data class Institution(
    val id: Long? = null,
    val code: String,
    val name: String,
    val gradeScaleMax: Double,
) {
    companion object {
        const val UDESC = "udesc"
    }
}
