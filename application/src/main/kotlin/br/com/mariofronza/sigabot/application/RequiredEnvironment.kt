package br.com.mariofronza.sigabot.application

private val REQUIRED_VARIABLES = listOf(
    "DATASOURCE_URL",
    "DATASOURCE_USERNAME",
    "DATASOURCE_PASSWORD",
    "JWT_SECRET",
    "SECRETKEY",
    "ONESIGNAL_APIKEY",
    "SIGA_YEAR",
    "SIGA_TERM"
)

fun requireEnvironment(environment: Map<String, String>) {
    val missing = REQUIRED_VARIABLES.filter { environment[it].isNullOrBlank() }
    check(missing.isEmpty()) { "Missing required environment variables: ${missing.joinToString()}" }
}
