package me.ilya40umov.kstatus.jasync

import com.github.jasync.sql.db.ConcreteConnection
import com.github.jasync.sql.db.SuspendingConnection
import com.github.jasync.sql.db.asSuspending
import com.github.jasync.sql.db.pool.ConnectionPool
import kotlinx.coroutines.future.await

// XXX ideally this should be provided by the library itself:
// https://github.com/jasync-sql/jasync-sql/issues/184
suspend fun <T, C : ConcreteConnection> ConnectionPool<C>.useSuspending(
    block: suspend (SuspendingConnection) -> T
): T {
    var exception: Throwable? = null
    val connection = take().await()
    try {
        return block(connection.asSuspending)
    } catch (e: Throwable) {
        exception = e
        throw e
    } finally {
        if (exception == null) {
            giveBack(connection).await()
        } else {
            try {
                giveBack(connection).await()
            } catch (closeException: Throwable) {
                exception.addSuppressed(closeException)
            }
        }
    }
}
