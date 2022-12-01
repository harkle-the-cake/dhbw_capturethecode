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
class TrainingTest (
	@Autowired val webClient: WebClient,
	@Autowired val teamService: TeamService,
	@Autowired val playerService: PlayerService,
	@Autowired val trainingService: TrainingService,
	@Value("\${server.port}") val port: Int,
	@Value("\${server.servlet.context-path}") private val baseURL: String,
)
{
	val base = "http://localhost:$port$baseURL/training"

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
		trainingService.clear()
		playerService.clear()
		teamService.clear()
		t1 = teamService.add( TeamDto(null, teamAToken, "TEAM_A"))
		t2 = teamService.add( TeamDto(null, teamBToken, "TEAM_B"))

		p4 = playerService.add(teamAToken,PlayerDto(null,"PLAYER_1","TEAM_A"))
		playerService.add(teamAToken,PlayerDto(null,"PLAYER_2","TEAM_A"))

		p1 = playerService.add(teamBToken,PlayerDto(null,"PLAYER_3","TEAM_B"))
		p2 = playerService.add(teamBToken,PlayerDto(null,"PLAYER_4","TEAM_B"))
		p3 = playerService.add(teamBToken,PlayerDto(null,"PLAYER_5","TEAM_B"))

		startB()
	}

	@AfterEach
	fun tearDown()
	{
		trainingService.stop(gameIDB, t2.uuid!!)
	}

	@AfterAll
	fun finishTest()
	{
		trainingService.clear()
		playerService.clear()
		teamService.clear()
	}

	private fun getToken(token: String) = "token $token"

	/*
	 * START / STOP
	 */
	@Test
	fun startA() {
		try {
			gameIDA = webClient.put()
				.uri(base)
				.header("Authorization",getToken(teamAToken))
				.retrieve().bodyToMono(UUID::class.java).block()
		}
		catch (e:WebClientResponseException)
		{
			fail(e)
		}
	}

	@Test
	fun startB() {
		try {
			gameIDB = webClient.put()
				.uri(base)
				.header("Authorization",getToken(teamBToken))
				.retrieve().bodyToMono(UUID::class.java).block()?: fail("no game ID got")
		}
		catch (e:WebClientResponseException)
		{
			fail(e)
		}
	}

	@Test
	fun turnNotStarted() {
		try {
			webClient.post()
				.uri("$base/${gameIDA}/turn")
				.header("Authorization",getToken(teamAToken))
				.retrieve().bodyToMono(Unit::class.java).block()
			fail("performed turn, but not in training")
		}
		catch (e:WebClientResponseException)
		{
			Assertions.assertEquals(412,e.rawStatusCode)
		}
	}

	@Test
	fun turn() {
		try {
			webClient.post()
				.uri("$base/${gameIDB}/turn")
				.header("Authorization",getToken(teamBToken))
				.retrieve().bodyToMono(Unit::class.java).block()
		}
		catch (e:WebClientResponseException)
		{
			fail(e)
		}
	}

	@Test
	fun stopNotStarted() {
		try {
			webClient.delete()
				.uri("$base/${gameIDA}")
				.header("Authorization",getToken(teamAToken))
				.retrieve().bodyToMono(Unit::class.java).block()
			fail("stopped not started game")
		}
		catch (e:WebClientResponseException)
		{
			Assertions.assertEquals(412,e.rawStatusCode)
		}
	}

	fun stop() {
		try {
			webClient.delete()
				.uri("$base/${gameIDB}")
				.header("Authorization",getToken(teamAToken))
				.retrieve().bodyToMono(Unit::class.java).block()
		}
		catch (e:WebClientResponseException)
		{
			fail(e)
		}
	}

	@Test
	fun score() {
		try {
			webClient.get()
				.uri("$base/${gameIDA}")
				.header("Authorization",getToken(teamAToken))
				.retrieve().bodyToMono(ScoreDto::class.java).block()
			fail("got score but not started")
		}
		catch (e:WebClientResponseException)
		{
			Assertions.assertEquals(412,e.rawStatusCode)
		}
	}

	@Test
	fun hasFlagNotStartet() {
		try {
			webClient.get()
				.uri("$base/${gameIDA}/user/${p4.uuid}")
				.header("Authorization",getToken(teamAToken))
				.retrieve().bodyToMono(Boolean::class.java).block()
			fail("action performed")
		}
		catch (e: WebClientResponseException)
		{
			Assertions.assertEquals(412,e.rawStatusCode)
		}
	}

	@Test
	private fun performAction()
	{
		try {
			 webClient.post()
				.uri("$base/${gameIDA}/user/${p1.uuid}/action/OBSERVE")
				.header("Authorization",getToken(teamAToken))
				.retrieve().bodyToMono(ActionResultDto::class.java).block()
			fail("action performed")
		}
		catch (e: WebClientResponseException)
		{
			Assertions.assertEquals(412,e.rawStatusCode)
		}
	}

	@Test
	private fun performAction2()
	{
		try {
			webClient.post()
				.uri("$base/${gameIDA}/user/${p1.uuid}/action/PUSH/target/${p2.uuid}")
				.header("Authorization",getToken(teamAToken))
				.retrieve().bodyToMono(ActionResultDto::class.java).block()
			fail("action performed")
		}
		catch (e: WebClientResponseException)
		{
			Assertions.assertEquals(412,e.rawStatusCode)
		}
	}

	@Test
	private fun performActionUserNotPart()
	{
		try {
			webClient.post()
				.uri("$base/${gameIDA}/user/${p4.uuid}/action/OBSERVE")
				.header("Authorization",getToken(teamBToken))
				.retrieve().bodyToMono(ActionResultDto::class.java).block()
			fail("action performed")
		}
		catch (e: WebClientResponseException)
		{
			Assertions.assertEquals(403,e.rawStatusCode)
		}
	}

	@Test
	private fun performAction2NotPart()
	{
		try {
			webClient.post()
				.uri("$base/${gameIDA}/user/${p4.uuid}/action/PUSH/target/${p2.uuid}")
				.header("Authorization",getToken(teamBToken))
				.retrieve().bodyToMono(ActionResultDto::class.java).block()
			fail("action performed")
		}
		catch (e: WebClientResponseException)
		{
			Assertions.assertEquals(403,e.rawStatusCode)
		}
	}

	/*
	* ############# GET LIST  ################
	* */
	@Test
	fun performRoundTripNoAction() {
		// score = 0
		val score = webClient.get()
			.uri("$base/${gameIDB}")
			.header("Authorization",getToken(teamAToken))
			.retrieve().bodyToMono(ScoreDto::class.java).block()
		Assertions.assertNotNull(score)
		Assertions.assertEquals(0, score!!.scoreTeamA)

		// next
		webClient.post()
			.uri("$base/${gameIDB}/turn")
			.header("Authorization",getToken(teamAToken))
			.retrieve().bodyToMono(Unit::class.java).block()

		// score = 1
		val score2 = webClient.get()
			.uri("$base/${gameIDB}")
			.header("Authorization",getToken(teamAToken))
			.retrieve().bodyToMono(ScoreDto::class.java).block()
		Assertions.assertNotNull(score2)
		Assertions.assertEquals(1, score2!!.scoreTeamA)
	}

	@Test
	fun performRoundTripPassing() {
		// check who has the token
		if (hasFlag(p1)) // p1
		{
			performAction(p3, Action.CATCH)
			performAction(p1, Action.PASS, p3)
		}
		else if (hasFlag(p2))
		{
			performAction(p3, Action.CATCH)
			performAction(p2, Action.PASS, p3)
		}

		// next
		turn()

		// p3 should have the token now
		Assertions.assertTrue(hasFlag(p3))

		// pass back
		performAction(p1, Action.CATCH)
		performAction(p3, Action.PASS, p1)

		// next
		turn()

		// p1 should have the token now
		Assertions.assertTrue(hasFlag(p1))

		// next
		turn()

		// score = 3, since 3 rounds
		score(3)
	}

	@Test
	fun performRoundTripGrap() {
		// check who has the token
		if (hasFlag(p1)) // p1
		{
			performAction(p3, Action.CATCH)
			performAction(p1, Action.PASS, p3)
		}
		else if (hasFlag(p2))
		{
			performAction(p3, Action.CATCH)
			performAction(p2, Action.PASS, p3)
		}
		else if (hasFlag(p3))
		{
			// all fine
		}
		else
		{
			fail("error")
		}

		// next
		turn()

		// p3 should have the token now
		Assertions.assertTrue(hasFlag(p3))

		// pass back
		performAction(p1, Action.GRAP, p3)

		// next
		turn()

		// p1 should have the token now
		Assertions.assertTrue(hasFlag(p1))

		// next
		turn()

		// score = 3, since 3 rounds
		score(3)
	}

	@Test
	fun performRoundTripPassingIntercept1() {
		// check who has the token
		if (hasFlag(p1)) // p1
		{
			performAction(p3, Action.CATCH)
			performAction(p1, Action.PASS, p3)
		}
		else if (hasFlag(p2))
		{
			performAction(p3, Action.CATCH)
			performAction(p2, Action.PASS, p3)
		}

		// next
		turn()

		// p3 should have the token now
		Assertions.assertTrue(hasFlag(p3))

		// intercept back
		performAction(p1, Action.CATCH)
		performAction(p2, Action.GRAP,p3)
		performAction(p3, Action.PASS, p1)

		// next
		turn()

		// p1 should have the token now, since targetting the wrong user
		Assertions.assertTrue(hasFlag(p1))

		// next
		turn()

		// score = 3, since 3 rounds
		score(3)
	}

	@Test
	fun performRoundTripPassingIntercept2() {
		// check who has the token
		if (hasFlag(p1)) // p1
		{
			performAction(p3, Action.CATCH)
			performAction(p1, Action.PASS, p3)
		}
		else if (hasFlag(p2))
		{
			performAction(p3, Action.CATCH)
			performAction(p2, Action.PASS, p3)
		}

		// next
		turn()

		// p3 should have the token now
		Assertions.assertTrue(hasFlag(p3))

		// intercept back
		performAction(p1, Action.CATCH)
		performAction(p2, Action.GRAP,p1)
		performAction(p3, Action.PASS, p1)

		// next
		turn()

		// p1 should have the token now
		Assertions.assertTrue(hasFlag(p2))

		// next
		turn()

		// score = 3, since 3 rounds
		score(3)
	}

	@Test
	fun performRoundTripPassingPushed1() {
		// check who has the token
		if (hasFlag(p1)) // p1
		{
			performAction(p3, Action.CATCH)
			performAction(p1, Action.PASS, p3)
		}
		else if (hasFlag(p2))
		{
			performAction(p3, Action.CATCH)
			performAction(p2, Action.PASS, p3)
		}

		// next
		turn()

		// p3 should have the token now
		Assertions.assertTrue(hasFlag(p3))

		// intercept back
		performAction(p1, Action.CATCH)
		performAction(p2, Action.PUSH,p3)
		performAction(p3, Action.PASS, p1)

		// next
		turn()

		// p1 should have the token now
		Assertions.assertTrue(hasFlag(p1))

		// next
		turn()

		// score = 3, since 3 rounds
		score(3)
	}

	@Test
	fun performRoundTripPassingPushed2() {
		// check who has the token
		if (hasFlag(p1)) // p1
		{
			performAction(p3, Action.CATCH)
			performAction(p1, Action.PASS, p3)
		}
		else if (hasFlag(p2))
		{
			performAction(p3, Action.CATCH)
			performAction(p2, Action.PASS, p3)
		}

		// next
		turn()

		// p3 should have the token now
		Assertions.assertTrue(hasFlag(p3))

		// intercept back
		performAction(p1, Action.CATCH)
		performAction(p2, Action.PUSH,p1)
		performAction(p3, Action.PASS, p1)

		// next
		turn()

		// p3 should have the token now
		Assertions.assertTrue(hasFlag(p3))

		// next
		turn()

		// score = 3, since 3 rounds
		score(3)
	}

	@Test
	fun performObservation() {
		// check who has the token

		var ret = performAction(p1, Action.OBSERVE)
		Assertions.assertNotNull(ret)
		Assertions.assertNotNull(ret.targetStates)
		Assertions.assertEquals(3,ret.targetStates!!.size)
		Assertions.assertEquals("READY", ret.targetStates!!.first { playerStateDto -> playerStateDto.uuid==p1.uuid }.state)
		Assertions.assertEquals("READY", ret.targetStates!!.first { playerStateDto -> playerStateDto.uuid==p2.uuid }.state)
		Assertions.assertEquals("READY", ret.targetStates!!.first { playerStateDto -> playerStateDto.uuid==p3.uuid }.state)


		performAction(p2, Action.PUSH, p3)

		ret = performAction(p1, Action.OBSERVE)
		Assertions.assertNotNull(ret)
		Assertions.assertNotNull(ret.targetStates)
		Assertions.assertEquals(3,ret.targetStates!!.size)
		Assertions.assertEquals("OBSERVE", ret.targetStates!!.first { playerStateDto -> playerStateDto.uuid==p1.uuid }.action)
		Assertions.assertEquals("PUSH", ret.targetStates!!.first { playerStateDto -> playerStateDto.uuid==p2.uuid }.action)
		Assertions.assertEquals("UNKNOWN", ret.targetStates!!.first { playerStateDto -> playerStateDto.uuid==p3.uuid }.action)

		turn()

		ret = performAction(p1, Action.OBSERVE)
		Assertions.assertNotNull(ret)
		Assertions.assertNotNull(ret.targetStates)
		Assertions.assertEquals(3,ret.targetStates!!.size)
		Assertions.assertEquals("READY", ret.targetStates!!.first { playerStateDto -> playerStateDto.uuid==p1.uuid }.state)
		Assertions.assertEquals("BANNED", ret.targetStates!!.first { playerStateDto -> playerStateDto.uuid==p2.uuid }.state)
		Assertions.assertEquals("ON_GROUND", ret.targetStates!!.first { playerStateDto -> playerStateDto.uuid==p3.uuid }.state)
	}

	/**
	 * method used to perform an action via rest
	 * */
	private fun performAction(player: PlayerDto, action: Action): ActionResultDto {
		try {
			val ret = webClient.post()
				.uri("$base/${gameIDB}/user/${player.uuid}/action/${action.name}")
				.header("Authorization",getToken(teamBToken))
				.retrieve().bodyToMono(ActionResultDto::class.java).block()

			Assertions.assertNotNull(ret)
			return ret!!
		}
		catch (e: WebClientResponseException)
		{
			fail(e)
		}
	}

	/**
	 * method used to perform an action via rest
	 * */
	private fun performAction(player: PlayerDto, action: Action, target: PlayerDto): ActionResultDto {
		try {
			val ret = webClient.post()
				.uri("$base/${gameIDB}/user/${player.uuid}/action/${action.name}/target/${target.uuid}")
				.header("Authorization",getToken(teamBToken))
				.retrieve().bodyToMono(ActionResultDto::class.java).block()

			Assertions.assertNotNull(ret)
			return ret!!
		}
		catch (e: WebClientResponseException)
		{
			fail(e)
		}
	}

	/**
	 * method used to perform an action via rest
	 * */
	private fun hasFlag(player: PlayerDto): Boolean {
		try {
			val ret = webClient.get()
				.uri("$base/${gameIDB}/user/${player.uuid}")
				.header("Authorization",getToken(teamBToken))
				.retrieve().bodyToMono(Boolean::class.java).block()

			Assertions.assertNotNull(ret)
			return ret!!
		}
		catch (e: WebClientResponseException)
		{
			fail(e)
		}
	}

	/**
	 * checks if the score is as expected
	 * */
	private fun score(exspectedScore: Int) {
		try {
			val ret = webClient.get()
				.uri("$base/${gameIDB}")
				.header("Authorization",getToken(teamBToken))
				.retrieve().bodyToMono(ScoreDto::class.java).block()

			Assertions.assertNotNull(ret)
			Assertions.assertEquals(exspectedScore, ret!!.scoreTeamA)
		}
		catch (e: WebClientResponseException)
		{
			fail(e)
		}
	}
}
