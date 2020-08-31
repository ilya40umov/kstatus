plugins {
    `java-library`
}

dependencies {
    api("io.github.microutils:kotlin-logging")
    implementation("org.apache.logging.log4j:log4j-core")
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j-impl")
}

