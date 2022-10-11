package eu.boxwork.dhbw.capturethecode.service

import eu.boxwork.dhbw.capturethecode.dto.ActionResultDto
import eu.boxwork.dhbw.capturethecode.dto.ScoreDto
import eu.boxwork.dhbw.capturethecode.dto.TeamWithMembersDto
import eu.boxwork.dhbw.capturethecode.enums.Action
import eu.boxwork.dhbw.capturethecode.model.GameGround
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.jvm.Throws

@Service
class CompetitionService(
    @Autowired private val teamService: TeamService,
    @Autowired private val playerService: PlayerService
) : AbstractGameService(teamService, playerService) {

    /**
     * the second may join
     * @param cordID the competition to join
     * @param teamID the team joining
     * */
    fun join(cordID: UUID, teamID: UUID) {
        synchronized(gameGrounds)
        {
            log.info("joining competition with ID $cordID")
            if (gameGrounds.containsKey(cordID)) {
                val team = teamService.get(uuid = teamID)?:throw ServiceException(404, "team A not found")
                val players = playerService.findByTeam(teamID)
                if (players.size==0)throw ServiceException(412, "team A has no team members")

                val teamToSet = TeamWithMembersDto(
                    team.uuid,
                    team.teamName,
                    players
                )

                gameGrounds[cordID]!!.startGame(teamToSet)

                log.info("started new competition cord for teams" +
                        " ${gameGrounds[cordID]!!.teamA.teamName} vs. " +
                        gameGrounds[cordID]!!.teamB!!.teamName
                )
            }
        }
    }
}