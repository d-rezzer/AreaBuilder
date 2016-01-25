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
import org.tdod.ether.ta.cosmos.Treasure;
import org.tdod.ether.taimpl.cosmos.DefaultTreasure;

public class TreasureDataReader extends DataReader {

   private static Log _log = LogFactory.getLog(TreasureDataReader.class);

   private static final String TREASURE_DESC_KEYWORD       = "TDES";
   private static final String TREASURE_STATS_KEYWORD      = "TSTT";

   private static HashMap<Integer, Treasure> _treasureMap = new HashMap<Integer, Treasure>();

   public TreasureDataReader() {
   }

   public void execute() throws Exception {
      String filename = "data/TSGARN-D.MSG";
      _log.info("Reading " + filename);
      readFile(filename);
      _log.info("Read in " + _treasureMap.size() + " treasures.");      
   }

   public void exportData(String fileName) throws FileNotFoundException {
      _log.info("Exporting to " + fileName);
      BufferedWriter out = null;
      try {
         out = new BufferedWriter(new FileWriter(fileName));
         
         Set<Integer> set = _treasureMap.keySet();
         for (Integer key:set) {
            Treasure treasure = _treasureMap.get(key);
            out.write(treasure.getVnum()  + ":");
            out.write(treasure.getMessage() + ":");
            out.write(treasure.getMinValue() + " ");
            out.write(treasure.getMaxValue() + "");
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
            if (line.startsWith(TREASURE_DESC_KEYWORD)) {
               parseDescLine(line, TREASURE_DESC_KEYWORD);
            } else if (line.startsWith(TREASURE_STATS_KEYWORD)) {
               parseStatsLine(line, TREASURE_STATS_KEYWORD);
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

   private void parseDescLine(String line, String keyword) {
      _log.debug("Found the " + keyword + " section");
      
      String data = line.substring(keyword.length());
      String[] split = data.split(" ");
      int index = new Integer(split[0]);
      Treasure treasure = getOrCreateTreasure(index);
      
      String tmpStr1 = getLineOfData(data);
      String tmpStr2 = getModifiedStringVariableToken(tmpStr1);
      treasure.setMessage(tmpStr2);
   }
   
   private void parseStatsLine(String line, String keyword) {
      _log.debug("Found the " + keyword + " section");
      
      String data = line.substring(keyword.length());
      String[] split = data.split(" ");
      int index = new Integer(split[0]);
      String statLine = getLineOfData(data);
      String[] statSplit = statLine.split(" ");

      Treasure treasure = getOrCreateTreasure(index);
      treasure.setMinValue(Integer.valueOf(statSplit[0]));
      treasure.setMaxValue(Integer.valueOf(statSplit[1]));

   }
   
   private Treasure getOrCreateTreasure(int index) {
      Treasure treasure = _treasureMap.get(Integer.valueOf(index));         

      if (treasure == null) {
         treasure = new DefaultTreasure();
         treasure.setVnum(index);
         _treasureMap.put(Integer.valueOf(index), treasure);
      }
      
      return treasure;
   }

}
