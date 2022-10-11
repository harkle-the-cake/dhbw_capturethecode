package eu.boxwork.dhbw.capturethecode.service

import eu.boxwork.dhbw.capturethecode.dto.TeamDto
import eu.boxwork.dhbw.capturethecode.model.Team
import eu.boxwork.dhbw.capturethecode.service.repo.PlayerRepository
import eu.boxwork.dhbw.capturethecode.service.repo.TeamRepository
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import javax.transaction.Transactional
import kotlin.collections.ArrayList
import kotlin.jvm.Throws

@Service
class TeamService(
    @PersistenceContext private val entityManagement: EntityManager,
    @Autowired private val repo: TeamRepository,
    @Autowired private val playerRepository: PlayerRepository,
    @Value("\${team.count}") private val max: Int,
) {
    private val log = LogManager.getLogger("TeamService")

    /*
    * CRUD functions
    * */
    /**
     * adds a new team, the uuid will be generated
     * @param team the team to add
     * @return the added team or null in case of an error
     * */
    @Transactional
    @Throws(ServiceException::class)
    fun add(team: TeamDto) : TeamDto
    {
        val cnt = repo.count()
        if (cnt>=max) throw ServiceException(418,"team limit exceeded")

        val ret = repo.findByTeamName(team.teamName)
        if (ret!=null)
        {
            throw ServiceException(409,"team already available")
        }
        if (team.teamToken==null)
        {
            throw ServiceException(400,"token not set, not adding the team")
        }

        val toAdd = Team(
            uuid = UUID.randomUUID(),
            team.teamToken,
            team.teamName
        )
        entityManagement.persist(toAdd)
        return toAdd.dto()
    }

    /**
     * changes a team, sets name
     * @param token password of the team
     * @param uuid the id
     * @param team team info to set, does not change the token
     * @return the changed team or null
     * */
    @Transactional
    fun change(token: String, uuid: UUID, team: TeamDto) : TeamDto?
    {
        val ret = entityManagement.find(Team::class.java, uuid)?: throw ServiceException(404,"team not found")
        if (ret.teamToken!=token)throw ServiceException(403,"not allowed to change the team")
        ret.teamName = team.teamName
        return ret.dto()
    }

    /**
     * deletes a team by uuid
     * @param uuid the id of the team
     * */
    @Transactional
    fun delete(uuid: UUID)
    {
        val inDB = entityManagement.find(Team::class.java, uuid)?: return
        entityManagement.remove(inDB)
    }

    /**
     * returns the team by UUID
     * @param uuid the id of the team
     * @return the team
     * */
    @Transactional
    fun get(uuid: UUID) : TeamDto?
    {
        val ret = entityManagement.find(Team::class.java, uuid)?: return null
        return ret.dto()
    }

    /**
     * clear all teams
     * */
    @Transactional
    fun clear() {
        repo.deleteAll()
    }

    /**
     * returns the count of teams
     * */
    @Transactional
    fun count() = repo.count()

    /**
     * returns all teams
     * @return all teams as list
     * */
    @Transactional
    fun list(): MutableList<TeamDto> {
        val teams = repo.findAll()
        val ret: MutableList<TeamDto> = ArrayList()
        teams.forEach { ret.add(it.dto()) }
        return ret
    }

    /**
     * clear team players
     * @param id team id
     * */
    @Transactional
    fun deleteMembers(id: UUID) {
        playerRepository.findByTeamUuid(id).forEach { player -> playerRepository.delete(player) }
    }
}