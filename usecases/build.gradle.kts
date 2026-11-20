dependencies {
    implementation(project(":entity"))
    implementation(kotlin("reflect"))
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-data-commons")
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("com.auth0:java-jwt:4.6.2")
}
