package me.ilya40umov.kstatus.site

import kotlinx.serialization.Serializable

@Serializable
data class SiteList(
    val sites: List<Site>,
    val offset: Int,
    val totalCount: Int
)