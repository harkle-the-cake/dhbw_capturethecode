package eu.boxwork.dhbw.capturethecode.service

import eu.boxwork.dhbw.capturethecode.dto.ActionResultDto
import eu.boxwork.dhbw.capturethecode.dto.PlayerDto
import eu.boxwork.dhbw.capturethecode.dto.ScoreDto
import eu.boxwork.dhbw.capturethecode.dto.TeamDto
import eu.boxwork.dhbw.capturethecode.enums.Action
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(TestConfiguration::class)
class AdminTest (
	@Autowired val webClient: WebClient,
	@Autowired val teamService: TeamService,
	@Autowired val playerService: PlayerService,
	@Autowired val trainingService: TrainingService,
	@Autowired val competitionService: CompetitionService,
	@Value("\${server.port}") val port: Int,
	@Value("\${admin.token}") val adminToken: String,
)
{
	val base = "http://localhost:$port/admin"

	val teamAToken = UUID.randomUUID()!!.toString()
	val teamBToken = UUID.randomUUID()!!.toString()

	lateinit var t1 : TeamDto
	lateinit var t2 : TeamDto

	lateinit var p1 : PlayerDto
	lateinit var p2 : PlayerDto
	lateinit var p3 : PlayerDto
	lateinit var p4 : PlayerDto

	var gameIDA = UUID.randomUUID()
	lateinit var gameIDB : UUID
	lateinit var gameIDC : UUID

	@BeforeEach
	fun initData()
	{
		playerService.clear()
		teamService.clear()
		trainingService.clear()
		competitionService.clear()

		t1 = teamService.add( TeamDto(null, teamAToken, "TEAM_A"))
		t2 = teamService.add( TeamDto(null, teamBToken, "TEAM_B"))

		p4 = playerService.add(PlayerDto(null,"PLAYER_1","TEAM_A"))
		playerService.add(PlayerDto(null,"PLAYER_2","TEAM_A"))

		p1 = playerService.add(PlayerDto(null,"PLAYER_3","TEAM_B"))
		p2 = playerService.add(PlayerDto(null,"PLAYER_4","TEAM_B"))
		p3 = playerService.add(PlayerDto(null,"PLAYER_5","TEAM_B"))

		gameIDA = trainingService.start(t1.uuid!!)
		gameIDB = trainingService.start(t2.uuid!!)

		gameIDC = competitionService.start(t1.uuid!!)
	}

	@AfterEach
	fun tearDown()
	{
		playerService.clear()
		teamService.clear()
		trainingService.clear()
		competitionService.clear()
	}

	@AfterAll
	fun finishTest()
	{
	}

	private fun getToken(token: String) = "token $token"

	/*
	* ############# GET LIST  ################
	* */
	@Test
	fun clearTrainings() {
		try {
			webClient.delete()
				.uri("$base/trainings")
				.header("Authorization",getToken(adminToken))
				.retrieve().bodyToMono(Unit::class.java).block()
			Assertions.assertEquals(0, trainingService.count())
			Assertions.assertEquals(1, competitionService.count())
			Assertions.assertEquals(2, teamService.count())
			Assertions.assertEquals(5, playerService.count())
		}
		catch ( e: WebClientResponseException)
		{
			fail(e)
		}
	}

	@Test
	fun clearCompetitions() {
		try {
			webClient.delete()
				.uri("$base/competitions")
				.header("Authorization",getToken(adminToken))
				.retrieve().bodyToMono(Unit::class.java).block()
			Assertions.assertEquals(2, trainingService.count())
			Assertions.assertEquals(0, competitionService.count())
			Assertions.assertEquals(2, teamService.count())
			Assertions.assertEquals(5, playerService.count())
		}
		catch ( e: WebClientResponseException)
		{
			fail(e)
		}
	}

	@Test
	fun clearAll() {
		try {
			webClient.delete()
				.uri("$base/clear")
				.header("Authorization",getToken(adminToken))
				.retrieve().bodyToMono(Unit::class.java).block()
			Assertions.assertEquals(0, trainingService.count())
			Assertions.assertEquals(0, competitionService.count())
			Assertions.assertEquals(0, teamService.count())
			Assertions.assertEquals(0, playerService.count())
		}
		catch ( e: WebClientResponseException)
		{
			fail(e)
		}
	}
}
