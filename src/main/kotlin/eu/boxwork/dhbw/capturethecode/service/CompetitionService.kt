package eu.boxwork.dhbw.capturethecode.service

import eu.boxwork.dhbw.capturethecode.dto.TeamWithMembersDto
import eu.boxwork.dhbw.capturethecode.runner.CompetitionRunner
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*
@Service
class CompetitionService(
    @Autowired private val teamService: TeamService,
    @Autowired private val playerService: PlayerService,
    @Value("\${competition.rounds}") private val rounds: Int,
    @Value("\${competition.delay}") private val delay: Int
) : AbstractGameService(teamService, playerService,rounds, false) {

    /**
     * the second may join
     * @param cordID the competition to join
     * @param teamID the team joining
     * */
    fun join(cordID: UUID, teamID: UUID) {
        synchronized(gameGrounds)
        {
            if (gameGrounds.containsKey(cordID)) {
                log.info("joining competition with ID $cordID")
                val team = teamService.get(uuid = teamID)?:throw ServiceException(404, "joining team not found")
                val players = playerService.findByTeam(teamID)
                if (players.size==0)throw ServiceException(412, "joining team has no team members")

                val teamToSet = TeamWithMembersDto(
                    team.uuid,
                    team.teamName,
                    players
                )

                gameGrounds[cordID]!!.startGame(teamToSet)
                val runner = CompetitionRunner(gameGrounds[cordID]!!,delay)
                runners[cordID] = runner
                val t = Thread(runner)
                t.start()

                log.info("started new competition cord for teams" +
                        " ${gameGrounds[cordID]!!.teamA.teamName} vs. " +
                        gameGrounds[cordID]!!.teamB!!.teamName
                )

            }
            else
            {
                throw ServiceException(404, "competition not found")
            }
        }
    }
}