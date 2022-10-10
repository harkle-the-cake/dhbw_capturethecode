# CAPTURE THE CODE/FLAG
Capture the code targets an example of distributed coordination of clients/processes.
Base concept is, that there are 2 teams trying to capture the flag/code from the other team.
Each team has up to 20 "players". The competition is done in rounds. Those players have a set of actions that can be done. Each round a player may perform one action.
The first action taken will be executed. Beside of this, a player may also check, if the flag/code is in possession of its own or look on another player to check this state.
As a separate action (not a free one), a player may observe the whole competition the check the states/actions of all players on the field.

## Teams
Each team has one token, a name and an ID. The token is used to authenticate while adding players or performing actions.
A team may add as many players are allowed to the team.
A team may only be created by an admin.

## Player
With the team token, a player may be added to the team until the maximum of players is reached.
A player may perform actions while the team is in training or in competition. Therefore the team token must be used.

## Actions
Each user may perform as many free actions as able, but only one normal action per turn.
Each action is a separate call to the backend. Once the turn is over, the normal actions are evaluated by the backend.


### Free Actions
| Action         | Target needed | Effect                                                        |
|----------------|---------------|---------------------------------------------------------------|
| check for flag | -             | returns if the player has the flag/code                       |
| look at        | +             | returns the current status of the target the player looks at. | 

### Normal Actions
| Action      | Target needed | Effect                                                         |
|-------------|---------------|----------------------------------------------------------------|
| observe     | -             | returns if the player has the flag/code                        |
| fetch/catch | -             | returns the current status of the target the player looks at   |
| grap        | +             | returns the current status of the target the player looks at   |
| push        | +             | returns the current status of the target the player looks at   |
| pass        | +             | returns the current status of the target the player looks at   |
| get ready   | +             | returns the current status of the target the player looks at   |

## Effects
Some actions cause effects on the player. This is mainly of bad players pushing other players around.
### status
| Action     | Status of actor | Status of target | Effect                                                       |
|------------|-----------------|------------------|--------------------------------------------------------------|
| push       | BANNED          | ON_GROUND        | returns if the player has the flag/code                      |
| all others | READY           | READY            | returns the current status of the target the player looks at |
Once banned or on ground, the player must take a "get ready" action to participate in the competition again.
On the start, the state is "ready". In case of an error, the state changes to "UNKNOWN".

## Flag/Code
The flag/code must be captured. Therefore a player may have different opportunities.
- hand over the flag to another player
- grap and capture the flag while hand over takes place
- flag/code is lost while the player posses the flag/code 

### hand over of flag/code
A player may pass the flag/code to another player using a pass action. 
The other player must use a fetch action in the same turn to get the flag/code.

### capture the flag/code
If a flag/code is passed an opponent may get the flag/code with grap action targetting the player who is target of the handover.
This is called interception.
If a flag/code is NOT passed. The opponent may also get the code performing a grap action targetting the player who posses the flag/code.
(!) Grapping without interception takes place AFTER a handover; therefore opponent needs to decide who to target.

### loss of flag/code
A flag/code may be lost if:
- the player with the flag/code is pushed
- the player passes the flag to another player, and this player is not fetching the flag/code
Once the flag/code is lost, the flag/code will be randomly given to:
- a player who is fetching in this turn
- a player who is ready in this turn
A banned player or a player "on the ground" may not fetch such a token.