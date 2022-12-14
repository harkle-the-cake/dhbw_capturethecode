package eu.boxwork.dhbw.capturethecode.service

import eu.boxwork.dhbw.capturethecode.dto.ActionResultDto
import eu.boxwork.dhbw.capturethecode.dto.ScoreDto
import eu.boxwork.dhbw.capturethecode.dto.TeamWithMembersDto
import eu.boxwork.dhbw.capturethecode.dto.SpectatedGameGroundDto
import eu.boxwork.dhbw.capturethecode.enums.Action
import eu.boxwork.dhbw.capturethecode.model.Event
import eu.boxwork.dhbw.capturethecode.model.GameGround
import eu.boxwork.dhbw.capturethecode.runner.CompetitionRunner
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Value
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedTransferQueue
import kotlin.collections.ArrayList
import kotlin.jvm.Throws

abstract class AbstractGameService(
    private val teamService: TeamService,
    private val playerService: PlayerService,
    @Value("\${competition.rounds}") private val rounds: Int,
    private val isTraining: Boolean
) {
    protected val log = LogManager.getLogger("AbstractGameService")
    protected val gameGrounds : MutableMap<UUID, GameGround> = ConcurrentHashMap()
    protected val runners : MutableMap<UUID, CompetitionRunner> = ConcurrentHashMap()

    @Throws(ServiceException::class)
    fun start(teamID: UUID): UUID {
        val cordID = UUID.randomUUID()
        val team = teamService.get(uuid = teamID)?:throw ServiceException(404, "team not found")
        val players = playerService.findByTeam(teamID)
        if (players.size==0)throw ServiceException(412, "team has no team members")

        val teamToSet = TeamWithMembersDto(
            team.uuid,
            team.teamName,
            players
        )

        val cord = GameGround(teamToSet,rounds, isTraining)
        if (isTraining) cord.startGame(teamToSet)

        log.info("initialising new ground for team $teamID")

        synchronized(gameGrounds)
        {
            gameGrounds[cordID] = cord
        }

        return cordID
    }

    fun clear()
    {
        synchronized(gameGrounds)
        {
            while(gameGrounds.isNotEmpty())
                gameGrounds.remove(
                    gameGrounds.keys.first()
                )!!.finish()

            while(runners.isNotEmpty())
                runners.remove(
                    runners.keys.first()
                )!!.finish()
        }
    }

    @Throws(ServiceException::class)
    fun stop(id: UUID, teamID: UUID) {
        synchronized(gameGrounds)
        {
            if (!gameGrounds.containsKey(id)) {
                throw ServiceException(412, "team not on game ground")
            }

            log.info("stopping game for ID $id")
            if (gameGrounds.containsKey(id)) {
                gameGrounds[id]!!.finish()
                gameGrounds.remove(id)
            }
        }
    }

    /**
     * returns the current score
     * */
    @Throws(ServiceException::class)
    fun getScore(id: UUID, teamID: UUID): ScoreDto? {
        if (!gameGrounds.containsKey(id)) {
            throw ServiceException(412, "team not on game ground")
        }
        else
        {
            val training = gameGrounds[id]
            return ScoreDto(
                training!!.teamA.teamName,
                training.resultA,
                training.teamB?.teamName,
                training.resultB,
                training.round
            )
        }
    }

    @Throws(ServiceException::class)
    fun action(groundID: UUID, teamID: UUID, userID: UUID, action: Action): ActionResultDto {
        if (!gameGrounds.containsKey(groundID)) {
            throw ServiceException(412, "team not in training")
        }
        if (!playerService.isTeamMember(teamID, userID)) {
            throw ServiceException(403, "user ist not part of the team")
        }
        return gameGrounds[groundID]!!.performAction(userID, action)
    }

    @Throws(ServiceException::class)
    fun action(groundID: UUID, teamID: UUID, userID: UUID, action: Action, target: UUID): ActionResultDto {
        if (!gameGrounds.containsKey(groundID)) {
            throw ServiceException(412, "team not in training")
        }
        if (!playerService.isTeamMember(teamID, userID)) {
            throw ServiceException(403, "user ist not part of the team")
        }
        return gameGrounds[groundID]!!.performAction(userID, action,target)
    }

    /**
     * checks if the user has the flag
     * */
    @Throws(ServiceException::class)
    fun checkForFlag(groundID: UUID, teamID: UUID, userID: UUID): Boolean {
        if (!gameGrounds.containsKey(groundID)) {
            throw ServiceException(412, "team not in game ground")
        }
        return gameGrounds[groundID]!!.hasFlag(userID)
    }

    fun count(): Int
    {
        return gameGrounds.size
    }

    fun listUI(): MutableList<SpectatedGameGroundDto> {
        val ret : MutableList<SpectatedGameGroundDto> = ArrayList()

        gameGrounds.forEach {
            val eventsToSet : LinkedTransferQueue<Event> = LinkedTransferQueue()
            eventsToSet.addAll(it.value.events)
            ret.add(
                SpectatedGameGroundDto(
                    it.key,
                    it.value.teamA.teamName,
                    it.value.teamB?.teamName,
                    eventsToSet,
                    it.value.getPlayerInfos()
                )
            )
        }
        return ret
    }

    /**
     * @param id game ground id
     * @return the information shown to the spectator
     * */
    fun spectate(id: UUID): SpectatedGameGroundDto? {
        if (!gameGrounds.containsKey(id)) return null
        val g = gameGrounds[id]
        val eventsToSet: LinkedTransferQueue<Event> = LinkedTransferQueue()
        eventsToSet.addAll(g!!.events)
        return SpectatedGameGroundDto(
            id,
            g.teamA.teamName,
            g.teamB?.teamName,
            eventsToSet,
            g.getPlayerInfos()
        )
    }
}