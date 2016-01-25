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
import org.tdod.ether.ta.cosmos.RoomDescriptions;
import org.tdod.ether.taimpl.cosmos.DefaultRoomDescriptions;

import com.thoughtworks.xstream.XStream;

public class TownDescriptionReader extends DataReader {

   private static Log _log = LogFactory.getLog(TownDescriptionReader.class);

   private static final String ROOM_NAME_KEYWORD  = "T4SH";
   private static final String ROOM_DESC_KEYWORD  = "T4LO";

   private RoomDescriptions _roomDescriptions = new DefaultRoomDescriptions();
   private XStream          _xstream = new XStream();

   public TownDescriptionReader() {
   }

   public void exportXml(String fileName) throws FileNotFoundException {
      _log.info("Exporting to " + fileName);
      
      File file = new File(fileName);
      
      FileOutputStream fOutStream = new FileOutputStream(file);
      PrintWriter printWriter = new PrintWriter(fOutStream, true);

      printWriter.println(_xstream.toXML(_roomDescriptions)) ;
      
      printWriter.flush();
      printWriter.close();      
   }

   public void execute() throws Exception {
      String filename = "data/TSGARN-T.MSG";
      _log.info("Reading " + filename);
      readFile(filename);
      _log.info("Read in " + _roomDescriptions.getMap().size() + " town descriptions.");
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
            if (line.startsWith(ROOM_NAME_KEYWORD)) {
               parseNameLine(line, ROOM_NAME_KEYWORD);
            } else if (line.startsWith(ROOM_DESC_KEYWORD)) {
               parseDescriptionLine(bufferedReader, line, ROOM_DESC_KEYWORD);
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

   private void parseNameLine(String line, String keyword) {
      _log.debug("Found the " + keyword + " section");

      String data = line.substring(keyword.length());
      String[] split = data.split(" ");
      int vnum = new Integer(split[0]);
      vnum = BuilderUtil.getRoomDescriptionIndex(vnum);
      
      _roomDescriptions.getMap().put(Integer.valueOf(vnum), getFullLineOfData(data));
   }
   
   private void parseDescriptionLine(BufferedReader bufferedReader, String line, String keyword)
   throws IOException {
      _log.debug("Found the " + keyword + " section");

      String data = line.substring(keyword.length());
      String[] split = data.split(" ");
      int roomVnum = new Integer(split[0]);
      roomVnum = -(roomVnum * 2);

      StringBuffer buffer = new StringBuffer();
      buffer.append(getDescriptionData(data));

      while (!line.equals(new String("}"))) {
         line = bufferedReader.readLine();
         if (!line.equals(new String("}"))) {
            buffer.append("\n" + line);            
         }
      }
      
      _roomDescriptions.getMap().put(Integer.valueOf(roomVnum), buffer.toString());
   }

   private String getFullLineOfData(String data) {
      int index = data.indexOf("{");
      String string = data.substring(index + 1, data.length());
      return string;
   }

}
