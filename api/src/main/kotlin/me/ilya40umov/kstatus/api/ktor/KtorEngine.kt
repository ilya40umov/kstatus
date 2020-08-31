package me.ilya40umov.kstatus.api.ktor

import io.ktor.config.ApplicationConfig
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.addShutdownHook
import io.ktor.server.engine.commandLineEnvironment
import io.ktor.server.engine.loadCommonConfiguration
import io.ktor.server.engine.stop
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.util.KtorExperimentalAPI
import java.util.concurrent.TimeUnit

// mostly borrowed from EngineMain.kt
// for more info see https://github.com/ktorio/ktor/issues/1819#issuecomment-620464701
@KtorExperimentalAPI
object KtorEngine {

    @JvmStatic
    fun start(args: Array<String>) {
        val engine = createApplicationEngine(args)
        engine.start(wait = true)
    }

    private fun createApplicationEngine(args: Array<String>): ApplicationEngine {
        val applicationEnvironment = commandLineEnvironment(args)
        val engine = NettyApplicationEngine(applicationEnvironment) {
            loadConfiguration(applicationEnvironment.config)
        }
        engine.addShutdownHook {
            engine.stop(1, 5, TimeUnit.SECONDS)
        }
        return engine
    }

    private fun NettyApplicationEngine.Configuration.loadConfiguration(config: ApplicationConfig) {
        val deploymentConfig = config.config("ktor.deployment")
        loadCommonConfiguration(deploymentConfig)
        deploymentConfig.propertyOrNull("requestQueueLimit")?.getString()?.toInt()?.let {
            requestQueueLimit = it
        }
        deploymentConfig.propertyOrNull("shareWorkGroup")?.getString()?.toBoolean()?.let {
            shareWorkGroup = it
        }
        deploymentConfig.propertyOrNull("responseWriteTimeoutSeconds")?.getString()?.toInt()?.let {
            responseWriteTimeoutSeconds = it
        }
    }
}