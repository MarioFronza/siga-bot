dependencies {
    implementation(project(":usecases"))
    implementation(project(":entity"))
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-api:3.2.0")

    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("io.kotest:kotest-extensions-spring:6.2.7")
}
