plugins {
    application
}

application {
    mainClass.set("br.com.mariofronza.sigabot.application.BootKt")
    applicationDefaultJvmArgs = listOf("-Duser.timezone=UTC")
}

dependencies {
    implementation(project(":entity"))
    implementation(project(":http"))
    implementation(project(":persistence"))
    implementation(project(":extraction"))
    implementation(project(":usecases"))
    implementation(project(":integration"))
}
