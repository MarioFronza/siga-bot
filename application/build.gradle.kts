plugins {
    application
}

application {
    mainClass.set("br.com.mariofronza.sigabot.application.BootKt")
    applicationDefaultJvmArgs = listOf("-Duser.timezone=UTC", "-XX:MaxRAMPercentage=75")
}

dependencies {
    implementation(project(":entity"))
    implementation(project(":http"))
    implementation(project(":persistence"))
    implementation(project(":extraction"))
    implementation(project(":usecases"))
    implementation(project(":integration"))
    implementation(project(":infrastructure"))
    implementation(kotlin("reflect"))
    implementation("org.springframework.boot:spring-boot-starter")
    runtimeOnly("org.springframework.boot:spring-boot-starter-actuator")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")

    testImplementation("com.lemonappdev:konsist:0.18.1")
}
