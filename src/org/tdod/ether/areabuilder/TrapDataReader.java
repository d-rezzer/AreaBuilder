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
import org.tdod.ether.ta.cosmos.Trap;
import org.tdod.ether.taimpl.cosmos.DefaultTrap;
import org.tdod.ether.taimpl.cosmos.enums.TrapType;

public class TrapDataReader extends DataReader {

   private static Log _log = LogFactory.getLog(TrapDataReader.class);

   private static final String TRAP_DESC_KEYWORD       = "XDES";
   private static final String TRAP_STATS_KEYWORD      = "XSTT";

   private static HashMap<Integer, Trap> _trapMap = new HashMap<Integer, Trap>();

   public TrapDataReader() {
   }
   
   public void execute() throws Exception {
      String filename = "data/TSGARN-D.MSG";
      _log.info("Reading " + filename);
      readFile(filename);
      _log.info("Read in " + _trapMap.size() + " traps.");      
   }

   public void exportData(String fileName) throws FileNotFoundException {
      _log.info("Exporting to " + fileName);
      BufferedWriter out = null;
      try {
         out = new BufferedWriter(new FileWriter(fileName));
         
         Set<Integer> set = _trapMap.keySet();
         for (Integer key:set) {
            Trap trap = _trapMap.get(key);
            out.write(trap.getVnum()  + ":");
            out.write(trap.getMessage() + ":");
            out.write(trap.getMinDamage() + " ");
            out.write(trap.getMaxDamage() + " ");
            out.write(trap.getTrapType().getNumber() + "");            
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
            if (line.startsWith(TRAP_DESC_KEYWORD)) {
               parseKeywordLine(line, TRAP_DESC_KEYWORD);
            } else if (line.startsWith(TRAP_STATS_KEYWORD)) {
               parseStatsLine(line, TRAP_STATS_KEYWORD);
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

   private void parseKeywordLine(String line, String keyword) {
      _log.debug("Found the " + keyword + " section");
      
      String data = line.substring(keyword.length());
      String[] split = data.split(" ");
      int index = new Integer(split[0]);
      Trap trap = getOrCreateTrap(index);
      
      trap.setMessage(getLineOfData(data));
      
   }
   
   private void parseStatsLine(String line, String keyword) {
      _log.debug("Found the " + keyword + " section");

      String data = line.substring(keyword.length());
      String[] split = data.split(" ");
      int index = new Integer(split[0]);
      String statLine = getLineOfData(data);
      String[] statSplit = statLine.split(" ");
      
      Trap trap = getOrCreateTrap(index);
      trap.setMinDamage(Integer.valueOf(statSplit[0]));
      trap.setMaxDamage(Integer.valueOf(statSplit[1]));
      trap.setTrapType(TrapType.getTrapType(Integer.valueOf(statSplit[2])));
   }
   
   private Trap getOrCreateTrap(int index) {
      Trap trap = _trapMap.get(Integer.valueOf(index));         

      if (trap == null) {
         trap = new DefaultTrap();
         trap.setVnum(index - 1);
         _trapMap.put(Integer.valueOf(index), trap);
      }
      
      return trap;
   }

}
