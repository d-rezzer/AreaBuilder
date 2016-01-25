/****************************************************************************
 * Ether Code base, version 1.0                                             *
 *==========================================================================*
 * Copyright (C) 2011 by Ron Kinney                                         *
 * All rights reserved.                                                     *
 *                                                                          *
 * This program is free software; you can redistribute it and/or modify     *
 * it under the terms of the GNU General Public License as published        *
 * by the Free Software Foundation; either version 2 of the License, or     *
 * (at your option) any later version.                                      *
 *                                                                          *
 * This program is distributed in the hope that it will be useful,          *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of           *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the            *
 * GNU General Public License for more details.                             *
 *                                                                          *
 * Redistribution and use in source and binary forms, with or without       *
 * modification, are permitted provided that the following conditions are   *
 * met:                                                                     *
 *                                                                          *
 * * Redistributions of source code must retain this copyright notice,      *
 *   this list of conditions and the following disclaimer.                  *
 * * Redistributions in binary form must reproduce this copyright notice    *
 *   this list of conditions and the following disclaimer in the            *
 *   documentation and/or other materials provided with the distribution.   *
 *                                                                          *
 *==========================================================================*
 * Ron Kinney (ronkinney@gmail.com)                                         *
 * Ether Homepage:   http://tdod.org/ether                                  *
 ****************************************************************************/

package org.tdod.ether.areabuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tdod.ether.ta.cosmos.Area;
import org.tdod.ether.ta.cosmos.Door;
import org.tdod.ether.ta.cosmos.Exit;
import org.tdod.ether.ta.cosmos.Lair;
import org.tdod.ether.ta.cosmos.Room;
import org.tdod.ether.ta.cosmos.Trigger;
import org.tdod.ether.ta.cosmos.enums.DoorType;
import org.tdod.ether.taimpl.cosmos.DefaultArea;
import org.tdod.ether.taimpl.cosmos.DefaultExit;
import org.tdod.ether.taimpl.cosmos.DefaultLair;
import org.tdod.ether.taimpl.cosmos.DefaultRoom;
import org.tdod.ether.taimpl.cosmos.DefaultTrigger;
import org.tdod.ether.taimpl.cosmos.ExitDirectionEnum;
import org.tdod.ether.taimpl.cosmos.RoomFlags;
import org.tdod.ether.taimpl.cosmos.doors.HasRuneDoor;
import org.tdod.ether.taimpl.cosmos.doors.ItemKeyDoor;
import org.tdod.ether.taimpl.cosmos.doors.MinimumRuneDoor;
import org.tdod.ether.taimpl.cosmos.doors.NullDoor;
import org.tdod.ether.taimpl.cosmos.doors.PrivateRoomDoor;
import org.tdod.ether.taimpl.cosmos.doors.PromoteDoor;
import org.tdod.ether.taimpl.cosmos.doors.PuzzleDoor;
import org.tdod.ether.taimpl.cosmos.enums.TriggerType;
import org.tdod.ether.taimpl.mobs.enums.Terrain;
import org.tdod.ether.util.GameUtil;

import com.thoughtworks.xstream.XStream;

// TSGARNDD
public class NumericWorldDataReader extends DataReader {

   private static Log _log = LogFactory.getLog(NumericWorldDataReader.class);

   private static final String DARKNESS_KEYWORD = "DD1";
   private static final String DD1TOT_KEYWORD   = "DD1TOT";
   private static final String TERRAIN_KEYWORD  = "DD2";
   private static final String DD2TOT_KEYWORD   = "DD2TOT";
   private static final String LAIR_KEYWORD     = "LAIR";
   private static final String LAIRTOT_KEYWORD  = "LAIRTOT";
   private static final String EXIT_KEYWORD     = "EXIT";
   private static final String EXITTOT_KEYWORD  = "EXITTOT";
   private static final String DOOR_KEYWORD     = "DOOR";
   private static final String DOORTOT_KEYWORD  = "DOORTOT";
   private static final String TRIG_KEYWORD     = "TRIG";
   private static final String TRIGTOT_KEYWORD  = "TRIGTOT";
   
   private Area                   _area = new DefaultArea();
   private XStream                _xstream = new XStream();

   private Area                  _townArea;
   
   public NumericWorldDataReader() {
   }

   public void execute() throws Exception {
      String filename = "data/TSGARNDD.MSG";
      _log.info("Reading " + filename);
      readFile(filename);
      _log.info("Read in " + _area.getRoomMap().size() + " rooms.");
   }
   
   public void exportXml(String fileName) throws FileNotFoundException {
      _log.info("Exporting to " + fileName);
      File file = new File(fileName);
      
      FileOutputStream fOutStream = new FileOutputStream(file);
      PrintWriter printWriter = new PrintWriter(fOutStream, true);

      printWriter.println(_xstream.toXML(_area)) ;
      
      printWriter.flush();
      printWriter.close();
   }

   public void setTownArea(Area townArea) {
      _townArea = townArea;
   }
   
   private void readFile(String filename) throws Exception {
      FileReader exitFileReader = null;
      BufferedReader exitBufferedReader = null;
      
      FileReader fileReader = null;
      BufferedReader bufferedReader = null;

      int lineNumber = 0;
      try {
         _log.info("Executing first pass.");
         File file = new File(filename);
         exitFileReader = new FileReader(file);
         exitBufferedReader = new BufferedReader (exitFileReader);
         String line ;
         while((line = exitBufferedReader.readLine()) != null) {
            lineNumber++; 
            if (line.startsWith(EXIT_KEYWORD)) {
               parseExitLine(line) ;               
            }
         }
         
         _log.info("Executing second pass.");
         fileReader = new FileReader(file);
         bufferedReader = new BufferedReader (fileReader);
         lineNumber = 0;
         while((line = bufferedReader.readLine()) != null) {
            lineNumber++; 
            if (line.startsWith(DARKNESS_KEYWORD)) {
               parseDarknessLine(line) ;
            } else if (line.startsWith(TERRAIN_KEYWORD)) {
               parseTerrainLine(line) ;
            } else if (line.startsWith(LAIR_KEYWORD)) {
               parseLairLine(line);
            } else if (line.startsWith(DOOR_KEYWORD)) {
               parseDoorLine(line);
            } else if (line.startsWith(TRIG_KEYWORD)) {
               parseTriggerLine(line);
            }
         }
         
         applyHacks();
      } catch (Exception e) {
         _log.error("Error in data file on line " + lineNumber) ;         
         throw e;
      } finally {
         try {
            if (exitBufferedReader != null) {
               exitBufferedReader.close() ;
            }
            if (exitFileReader != null) {
               exitFileReader.close() ;
            }            
            if (bufferedReader != null) {
               bufferedReader.close() ;
            }
            if (fileReader != null) {
               fileReader.close() ;
            }            
         } catch (IOException e) {
            throw e;
         }
      }
   }
   
   private void parseExitLine(String line) {
      if (line.startsWith(EXITTOT_KEYWORD)) {
         _log.debug("Found the " + EXITTOT_KEYWORD + " section");
         return;
      }
      String data = line.substring(EXIT_KEYWORD.length());
      String[] split = data.split(" ");
      int roomVnum = new Integer(split[0]);
      Room room = getOrCreateRoom(roomVnum);
      
      String[] exitSplit = getData(data);
      
      for (int count = 0; count < exitSplit.length - 1; count++) {
         int exitRoomVnum = Integer.valueOf(exitSplit[count]); 
         if (exitRoomVnum != 0) {
            ExitDirectionEnum exitDir = getExitDirectionEnum(count);
            room.addExit(new DefaultExit(exitDir, exitRoomVnum));
            // If the destination room is negative, it must go to a town.  Create the other end.
            if (exitRoomVnum < 0) {
               ExitDirectionEnum oppositeExitDir = GameUtil.getOppositeExit(exitDir);
               _townArea.getEntry(exitRoomVnum).addExit(new DefaultExit(oppositeExitDir, roomVnum));
            }            
         }
      }
      
      room.setDefaultDescription(Integer.valueOf(exitSplit[10]).intValue()) ;
      room.setAltDescription(Integer.valueOf(exitSplit[10]).intValue()) ;

      _area.getRoomMap().put(room.getRoomNumber(), room);
   }

   private void parseDarknessLine(String line) {
      if (line.startsWith(DD1TOT_KEYWORD)) {
         _log.debug("Found the " + DD1TOT_KEYWORD + " section");
         return;
      }
      
      String data = line.substring(DARKNESS_KEYWORD.length());
      String[] roomDataSplit = getData(data);

      int start = new Integer(roomDataSplit[0]).intValue() ;
      int end = new Integer(roomDataSplit[1]).intValue() ;
      int darknessFlag = new Integer(roomDataSplit[2]).intValue() ;
      for (int roomVnum=start; roomVnum<=end;roomVnum++) {
         Room room = getOrCreateRoom(roomVnum) ;
         int flags = room.getRoomFlags();
         
         if (darknessFlag == 1) {
            flags += RoomFlags.PERM_DARK.getBit();
         } else {
            flags += RoomFlags.DARK.getBit();            
         }
         
         room.setRoomFlags(flags) ;
      }
   }
   
   private void parseTerrainLine(String line) {
      if (line.startsWith(DD2TOT_KEYWORD)) {
         _log.debug("Found the " + DD2TOT_KEYWORD + " section");
         return;
      }

      String data = line.substring(TERRAIN_KEYWORD.length());
      String[] roomDataSplit = getData(data);

      int start = new Integer(roomDataSplit[0]).intValue() ;
      int end = new Integer(roomDataSplit[1]).intValue() ;
      for (int roomVnum=start; roomVnum<=end;roomVnum++) {
         Room room = getOrCreateRoom(roomVnum) ;
         Terrain terrain = Terrain.getTerrain(new Integer(roomDataSplit[2]).intValue()) ;
         room.setTerrain(terrain) ;
      }
   }
   
   private void parseLairLine(String line) {
      if (line.startsWith(LAIRTOT_KEYWORD)) {
         _log.debug("Found the " + LAIRTOT_KEYWORD + " section");
         return;
      }

      String data = line.substring(TERRAIN_KEYWORD.length());
      String[] lairDataSplit = getData(data);

      int roomVnum = Integer.valueOf(lairDataSplit[0]) ;
      int mob = Integer.valueOf(lairDataSplit[1]);
      int number = Integer.valueOf(lairDataSplit[2]) ;
      int item = Integer.valueOf(lairDataSplit[3]) ;
      Lair lair = new DefaultLair(mob, number, item);
      
      Room room = getOrCreateRoom(roomVnum) ;
      
      room.getLairs().add(lair);
   }

   private void parseDoorLine(String line) {
      if (line.startsWith(DOORTOT_KEYWORD)) {
         _log.debug("Found the " + DOORTOT_KEYWORD + " section");
         return;
      }

      String data = line.substring(DOOR_KEYWORD.length());
      String[] doorDataSplit = getData(data);

      int fromRoom = Integer.valueOf(doorDataSplit[1]);
      int toRoom = Integer.valueOf(doorDataSplit[2]);

      Room room = _area.getEntry(fromRoom) ;
      if (room == null) {
         room = _townArea.getEntry(fromRoom);
      }
      
      Exit exit = room.getExit(toRoom) ;
      if (exit == null) {
         _log.info("Exit from room " + room.getRoomNumber() + " to room " + toRoom + " does not exist!  Skipping");
         return ;         
      }
      
      int doorTypeInt = Integer.valueOf(doorDataSplit[0]).intValue();
      Door door = new NullDoor();

      if (doorTypeInt >= 0) {
         door = new ItemKeyDoor();
      } else {
         DoorType doorType = DoorType.getDoorType(doorTypeInt);
         if (doorType.equals(DoorType.HAS_RUNE)) {
            door = new HasRuneDoor();
         } else if (doorType.equals(DoorType.MINIMUM_RUNE)) {
            door = new MinimumRuneDoor();
         } else if (doorType.equals(DoorType.PRIVATE_ROOM)) {
            door = new PrivateRoomDoor();
         } else if (doorType.equals(DoorType.PUZZLE)) {
            door = new PuzzleDoor();
         } else if (doorType.equals(DoorType.PROMOTE_DOOR)) {
            door = new PromoteDoor();            
         }
      }
      door.setV0(Integer.valueOf(doorDataSplit[0]).intValue());
      door.setV3(Integer.valueOf(doorDataSplit[3]).intValue());
      door.setV4(Integer.valueOf(doorDataSplit[4]).intValue());
      door.setV5(Integer.valueOf(doorDataSplit[5]).intValue());
      door.setV6(Integer.valueOf(doorDataSplit[6]).intValue());         
      
      exit.setDoor(door);
   }
   
   private void parseTriggerLine(String line) {
      if (line.startsWith(TRIGTOT_KEYWORD)) {
         _log.debug("Found the " + TRIGTOT_KEYWORD + " section");
         return;
      }

      String data = line.substring(TRIG_KEYWORD.length());
      String[] trigDataSplit = getData(data);

      int roomVnum = Integer.valueOf(trigDataSplit[0]);
      TriggerType triggerType = TriggerType.getTriggerType(Integer.valueOf(trigDataSplit[1]));
      
      if (triggerType.equals(TriggerType.Invalid)) {
         _log.info("Invalid trigger found -- " + trigDataSplit[1]);
      }
      
      Room room = _area.getEntry(roomVnum) ;
      if (room == null) {
         _log.info("Room " + roomVnum + " does not exist!  Skipping");
         return ;
      }

      Trigger trigger = new DefaultTrigger();
      trigger.setTriggerType(triggerType);
      trigger.setV2(Integer.valueOf(trigDataSplit[2]));
      trigger.setV3(Integer.valueOf(trigDataSplit[3]));
      trigger.setV4(Integer.valueOf(trigDataSplit[4]));
      trigger.setV5(Integer.valueOf(trigDataSplit[5]));
      trigger.setV6(Integer.valueOf(trigDataSplit[6]));
      trigger.setV7(Integer.valueOf(trigDataSplit[7]));
      room.getTriggers().add(trigger);
   }
   
   private Room getOrCreateRoom(int vnum) {
      Room room = _area.getEntry(vnum) ;

      if (room == null) {
         room = new DefaultRoom() ;
         room.setRoomNumber(vnum) ;
         _area.getRoomMap().put(vnum, room) ;
      }
      
      return room;
   }
   
   private void applyHacks() {
      _log.info("Applying hacks to world data.");
      
      // Old: EXIT1605 {1595 0 1606 0 0 0 1604 0 1712 0 1087}
      // New: EXIT1605 {1595 0 1606 0 0 0 1604 0 1680 0 1087}
      fixExits();

      // Old: DOOR30   {-8 1505 1515 4 0 0 1}
      // New: DOOR30   {-8 1515 1505 4 0 0 1}
      fixDoorPuzzle(1515, 1505);

      // Old: DOOR32   {-8 1701 1711 4 0 0 1}
      // New: DOOR32   {-8 1711 1701 4 0 0 1}
      fixDoorPuzzle(1711, 1701);
    
      // Old: DOOR3    {38 1 13 4 0 2 1}
      // New: DOOR3    {38 1 13 4 1 2 1}
      fixDoorV4(1, ExitDirectionEnum.NORTHEAST, 1);
      
      // Old: DOOR8    {22 179 76 6 1 0 1}
      // New: DOOR8    {22 179 76 6 0 0 1}
      fixDoorV4(179, ExitDirectionEnum.UP, 0);
      
      // Old: DOOR9    {22 180 73 6 1 0 1}
      // New: DOOR9    {22 180 73 6 0 0 1}
      fixDoorV4(180, ExitDirectionEnum.UP, 0);
      
      // Old: TRIG66   {1483 4 1 14 1505 1515 0 1}
      // New: TRIG66   {1483 4 1 14 1515 1517 0 1}
      fixTriggerDescription(1483, 1515, 1517);
      
      // Old: TRIG68   {1731 4 1 14 1701 1523 0 1}
      // New: TRIG68   {1731 4 1 14 1711 1525 0 1}
      fixTriggerDescription(1731, 1711, 1525);
      
      // Old: TRIG95   {3122 3 3100 4 0 0 0 1}
      // New: TRIG95   {3122 3 3100 4 0 0 0 0}
      fixTriggerV7(3122, 0);
   }
   
   private void fixExits() {
      int roomVnum = 1605;
      int oldValue = 1712;
      int newValue = 1680;
      Room room = _area.getRoomMap().get(roomVnum);
      Exit exit = room.getExit(ExitDirectionEnum.UP);
      if (exit.getToRoom() == oldValue) {
         exit.setToRoom(newValue);
      } else {
         _log.error("Room " + roomVnum + ".  Expected to see " + oldValue + " but got " + exit.getToRoom() + ".");
      }      
   }
   
   private void fixDoorPuzzle(int roomVnum1, int roomVnum2) {
      try {
         Room room1 = _area.getRoomMap().get(roomVnum1);
         Exit exit1 = room1.getExit(ExitDirectionEnum.NORTH);
         Room room2 = _area.getRoomMap().get(roomVnum2);
         Exit exit2 = room2.getExit(ExitDirectionEnum.SOUTH);
         exit1.setDoor(exit2.getDoor());
         exit2.setDoor(null);         
      } catch (Exception e) {
         _log.error("Error attempting to hack door puzzle for room " + roomVnum1);
      }
   }
   
   private void fixDoorV4(int fromRoom, ExitDirectionEnum exitDirection, int v4) {
      try {
         Room room = _area.getRoomMap().get(fromRoom);
         Exit exit = room.getExit(exitDirection);
         exit.getDoor().setV4(v4);
      } catch (Exception e) {
         _log.error("Error attempting to hack door consumes value for room " + fromRoom);
      }      
   }
   
   private void fixTriggerDescription(int roomVnum, int v4, int v5) {
      try {
         Room room = _area.getRoomMap().get(roomVnum);
         Trigger trigger = room.getTriggers().get(0);
         trigger.setV4(v4);
         trigger.setV5(v5);
      } catch (Exception e) {
         _log.error("Error attempting to hack triggers description value for room " + roomVnum);
      }      
   }
   
   private void fixTriggerV7(int roomVnum, int v7) {
      try {
         Room room = _area.getRoomMap().get(roomVnum);
         room.getTriggers().get(0).setV7(v7);
      } catch (Exception e) {
         _log.error("Error attempting to trigger V7 for room " + roomVnum);
      }            
   }
   
   private ExitDirectionEnum getExitDirectionEnum(int index) {
      switch (index) {
      case 0: return ExitDirectionEnum.NORTH; 
      case 1: return ExitDirectionEnum.NORTHEAST; 
      case 2: return ExitDirectionEnum.EAST; 
      case 3: return ExitDirectionEnum.SOUTHEAST; 
      case 4: return ExitDirectionEnum.SOUTH; 
      case 5: return ExitDirectionEnum.SOUTHWEST; 
      case 6: return ExitDirectionEnum.WEST; 
      case 7: return ExitDirectionEnum.NORTHWEST; 
      case 8: return ExitDirectionEnum.UP; 
      case 9: return ExitDirectionEnum.DOWN; 
      }
      
      return ExitDirectionEnum.UNKNOWN;
   }
}
