package eu.boxwork.dhbw.capturethecode.model

import eu.boxwork.dhbw.capturethecode.dto.ActionResultDto
import eu.boxwork.dhbw.capturethecode.dto.TeamWithMembersDto
import eu.boxwork.dhbw.capturethecode.enums.Action
import eu.boxwork.dhbw.capturethecode.enums.PlayerState
import org.springframework.scheduling.annotation.Scheduled
import java.util.UUID

class GameCord (
    val teamA: TeamWithMembersDto,
    private val states : MutableMap<UUID, PlayerState> = HashMap(),
    private var userWithToken : UUID? = null,
    var resultA : Int = 0,
    var resultB : Int = 0,
    private var gameOver : Boolean = true
) {
    var teamB: TeamWithMembersDto? = null
    private val rounds=100
    private var round=0
    /**
     * initialises the cord with the first team right at the beginning
     * */
    init
    {
        teamA.teamMembers.forEach{
            states[it.uuid!!]=PlayerState.READY
        }
    }

    /**
     * evaluates which team gets a point
     * @return true if game is over
     * */
    @Scheduled(fixedDelayString = "\${scoring.interval}")
    private fun score(): Boolean
    {
        synchronized(this)
        {
            if (!gameOver)
            {
                if (teamA.teamMembers.any { playerDto -> playerDto.uuid == userWithToken })                resultA++
                else if (teamB?.teamMembers?.any { playerDto -> playerDto.uuid == userWithToken } == true) resultB++
                else
                {
                    // no team scores
                }
                if(userWithToken==null)
                {
                    dropTokenRandomly()
                }
                round++
                if (round>rounds)
                {
                    finish()
                }
            }
            else{
                // game is over
            }
            return gameOver
        }
    }

    /**
     * finishes the game
     * */
    fun finish()
    {
        synchronized(this)
        {
            this.gameOver = true
            userWithToken = null
        }
    }

    /**
     * method used to perform an action on the cord and evaluates the result
     * @param user user id
     * @param target the target like the other user
     * @param action action to be performed
     * */
    fun performAction(user: UUID, action: Action, target: UUID?): ActionResultDto {
        synchronized(this)
        {
            if (gameOver)
            {
                return ActionResultDto(states[user]!!.name, hasFlag = false, haveFlag = false, gameOver = gameOver) // no seeing anything
            }
            // player is banned forever
            if (states[user]==PlayerState.BANNED_FOREVER)
            {
                return ActionResultDto(states[user]!!.name, false, userWithToken==user, gameOver ) // no seeing anything
            }
            // we can evaluate the action
            when(action)
            {
                Action.LOOK -> {}
                Action.NOTHING -> return ActionResultDto(states[user]!!.name, userWithToken==user, userWithToken==user, gameOver  )
                Action.GETREADY -> {
                    states[user] = PlayerState.READY
                }
                Action.PUSH -> {
                    if (target!=null)
                    {
                        // penalty, target is on ground and user is banned
                        states[user]=PlayerState.BANNED
                        states[target]=PlayerState.ON_GROUND
                        if (userWithToken==target)
                        {
                            userWithToken = null
                            dropTokenRandomly()
                        }
                    }
                    else{
                        // nothing changed
                    }
                }
                Action.CATCH -> {
                    if (states[user]==PlayerState.READY) {
                        states[user] = PlayerState.FETCHING // user must be ready before
                    }
                }
                Action.GRAP -> {
                    // another user tries to grap the flag, only possible if ready
                    if (states[user]==PlayerState.READY && userWithToken==target) {
                        userWithToken = user
                    }
                }
                Action.PASS -> {
                    if (states[user]==PlayerState.READY && userWithToken==user)
                    {
                        // throwing user must be ready and having the flag
                        if (target!=null && states[target]==PlayerState.FETCHING) {
                            userWithToken=target // user ready to fetch the token, we pass it
                            states[target]=PlayerState.READY // target is ready afterwards
                        }
                        else
                        {
                            // user passed into nothing, or target was not ready => fail
                            dropTokenRandomly()
                        }
                    }
                    else
                    {
                        // user was not ready or having the token
                    }
                }
            }
            return ActionResultDto(states[user]!!.name, userWithToken==target, userWithToken==user, gameOver ) // no seeing anything
        }
    }

    /**
     * once the token was lost to "nowhere", we randomly assign it, first to the fetching ones, than to the ready ones
     * */
    private fun dropTokenRandomly() {
        val listFetchingUsers : MutableList<UUID> = ArrayList()
        states.keys.forEach{ userID  -> if (states[userID]==PlayerState.FETCHING )
            listFetchingUsers.add(userID)
        }

        if (listFetchingUsers.size>0)
        {
            listFetchingUsers.shuffle()
            userWithToken = listFetchingUsers[0]
        }

        // lets get randomly a user
        val listActiveUsers : MutableList<UUID> = ArrayList()
        states.keys.forEach{ userID  -> if (states[userID]==PlayerState.READY )
            listActiveUsers.add(userID)
        }

        if (listActiveUsers.size>0)
        {
            listActiveUsers.shuffle()
            userWithToken = listActiveUsers[0]
        }
    }

    /**
     * sets the second team and starts all
     * @param teamToSet second team
     * */
    fun startGame(teamToSet: TeamWithMembersDto) {
        teamB = teamToSet

        teamB?.teamMembers?.forEach{
            states[it.uuid!!]=PlayerState.READY
        }

        dropTokenRandomly()
        gameOver = false
    }
}