plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "agis"
include("application")
include("persistence")
include("entity")
include("extraction")
include("usecases")
include("http")
include("integration")
