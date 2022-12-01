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
class CompetitionTest (
	@Autowired val webClient: WebClient,
	@Autowired val teamService: TeamService,
	@Autowired val playerService: PlayerService,
	@Autowired val competitionService: CompetitionService,
	@Value("\${server.port}") val port: Int,
	@Value("\${server.servlet.context-path}") private val baseURL: String,
)
{
	val base = "http://localhost:$port$baseURL/competition"

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

	@BeforeEach
	fun initData()
	{
		competitionService.clear()
		playerService.clear()
		teamService.clear()
		t1 = teamService.add( TeamDto(null, teamAToken, "TEAM_A"))
		t2 = teamService.add( TeamDto(null, teamBToken, "TEAM_B"))

		p4 = playerService.add(teamAToken,PlayerDto(null,"PLAYER_1","TEAM_A"))
		playerService.add(teamAToken,PlayerDto(null,"PLAYER_2","TEAM_A"))

		p1 = playerService.add(teamBToken,PlayerDto(null,"PLAYER_3","TEAM_B"))
		p2 = playerService.add(teamBToken,PlayerDto(null,"PLAYER_4","TEAM_B"))
		p3 = playerService.add(teamBToken,PlayerDto(null,"PLAYER_5","TEAM_B"))
	}

	@AfterEach
	fun tearDown()
	{
	}

	@AfterAll
	fun finishTest()
	{
		competitionService.clear()
		playerService.clear()
		teamService.clear()
	}

	private fun getToken(token: String) = "token $token"

	/*
	 * START / STOP
	 */
	@Test
	fun startAndJoin() {
		try {
			// start
			gameIDA = webClient.put()
				.uri(base)
				.header("Authorization",getToken(teamAToken))
				.retrieve().bodyToMono(UUID::class.java).block()
			// join
			webClient.post()
				.uri("$base/$gameIDA/join")
				.header("Authorization",getToken(teamBToken))
				.retrieve().bodyToMono(UUID::class.java).block()

			// wait
			Thread.sleep(2000)

			// evaluate
			val gameInfo = webClient.get()
				.uri("$base/$gameIDA")
				.header("Authorization",getToken(teamBToken))
				.retrieve().bodyToMono(ScoreDto::class.java).block()

			Assertions.assertNotNull(gameInfo)
			Assertions.assertTrue( (gameInfo!!.scoreTeamA==10 || gameInfo.scoreTeamB==10) )
		}
		catch (e:WebClientResponseException)
		{
			fail(e)
		}
	}

}
