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
import org.tdod.ether.ta.cosmos.CommandTrigger;
import org.tdod.ether.ta.cosmos.CommandTriggers;
import org.tdod.ether.ta.cosmos.RoomDescriptions;
import org.tdod.ether.taimpl.cosmos.DefaultCommandTrigger;
import org.tdod.ether.taimpl.cosmos.DefaultCommandTriggers;
import org.tdod.ether.taimpl.cosmos.DefaultRoomDescriptions;

import com.thoughtworks.xstream.XStream;

// TSGARNDT
public class WorldDescriptionReader extends DataReader {

   private static Log _log = LogFactory.getLog(NumericWorldDataReader.class);

   private static final String ROOM_KEYWORD    = "ROOM";

   private static final String COMMAND_KEYWORD        = "COMM";
   private static final String COMMAND_PLAYER_MESSAGE = "CYOU";
   private static final String COMMAND_ROOM_MESSAGE   = "CTHM";
   

   private CommandTriggers  _commandTriggers = new DefaultCommandTriggers();
   private RoomDescriptions _roomDescriptions = new DefaultRoomDescriptions();
   private XStream          _xstream = new XStream();

   public WorldDescriptionReader() {
   }
   
   public void execute() throws Exception {
      String filename = "data/TSGARNDT.MSG";
      _log.info("Reading " + filename);
      readFile(filename);         
      _log.info("Read in " + _roomDescriptions.getMap().size() + " room descriptions");
   }

   public void readFile(String filename) throws Exception {
      FileReader fileReader = null;
      BufferedReader bufferedReader = null;

      int lineNumber = 0;
      try {
         File file = new File(filename) ;
         fileReader = new FileReader(file);
         bufferedReader = new BufferedReader (fileReader);
     
         String line ;
         boolean isReadingRoomDescription = false;
         Integer roomNumber = 0;
         StringBuffer roomDescription = new StringBuffer();
         while((line = bufferedReader.readLine()) != null) {
            lineNumber++; 
            if (line.startsWith(ROOM_KEYWORD)) {
               isReadingRoomDescription = true;
               roomDescription = new StringBuffer();
               roomNumber = getNumber(line);
               int index = line.indexOf("{") + 1;
               roomDescription.append(line.substring(index)) ;
            } else if (line.endsWith("}") && isReadingRoomDescription) {
               isReadingRoomDescription = false;
               _roomDescriptions.getMap().put(roomNumber, roomDescription.toString());
            } else if (isReadingRoomDescription) {
               if (!line.equals("")) {
                  roomDescription.append("\n" + line);                  
               }
            } else if (line.startsWith(COMMAND_KEYWORD)) {
               readKeyword(line, COMMAND_KEYWORD);
            } else if (line.startsWith(COMMAND_PLAYER_MESSAGE)) {
               readPlayerMessage(bufferedReader, line, COMMAND_PLAYER_MESSAGE);
            } else if (line.startsWith(COMMAND_ROOM_MESSAGE)) {
               readRoomMessage(bufferedReader, line, COMMAND_ROOM_MESSAGE);
            }
            
         }  
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
   
   protected int getNumber(String line) {      
      String data = line.substring(ROOM_KEYWORD.length());
      String[] split = data.split(" ");
      return new Integer(split[0]) ;
   }
   
   public void exportRoomDescriptionXml(String roomDescFilename) throws FileNotFoundException {
      _log.info("Exporting to " + roomDescFilename);
      File file = new File(roomDescFilename);
      
      FileOutputStream fOutStream = new FileOutputStream(file);
      PrintWriter printWriter = new PrintWriter(fOutStream, true);

      printWriter.println(_xstream.toXML(_roomDescriptions)) ;
      
      printWriter.flush();
      printWriter.close();

   }

   public void exportCommandTriggerXml(String fileName) throws FileNotFoundException {
      _log.info("Exporting to " + fileName);
      File file = new File(fileName);
      
      FileOutputStream fOutStream = new FileOutputStream(file);
      PrintWriter printWriter = new PrintWriter(fOutStream, true);

      printWriter.println(_xstream.toXML(_commandTriggers)) ;
      
      printWriter.flush();
      printWriter.close();
   }

   
   private void readKeyword(String line, String keyword) {
      _log.debug("Found the " + keyword + " section");
      String data = line.substring(keyword.length());
      String[] split = data.split(" ");
      int index = Integer.valueOf(split[0]); 
      String command = getLineOfData(data);

      CommandTrigger commandTrigger = getOrCreateCommandTrigger(index);
      commandTrigger.setCommand(command);
   }
   
   private void readPlayerMessage(BufferedReader bufferedReader, String line, String keyword)
   throws IOException {
      _log.debug("Found the " + keyword + " section");

      String data = line.substring(keyword.length());
      String[] split = data.split(" ");
      int index = new Integer(split[0]) /  2;
      CommandTrigger commandTrigger = getOrCreateCommandTrigger(index);

      StringBuffer buffer = new StringBuffer();
      buffer.append(getDescriptionData(data));

      while (!line.equals(new String("}"))) {
         line = bufferedReader.readLine();
         if (!line.equals(new String("}"))) {
            buffer.append("\n" + line);            
         }
      }

      commandTrigger.setPlayerMessage(buffer.toString());
   }

   private void readRoomMessage(BufferedReader bufferedReader, String line, String keyword)
   throws IOException {
      _log.debug("Found the " + keyword + " section");

      String data = line.substring(keyword.length());
      String[] split = data.split(" ");
      int index = new Integer(split[0]) /  2;
      CommandTrigger commandTrigger = getOrCreateCommandTrigger(index);

      StringBuffer buffer = new StringBuffer();
      buffer.append(getDescriptionData(data));

      while (!line.equals(new String("}"))) {
         line = bufferedReader.readLine();
         if (!line.equals(new String("}"))) {
            buffer.append("\n" + line);            
         }
      }

      String tmpString = getModifiedStringVariableToken(buffer.toString());
      
      commandTrigger.setRoomMessage(tmpString);
   }
   
   private CommandTrigger getOrCreateCommandTrigger(int index) {
      CommandTrigger commandTrigger = _commandTriggers.getMap().get(Integer.valueOf(index));         

      if (commandTrigger == null) {
         commandTrigger = new DefaultCommandTrigger();
         _commandTriggers.getMap().put(Integer.valueOf(index), commandTrigger);
      }

      return commandTrigger;
   }
}
