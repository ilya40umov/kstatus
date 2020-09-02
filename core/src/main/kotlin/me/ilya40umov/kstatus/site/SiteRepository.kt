package me.ilya40umov.kstatus.site

import com.github.jasync.sql.db.RowData
import com.github.jasync.sql.db.mysql.MySQLConnection
import com.github.jasync.sql.db.mysql.MySQLQueryResult
import com.github.jasync.sql.db.pool.ConnectionPool
import me.ilya40umov.kstatus.jasync.toJoda
import me.ilya40umov.kstatus.jasync.toLocalDateTime
import me.ilya40umov.kstatus.jasync.useSuspending

class SiteRepository(
    private val connectionPool: ConnectionPool<MySQLConnection>
) {
    suspend fun listAll(offset: Int, limit: Int): List<Site> {
        return connectionPool.useSuspending { c ->
            c.sendPreparedStatement(
                query = "SELECT * FROM sites LIMIT ? OFFSET ?",
                values = listOf(limit, offset)
            ).rows.map(::toSite)
        }
    }

    suspend fun countAll(): Int {
        return connectionPool.useSuspending { c->
            c.sendPreparedStatement("SELECT COUNT(*) FROM sites")
                .rows.first().getAs(0)
        }
    }

    suspend fun findById(siteId: Int): Site? {
        return connectionPool.useSuspending { c ->
            c.sendPreparedStatement(
                query = "SELECT * FROM sites WHERE site_id = ?",
                values = listOf(siteId)
            ).rows.firstOrNull()?.let(::toSite)
        }
    }

    suspend fun insert(site: Site): Int {
        return connectionPool.useSuspending { c ->
            val res = c.sendPreparedStatement(
                query = """
                    INSERT INTO sites (
                        url, 
                        created_at, 
                        check_interval_seconds, 
                        last_checked_at, 
                        last_status_check_result, 
                        next_scheduled_for, 
                        last_enqueued_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """.trimIndent(),
                values = listOf(
                    site.url,
                    site.createdAt.toJoda(),
                    site.checkIntervalSeconds,
                    site.lastCheckedAt?.toJoda(),
                    site.lastStatusCheckResult,
                    site.nextScheduledFor?.toJoda(),
                    site.lastEnqueuedAt?.toJoda()
                )
            ) as MySQLQueryResult
            res.lastInsertId.toInt()
        }
    }

    suspend fun update(site: Site) {
        connectionPool.useSuspending { c ->
            c.sendPreparedStatement(
                query = """
                    UPDATE sites SET
                        url = ?, 
                        created_at = ?, 
                        check_interval_seconds = ?, 
                        last_checked_at = ?, 
                        last_status_check_result = ?, 
                        next_scheduled_for = ?, 
                        last_enqueued_at = ?
                    WHERE site_id = ?
                """.trimIndent(),
                values = listOf(
                    site.url,
                    site.createdAt.toJoda(),
                    site.checkIntervalSeconds,
                    site.lastCheckedAt?.toJoda(),
                    site.lastStatusCheckResult,
                    site.nextScheduledFor?.toJoda(),
                    site.lastEnqueuedAt?.toJoda(),
                    site.siteId
                )
            )
        }
    }

    suspend fun delete(siteId: Int) {
        return connectionPool.useSuspending { c ->
            c.sendPreparedStatement(
                query = "DELETE FROM sites WHERE site_id = ?",
                values = listOf(siteId)
            )
        }
    }

    private fun toSite(rowData: RowData) = with(rowData) {
        Site(
            siteId = getAs("site_id"),
            url = getAs("url"),
            createdAt = getAs<org.joda.time.LocalDateTime>("created_at").toLocalDateTime(),
            checkIntervalSeconds = getAs("check_interval_seconds"),
            lastCheckedAt = getDate("last_checked_at")?.toLocalDateTime(),
            lastStatusCheckResult = getString("last_status_check_result")?.let {
                StatusCheckResult.valueOf(it)
            },
            nextScheduledFor = getDate("next_scheduled_for")?.toLocalDateTime(),
            lastEnqueuedAt = getDate("last_enqueued_at")?.toLocalDateTime()
        )
    }
}