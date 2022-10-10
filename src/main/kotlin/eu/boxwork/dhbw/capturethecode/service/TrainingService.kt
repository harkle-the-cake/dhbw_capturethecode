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

    /**
     * performs a turn in the training
     * @return true, if game is over
     * */
    @Throws(ServiceException::class)
    fun turn(teamID: UUID?) : Boolean {
        if (!trainings.containsKey(teamID)) {
            throw ServiceException(412, "team not in training")
        }
        return trainings[teamID]!!.performRound()
    }

    @Throws(ServiceException::class)
    fun action(teamID: UUID, userID: UUID, action: Action): ActionResultDto {
        if (!trainings.containsKey(teamID)) {
            throw ServiceException(412, "team not in training")
        }
        if (!playerService.isTeamMember(teamID, userID)) {
            throw ServiceException(403, "user ist not part of the team")
        }
        return trainings[teamID]!!.performAction(userID, action)
    }
    @Throws(ServiceException::class)
    fun action(teamID: UUID, userID: UUID, action: Action, target: UUID): ActionResultDto {
        if (!trainings.containsKey(teamID)) {
            throw ServiceException(412, "team not in training")
        }
        if (!playerService.isTeamMember(teamID, userID)) {
            throw ServiceException(403, "user ist not part of the team")
        }
        return trainings[teamID]!!.performAction(userID, action,target)
    }

    /**
     * performs an observation
     * */
    @Throws(ServiceException::class)
    fun observe(teamID: UUID, userID: UUID): ActionResultDto? {
        if (!trainings.containsKey(teamID)) {
            throw ServiceException(412, "team not in training")
        }
        if (!playerService.isTeamMember(teamID, userID)) {
            throw ServiceException(403, "user ist not part of the team")
        }
        return trainings[teamID]!!.performActionObserve(userID)
    }
    /**
     * checks if the user has the flag
     * */
    @Throws(ServiceException::class)
    fun checkForFlag(teamID: UUID, userID: UUID): Boolean {
        if (!trainings.containsKey(teamID)) {
            throw ServiceException(412, "team not in training")
        }
        return trainings[teamID]!!.hasFlag(userID)
    }
}