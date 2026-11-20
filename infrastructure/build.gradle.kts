dependencies {
    implementation(project(":usecases"))
    implementation(project(":entity"))
    implementation(kotlin("reflect"))
    implementation("org.springframework:spring-context")
    implementation("org.springframework.security:spring-security-crypto")
    implementation("io.micrometer:micrometer-core")
}
