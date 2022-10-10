package eu.boxwork.dhbw.capturethecode.service.repo

import eu.boxwork.dhbw.capturethecode.model.Team
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

@org.springframework.stereotype.Repository
interface TeamRepository : JpaRepository<Team, UUID> {
    fun findByTeamName(teamName: String): Team?
    fun findByTeamToken(teamToken: String): Team?
}