package eu.boxwork.dhbw.capturethecode.model

import eu.boxwork.dhbw.capturethecode.dto.*
import eu.boxwork.dhbw.capturethecode.enums.Action
import eu.boxwork.dhbw.capturethecode.enums.PlayerState
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.LinkedTransferQueue

class GameGround (
    val teamA: TeamWithMembersDto,
    private val rounds:Int=100,
    val isTraining : Boolean = true
) {
    private val states : MutableMap<UUID, PlayerState> = ConcurrentHashMap()
    private val actions : MutableMap<UUID, Action> = ConcurrentHashMap()
    private val targets : MutableMap<UUID, UUID> = ConcurrentHashMap()
    private val players : MutableMap<UUID, PlayerDto> = ConcurrentHashMap()
    val events : MutableCollection<Event> = ConcurrentLinkedDeque()

    private var userWithToken : UUID? = null
    var resultA : Int = 0
    var resultB : Int = 0
    private var gameOver : Boolean = true

    var round=0
    var teamB: TeamWithMembersDto? = null
    /**
     * initialises the cord with the first team right at the beginning
     * */
    init
    {
        teamA.teamMembers.forEach{
            states[it.uuid!!]=PlayerState.READY
            players[it.uuid] = it
        }
    }

    /**
     * performs this round
     * @return true if game is over
     * */
    //@Scheduled(fixedDelayString = "\${scoring.interval}")
    fun performRound(): Boolean
    {
        synchronized(this)
        {
            if (!gameOver)
            {
                executeActions()
                score()
                finishRound()
                round++
                if (round>=rounds) {
                   finish()
                }
            }
            return gameOver
        }
    }

    private fun finishRound() {
        actions.clear()
        targets.clear()
    }

    /**
     * executes all actions
     * */
    private fun executeActions()
    {
        if(userWithToken==null) // no token at the start of the round
        {
            userWithToken = dropTokenRandomly()
        }

        while(actions.containsKey(userWithToken))
        {
            // let's check if the user is pushed before doing anything
            val pushed = getPushed(userWithToken!!)

            if (!pushed)
            {
                when(actions.remove(userWithToken))
                {
                    Action.PASS -> userWithToken = pass(userWithToken!!, targets[userWithToken])
                    Action.PUSH -> userWithToken = push(userWithToken!!, targets[userWithToken])
                    Action.GETREADY -> states[userWithToken!!]=PlayerState.READY
                    else -> {
                        if (states[userWithToken!!]!=PlayerState.BANNED
                            && states[userWithToken!!]!=PlayerState.ON_GROUND)
                            states[userWithToken!!]=PlayerState.READY
                    }
                }
            }
            else {
                // the user was pushed
                events.add(Event(round, players[userWithToken]?.name,null, "Was pushed. And lost flag."))
            }
        }

        // all users with the token are handled, check the rest, only pushing may be applied
        actions.keys.forEach {
            if (actions[it]==Action.PUSH)
            {
                push(it, targets[it])
            }
            else if (actions[it]==Action.GRAP)
            {
                grap(it, targets[it])
            }
        }

        // clear actions
        actions.clear()
    }

    /**
     * graps the token flag a user
     * @param source the current who tries to grep the flag
     * @param target the user that could have the flag now
     * */
    private fun grap(source: UUID, target: UUID?) {
        if (userWithToken==target && userWithToken!=null)
        {
            // grapped sucessfully
            userWithToken = source
            events.add(Event(round, players[source]?.name,players[target]?.name, "player grapped code"))
        }
        else
        {
            // nothing happend at all
        }
    }

    /**
     * return if token was intercepted
     * @param userWithTokenIn the current user with token
     * @param target the user that should get the token
     * @return the user who has the token afterwards
     * */
    private fun pass(userWithTokenIn: UUID, target: UUID?): UUID? {
        if (target==null) // user passes to nowhere
            return dropTokenRandomly()

        // see if the token is intercepted
        targets.forEach { entry -> if (entry.value==target) { // only the target may be intercepted
                if (actions[entry.key]==Action.GRAP) {
                    // token intercepted !
                    actions.remove(entry.key)
                    states[entry.key] = PlayerState.READY
                    events.add(Event(round, players[entry.key]?.name,players[target]?.name, "player intercepted code"))
                    return entry.key
                }
            }
        }

        states[userWithTokenIn] = PlayerState.READY

        // there is no interception, let's see if the target is ready
        return if (actions[target]==Action.CATCH || actions[target]==Action.GRAP) {
            events.add(Event(round, players[userWithTokenIn]?.name,players[target]?.name, "player passed code to another one"))
            target
        } else {
            // not graped, flag somewhere
            dropTokenRandomly()
        }
    }

    /**
     * method to push a target, source is banned, target is on ground, flag is potentially lost
     * @param source the pushing player
     * @param target the target of the pushing
     * @return the player with the flag after pushing
     * */
    private fun push(source: UUID, target: UUID?) : UUID? {
        if (actions.containsKey(source)) actions.remove(source)
        if(target != null)
        {
            if (actions.containsKey(target)) actions.remove(target)
            // state changed
            states[target] = PlayerState.ON_GROUND
            states[source] = PlayerState.BANNED
            events.add(Event(round, players[source]?.name,players[target]?.name, "player pushed another one"))
        }
        // if no target, no banned player, flag is dropped anyway
        // flag lost
        return dropTokenRandomly()
    }

    /**
     * method to check if target is pushed
     * @param pushedTarget the player that may be pushed
     * @return true, if pushed, else false
     * */
    private fun getPushed(pushedTarget: UUID): Boolean {
        // see if the target was set as target
        targets.forEach { entry -> if (entry.value==pushedTarget) {
                if (actions[entry.key]==Action.PUSH) {
                    userWithToken = push(entry.key, pushedTarget)
                    return true
                }
            }
        }
        return false
    }

    /**
     * evaluates which team gets a point
     * */
    private fun score()
    {
        if (!gameOver)
        {
            if (teamA.teamMembers.any { playerDto -> playerDto.uuid == userWithToken })                resultA++
            else if (teamB?.teamMembers?.any { playerDto -> playerDto.uuid == userWithToken } == true) resultB++
            else
            {
                // no team scores
            }
        }
        else{
            // game is over
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
     * @param action action to be performed
     * */
    fun performAction(user: UUID, action: Action): ActionResultDto {
        synchronized(this)
        {
            if (actions.containsKey(user) && actions[user]!=Action.LOOK)
            {
                // action != look already set, no new action
            }
            else if (states[user]==PlayerState.ON_GROUND || states[user]==PlayerState.BANNED)
            {
                // user may only get up
                if (action==Action.GETREADY) {
                    actions[user] = action
                    events.add(Event(round, players[user]?.name,null, "gets ready again."))
                }
                else {
                    // user sent the wrong action
                }
            }
            else
            {
                actions[user] = action
                events.add(Event(round, players[user]?.name,null, "performs action: $action"))
            }

            val currentAction = actions[user]
            val targetAction: UUID? = targets[user]
            val hasFlag = if (userWithToken!=null) userWithToken==targetAction else false
            val haveFlag = user == userWithToken
            var targetStates : MutableList<PlayerStateDto>? = null

            if (currentAction==Action.OBSERVE)
            {
                // if this action is look, the player gets the infos of the target, but no names, this is always possible
                targetStates = getTargetStates(false)
            }
            else
            {
                // no action with looking around
            }

            return ActionResultDto(
                round,
                rounds,
                states[user]?.name?:PlayerState.UNKNOWN.name,
                hasFlag,
                haveFlag,
                gameOver,
                null,
                targetStates
            )
        }
    }

    /**
     * method used to perform an action on the cord and evaluates the result
     * @param user user id
     * @param target the target like the other user
     * @param action action to be performed
     * */
    fun performAction(user: UUID, action: Action, target: UUID): ActionResultDto {
        synchronized(this)
        {
            if (actions.containsKey(user) && actions[user]!=Action.LOOK)
            {
                // action != look already set, no new action
            }
            else if (states[user]==PlayerState.ON_GROUND || states[user]==PlayerState.BANNED)
            {
                // user may only get up
                if (action==Action.GETREADY) {
                    actions[user] = action
                    events.add(Event(round, players[user]?.name,null, "gets ready again."))
                }
                else {
                    // user sent the wrong action
                }
            }
            else
            {
                actions[user] = action
                targets[user] = target
                events.add(Event(round, players[user]?.name,players[target]?.name, "performs action: $action"))
            }

            val currentAction = actions[user]
            val targetAction: UUID? = targets[user]
            val hasFlag = if (userWithToken!=null) userWithToken==targetAction else false
            val haveFlag = user == userWithToken
            var targetState : PlayerStateDto? = null
            var targetStates : MutableList<PlayerStateDto>? = null

            if (action==Action.LOOK)
            {
                // if this action is look, the player gets the infos of the target, but no names, this is always possible
                targetState = getTargetState(target, false)
            }
            else if (currentAction==Action.OBSERVE)
            {
                // if this action is look, the player gets the infos of the target, but no names, this is always possible
                targetStates = getTargetStates(false)
            }
            else
            {
                // no action with looking around
            }

            return ActionResultDto(
                round,
                rounds,
                states[user]?.name?:PlayerState.UNKNOWN.name,
                hasFlag,
                haveFlag,
                gameOver,
                targetState,
                targetStates
                )
        }
    }


    /**
     * returns the states of all players
     * @param returnName the name of the user should be returned, instead of the uuid
     * @return the current player states of all players of this round
     * */
    private fun getTargetStates(returnName: Boolean) : MutableList<PlayerStateDto> {
        val ret : MutableList<PlayerStateDto> = ArrayList()
        states.keys.forEach { uuid -> getTargetState(uuid, returnName)?.let { ret.add(it) } }
        return ret
    }

    /**
     * returns the state of the target
     * @param target the user id of the target
     * @param returnName the name of the user should be returned, instead of the uuid
     * @return the player state in this round if the player is there
     * */
    private fun getTargetState(target: UUID, returnName: Boolean): PlayerStateDto? {
        var player : PlayerDto? = null

        try {
            player = teamA.teamMembers.first { playerDto -> playerDto.uuid==target  }
        }
        catch (e:Exception)
        {
            // no such player
        }

        if (player==null) {
            try {
                player = teamB?.teamMembers?.first { playerDto -> playerDto.uuid==target  }
            }
            catch (e:Exception)
            {
                // no such player
            }
        }

        return if(player!=null && states.containsKey(target)) {
            PlayerStateDto(
                if (returnName) null else target,
                if (returnName) player.name else null,
                states[target]?.name?:PlayerState.UNKNOWN.name,
                actions[target]?.name?:Action.UNKNOWN.name,
                target==userWithToken
            )
        } else null
    }


    /**
     * once the token was lost to "nowhere", we randomly assign it, first to the fetching ones, than to the ready ones
     * @return the new user with the token
     * */
    private fun dropTokenRandomly(): UUID? {
        var newUser : UUID? = null
        val listFetchingUsers : MutableList<UUID> = ArrayList()
        states.keys.forEach{ userID  -> if (actions[userID]==Action.GRAP || actions[userID]==Action.CATCH )
            listFetchingUsers.add(userID)
        }

        if (listFetchingUsers.size>0)
        {
            listFetchingUsers.shuffle()
            newUser = listFetchingUsers[0]
        }

        if (newUser==null)
        {
            // lets get randomly a user
            val listActiveUsers : MutableList<UUID> = ArrayList()
            states.keys.forEach{ userID  -> if (states[userID]==PlayerState.READY )
                listActiveUsers.add(userID)
            }

            if (listActiveUsers.size>0)
            {
                listActiveUsers.shuffle()
                newUser = listActiveUsers[0]
            }
        }

        events.add(Event(round, null,players[newUser]?.name, "code was randomly set to a new player"))

        return newUser
    }

    /**
     * sets the second team and starts all
     * @param teamToSet second team
     * */
    fun startGame(teamToSet: TeamWithMembersDto) {
        teamB = teamToSet

        teamB?.teamMembers?.forEach{
            states[it.uuid!!]=PlayerState.READY
            players[it.uuid] = it
        }

        //userWithToken = dropTokenRandomly()
        gameOver = false
    }

    /**
     * returns true, if the player has the flag
     * @param userID the player to check
     * @return true, if the player has the flag, else false
     * */
    fun hasFlag(userID: UUID): Boolean {
        synchronized(this)
        {
            return userWithToken==userID
        }
    }

    /**
     * return the player infos only if not training
     * @return the player infos if not in training
     * */
    fun getPlayerInfos(): LinkedTransferQueue<SpectatedPlayerInfo>? {
        synchronized(this)
        {
            if (isTraining)
            {
                val ret : LinkedTransferQueue<SpectatedPlayerInfo> = LinkedTransferQueue()
                players.forEach {
                    ret.add(
                        SpectatedPlayerInfo(
                            it.value.name,
                            states [it.key]?.name?:PlayerState.UNKNOWN.name,
                            actions [it.key]?.name?:Action.UNKNOWN.name,
                            (it.key==userWithToken)
                        )
                    )
                }
                return ret
            }
            else return null
        }
    }
}