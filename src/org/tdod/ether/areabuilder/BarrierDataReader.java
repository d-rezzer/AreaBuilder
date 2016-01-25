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
import org.tdod.ether.ta.cosmos.Barrier;
import org.tdod.ether.taimpl.cosmos.DefaultBarrier;

public class BarrierDataReader extends DataReader {

   private static Log _log = LogFactory.getLog(BarrierDataReader.class);

   private static HashMap<Integer, Barrier> _barrierMap = new HashMap<Integer, Barrier>();

   private static final String BARRIER_LOCKED_KEYWORD   = "BDES";
   private static final String BARRIER_UNLOCKED_KEYWORD = "BUNL";
   private static final String BARRIER_ROGUE_KEYWORD    = "BROG";
   private static final String BARRIER_VALUE_KEYWORD    = "BNUM";
   
   private static final String NULL = "#NULL#";
   
   public BarrierDataReader() {
   }

   public void execute() throws Exception {
      String filename = "data/TSGARN-D.MSG";
      _log.info("Reading " + filename);
      readFile(filename);
      _log.info("Read in " + _barrierMap.size() + " barriers.");
   }

   public void exportData(String fileName) throws FileNotFoundException {
      _log.info("Exporting to " + fileName);
      BufferedWriter out = null;
      try {
         out = new BufferedWriter(new FileWriter(fileName));
         
         Set<Integer> set = _barrierMap.keySet();
         for (Integer key:set) {
            Barrier barrier = _barrierMap.get(key);
            out.write(barrier.getNumber() + ":");
            out.write(barrier.getLockedMessage() + ":");
            out.write(barrier.getUnlockedMessage() + ":");
            out.write(barrier.getRogueMessage() + ":");
            out.write(barrier.getValue() + "");
            
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
            if (line.startsWith(BARRIER_LOCKED_KEYWORD)) {
               parseLockedLine(line, BARRIER_LOCKED_KEYWORD);
            } else if (line.startsWith(BARRIER_UNLOCKED_KEYWORD)) {
               parseUnlockedLine(line, BARRIER_UNLOCKED_KEYWORD);
            } else if (line.startsWith(BARRIER_ROGUE_KEYWORD)) {
               parseRogueLine(line, BARRIER_ROGUE_KEYWORD);
            } else if (line.startsWith(BARRIER_VALUE_KEYWORD)) {
               parseValueLine(line, BARRIER_VALUE_KEYWORD);
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

   private void parseLockedLine(String line, String keyword) {
      _log.debug("Found the " + keyword + " section");
      
      String data = line.substring(keyword.length());
      String[] split = data.split(" ");
      int index = Integer.valueOf(split[0]); 
      String lockedMessage = getLineOfData(data);
      
      if (lockedMessage.equals(NULL)) {
         lockedMessage = "";
      }
      
      lockedMessage = getModifiedStringVariableToken(lockedMessage);
      
      Barrier barrier = getOrCreateBarrier(index);
      barrier.setLockedMessage(lockedMessage);
   }

   private void parseUnlockedLine(String line, String keyword) {
      _log.debug("Found the " + keyword + " section");
      
      String data = line.substring(keyword.length());
      String[] split = data.split(" ");
      int index = Integer.valueOf(split[0]); 
      String unlockedMessage = getLineOfData(data);

      if (unlockedMessage.equals(NULL)) {
         unlockedMessage = "";
      }

      unlockedMessage = getModifiedStringVariableToken(unlockedMessage);

      Barrier barrier = getOrCreateBarrier(index);
      barrier.setUnlockedMessage(unlockedMessage);      
   }

   private void parseRogueLine(String line, String keyword) {
      _log.debug("Found the " + keyword + " section");
      
      String data = line.substring(keyword.length());
      String[] split = data.split(" ");
      int index = Integer.valueOf(split[0]); 
      String rogueMessage = getLineOfData(data);
      
      if (rogueMessage.equals(NULL)) {
         rogueMessage = "";
      }
      
      rogueMessage = getModifiedStringVariableToken(rogueMessage);

      Barrier barrier = getOrCreateBarrier(index);
      barrier.setRogueMessage(rogueMessage);            
   }

   private void parseValueLine(String line, String keyword) {
      _log.debug("Found the " + keyword + " section");
      
      String data = line.substring(keyword.length());
      String[] split = data.split(" ");
      int index = Integer.valueOf(split[0]); 
      String value = getLineOfData(data);

      Barrier barrier = getOrCreateBarrier(index);
      barrier.setValue(Integer.valueOf(value));

   }

   private Barrier getOrCreateBarrier(int index) {
      Barrier barrier = _barrierMap.get(Integer.valueOf(index));
      
      if (barrier == null) {
         barrier = new DefaultBarrier();
         barrier.setNumber(index);
         _barrierMap.put(Integer.valueOf(index), barrier);
      }

      return barrier;
   }

}
