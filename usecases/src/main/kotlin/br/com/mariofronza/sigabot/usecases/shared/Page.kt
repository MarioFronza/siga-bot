package br.com.mariofronza.sigabot.usecases.shared

data class PageRequest(val number: Int, val size: Int) {

    init {
        require(number >= 0) { "Page number $number cannot be negative" }
        require(size > 0) { "Page size $size must be positive" }
    }

    val offset: Long get() = number.toLong() * size
}

data class Page<T>(val items: List<T>, val request: PageRequest, val totalItems: Long) {

    fun <R> map(transform: (T) -> R) = Page(items.map(transform), request, totalItems)
}
