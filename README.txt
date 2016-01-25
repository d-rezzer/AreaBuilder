                ********************************
                *   AreaBuilder, version 1.0   *
                ********************************

 Based on Tele-Arena 5.56d Copyright (c) 2011 Elwynor Technologies

                     Ether 1.0 by Ron Kinney
                  AreaBuilder 1.0 by Ron Kinney

Send comments, bugs, issues, patches, ideas, etc to "ronkinney@gmail.com"
          The Ether Home Page is at "http://tdod.org/ether"

=== General Info ===

This is the README file for AreaBuilder 1.0.

Ether is a Java application that allows the user to run the online
multiplayer fantasy RPG, Tele-Arena, developed in the 1990s by Sean
Ferrel for the MajorBBS bulletin board system. This distribution does
not include the copyright data files. Ether, itself, is just an mud
engine that has the capabilities of reading in data from the original
Tele-Arena distribution.  The user has the capabilities of creating
his/her own data files and building their own world which may or may
not resemble Tele-Arena.

To legally run the Tele-Arena data files, I'd assume that the user
will have to own a legal copy of Tele-Arena for the MajorBBS system. 
This shouldn't prevent the user to run "Ether" as their own MUD
system.  Data files can be created by the user to create their own
world.  As of the initial release, the only world data files that 
exist are those from Tele-Arena.

This version of AreaBuilder will run on Mac OSX, Windows, and Unix.

=== Running ===

Java is required to run AreaBuilder.  Make sure the JVM is in your
path.  For best results, use the Sun JVM 1.6, as that's what
AreaBuilder was compiled in.

The following data files must be placed in the "data" directory:
  TSGARN-C.MSG
  TSGARNDD.MSG
  TSGARN-D.MSG
  TSGARNDT.MSG
  TSGARN-M.MSG
  TSGARN-T.MSG

Microsoft Windows:
  Use "run.bat"

Mac OSX, Linux, Other:
  Use "run.sh"

The command line is:
  java -DTaConfigFile="config/ta.properties" -jar AreaBuilder.jar

=== Output ===

After running AreaBuilder and getting no errors, the following
files are produced and placed in the "build" directory:
 town.xml
 world1.xml
 world_room_desc.xml
 town_room_desc.xml
 armor.dat
 spells.dat
 barriers.dat
 emotes.dat
 mobs.dat
 tamessages.properties
 traps.dat
 equipment.dat
 mob_weapons.dat
 teleporters.dat
 treasures.dat
 cmd_triggers.xml
 help.dat
 npcs.dat
 weapons.dat

