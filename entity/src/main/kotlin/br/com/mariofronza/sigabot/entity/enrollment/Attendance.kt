package br.com.mariofronza.sigabot.entity.enrollment

@JvmInline
value class Attendance(val percentage: Double) {

    init {
        require(percentage in 0.0..100.0) { "Attendance $percentage% is outside 0-100%" }
    }
}
