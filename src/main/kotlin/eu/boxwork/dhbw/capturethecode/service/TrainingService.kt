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
class TrainingService(
    @Autowired private val teamService: TeamService,
    @Autowired private val playerService: PlayerService
) : AbstractGameService(teamService, playerService) {

    /**
     * performs a turn in the training
     * @return true, if game is over
     * */
    @Throws(ServiceException::class)
    fun turn(id: UUID, teamID: UUID) : Boolean {
        if (!gameGrounds.containsKey(id)) {
            throw ServiceException(412, "team not in training")
        }
        return gameGrounds[id]!!.performRound()
    }

}