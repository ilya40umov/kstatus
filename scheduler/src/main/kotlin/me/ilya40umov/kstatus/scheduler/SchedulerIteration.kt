package me.ilya40umov.kstatus.scheduler

import me.ilya40umov.kstatus.scheduler.sitecheck.SiteCheckQueue
import me.ilya40umov.kstatus.site.SiteService
import me.ilya40umov.kstatus.site.events.SiteProcessedByScheduler
import mu.KotlinLogging
import java.time.LocalDateTime
import java.util.UUID

private val logger = KotlinLogging.logger {}

class SchedulerIteration(
    private val siteService: SiteService,
    private val siteCheckQueue: SiteCheckQueue
) {
    val uuid: String = UUID.randomUUID().toString()
    val startedAt: LocalDateTime = LocalDateTime.now()

    suspend fun run() {
        val sites = siteService.listWhereNextScheduledForIsBefore(
            before = startedAt,
            limit = DB_BATCH_SIZE
        ).also {
            logger.info { "Retrieved ${it.size} sites to process..." }
        }

        val sitesToEnqueue = sites.filter { site ->
            val lastEnqueuedAt = site.lastEnqueuedAt
            if (lastEnqueuedAt == null) {
                true
            } else {
                val whenToEnqueue = lastEnqueuedAt.plusSeconds(site.checkIntervalSeconds.toLong())
                whenToEnqueue <= startedAt
            }
        }

        val enqueuedSiteIds = siteCheckQueue.enqueueSites(sitesToEnqueue, startedAt)
        logger.info { "Enqueued ${enqueuedSiteIds.size} sites." }

        val enqueuingFailedForSiteIds = sitesToEnqueue.asSequence().map { it.siteId }.toSet() - enqueuedSiteIds
        val processedSites = sites.filter { it.siteId !in enqueuingFailedForSiteIds }.map { site ->
            SiteProcessedByScheduler(
                siteId = site.siteId,
                scheduledFor = if (enqueuedSiteIds.contains(site.siteId) || site.lastEnqueuedAt == null) {
                    startedAt.plusSeconds(site.checkIntervalSeconds.toLong())
                } else {
                    site.lastEnqueuedAt!!.plusSeconds(site.checkIntervalSeconds.toLong())
                },
                enqueuedAt = if (enqueuedSiteIds.contains(site.siteId)) startedAt else site.lastEnqueuedAt
            )
        }
        if (processedSites.isNotEmpty()) {
            siteService.onSitesProcessedByScheduler(processedSites)
        }
    }

    companion object {
        const val DB_BATCH_SIZE = 50
    }
}