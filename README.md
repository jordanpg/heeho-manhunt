# heeho-manhunt
minecraft manhunt paper plugin because i didnt like manhunt+

## Implemented Features
* Assignment of roles to players
* Compass tracking of runners in the same dimension
* Compass tracking of a runner's last-used portal in the hunter's dimension if they have moved to a different dimension
* Compass tracking of nether fortresses for hunters

## Unimplemented/Planned Features
* Start command
* Win condition handling for hunters and runners
* Runner lives and elimination
* Role persistence
* Experiment with automatic tracker updating

## Commands
Commands are implemented under the `/hhmanhunt` (or `/hhm`) command. Autocomplete is implemented for all commands.

#### Add
`/hhmanhunt add <runner|hunter|spectator> <players...>`  

The add command is used to manage manhunt roles for each player:  
* Runner: Players who will attempt to complete the game while being hunted
* Hunters: Players who will hunt runners
* Spectators: Players who are not included in the manhunt  

Multiple names can be listed to easily assign roles to many players.
