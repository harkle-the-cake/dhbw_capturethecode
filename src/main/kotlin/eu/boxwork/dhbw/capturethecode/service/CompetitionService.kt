package eu.boxwork.dhbw.capturethecode.service

import eu.boxwork.dhbw.capturethecode.dto.ActionResultDto
import eu.boxwork.dhbw.capturethecode.dto.ScoreDto
import eu.boxwork.dhbw.capturethecode.dto.TeamWithMembersDto
import eu.boxwork.dhbw.capturethecode.enums.Action
import eu.boxwork.dhbw.capturethecode.model.GameCord
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
) {
    private val log = LogManager.getLogger("CompetitionService")
    private val competitions : MutableMap<UUID, GameCord> = ConcurrentHashMap()

    /**
     * open the competition
     * */
    @Throws(ServiceException::class)
    fun start(teamIDA: UUID): UUID {
        val cordID = UUID.randomUUID()
        val teamA = teamService.get(uuid = teamIDA)?:throw ServiceException(404, "team not found")
        val playersA = playerService.findByTeam(teamIDA)
        if (playersA.size==0)throw ServiceException(412, "team has no team members")

        val teamToSetA = TeamWithMembersDto(
            teamA.uuid,
            teamA.teamName,
            playersA
        )

        val cord = GameCord(teamToSetA)

        log.info("started new competition cord for teams ${teamA.teamName} vs. ???")

        synchronized(competitions)
        {
            competitions[cordID] = cord
        }

        return cordID
    }

    /**
     * the second may join
     * @param cordID the competition to join
     * @param teamID the team joining
     * */
    fun join(cordID: UUID, teamID: UUID) {
        synchronized(competitions)
        {
            log.info("joining competition with ID $cordID")
            if (competitions.containsKey(cordID)) {
                val team = teamService.get(uuid = teamID)?:throw ServiceException(404, "team A not found")
                val players = playerService.findByTeam(teamID)
                if (players.size==0)throw ServiceException(412, "team A has no team members")

                val teamToSet = TeamWithMembersDto(
                    team.uuid,
                    team.teamName,
                    players
                )

                competitions[cordID]!!.startGame(teamToSet)

                log.info("started new competition cord for teams" +
                        " ${competitions[cordID]!!.teamA.teamName} vs. " +
                        competitions[cordID]!!.teamB!!.teamName
                )
            }
        }
    }

    /**
     * cleans up the competition
     * */
    fun clear()
    {
        synchronized(competitions)
        {
            while(competitions.isNotEmpty())
                competitions.remove(
                    competitions.keys.first()
                )!!.finish()
        }
    }

    /**
     * stops the competition
     * */
    fun stop(cordID: UUID, teamID: UUID) {
        synchronized(competitions)
        {
            log.info("stopping competition for with ID $cordID")
            if (competitions.containsKey(cordID)) {
                val c = competitions[cordID]

                if (c!!.teamA.uuid==teamID || c.teamB?.uuid==teamID)
                {
                    competitions.remove(cordID)!!.finish()
                }
                else throw ServiceException(403, "team is not participating in the competition and therefore not allowed to stop it")
            }
            else throw ServiceException(404, "competition not found")
        }
    }

    /**
     * perform a player action
     * */
    @Throws(ServiceException::class)
    fun action(cordID: UUID, teamID: UUID, userID: UUID, action: Action, target: UUID): ActionResultDto {
        if (!competitions.containsKey(cordID)) {
            throw ServiceException(412, "no competition found")
        }
        else
        {
            val c = competitions[cordID]
            if (c!!.teamA.uuid==teamID || c.teamB?.uuid==teamID)
            {
                return competitions[cordID]!!.performAction(userID, action,target)
            }
            else throw ServiceException(403, "team is not participating in the competition and therefore not allowed to perform an action")
        }
    }

    /**
     * returns the current score
     * */
    @Throws(ServiceException::class)
    fun getScore(cordID: UUID?): ScoreDto? {
        if (!competitions.containsKey(cordID)) {
            throw ServiceException(412, "no competition found")
        }
        else
        {
            val training = competitions[cordID]
            return ScoreDto(
                training!!.teamA.teamName,
                training.resultA,
                training.teamB?.teamName,
                training.resultB
            )
        }
    }
}