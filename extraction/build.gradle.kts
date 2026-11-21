dependencies {
    implementation(project(":usecases"))
    implementation(project(":entity"))
    implementation(kotlin("reflect"))
    implementation("org.springframework:spring-context")
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("org.jsoup:jsoup:1.24.1")

    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
}
