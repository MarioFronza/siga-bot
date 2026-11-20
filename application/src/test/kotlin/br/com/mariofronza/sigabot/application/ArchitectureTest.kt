package br.com.mariofronza.sigabot.application

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import io.kotest.core.spec.style.FunSpec

class ArchitectureTest : FunSpec({

    val root = "br.com.mariofronza.sigabot"
    val adapters = listOf("persistence", "extraction", "http", "integration", "infrastructure")

    fun module(name: String) = Konsist.scopeFromProduction(moduleName = name)

    test("the domain depends on nothing but the language") {
        module("entity").imports.assertTrue { import ->
            import.name.startsWith("$root.entity.") || import.name.startsWith("java.") || import.name.startsWith("kotlin.")
        }
    }

    test("use cases never reach into adapters or the application") {
        module("usecases").imports.assertFalse { import ->
            (adapters + "application").any { import.name.startsWith("$root.$it.") }
        }
    }

    test("use cases depend only on the domain, the language and a logging facade") {
        val allowed = listOf("$root.entity.", "$root.usecases.", "java.", "kotlin.", "org.slf4j.")
        module("usecases").imports.assertTrue { import -> allowed.any { import.name.startsWith(it) } }
    }

    test("adapters do not depend on each other") {
        adapters.forEach { adapter ->
            module(adapter).imports.assertFalse { import ->
                (adapters - adapter).any { import.name.startsWith("$root.$it.") }
            }
        }
    }

    test("nullability is handled without the double-bang operator") {
        val doubleBang = Regex("!{2}")
        Konsist.scopeFromProject().files.assertFalse { doubleBang.containsMatchIn(it.text) }
    }
})
