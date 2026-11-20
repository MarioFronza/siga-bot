plugins {
    kotlin("jvm") version "2.4.21" apply false
    kotlin("plugin.spring") version "2.4.21" apply false
}

allprojects {
    group = "br.com.mariofronza"
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")

    repositories {
        mavenCentral()
    }

    extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
        jvmToolchain(25)
        compilerOptions.freeCompilerArgs.add("-Xjsr305=strict")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging { events("passed", "skipped", "failed") }
    }

    dependencies {
        "implementation"(platform("org.springframework.boot:spring-boot-dependencies:4.1.2"))

        "testImplementation"(platform("org.springframework.boot:spring-boot-dependencies:4.1.2"))
        "testImplementation"("io.kotest:kotest-runner-junit5:6.2.7")
        "testImplementation"("io.mockk:mockk:1.14.12")
        "testImplementation"("org.springframework:spring-test")
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    }
}
