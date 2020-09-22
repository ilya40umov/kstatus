package me.ilya40umov.kstatus.api.testing

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.toByteArray

// XXX version of ByteReadChannel used to preserve request body for validation against OpenAPI spec
class RequestByteReadChannel(
    val body: String
) : ByteReadChannel by ByteReadChannel(body.toByteArray())