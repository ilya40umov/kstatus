plugins {
    application
    id("com.github.johnrengelman.shadow")
}

application {
    mainClassName = "me.ilya40umov.kstatus.api.ApiKt"
}

dependencies {
    implementation(project(":core"))
    implementation("io.ktor:ktor-server-netty")
    implementation("io.ktor:ktor-serialization")
    implementation("io.ktor:ktor-metrics-micrometer")
    implementation("io.micrometer:micrometer-registry-prometheus:1.5.4")
    implementation("io.ktor:ktor-client-apache")
    implementation("io.ktor:ktor-client-serialization-jvm")
    testImplementation("io.ktor:ktor-server-tests")
}