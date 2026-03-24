package com.ileader.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class QrPayload(
    val v: Int = 1,
    val uid: String,
    val tid: String,
    val ts: Long
)
