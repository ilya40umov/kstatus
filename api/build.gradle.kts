plugins {
    application
    id("com.github.johnrengelman.shadow")
}

application {
    mainClassName = "me.ilya40umov.kstatus.api.ApiKt"
}

dependencies {
    implementation(project(":core"))
    testImplementation("io.ktor:ktor-server-tests")
}