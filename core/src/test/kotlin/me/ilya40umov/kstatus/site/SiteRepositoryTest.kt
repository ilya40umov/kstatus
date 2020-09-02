package me.ilya40umov.kstatus.site

import com.github.jasync.sql.db.mysql.MySQLConnectionBuilder
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime

class SiteRepositoryTest : StringSpec({

    val connectionPool = MySQLConnectionBuilder.createConnectionPool(
        "jdbc:mysql://localhost:3306/kstatus?user=kstatus&password=kstatus123"
    )
    val repository = SiteRepository(connectionPool)

    val site = Site(
        siteId = 1,
        url = "http://a.com",
        createdAt = LocalDateTime.now(),
        checkIntervalSeconds = 60
    )

    "insert() should return ID assigned to the site" {
        repository.insert(site).also { siteId ->
            siteId shouldBeGreaterThan 0
            repository.findById(siteId)?.url shouldBe site.url
        }
    }
})