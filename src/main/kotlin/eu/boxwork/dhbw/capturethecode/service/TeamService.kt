package eu.boxwork.dhbw.capturethecode.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import eu.boxwork.dhbw.capturethecode.dto.*
import eu.boxwork.dhbw.capturethecode.model.Team
import eu.boxwork.dhbw.capturethecode.service.repo.PlayerRepository
import eu.boxwork.dhbw.capturethecode.service.repo.TeamRepository
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.io.File
import java.util.*
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import javax.transaction.Transactional
import kotlin.collections.ArrayList

@Service
@Transactional
class TeamService(
    @PersistenceContext private val entityManagement: EntityManager,
    @Autowired private val repo: TeamRepository,
    @Autowired private val playerRepository: PlayerRepository,
    @Value("\${team.count}") private val max: Int,
    @Value("\${spring.profiles.active}") private val profile: String = "prod",
    @Value("\${team.definition.file}") private val teamDefinitionFileName: String,
) {
    private val log = LogManager.getLogger("TeamService")

    @EventListener(ApplicationReadyEvent::class)
    fun runAfterStartup() {
       if (profile == "prod") loadTeamDefinition()
       else if (log.isDebugEnabled) log.debug("not loading teams definition, not in PROD mode")
    }

    /**
     * loads the team definition from the file; updates the content and adds the teams
     * */
    @Throws(Exception::class)
    fun loadTeamDefinition()
    {
        val mapper = ObjectMapper()
        // read the file
        val teams = mapper.readValue(File(teamDefinitionFileName), object : TypeReference<List<TeamDto>>(){} )
        // iterate through all teams in definition and load it
        log.info("loading ${teams.size} teams")
        teams.forEach {
                teamDto ->
            run {
                if (log.isDebugEnabled) log.debug("adding team: ${teamDto.teamName}")
                // we add the new one
                val toAdd = Team(
                    teamDto.uuid?: UUID.randomUUID(),
                    teamDto.teamToken?:"",
                    teamDto.teamName
                )
                entityManagement.persist(toAdd)
            }
        }
        log.info("loaded/updated ${teams.size} teams")
    }

    /*
    * CRUD functions
    * */
    /**
     * adds a new team, the uuid will be generated
     * @param team the team to add
     * @return the added team or null in case of an error
     * */
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
            team.uuid?:UUID.randomUUID(),
            team.teamToken.trim().lowercase(),
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
    fun get(uuid: UUID) : TeamDto?
    {
        val ret = entityManagement.find(Team::class.java, uuid)?: return null
        return ret.dto()
    }

    /**
     * clear all teams
     * */
    fun clear() {
        repo.deleteAll()
    }

    /**
     * returns the count of teams
     * */
    fun count() = repo.count()

    /**
     * returns all teams
     * @return all teams as list
     * */
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
    fun deleteMembers(id: UUID) {
        playerRepository.findByTeamUuid(id).forEach { player -> playerRepository.delete(player) }
    }

    /**
     * returns all teams with secret
     * @return all teams as list
     * */
    fun listWithSecret(): MutableList<TeamDto>  {
        val teams = repo.findAll()
        val ret: MutableList<TeamDto> = ArrayList()
        teams.forEach { ret.add(it.dtoWithSecret()) }
        return ret
    }

    fun listUI(): MutableList<SpectatedTeamDto> {
        val ret : MutableList<SpectatedTeamDto> = ArrayList()
        val teams = repo.findAll()
        teams.forEach {
            ret.add(
                SpectatedTeamDto(
                    it.uuid,
                    it.teamName                )
            )
        }
        return ret
    }

    fun getTeamUI(id: UUID): SpectatedTeamWithMembersDto? {
        val ret = entityManagement.find(Team::class.java, id)?: return null
        val members = playerRepository.findByTeamUuid(id)
        val toSet : MutableList<SpectatedPlayerDto> = ArrayList()
        members.forEach { toSet.add(SpectatedPlayerDto(it.name)) }
        return SpectatedTeamWithMembersDto(ret.uuid, ret.teamName, toSet)
    }
}