plugins {
    val kotlinVersion = "1.4.0"
    jacoco
    kotlin("jvm") version kotlinVersion apply false
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

    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    dependencies {
        // BOMs
        "implementation"(platform("org.apache.logging.log4j:log4j-bom:2.13.3"))
        "implementation"(platform("io.ktor:ktor-bom:1.4.0"))
        // individual version constrains
        constraints {
            "implementation"("io.github.microutils:kotlin-logging:1.8.3")
            "implementation"("io.micrometer:micrometer-registry-prometheus:1.5.4")
            "implementation"("org.kodein.di:kodein-di-conf-jvm:7.0.0")
            "implementation"("com.charleskorn.kaml:kaml:0.20.0")
            "implementation"("com.sksamuel.hoplite:hoplite-yaml:1.3.5")
        }
        // test libraries used in all sub-modules
        val kotestVersion = "4.2.2"
        "testImplementation"("io.kotest:kotest-runner-junit5:$kotestVersion")
        "testImplementation"("io.kotest:kotest-assertions-core:$kotestVersion")
        "testImplementation"("io.kotest:kotest-property:$kotestVersion")
    }

    tasks {
        withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
            kotlinOptions {
                jvmTarget = "1.8"
                freeCompilerArgs = listOf("-Xjsr305=strict")
            }
        }
        withType<Test> {
            useJUnitPlatform()
        }
    }
}