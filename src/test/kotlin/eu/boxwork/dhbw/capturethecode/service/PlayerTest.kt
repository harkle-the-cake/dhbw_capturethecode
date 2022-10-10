package eu.boxwork.dhbw.capturethecode.service

import eu.boxwork.dhbw.capturethecode.dto.PlayerDto
import eu.boxwork.dhbw.capturethecode.dto.TeamDto
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.net.URI
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(TestConfiguration::class)
class PlayerTest (
	@Autowired val webClient: WebClient,
	@Autowired val teamService: TeamService,
	@Autowired val playerService: PlayerService,
	@Value("\${server.port}") val port: Int,
	@Value("\${admin.token}") val adminToken: String,
)
{
	val base = "http://localhost:$port/player"

	val teamAToken = UUID.randomUUID()!!.toString()
	val teamBToken = UUID.randomUUID()!!.toString()

	lateinit var t1 : TeamDto
	lateinit var t2 : TeamDto
	lateinit var p1 : PlayerDto
	lateinit var p3 : PlayerDto

	@BeforeEach
	fun initData()
	{
		playerService.clear()
		teamService.clear()
		t1 = teamService.add( TeamDto(null, teamAToken, "TEAM_A"))
		t2 = teamService.add( TeamDto(null, teamBToken, "TEAM_B"))

		p1 = playerService.add(PlayerDto(null,"PLAYER_1","TEAM_A"))
		playerService.add(PlayerDto(null,"PLAYER_2","TEAM_A"))

		p3 = playerService.add(PlayerDto(null,"PLAYER_3","TEAM_B"))
		playerService.add(PlayerDto(null,"PLAYER_4","TEAM_B"))
		playerService.add(PlayerDto(null,"PLAYER_5","TEAM_B"))
	}

	@AfterAll
	fun finishTest()
	{
		playerService.clear()
		teamService.clear()
	}

	private fun getToken(token: String) = "token $token"

	/*
	* ############# GET LIST  ################
	* */
	@Test
	fun getListAllPlayersAdmin() {
		val list = webClient.get()
			.uri(base)
			.header("Authorization",getToken(adminToken))
			.retrieve().bodyToMono(List::class.java).block()

		Assertions.assertNotNull(list)
		Assertions.assertEquals(5,list!!.size)
	}

	@Test
	fun getListAllPlayers() {
		val list = webClient.get()
			.uri(base)
			.header("Authorization",getToken(teamAToken))
			.retrieve().bodyToMono(List::class.java).block()

		Assertions.assertNotNull(list)
		Assertions.assertEquals(5,list!!.size)
	}

	/*
	* ############# GET LIST for TEAM ################
	* */
	@Test
	fun getListAllPlayersInTeamAdmin() {
		val list = webClient.get()
			.uri("$base/team/${t1.uuid}")
			.header("Authorization",getToken(adminToken))
			.retrieve().bodyToMono(List::class.java).block()

		Assertions.assertNotNull(list)
		Assertions.assertEquals(2,list!!.size)
	}

	@Test
	fun getListAllPlayersInTeam() {
		val list = webClient.get()
			.uri("$base/team/${t1.uuid}")
			.header("Authorization",getToken(teamAToken))
			.retrieve().bodyToMono(List::class.java).block()

		Assertions.assertNotNull(list)
		Assertions.assertEquals(2,list!!.size)
	}

	/*
	* SINGLE PLAYER
	* */
	@Test
	fun getPlayer() {
		val player = webClient.get()
			.uri("$base/${p1.uuid}")
			.header("Authorization",getToken(teamAToken))
			.retrieve().bodyToMono(PlayerDto::class.java).block()

		Assertions.assertNotNull(player)
		Assertions.assertEquals(p1.teamName, player!!.teamName)
		Assertions.assertEquals(p1.name, player.name)
	}

	@Test
	fun getPlayerAllowed() {
		val player = webClient.get()
			.uri("$base/${p1.uuid}")
			.header("Authorization",getToken(teamBToken))
			.retrieve().bodyToMono(PlayerDto::class.java).block()

		Assertions.assertNotNull(player)
		Assertions.assertEquals(p1.teamName, player!!.teamName)
		Assertions.assertEquals(p1.name, player.name)
	}

	@Test
	fun getPlayerNotFound() {
		try {
			webClient.get()
				.uri("$base/${UUID.randomUUID()}")
				.header("Authorization",getToken(teamAToken))
				.retrieve().bodyToMono(TeamDto::class.java).block()
			fail("got player infos")
		}
		catch (e: WebClientResponseException)
		{
			Assertions.assertEquals(404, e.rawStatusCode)
		}
	}


	/*
	* ############# ADD  ################
	* */
	@Test
	fun addNewPlayer() {
		val toAdd = PlayerDto(
			null,
			"newPLAYER",
			"TEAM_A"
		)
		val added = webClient.put()
			.uri(base)
			.header("Authorization",getToken(teamAToken))
			.bodyValue(toAdd)
			.retrieve().bodyToMono(UUID::class.java).block()

		Assertions.assertNotNull(added)
	}

	@Test
	fun addNewPlayerNotAllowed() {
		val toAdd = PlayerDto(
			null,
			"newPLAYERName",
			"TEAM_A"
		)

		try {
			webClient.put()
				.uri(base)
				.header("Authorization",getToken(adminToken))
				.bodyValue(toAdd)
				.retrieve().bodyToMono(UUID::class.java).block()
			fail("added player")
		} catch (e: WebClientResponseException)
		{
			Assertions.assertEquals(403,e.rawStatusCode)
		}
	}

	/*
	* ############# CHANGE  ################
	* */

	@Test
	fun changePlayer() {
		val toAdd = PlayerDto(
			null,
			"newPLAYERName",
			"TEAM_B"
		)
		val player = webClient.post()
			.uri("$base/${p1.uuid}")
			.header("Authorization",getToken(teamAToken))
			.bodyValue(toAdd)
			.retrieve().bodyToMono(PlayerDto::class.java).block()

		Assertions.assertNotNull(player)
		Assertions.assertEquals(t1.teamName,player!!.teamName)
		Assertions.assertEquals(toAdd.name,player.name)
	}

	@Test
	fun changePlayerTokenInvalid() {
		val toAdd = PlayerDto(
			null,
			"newPLAYERName",
			"TEAM_B"
		)
		try {
			webClient.post()
				.uri("$base/${p1.uuid}")
				.header("Authorization",getToken(teamBToken))
				.bodyValue(toAdd)
				.retrieve().bodyToMono(PlayerDto::class.java).block()
			fail("changed player")
		}
		catch (e: WebClientResponseException)
		{
			Assertions.assertEquals(403, e.rawStatusCode)
		}
	}

	@Test
	fun changePlayerNotFound() {
		val toAdd = PlayerDto(
			null,
			"newPLAYERName",
			"TEAM_B"
		)
		try {
			webClient.post()
				.uri("$base/${UUID.randomUUID()}")
				.header("Authorization",getToken(teamBToken))
				.bodyValue(toAdd)
				.retrieve().bodyToMono(PlayerDto::class.java).block()
		}
		catch (e: WebClientResponseException)
		{
			Assertions.assertEquals(404, e.rawStatusCode)
		}
	}

	/*
	* ############# DELETE ################
	* */
	@Test
	fun deletePlayer() {
		try {
			webClient.delete()
				.uri("$base/${p1.uuid}")
				.header("Authorization",getToken(adminToken))
				.retrieve().bodyToMono(Unit::class.java).block()
		}
		catch (e: Exception)
		{
			fail(e)
		}
	}

	@Test
	fun deletePlayerTokenInValid() {
		try {
			webClient.delete()
				.uri("$base/${p1.uuid}")
				.header("Authorization",getToken(teamBToken))
				.retrieve().bodyToMono(TeamDto::class.java).block()
			fail("deleted player")
		}
		catch (e: WebClientResponseException)
		{
			Assertions.assertEquals(403, e.rawStatusCode)
		}
	}

	@Test
	fun deletePlayerTokenValid() {
		try {
			webClient.delete()
				.uri("$base/${p1.uuid}")
				.header("Authorization",getToken(teamAToken))
				.retrieve().bodyToMono(TeamDto::class.java).block()
		}
		catch (e: WebClientResponseException)
		{
			fail(e)
		}
	}

	@Test
	fun deletePlayerNotFound() {
		try {
			webClient.delete()
				.uri("$base/${UUID.randomUUID()}")
				.header("Authorization",getToken(adminToken))
				.retrieve().bodyToMono(TeamDto::class.java).block()
		}
		catch (e: WebClientResponseException)
		{
			Assertions.assertEquals(404, e.rawStatusCode)
		}
	}
}
