package com.ileader.app.data.repository

import com.ileader.app.data.remote.SupabaseModule
import com.ileader.app.data.remote.dto.*
import com.ileader.app.data.util.MemoryCache
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order

class LocationRepository {
    private val client = SupabaseModule.client

    suspend fun getLocationDetail(locationId: String): LocationDto =
        MemoryCache.cached("location:$locationId", ttlMs = 1_800_000L) {
            client.from("locations")
                .select(Columns.raw("id, name, type, address, city, capacity, facilities, description, owner_id, rating, phone, email, website, image_urls, coordinates, created_at, updated_at")) {
                    filter { eq("id", locationId) }
                }
                .decodeSingle<LocationDto>()
        }

    suspend fun getLocationReviews(locationId: String): List<LocationReviewDto> =
        MemoryCache.cached("location:reviews:$locationId", ttlMs = 600_000L) {
            client.from("location_reviews")
                .select(Columns.raw("id, location_id, user_id, overall, criteria, comment, created_at, profiles(id, name, avatar_url)")) {
                    filter { eq("location_id", locationId) }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<LocationReviewDto>()
        }

    suspend fun createReview(data: LocationReviewInsertDto) {
        client.from("location_reviews").insert(data)
        MemoryCache.invalidate("location:reviews:${data.locationId}")
        // The location's aggregate rating likely changed too.
        MemoryCache.invalidate("location:${data.locationId}")
    }
}
