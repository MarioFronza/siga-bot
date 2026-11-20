import org.flywaydb.core.Flyway
import org.jooq.codegen.gradle.CodegenTask
import org.testcontainers.postgresql.PostgreSQLContainer

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.testcontainers:testcontainers-postgresql:2.0.5")
        classpath("org.flywaydb:flyway-database-postgresql:12.4.0")
        classpath("org.postgresql:postgresql:42.7.14")
    }
}

plugins {
    id("org.jooq.jooq-codegen-gradle") version "3.21.9"
}

dependencies {
    implementation(project(":entity"))
    implementation(project(":usecases"))
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.flywaydb:flyway-database-postgresql")

    jooqCodegen("org.postgresql:postgresql:42.7.14")

    testImplementation("org.springframework.boot:spring-boot-starter-jooq-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testImplementation("io.kotest:kotest-extensions-spring:6.2.7")
}

val migrations = layout.projectDirectory.dir("src/main/resources/db/migration")
var codegenDatabase: PostgreSQLContainer? = null

jooq {
    configuration {
        generator {
            name = "org.jooq.codegen.KotlinGenerator"
            database {
                name = "org.jooq.meta.postgres.PostgresDatabase"
                inputSchema = "public"
                excludes = "flyway_schema_history"
            }
            generate {
                isKotlinNotNullRecordAttributes = true
            }
            target {
                packageName = "br.com.mariofronza.sigabot.persistence.jooq"
            }
        }
    }
}

val codegenJdbcProperties = listOf("url", "user", "password").map { "jooq.codegen.jdbc.$it" }

val stopCodegenDatabase = tasks.register("stopCodegenDatabase") {
    doLast {
        codegenJdbcProperties.forEach(System::clearProperty)
        codegenDatabase?.stop()
    }
}

tasks.named<CodegenTask>("jooqCodegen") {
    inputs.dir(migrations)
    doFirst {
        val database = PostgreSQLContainer("postgres:18-alpine").apply { start() }
        codegenDatabase = database
        Flyway.configure(Flyway::class.java.classLoader)
            .dataSource(database.jdbcUrl, database.username, database.password)
            .locations("filesystem:${migrations.asFile}")
            .load()
            .migrate()
        codegenJdbcProperties.zip(listOf(database.jdbcUrl, database.username, database.password))
            .forEach { (key, value) -> System.setProperty(key, value) }
    }
    finalizedBy(stopCodegenDatabase)
}

configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
    sourceSets.named("main") { kotlin.srcDir(tasks.named("jooqCodegen")) }
}
