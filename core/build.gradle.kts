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
    api("org.jetbrains.kotlinx:kotlinx-serialization-core")
    api("com.github.jasync-sql:jasync-mysql")
    api("software.amazon.awssdk:sqs")
    implementation("org.apache.logging.log4j:log4j-core")
    // XXX JodaTime is only here because of jasync
    implementation("joda-time:joda-time")
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j-impl")
}

