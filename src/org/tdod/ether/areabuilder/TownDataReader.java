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
import org.tdod.ether.ta.cosmos.Room;
import org.tdod.ether.taimpl.commands.handler.HandlePassage;
import org.tdod.ether.taimpl.cosmos.DefaultArea;
import org.tdod.ether.taimpl.cosmos.DefaultExit;
import org.tdod.ether.taimpl.cosmos.DefaultRoom;
import org.tdod.ether.taimpl.cosmos.ExitDirectionEnum;
import org.tdod.ether.taimpl.cosmos.RoomFlags;
import org.tdod.ether.taimpl.mobs.enums.Terrain;

import com.thoughtworks.xstream.XStream;

public class TownDataReader extends DataReader {

   private static Log _log = LogFactory.getLog(TownDataReader.class);

   private static final String ROOM_NPC_KEYWORD   = "T1NO";
   private static final String ROOM_EXITS_KEYWORD = "T3NO";
   private static final String ROOM_FLAG_KEYWORD  = "T6NO";

   public static final int ROOM_NUM_OFFSET = 100;

   private Area      _area = new DefaultArea();
   private XStream   _xstream = new XStream();

   public TownDataReader(){
   }

   public void execute() throws Exception {
      String filename = "data/TSGARN-T.MSG";
      _log.info("Reading " + filename);
      readFile(filename);
      _log.info("Read in " + _area.getRoomMap().size() + " town rooms.");
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

   public Area getTownArea() {
      return _area;
   }

   private void readFile(String filename) throws Exception {
      FileReader fileReader = null;
      BufferedReader bufferedReader = null;

      int lineNumber = 0;
      try {
         File file = new File(filename);
         fileReader = new FileReader(file);
         bufferedReader = new BufferedReader (fileReader);
         String line;
         while((line = bufferedReader.readLine()) != null) {
            lineNumber++;
            if (line.startsWith(ROOM_NPC_KEYWORD)) {
               parseNpcLine(line, ROOM_NPC_KEYWORD);
            } else if (line.startsWith(ROOM_EXITS_KEYWORD)) {
               parseExitLine(line, ROOM_EXITS_KEYWORD);
            } else if (line.startsWith(ROOM_FLAG_KEYWORD)) {
               parseFlagLine(line, ROOM_FLAG_KEYWORD);
            }
         }
         
         applyHacks();
      } catch (Exception e) {
         _log.error("Error in data file on line " + lineNumber) ;         
         throw e;
      } finally {
         try {
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

   private void parseNpcLine(String line, String keyword) {
      _log.debug("Found the " + keyword + " section");
      
      String data = line.substring(keyword.length());
      String[] split = getData(data);

      int mobVnum = Integer.valueOf(split[0]);
      int roomVnum = Integer.valueOf(split[1]) - ROOM_NUM_OFFSET;
      Room room = getOrCreateRoom(roomVnum);
      
      room.getNpcVnums().add(mobVnum);
   }

   private void parseExitLine(String line, String keyword) {
      _log.debug("Found the " + keyword + " section");

      String data = line.substring(keyword.length());
      String[] split = data.split(" ");
      int roomVnum = new Integer(split[0]) - ROOM_NUM_OFFSET;
      Room room = getOrCreateRoom(roomVnum);
      
      String[] exitSplit = getData(data);

      int exitRoomVnum = new Integer(exitSplit[0]);
      if (exitRoomVnum != 0) {
         room.addExit(new DefaultExit(ExitDirectionEnum.NORTH, exitRoomVnum - ROOM_NUM_OFFSET));
      }
      exitRoomVnum = new Integer(exitSplit[1]) ;
      if (exitRoomVnum != 0) {
         room.addExit(new DefaultExit(ExitDirectionEnum.NORTHEAST, exitRoomVnum - ROOM_NUM_OFFSET));
      }
      exitRoomVnum = new Integer(exitSplit[2]) ;
      if (exitRoomVnum != 0) {
         room.addExit(new DefaultExit(ExitDirectionEnum.EAST, exitRoomVnum - ROOM_NUM_OFFSET));
      }
      exitRoomVnum = new Integer(exitSplit[3]) ;
      if (exitRoomVnum != 0) {
         room.addExit(new DefaultExit(ExitDirectionEnum.SOUTHEAST, exitRoomVnum - ROOM_NUM_OFFSET));
      }
      exitRoomVnum = new Integer(exitSplit[4]) ;
      if (exitRoomVnum != 0) {
         room.addExit(new DefaultExit(ExitDirectionEnum.SOUTH, exitRoomVnum - ROOM_NUM_OFFSET));
      }
      exitRoomVnum = new Integer(exitSplit[5]) ;
      if (exitRoomVnum != 0) {
         room.addExit(new DefaultExit(ExitDirectionEnum.SOUTHWEST, exitRoomVnum - ROOM_NUM_OFFSET));
      }
      exitRoomVnum = new Integer(exitSplit[6]) ;
      if (exitRoomVnum != 0) {
         room.addExit(new DefaultExit(ExitDirectionEnum.WEST, exitRoomVnum - ROOM_NUM_OFFSET));
      }
      exitRoomVnum = new Integer(exitSplit[7]) ;
      if (exitRoomVnum != 0) {
         room.addExit(new DefaultExit(ExitDirectionEnum.NORTHWEST, exitRoomVnum - ROOM_NUM_OFFSET));
      }
      exitRoomVnum = new Integer(exitSplit[8]) ;
      if (exitRoomVnum != 0) {
         room.addExit(new DefaultExit(ExitDirectionEnum.UP, exitRoomVnum - ROOM_NUM_OFFSET));
      }
      exitRoomVnum = new Integer(exitSplit[9]) ;
      if (exitRoomVnum != 0) {
         room.addExit(new DefaultExit(ExitDirectionEnum.DOWN, exitRoomVnum - ROOM_NUM_OFFSET));
      }
      
      int descriptionIndex = BuilderUtil.getRoomDescriptionIndex(Integer.valueOf(split[0]));
      room.setDefaultDescription(descriptionIndex);
      room.setAltDescription(descriptionIndex);
   }

   private void parseFlagLine(String line, String keyword) {
      _log.debug("Found the " + keyword + " section");

      String data = line.substring(keyword.length());
      String[] flagSplit = getData(data);

      int roomVnum = Integer.valueOf(flagSplit[1]) - ROOM_NUM_OFFSET;
      RoomFlags roomFlag = RoomFlags.NONE;
      int serviceLevel = Integer.valueOf(flagSplit[2]);

      Room room = getOrCreateRoom(roomVnum);

      switch (Integer.valueOf(flagSplit[0])) {
         case 0: roomFlag = RoomFlags.EQUIPMENT_SHOP; break;
         case 1: roomFlag = RoomFlags.WEAPON_SHOP; break;
         case 2: roomFlag = RoomFlags.ARMOR_SHOP; break;
         case 3: roomFlag = RoomFlags.MAGIC_SHOP; break;
         case 4: roomFlag = RoomFlags.TAVERN; break;
         case 5: roomFlag = RoomFlags.TEMPLE; break;
         case 6: roomFlag = RoomFlags.GUILD_HALL; break;
         case 7: roomFlag = RoomFlags.VAULT; break;
         case 8: roomFlag = RoomFlags.ARENA; room.setRoomFlags(RoomFlags.NONE.getBit());  break;
         default : _log.error("Flag " + flagSplit[0] + " not supported!");
      }
      
      room.setRoomFlags(roomFlag.getBit() + room.getRoomFlags());         
      room.setServiceLevel(serviceLevel);

   }
   
   private Room getOrCreateRoom(int vnum) {
      Room room = _area.getEntry(vnum) ;

      if (room == null) {
         room = new DefaultRoom() ;
         room.setRoomNumber(vnum) ;
         room.setTerrain(Terrain.TOWN);
         room.setRoomFlags(RoomFlags.SAFE.getBit());
         
         _area.getRoomMap().put(vnum, room) ;
      }
      
      return room;
   }

   private void applyHacks() {
      _log.info("Applying hacks to town data.");
      
      Room room = _area.getEntry(HandlePassage.TOWN1_DOCKS);
      room.setRoomFlags(RoomFlags.DOCKS.getBit() + room.getRoomFlags());
      
      room = _area.getEntry(HandlePassage.TOWN2_DOCKS);
      room.setRoomFlags(RoomFlags.DOCKS.getBit() + room.getRoomFlags());
      
      
   }

}
