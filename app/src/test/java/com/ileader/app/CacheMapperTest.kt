package com.ileader.app

import com.ileader.app.data.local.toCached
import com.ileader.app.data.local.toDto
import com.ileader.app.data.remote.dto.SportDto
import com.ileader.app.data.remote.dto.TournamentWithCountsDto
import org.junit.Assert.*
import org.junit.Test

class CacheMapperTest {

    @Test
    fun `SportDto roundtrip through cache`() {
        val original = SportDto(
            id = "s1",
            name = "Картинг",
            slug = "karting",
            athleteLabel = "Пилот",
            iconUrl = null,
            isActive = true
        )
        val cached = original.toCached()
        val restored = cached.toDto()

        assertEquals(original.id, restored.id)
        assertEquals(original.name, restored.name)
        assertEquals(original.slug, restored.slug)
        assertEquals(original.athleteLabel, restored.athleteLabel)
        assertEquals(original.isActive, restored.isActive)
    }

    @Test
    fun `TournamentWithCountsDto roundtrip through cache`() {
        val original = TournamentWithCountsDto(
            id = "t1",
            name = "Кубок Алматы",
            sportId = "s1",
            sportName = "Картинг",
            locationName = "Arena",
            organizerName = "Org",
            status = "registration_open",
            startDate = "2026-05-01",
            endDate = "2026-05-02",
            imageUrl = null,
            maxParticipants = 32,
            participantCount = 12,
            region = "Алматы",
            ageCategory = "adult"
        )
        val cached = original.toCached()
        val restored = cached.toDto()

        assertEquals(original.id, restored.id)
        assertEquals(original.name, restored.name)
        assertEquals(original.sportName, restored.sportName)
        assertEquals(original.participantCount, restored.participantCount)
        assertEquals(original.status, restored.status)
    }
}
