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
class TrainingService(
    @Autowired private val teamService: TeamService,
    @Autowired private val playerService: PlayerService
) {
    private val log = LogManager.getLogger("TrainingService")
    private val trainings : MutableMap<UUID, GameCord> = ConcurrentHashMap()

    @Throws(ServiceException::class)
    fun start(teamID: UUID) {
        val team = teamService.get(uuid = teamID)?:throw ServiceException(404, "team not found")
        val players = playerService.findByTeam(teamID)
        if (players.size==0)throw ServiceException(412, "team has no team members")

        val teamToSet = TeamWithMembersDto(
            team.uuid,
            team.teamName,
            players
        )

        val cord = GameCord(teamToSet)
        cord.startGame(teamToSet)

        log.info("started new training cord for team $teamID")

        synchronized(trainings)
        {
            trainings[teamID] = cord
        }
    }

    fun clear()
    {
        synchronized(trainings)
        {
            while(trainings.isNotEmpty())
                trainings.remove(
                    trainings.keys.first()
                )!!.finish()
        }
    }

    fun stop(teamID: UUID) {
        synchronized(trainings)
        {
            log.info("stopping training cord for team $teamID")
            if (trainings.containsKey(teamID)) {
                trainings[teamID]!!.finish()
                trainings.remove(teamID)
            }
        }
    }

    @Throws(ServiceException::class)
    fun action(teamID: UUID, userID: UUID, action: Action, target: UUID): ActionResultDto {
        if (!trainings.containsKey(teamID)) {
            throw ServiceException(412, "team not in training")
        }
        return trainings[teamID]!!.performAction(userID, action,target)
    }

    /**
     * returns the current score
     * */
    @Throws(ServiceException::class)
    fun getScore(teamID: UUID?): ScoreDto? {
        if (!trainings.containsKey(teamID)) {
            throw ServiceException(412, "team not in training")
        }
        else
        {
            val training = trainings[teamID]
            return ScoreDto(
                training!!.teamA.teamName,
                training.resultA,
                training.teamB?.teamName,
                training.resultB
            )
        }
    }
}