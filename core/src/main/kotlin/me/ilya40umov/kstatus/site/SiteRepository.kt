package me.ilya40umov.kstatus.site

import java.lang.Integer.min
import java.time.Duration
import java.time.LocalDateTime

class SiteRepository {

    // TODO implement operations on database using jasync
    private val sites = mutableListOf(
        Site(
            siteId = 1,
            url = "https://www.google.com",
            createdAt = LocalDateTime.now(),
            checkIntervalSeconds = 60,
            lastCheckedAt = LocalDateTime.now(),
            lastStatusCheckResult = StatusCheckResult.UP,
            nextScheduledFor = LocalDateTime.now().plus(Duration.ofMinutes(1)),
            lastEnqueuedAt = LocalDateTime.now()
        )
    )

    fun listAll(offset: Int, limit: Int): List<Site> {
        if (offset >= sites.size) {
            return emptyList()
        }
        return sites.subList(offset, min(offset + limit, sites.size))
    }

    fun findById(siteId: Int): Site? {
        return sites.find { it.siteId == siteId }
    }

    fun insert(site: Site): Site {
        val siteWithId = site.copy(
            siteId = sites.map { it.siteId }.maxOrNull()?.let { it + 1 } ?: 1
        )
        sites.add(siteWithId)
        return siteWithId
    }

    fun update(site: Site): Site? {
        val index = sites.indexOfFirst { it.siteId == site.siteId }
        if (index != -1) {
            sites[index] = site
            return site
        }
        return null
    }

    fun delete(siteId: Int): Site? {
        return sites.find { it.siteId == siteId }.also {
            sites.remove(it)
        }
    }
}