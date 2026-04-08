package com.ileader.app.data.repository

import com.ileader.app.data.remote.SupabaseModule
import com.ileader.app.data.remote.dto.*
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order

class LocationRepository {
    private val client = SupabaseModule.client

    suspend fun getLocationDetail(locationId: String): LocationDto {
        return client.from("locations")
            .select(Columns.raw("id, name, type, address, city, capacity, facilities, description, owner_id, rating, phone, email, website, image_urls, coordinates, created_at, updated_at")) {
                filter { eq("id", locationId) }
            }
            .decodeSingle<LocationDto>()
    }

    suspend fun getLocationReviews(locationId: String): List<LocationReviewDto> {
        return client.from("location_reviews")
            .select(Columns.raw("id, location_id, user_id, overall, criteria, comment, created_at, profiles(id, name, avatar_url)")) {
                filter { eq("location_id", locationId) }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<LocationReviewDto>()
    }

    suspend fun createReview(data: LocationReviewInsertDto) {
        client.from("location_reviews").insert(data)
    }
}
