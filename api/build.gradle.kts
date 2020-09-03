plugins {
    application
    id("com.github.johnrengelman.shadow")
}

application {
    mainClassName = "me.ilya40umov.kstatus.api.ApiKt"
}

dependencies {
    implementation(project(":core"))
    implementation("com.atlassian.oai:swagger-request-validator-core:2.11.0")
    testImplementation("io.ktor:ktor-server-tests") {
        exclude(group = "ch.qos.logback", module = "logback-classic")
    }
}