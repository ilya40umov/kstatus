package me.ilya40umov.kstatus.site

import java.time.LocalDateTime
import java.time.ZoneOffset

class SiteService(
    private val repository: SiteRepository
) {

    suspend fun listAll(offset: Int, limit: Int): SiteList {
        return SiteList(
            sites = repository.listAll(offset, limit),
            offset = offset,
            totalCount = 1 // TODO calculate total count
        )
    }

    suspend fun findById(siteId: Int): Site? {
        return repository.findById(siteId)
    }

    suspend fun create(site: Site): Site {
        val siteId = repository.insert(
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
        return repository.findById(siteId)
            ?: throw RuntimeException("Failed to look up the inserted record with ID $siteId")
    }

    suspend fun update(siteId: Int, site: Site): Site? {
        val siteToUpdate = repository.findById(siteId) ?: return null
        val updatedSite = siteToUpdate.copy(
            url = site.url,
            checkIntervalSeconds = site.checkIntervalSeconds,
            nextScheduledFor = site.nextScheduledFor
        )
        repository.update(updatedSite)
        return updatedSite
    }

    suspend fun deleteById(siteId: Int): Site? {
        return repository.findById(siteId)?.also {
            repository.delete(siteId)
        }
    }
}