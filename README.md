# Npcs
This is a plugin I made for my schools minecraft club. It lets you create an npc with a 16-character name and set its skin. You can also add "events" for the npc when it gets left or right clicked. Right now you can only send any command with the player who clicked as the command-sender and send a message to the play who clicked.

The commands:
/createNpc [id] [display-name] 
permission: npcs.commands.create
This command will create an npc and spawn it in your world. Note: The display-name can only be up to 16 characters including spaces

/deleteNpc [id]
permission: npcs.commands.delete
This command will delete the npc from world and files.

/loadSkin [skin-id] [username-of-owner]
permission: npcs.commands.loadskin
This command will load a skin based off of a real player username and save it to a file. Keep in mind this only saves the current skin and will not change when the player changes their skin. This can be used as a means to get any skin you want without needing a different player on each one.

/setSkin [npc-id] [skin-id]
permission: npcs.commands.setskin
This command will set the skin of the npc in question to the skin saved in the skin file with the skin-id id;

/editNpc [npc-id] [trigger] [type] [add-or-remove] [arg]

permission: npcs.commands.edit

This command define what the npc will do when you left or right click it which are the triggers. This command is a little complicated so follow the tab completer for this one since I made it pretty descriptive. Keep in mind arg represents the message you want to send or the command you want to execute. Using [player] as a placeholder will get the player who clicked the npc when the action is called.

/editNpc [npc-id] [setName] [name]

permission: npcs.commands.edit

Pretty simple, changes the name of the npc.
