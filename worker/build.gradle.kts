plugins {
    application
    id("com.github.johnrengelman.shadow")
}

application {
    mainClassName = "me.ilya40umov.kstatus.worker.WorkerKt"
}

dependencies {
    implementation(project(":core"))
}
