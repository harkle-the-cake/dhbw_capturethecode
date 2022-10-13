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
class TeamTest (
	@Autowired val webClient: WebClient,
	@Autowired val teamService: TeamService,
	@Autowired val playerService: PlayerService,
	@Value("\${server.port}") val port: Int,
	@Value("\${admin.token}") val adminToken: String,
	@Value("\${server.servlet.context-path}") private val baseURL: String,
)
{
	val base = "http://localhost:$port$baseURL/team"

	val teamAToken = UUID.randomUUID()!!.toString()
	val teamBToken = UUID.randomUUID()!!.toString()

	lateinit var t1 : TeamDto
	lateinit var t2 : TeamDto

	@BeforeEach
	fun initData()
	{
		playerService.clear()
		teamService.clear()
		t1 = teamService.add( TeamDto(null, teamAToken, "TEAM_A"))
		t2 = teamService.add( TeamDto(null, teamBToken, "TEAM_B"))

		playerService.add(PlayerDto(null,"PLAYER_1","TEAM_A"))
		playerService.add(PlayerDto(null,"PLAYER_2","TEAM_A"))

		playerService.add(PlayerDto(null,"PLAYER_3","TEAM_B"))
		playerService.add(PlayerDto(null,"PLAYER_4","TEAM_B"))
		playerService.add(PlayerDto(null,"PLAYER_5","TEAM_B"))
	}

	@AfterAll
	fun finishTest()
	{
		teamService.clear()
	}

	private fun getToken(token: String) = "token $token"

	/*
	* ############# GET LIST  ################
	* */
	@Test
	fun getListAdmin() {
		val list = webClient.get()
			.uri(base)
			.header("Authorization",getToken(adminToken))
			.retrieve().bodyToMono(List::class.java).block()

		Assertions.assertNotNull(list)
		Assertions.assertEquals(2,list!!.size)
	}

	@Test
	fun getListTeam() {
		val list = webClient.get()
			.uri(base)
			.header("Authorization",getToken(teamAToken))
			.retrieve().bodyToMono(List::class.java).block()

		Assertions.assertNotNull(list)
		Assertions.assertEquals(2,list!!.size)
	}

	@Test
	fun getTeam() {
		val team = webClient.get()
			.uri("$base/${t1.uuid}")
			.header("Authorization",getToken(teamAToken))
			.retrieve().bodyToMono(TeamDto::class.java).block()

		Assertions.assertNotNull(team)
		Assertions.assertEquals(t1.teamName, team!!.teamName)
	}

	@Test
	fun getTeamAllowed() {
		val team = webClient.get()
			.uri("$base/${t1.uuid}")
			.header("Authorization",getToken(teamBToken))
			.retrieve().bodyToMono(TeamDto::class.java).block()

		Assertions.assertNotNull(team)
		Assertions.assertEquals(t1.teamName, team!!.teamName)
	}

	@Test
	fun getTeamNotFound() {
		try {
			webClient.get()
				.uri("$base/${UUID.randomUUID()}")
				.header("Authorization",getToken(teamAToken))
				.retrieve().bodyToMono(TeamDto::class.java).block()
			fail("got team infos")
		}
		catch (e: WebClientResponseException)
		{
			Assertions.assertEquals(404, e.rawStatusCode)
		}
	}

	/*
	* ############# LOAD  ################
	* */
	@Test
	fun loadTeamsDefinition() {
		teamService.clear()
		teamService.loadTeamDefinition()
		val list = teamService.listWithSecret()
		Assertions.assertEquals(2, list.size)
		Assertions.assertEquals("d8b7c387-d27b-4d1d-af1e-a3e2b704bf31",
			list.first { teamDto -> teamDto.uuid==UUID.fromString("d8b7c387-d27b-4d1d-af1e-a3e2b704bf31") }.uuid.toString())
		Assertions.assertEquals("b74c963cc05038a215c7c9bfb10a32a078a45bfb69e23f6750b3aebcdb4ff822",
			list.first { teamDto -> teamDto.uuid==UUID.fromString("d8b7c387-d27b-4d1d-af1e-a3e2b704bf31") }.teamToken)
		Assertions.assertEquals("TEAM_A",
			list.first { teamDto -> teamDto.uuid==UUID.fromString("d8b7c387-d27b-4d1d-af1e-a3e2b704bf31") }.teamName)

		Assertions.assertEquals("f3e5dc72-d318-4a78-86dc-560db901dad1",
			list.first { teamDto -> teamDto.uuid==UUID.fromString("f3e5dc72-d318-4a78-86dc-560db901dad1") }.uuid.toString())
		Assertions.assertEquals("8630c6b38f6522ad067462739026eeb98361d5de89ac91e6f787ab028e4eebf6",
			list.first { teamDto -> teamDto.uuid==UUID.fromString("f3e5dc72-d318-4a78-86dc-560db901dad1") }.teamToken)
		Assertions.assertEquals("TEAM_B",
			list.first { teamDto -> teamDto.uuid==UUID.fromString("f3e5dc72-d318-4a78-86dc-560db901dad1") }.teamName)
	}

	/*
	* ############# ADD  ################
	* */
	@Test
	fun addNewTeam() {
		val toAdd = TeamDto(
			null,
			"AABBCCDDEEFF0011AABBCCDDEEFF0011AABBCCDDEEFF0011AABBCCDDEEFF0011",
			"TEAM_C"
		)
		val uri = webClient.put()
			.uri(base)
			.header("Authorization",getToken(adminToken))
			.bodyValue(toAdd)
			.retrieve().bodyToMono(URI::class.java).block()

		Assertions.assertNotNull(uri)
	}

	@Test
	fun addNewTeamInvalidToken() {
		val toAdd = TeamDto(
			null,
			"TOKEN",
			"TEAM_C"
		)
		try {
			webClient.put()
				.uri(base)
				.header("Authorization",getToken(adminToken))
				.bodyValue(toAdd)
				.retrieve().bodyToMono(URI::class.java).block()
			fail("added team")
		}
		catch (e: WebClientResponseException)
		{
			Assertions.assertEquals(400, e.rawStatusCode)
		}
	}

	@Test
	fun addNewTeamInvalidName() {
		val toAdd = TeamDto(
			null,
			"AABBCCDDEEFF0011AABBCCDDEEFF0011AABBCCDDEEFF0011AABBCCDDEEFF0011",
			"TEAM123456789012345678901234567890"
		)
		try {
			webClient.put()
				.uri(base)
				.header("Authorization",getToken(adminToken))
				.bodyValue(toAdd)
				.retrieve().bodyToMono(URI::class.java).block()
			fail("added team")
		}
		catch (e: WebClientResponseException)
		{
			Assertions.assertEquals(400, e.rawStatusCode)
		}
	}


	@Test
	fun addNewTeamNoToken() {
		val toAdd = TeamDto(
			null,
			null,
			"TEAM_C"
		)
		try {
			webClient.put()
				.uri(base)
				.header("Authorization", getToken(adminToken))
				.bodyValue(toAdd)
				.retrieve().bodyToMono(URI::class.java).block()
			fail("added team with no token")
		}
		catch (e: WebClientResponseException)
		{
			Assertions.assertEquals(400, e.rawStatusCode)
		}
	}

	@Test
	fun addNewTeamTwice() {
		val toAdd = TeamDto(
			null,
			"AABBCCDDEEFF0011AABBCCDDEEFF0011AABBCCDDEEFF0011AABBCCDDEEFF0011",
			"TEAM_A"
		)
		try
		{
			webClient.put()
				.uri(base)
				.header("Authorization",getToken(adminToken))
				.bodyValue(toAdd)
				.retrieve().bodyToMono(URI::class.java).block()
			fail("added team twice")
		}
		catch (e: WebClientResponseException)
		{
			Assertions.assertEquals(409, e.rawStatusCode)
		}
	}

	/*
	* ############# CHANGE  ################
	* */

	@Test
	fun changeTeam() {
		val toAdd = TeamDto(
			null,
			null,
			"TEAM_D"
		)
		val team = webClient.post()
			.uri("$base/${t1.uuid}")
			.header("Authorization",getToken(teamAToken))
			.bodyValue(toAdd)
			.retrieve().bodyToMono(TeamDto::class.java).block()

		Assertions.assertNotNull(team)
		Assertions.assertEquals(team!!.teamName,team.teamName)
	}

	@Test
	fun changeTeamTokenInvalid() {
		val toAdd = TeamDto(
			null,
			null,
			"TEAM_D"
		)
		try {
			webClient.post()
				.uri("$base/${t1.uuid}")
				.header("Authorization",getToken(teamBToken))
				.bodyValue(toAdd)
				.retrieve().bodyToMono(TeamDto::class.java).block()
		}
		catch (e: WebClientResponseException)
		{
			Assertions.assertEquals(403, e.rawStatusCode)
		}
	}

	@Test
	fun changeTeamNotFound() {
		val toAdd = TeamDto(
			null,
			null,
			"TEAM_D"
		)
		try {
			webClient.post()
				.uri("$base/${UUID.randomUUID()}")
				.header("Authorization",getToken(teamAToken))
				.bodyValue(toAdd)
				.retrieve().bodyToMono(TeamDto::class.java).block()
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
	fun deleteTeam() {
		try {
			webClient.delete()
				.uri("$base/${t1.uuid}")
				.header("Authorization",getToken(adminToken))
				.retrieve().bodyToMono(Unit::class.java).block()
		}
		catch (e: Exception)
		{
			fail(e)
		}
	}

	@Test
	fun deleteTeamTokenInvalid() {
		try {
			webClient.delete()
				.uri("$base/${t1.uuid}")
				.header("Authorization",getToken(teamAToken))
				.retrieve().bodyToMono(TeamDto::class.java).block()
		}
		catch (e: WebClientResponseException)
		{
			Assertions.assertEquals(403, e.rawStatusCode)
		}
	}

	@Test
	fun deleteTeamNotFound() {
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

	@Test
	fun deleteTeamMembers() {
		try {
			webClient.delete()
				.uri("$base/members")
				.header("Authorization",getToken(teamAToken))
				.retrieve().bodyToMono(Unit::class.java).block()
			Assertions.assertEquals(0,playerService.findByTeam(t1.uuid!!).size)
		}
		catch (e: Exception)
		{
			fail(e)
		}
	}
}
