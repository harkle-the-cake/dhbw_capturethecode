package eu.boxwork.dhbw.capturethecode.service

import eu.boxwork.dhbw.capturethecode.dto.PlayerDto
import eu.boxwork.dhbw.capturethecode.model.Player
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
class PlayerService(
    @PersistenceContext private val entityManagement: EntityManager,
    @Autowired private val repo: PlayerRepository,
    @Autowired private val teamRepo: TeamRepository,
    @Value("\${admin.token}") private val adminToken: String,
) {
    private val log = LogManager.getLogger("PlayerService")
    private val max = 100

    /*
    * CRUD functions
    * */
    /**
     * adds a new player, the uuid will be generated
     * @param player the player to add
     * @return the player team or null in case of an error
     * */
    @Transactional
    @Throws(ServiceException::class)
    fun add(player: PlayerDto) : PlayerDto
    {
        val cnt = repo.count()
        if (cnt>=max) throw ServiceException(418,"team limit exceeded")

        val playerIn = repo.findByName(player.name)

        if (playerIn!=null)
        {
            throw ServiceException(409,"player already exists")
        }

        val team = teamRepo.findByTeamName(player.teamName)?:throw ServiceException(412,"player not available")

        val toAdd = Player(
            uuid = UUID.randomUUID(),
            player.name,
            team
        )
        entityManagement.persist(toAdd)
        return toAdd.dto()
    }

    /**
     * changes a player, sets name
     * @param token password of the team
     * @param uuid the id
     * @param player player info to set
     * @return the changed player or null
     * */
    @Transactional
    @Throws(ServiceException::class)
    fun change(token: String, uuid: UUID, player: PlayerDto) : PlayerDto?
    {
        val ret = entityManagement.find(Player::class.java, uuid)?: throw ServiceException(404,"player not found")
        if (ret.team.teamToken == token || adminToken==token)
        {
            ret.name = player.name
            return ret.dto()
        }
        else
        {
            throw ServiceException(403,"not allowed to change the player")
        }
    }

    /**
     * deletes a players by uuid
     * @param uuid the id of the players
     * */
    @Transactional
    fun delete(token: String, uuid: UUID)
    {
        val inDB = entityManagement.find(Player::class.java, uuid)?: return
        if (inDB.team.teamToken!=token && adminToken!=token)throw ServiceException(403,"not allowed to delete the player")
        entityManagement.remove(inDB)
    }

    /**
     * returns the players by UUID
     * @param uuid the id of the players
     * @return the players
     * */
    @Transactional
    fun get(uuid: UUID) : PlayerDto?
    {
        val ret = entityManagement.find(Player::class.java, uuid)?: return null
        return ret.dto()
    }

    /**
     * returns the players by name
     * @param name the name of the players
     * @return the players
     * */
    @Transactional
    fun get(name: String) : PlayerDto?
    {
        val ret = repo.findByName(name)?: return null
        return ret.dto()
    }

    /**
     * clear all players
     * */
    @Transactional
    fun clear() {
        repo.deleteAll()
    }

    /**
     * returns the count of players
     * */
    @Transactional
    fun count() = repo.count()

    /**
     * returns all players
     * @return all players as list
     * */
    @Transactional
    fun list(): MutableList<PlayerDto> {
        val players = repo.findAll()
        val ret: MutableList<PlayerDto> = ArrayList()
        players.forEach { ret.add(it.dto()) }
        return ret
    }

    /**
     * returns all players for a team
     * @param id the team id
     * @return all players as list
     * */
    fun findByTeam(id: UUID): MutableList<PlayerDto> {
        val players = repo.findByTeamUuid(id)
        val ret: MutableList<PlayerDto> = ArrayList()
        players.forEach { ret.add(it.dto()) }
        return ret
    }
}