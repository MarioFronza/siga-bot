plugins {
    kotlin("jvm") version "2.4.21" apply false
    kotlin("plugin.spring") version "2.4.21" apply false
    kotlin("plugin.jpa") version "2.4.21" apply false
}

allprojects {
    version = "1.0"
    group = "br.com.mariofronza"
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "org.jetbrains.kotlin.plugin.jpa")

    repositories {
        mavenCentral()
    }

    extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
        jvmToolchain(21)
        compilerOptions.freeCompilerArgs.add("-Xjsr305=strict")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging { events("passed", "skipped", "failed") }
    }

    dependencies {
        "implementation"(platform("org.springframework.boot:spring-boot-dependencies:4.1.2"))

        "implementation"(kotlin("reflect"))
        "implementation"("org.springframework.boot:spring-boot-starter-webmvc")
        "implementation"("org.springframework.boot:spring-boot-starter-data-jpa")
        "implementation"("org.springframework.boot:spring-boot-starter-validation")
        "implementation"("org.springframework.boot:spring-boot-starter-security")
        "implementation"("tools.jackson.module:jackson-module-kotlin")
        "implementation"("com.auth0:java-jwt:4.6.2")
        "implementation"("com.squareup.retrofit2:retrofit:3.0.0")
        "implementation"("com.squareup.retrofit2:converter-jackson:3.0.0")
        "implementation"("org.jsoup:jsoup:1.24.1")
        "runtimeOnly"("org.postgresql:postgresql")

        "testImplementation"(platform("org.springframework.boot:spring-boot-dependencies:4.1.2"))
        "testImplementation"("io.kotest:kotest-runner-junit5:6.2.6")
        "testImplementation"("io.mockk:mockk:1.14.12")
        "testImplementation"("org.springframework:spring-test")
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    }
}
