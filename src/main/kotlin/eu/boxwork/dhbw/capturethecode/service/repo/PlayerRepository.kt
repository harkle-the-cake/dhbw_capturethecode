package eu.boxwork.dhbw.capturethecode.service.repo

import eu.boxwork.dhbw.capturethecode.model.Player
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

@org.springframework.stereotype.Repository
interface PlayerRepository : JpaRepository<Player, UUID> {
    fun findByName(name: String): Player?
    fun findByTeamUuid(teamUuid: UUID): List<Player>
    fun countByTeamUuid(teamUuid: UUID): Int
}