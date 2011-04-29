mud is a MUD (Multi-User Dungeon) game. It is a distributed application with a
central server and any number of connecting clients.

# Requirements

mud is written in Java (1.5 generation) and uses RMI (remote method
invocation) for remote calls.

# Compiling

To build client and server:

	./compile

# Running the server

To start the server:

	./runserv

The server starts and displays a prompt:

	serv>

Hit RETURN to get a list of commands:

	users             display users
	godmode           heal and arm all users
	monsters          display monsters
	rooms             display rooms
	s                 show (s)tack
	halt              (h)alt
	h                 (h)elp

# Playing the game

To start the client:

	./runclient

The client starts and displays a prompt:

	>>

Hit RETURN to get a list of commands:

	u <user> <pass>   set (u)ser
	l                 (l)ogin user
	o                 l(o)ok/(o)rient
	i                 (i)nventory
	c <item>          (c)onsume food item
	e <room>          (e)xit to room
	p <item>          (p)ick up item
	a <player>        (a)ttack player
	t                 (t)erminate combat
	echo              send (e)cho signal
	ping              send (p)ing signal
	s                 show (s)tack
	q                 (q)uit
	h                 (h)elp

Select a username and password to log into the game:

	>> u Andres secret

This will place the human player Andres in The Lobby:

	>>> Entered The Lobby
	 Room: The Lobby
	 {Exits}
	 Pass of Azotus
	 {Humans}    {health}  {weapon}  {armor}
	 Andres            53         5        0

The Lobby is a safe haven where no monsters enter and you can stay there any
amount of time while your health slowly increments. But it contains no useful
objects and has no action unless human characters fight amongst each other.

To exit to a different room type part of its name:

	>> e pas
	>>> Entered Pass of Azotus
	 Room: Pass of Azotus
	 {Exits}
	 The Lobby
	 Cell of Nathanael
	 {Humans}    {health}  {weapon}  {armor}
	 Andres            53         5        0
	 {Monsters}  {health}  {weapon}  {armor}
	 Prithivi          94        80       25
	 {Armor}            {strength}
	 helmet                     40
	 mail                       45
	 gauntlet                   25

In a room with objects you can pick up weapons and armor to improve your
combat readiness, and consume food to improve your health. But if the room
contains a monster combat will begin:

	>>> Under attack from Prithivi
	>>> Struck by Prithivi taking 46 damage [7]
	>>> Struck Prithivi causing 1 damage [93]
	>>> Struck Prithivi causing 1 damage [92]
	>>> Killed by Prithivi

Any character (human or monster) that is killed will be respawned. Human
characters will be respawned in The Lobby:

	>>> User Andres respawned in The Lobby

