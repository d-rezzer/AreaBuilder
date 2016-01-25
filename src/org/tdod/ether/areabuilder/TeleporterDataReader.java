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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tdod.ether.ta.cosmos.Teleporter;
import org.tdod.ether.taimpl.cosmos.DefaultTeleporter;

public class TeleporterDataReader extends DataReader {

   private static Log _log = LogFactory.getLog(TeleporterDataReader.class);

   private static HashMap<Integer, Teleporter> _teleporterMap = new HashMap<Integer, Teleporter>();

   private static final String TELEPORTER_VICTIM_KEYWORD     = "PYOU";
   private static final String TELEPORTER_FROM_ROOM_KEYWORD = "PAPP";
   private static final String TELEPORTER_TO_ROOM_KEYWORD    = "PDIS";

   public TeleporterDataReader() {
   }
   
   public void execute() throws Exception {
      String filename = "data/TSGARN-D.MSG";
      _log.info("Reading " + filename);
      readFile(filename);
      _log.info("Read in " + _teleporterMap.size() + " teleporters.");
   }

   public void exportData(String fileName) throws FileNotFoundException {
      _log.info("Exporting to " + fileName);
      BufferedWriter out = null;
      try {
         out = new BufferedWriter(new FileWriter(fileName));
         
         Set<Integer> set = _teleporterMap.keySet();
         for (Integer key:set) {
            Teleporter teleporter = _teleporterMap.get(key);
            out.write(teleporter.getVnum() - 1 + ":");
            out.write(teleporter.getVictimMessage() + ":");
            out.write(teleporter.getFromRoomMessage() + ":");
            out.write(teleporter.getToRoomMessage());

            out.write("\n");
         }
      } catch (IOException e) {
         e.printStackTrace();
      } finally {
         if (out != null) {
            try {
               out.close();               
            } catch (IOException e) {
               e.printStackTrace();
            }
         }
      }      
   }

   private void readFile(String filename) throws Exception {
      FileReader fileReader = null;
      BufferedReader bufferedReader = null;

      int lineNumber = 0;
      try {
         File file = new File(filename);
         fileReader = new FileReader(file);
         bufferedReader = new BufferedReader (fileReader);
         String line ;
         while((line = bufferedReader.readLine()) != null) {
            lineNumber++;
            if (line.startsWith(TELEPORTER_VICTIM_KEYWORD)) {
               parseVictimLine(line, TELEPORTER_VICTIM_KEYWORD);
            } else if (line.startsWith(TELEPORTER_FROM_ROOM_KEYWORD)) {
               parseFromLine(line, TELEPORTER_FROM_ROOM_KEYWORD);
            } else if (line.startsWith(TELEPORTER_TO_ROOM_KEYWORD)) {
               parseToLine(line, TELEPORTER_TO_ROOM_KEYWORD);
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

   private void parseVictimLine(String line, String keyword) {
      _log.debug("Found the " + keyword + " section");

      String data = line.substring(keyword.length());
      String[] split = data.split(" ");
      int index = Integer.valueOf(split[0]); 
      String victimMessage = getLineOfData(data);
      Teleporter teleporter = getOrCreateTeleporter(index);
      teleporter.setVictimMessage(victimMessage);
   }

   private void parseFromLine(String line, String keyword) {
      _log.debug("Found the " + keyword + " section");

      String data = line.substring(keyword.length());
      String[] split = data.split(" ");
      int index = Integer.valueOf(split[0]); 
      String fromMessage = getModifiedStringVariableToken(getLineOfData(data));
      Teleporter teleporter = getOrCreateTeleporter(index);
      teleporter.setFromRoomMessage(fromMessage);
   }

   private void parseToLine(String line, String keyword) {
      _log.debug("Found the " + keyword + " section");

      String data = line.substring(keyword.length());
      String[] split = data.split(" ");
      int index = Integer.valueOf(split[0]); 
      String toMessage = getModifiedStringVariableToken(getLineOfData(data));
      Teleporter teleporter = getOrCreateTeleporter(index);
      teleporter.setToRoomMessage(toMessage);

   }

   private Teleporter getOrCreateTeleporter(int index) {
      Teleporter teleporter = _teleporterMap.get(Integer.valueOf(index));
      
      if (teleporter == null) {
         teleporter = new DefaultTeleporter();
         teleporter.setVnum(index);
         _teleporterMap.put(Integer.valueOf(index), teleporter);
      }

      return teleporter;
   }

}
