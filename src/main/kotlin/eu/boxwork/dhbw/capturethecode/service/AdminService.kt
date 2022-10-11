package eu.boxwork.dhbw.capturethecode.service

import eu.boxwork.dhbw.capturethecode.service.repo.PlayerRepository
import eu.boxwork.dhbw.capturethecode.service.repo.TeamRepository
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