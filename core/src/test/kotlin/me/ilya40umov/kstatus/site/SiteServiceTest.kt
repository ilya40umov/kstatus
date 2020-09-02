package me.ilya40umov.kstatus.site

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import java.time.LocalDateTime

class SiteServiceTest : StringSpec({

    val repository = mockk<SiteRepository>()
    val service = SiteService(repository)

    val site = Site(
        siteId = 1,
        url = "http://a.com",
        createdAt = LocalDateTime.now(),
        checkIntervalSeconds = 60
    )

    "deleteById() should return null if site can not be found" {
        coEvery { repository.findById(1) } returns null

        service.deleteById(1).also { site ->
            site shouldBe null
        }
    }

    "deleteById() should return deleted site if site was successfully deleted" {
        coEvery { repository.findById(1) } returns site
        coJustRun { repository.delete(1) }

        service.deleteById(1).also { site ->
            site shouldBe site
            coVerify { repository.delete(1) }
        }
    }
})