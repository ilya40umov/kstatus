plugins {
    val kotlinVersion = "1.4.0"
    jacoco
    kotlin("jvm") version kotlinVersion apply false
    kotlin("plugin.serialization") version kotlinVersion apply false
    id("com.github.johnrengelman.shadow") version "6.0.0" apply false
}

allprojects {
    group = "me.ilya40umov.kstatus"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenLocal()
        jcenter()
    }
}

subprojects {
    apply<JavaPlugin>()
    apply<JacocoPlugin>()
    plugins.apply("org.jetbrains.kotlin.jvm")
    plugins.apply("org.jetbrains.kotlin.plugin.serialization")

    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    dependencies {
        // BOMs
        "implementation"(platform("org.apache.logging.log4j:log4j-bom:2.13.3"))
        "implementation"(platform("io.ktor:ktor-bom:1.4.0"))
        // individual version constrains    q
        constraints {
            "implementation"("io.github.microutils:kotlin-logging:1.8.3")
            "implementation"("io.micrometer:micrometer-registry-prometheus:1.5.4")
            "implementation"("org.kodein.di:kodein-di-conf-jvm:7.0.0")
            "implementation"("com.charleskorn.kaml:kaml:0.20.0")
            "implementation"("com.sksamuel.hoplite:hoplite-yaml:1.3.5")
            "implementation"("org.jetbrains.kotlinx:kotlinx-serialization-core:1.0.0-RC")
        }
        // libraries that make sense for each sub-module
        "implementation"(kotlin("stdlib", org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION))
        // test libraries that make sense for each sub-module
        val kotestVersion = "4.2.2"
        "testImplementation"("io.kotest:kotest-runner-junit5:$kotestVersion")
        "testImplementation"("io.kotest:kotest-assertions-core:$kotestVersion")
        "testImplementation"("io.kotest:kotest-property:$kotestVersion")
    }

    tasks {
        withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
            kotlinOptions {
                jvmTarget = "1.8"
                freeCompilerArgs = listOf("-Xjsr305=strict", "-Xopt-in=kotlin.RequiresOptIn")
            }
        }
        withType<Test> {
            useJUnitPlatform()
        }
    }
}