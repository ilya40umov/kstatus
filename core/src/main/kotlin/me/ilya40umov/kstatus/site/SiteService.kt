package me.ilya40umov.kstatus.site

import java.time.LocalDateTime

class SiteService(
    private val repository: SiteRepository
) {

    fun listAll(offset: Int, limit: Int): SiteList {
        return SiteList(
            sites = repository.listAll(offset, limit),
            offset = offset,
            totalCount = 1
        )
    }

    fun findById(siteId: Int): Site? {
        return repository.findById(siteId)
    }

    fun create(site: Site): Site {
        return repository.insert(
            Site(
                siteId = 0,
                url = site.url,
                createdAt = LocalDateTime.now(),
                checkIntervalSeconds = site.checkIntervalSeconds,
                lastCheckedAt = null,
                lastStatusCheckResult = null,
                nextScheduledFor = LocalDateTime.now(),
                lastEnqueuedAt = null
            )
        )
    }

    fun update(siteId: Int, site: Site): Site? {
        val siteToUpdate = repository.findById(siteId) ?: return null
        return repository.update(
            siteToUpdate.copy(
                url = site.url,
                checkIntervalSeconds = site.checkIntervalSeconds,
                nextScheduledFor = site.nextScheduledFor
            )
        )
    }

    fun deleteById(siteId: Int): Site? {
        return repository.delete(siteId)
    }
}