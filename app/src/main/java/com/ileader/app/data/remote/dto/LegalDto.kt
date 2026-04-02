package com.ileader.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PlatformSettingValueDto(
    val key: String,
    val value: String? = null
)

@Serializable
data class LegalPageDto(
    val id: String = "",
    val title: String = "",
    val slug: String = "",
    val content: String = "",
    val enabled: Boolean = true
)
