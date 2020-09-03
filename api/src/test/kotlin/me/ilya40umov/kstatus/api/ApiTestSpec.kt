package me.ilya40umov.kstatus.api

import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestContext
import io.kotest.core.test.createTestName
import io.ktor.application.Application
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.createTestEnvironment
import org.kodein.di.DI

/** Extended version of StringSpec which is assuming usage of DI and TestApplicationEngine for each test. */
abstract class ApiTestSpec(
    private val configureDi: DI.MainBuilder.() -> Unit,
    private val addModules: Application.(DI) -> Unit,
    body: ApiTestSpec.() -> Unit = {}
) : StringSpec({}) {

    init {
        body()
    }

    operator fun String.invoke(test: suspend TestContext.(TestApplicationEngine, DI) -> Unit) =
        registration().addTest(
            createTestName(null, this, false),
            xdisabled = false,
            test = {
                val di = DI(allowSilentOverride = false, configureDi)
                val engine = TestApplicationEngine(createTestEnvironment())
                engine.start()
                engine.application.addModules(di)
                try {
                    test(engine, di)
                } finally {
                    engine.stop(0L, 0L)
                }
            }
        )
}