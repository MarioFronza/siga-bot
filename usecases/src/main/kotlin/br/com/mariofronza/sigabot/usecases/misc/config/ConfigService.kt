package br.com.mariofronza.sigabot.usecases.misc.config

import org.springframework.context.annotation.PropertySource
import org.springframework.stereotype.Service

@Service
@PropertySource("classpath:/application.properties")
class ConfigService(
    private val baseConfigService: BaseConfigService
) {

    fun getRequiredString(property: String) = getRequiredProperty(property)

    fun getRequiredInt(property: String) = getRequiredProperty(property).toInt()

    fun getRequiredLong(property: String) = getRequiredProperty(property).toLong()

    private fun getRequiredProperty(property: String): String {
        return getProperty(property)
            ?: throw IllegalArgumentException("Required configuration property $property not found.")
    }

    private fun getProperty(property: String): String? {
        return baseConfigService.getString(property)
    }
}
