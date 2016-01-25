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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tdod.ether.ta.cosmos.Emote;
import org.tdod.ether.taimpl.cosmos.DefaultEmote;

public class EmoteDataReader extends DataReader {

   private static Log _log = LogFactory.getLog(EmoteDataReader.class);

   private static HashMap<Integer, Emote> _emoteMap = new HashMap<Integer, Emote>();

   private static final String EMOTE_ACTION_KEYWORD = "ACTION";
   private static final String EMOTE_PLAYER_KEYWORD = "ASYOU";
   private static final String EMOTE_TARGET_KEYWORD = "ATHEM";
   private static final String EMOTE_ROOM_KEYWORD   = "ALONE";

   public EmoteDataReader() {
   }
   
   public void execute() throws Exception {
      String filename = "data/TSGARN-T.MSG";
      _log.info("Reading " + filename);
      readFile(filename);
      _log.info("Read in " + _emoteMap.size() + " emotes.");
   }

   public void exportData(String fileName) throws FileNotFoundException {
      _log.info("Exporting to " + fileName);
      BufferedWriter out = null;
      try {
         out = new BufferedWriter(new FileWriter(fileName));
         
         Set<Integer> set = _emoteMap.keySet();
         for (Integer key:set) {
            Emote emote = _emoteMap.get(key);
            out.write(emote.getKeyword() + ":");
            out.write(emote.getToPlayer() + ":");
            out.write(emote.getToTarget() + ":");
            out.write(emote.getToRoom());
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

   public List<Emote> getSortedList() {
      List<Emote> unsortedList = new ArrayList<Emote>(_emoteMap.values());
      Collections.sort(unsortedList);
      return unsortedList;      
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
            if (line.startsWith(EMOTE_ACTION_KEYWORD)) {
               parseActionLine(line, EMOTE_ACTION_KEYWORD);
            } else if (line.startsWith(EMOTE_PLAYER_KEYWORD)) {
               parsePlayerLine(line, EMOTE_PLAYER_KEYWORD);
            } else if (line.startsWith(EMOTE_TARGET_KEYWORD)) {
               parseTargetLine(line, EMOTE_TARGET_KEYWORD);
            } else if (line.startsWith(EMOTE_ROOM_KEYWORD)) {
               parseAloneLine(line, EMOTE_ROOM_KEYWORD);
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

   private void parseActionLine(String line, String keyword) {
      _log.debug("Found the " + keyword + " section");

      String data = line.substring(keyword.length());
      String[] split = data.split(" ");
      int index = Integer.valueOf(split[0]); 
      String action = getLineOfData(data);
      Emote emote = getOrCreateEmote(index);
      emote.setKeyword(action);
   }

   private void parsePlayerLine(String line, String keyword) {
      _log.debug("Found the " + keyword + " section");

      String data = line.substring(keyword.length());
      String[] split = data.split(" ");
      int index = Integer.valueOf(split[0]); 
      String action = getLineOfData(data);
      Emote emote = getOrCreateEmote(index);
      emote.setToPlayer(action);

   }
   
   private void parseTargetLine(String line, String keyword) {
      _log.debug("Found the " + keyword + " section");

      String data = line.substring(keyword.length());
      String[] split = data.split(" ");
      int index = Integer.valueOf(split[0]); 
      String action = getLineOfData(data);
      Emote emote = getOrCreateEmote(index);

      emote.setToTarget(getModifiedStringVariableToken(action));
   }
   
   private void parseAloneLine(String line, String keyword) {
      _log.debug("Found the " + keyword + " section");

      String data = line.substring(keyword.length());
      String[] split = data.split(" ");
      int index = Integer.valueOf(split[0]); 
      String action = getLineOfData(data);
      Emote emote = getOrCreateEmote(index);
      
      emote.setToRoom(getModifiedStringVariableToken(action));
   }
   
   private Emote getOrCreateEmote(int index) {
      Emote emote = _emoteMap.get(Integer.valueOf(index));         

      if (emote == null) {
         emote = new DefaultEmote();
         emote.setKeyword("");
         emote.setToPlayer("");
         emote.setToRoom("");
         emote.setToTarget("");
         _emoteMap.put(Integer.valueOf(index), emote);
      }
      
      return emote;
   }

   
}
