plugins {
    `java-library`
}

dependencies {
    api("io.github.microutils:kotlin-logging")
    api("org.kodein.di:kodein-di-conf-jvm")
    api("io.ktor:ktor-server-netty")
    api("io.ktor:ktor-serialization")
    api("io.ktor:ktor-metrics-micrometer")
    api("io.micrometer:micrometer-registry-prometheus")
    api("io.ktor:ktor-client-apache")
    api("io.ktor:ktor-client-serialization-jvm")
    api("com.sksamuel.hoplite:hoplite-yaml")
    implementation("org.apache.logging.log4j:log4j-core")
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j-impl")
}

