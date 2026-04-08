package com.ileader.app

import com.ileader.app.data.models.*
import com.ileader.app.data.remote.dto.ProfileDto
import com.ileader.app.data.remote.dto.RoleDto
import org.junit.Assert.*
import org.junit.Test

class ModelTest {

    @Test
    fun `ProfileDto toDomain maps role correctly`() {
        val dto = ProfileDto(
            id = "user-1",
            name = "Test User",
            email = "test@mail.com",
            city = "Almaty",
            country = "KZ",
            roles = RoleDto(id = "r1", name = "athlete")
        )
        val user = dto.toDomain()
        assertEquals("user-1", user.id)
        assertEquals("Test User", user.name)
        assertEquals(UserRole.ATHLETE, user.role)
        assertEquals("Almaty", user.city)
    }

    @Test
    fun `ProfileDto toDomain defaults to USER for unknown role`() {
        val dto = ProfileDto(
            id = "u2",
            name = "Viewer",
            roles = RoleDto(id = "r2", name = "unknown")
        )
        assertEquals(UserRole.USER, dto.toDomain().role)
    }

    @Test
    fun `ProfileDto toDomain handles null role`() {
        val dto = ProfileDto(id = "u3", name = "No role")
        assertEquals(UserRole.USER, dto.toDomain().role)
    }

    @Test
    fun `MatchStatus fromString maps correctly`() {
        assertEquals(MatchStatus.COMPLETED, MatchStatus.fromString("completed"))
        assertEquals(MatchStatus.IN_PROGRESS, MatchStatus.fromString("in_progress"))
        assertEquals(MatchStatus.SCHEDULED, MatchStatus.fromString("unknown"))
        assertEquals(MatchStatus.SCHEDULED, MatchStatus.fromString(null))
    }

    @Test
    fun `TournamentFormat fromString maps correctly`() {
        assertEquals(TournamentFormat.SINGLE_ELIMINATION, TournamentFormat.fromString("single_elimination"))
        assertEquals(TournamentFormat.DOUBLE_ELIMINATION, TournamentFormat.fromString("double_elimination"))
        assertEquals(TournamentFormat.ROUND_ROBIN, TournamentFormat.fromString("round_robin"))
        assertNull(TournamentFormat.fromString("nonexistent"))
    }

    @Test
    fun `UserRole values are correct`() {
        assertEquals(9, UserRole.entries.size)
        assertTrue(UserRole.entries.map { it.name }.contains("ATHLETE"))
        assertTrue(UserRole.entries.map { it.name }.contains("TRAINER"))
        assertTrue(UserRole.entries.map { it.name }.contains("REFEREE"))
        assertTrue(UserRole.entries.map { it.name }.contains("CONTENT_MANAGER"))
    }
}
