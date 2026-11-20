package br.com.mariofronza.sigabot.entity.term

data class AcademicPeriod(val year: Int, val term: Int) : Comparable<AcademicPeriod> {

    init {
        require(year > 1900) { "Year $year is not a valid academic year" }
        require(term > 0) { "Term $term is not a valid academic term" }
    }

    override fun compareTo(other: AcademicPeriod) = compareValuesBy(this, other, AcademicPeriod::year, AcademicPeriod::term)

    override fun toString() = "$year/$term"

    companion object {
        fun parse(text: String): AcademicPeriod {
            val parts = text.trim().split("/")
            require(parts.size == 2) { "Period '$text' is not in the year/term format" }
            val (year, term) = parts.map { requireNotNull(it.toIntOrNull()) { "Period '$text' is not in the year/term format" } }
            return AcademicPeriod(year, term)
        }
    }
}
