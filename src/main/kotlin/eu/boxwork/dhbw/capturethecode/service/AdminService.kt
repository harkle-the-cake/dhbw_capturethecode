package eu.boxwork.dhbw.capturethecode.service

import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*
import javax.transaction.Transactional

@Service
class AdminService(
    @Autowired private val repoPlayer: PlayerRepository,
    @Autowired private val repoTeam: TeamRepository,
    @Autowired private val repoTraining: TrainingService,
    @Autowired private val repoCompetition: CompetitionService,
) {
    private val log = LogManager.getLogger("AdminService")

    /**
     * clear all
     * */
    @Transactional
    fun clear() {
        repoCompetition.clear()
        repoTraining.clear()
        repoPlayer.deleteAll()
        repoTeam.deleteAll()
    }

    /**
     * clear team
     * @param id team id
     * */
    @Transactional
    fun deleteTeam(id: UUID) {
        repoPlayer.findByTeamUuid(id).forEach { player -> repoPlayer.delete(player) }
        val teamRet = repoTeam.findById(id)
        if (teamRet.isPresent) repoTeam.delete(teamRet.get())
    }

    /**
     * clear player
     * @param id player id
     * */
    @Transactional
    fun deletePlayer(id: UUID) {
        val ret = repoPlayer.findById(id)
        if (ret.isPresent) repoPlayer.delete(ret.get())
    }

    /**
     * clear team players
     * @param id team id
     * */
    @Transactional
    fun deleteMembers(id: UUID) {
        repoPlayer.findByTeamUuid(id).forEach { player -> repoPlayer.delete(player) }
    }

    /**
     * clear team players
     * @param id team id
     * */
    fun clearTrainings() {
        repoTraining.clear()
    }

    /**
     * clear team players
     * @param id team id
     * */
    fun clearCompetitions() {
        repoCompetition.clear()
    }
}