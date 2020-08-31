plugins {
    application
    id("com.github.johnrengelman.shadow")
}

application {
    mainClassName = "me.ilya40umov.kstatus.scheduler.SchedulerKt"
}

dependencies {
    implementation(project(":core"))
}
