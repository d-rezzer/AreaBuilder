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
import org.tdod.ether.ta.mobs.Mob;
import org.tdod.ether.taimpl.mobs.DefaultMob;

public class NpcDataReader extends DataReader {

   private static Log _log = LogFactory.getLog(NpcDataReader.class);

   private static final String NPC_COUNT_KEYWORD       = "T2TOT";
   private static final String NPC_VALUE_KEYWORD       = "SMTY";
   private static final String NPC_NAME_KEYWORD        = "SNAM";
   private static final String NPC_PLURAL_KEYWORD      = "SPLU";
   private static final String NPC_DESCRIPTION_KEYWORD = "SDES";

   private static HashMap<Integer, Mob> _npcMap = new HashMap<Integer, Mob>();

   public NpcDataReader() {
   }
   
   public void execute() throws Exception {
      String filename = "data/TSGARN-T.MSG";
      _log.info("Reading " + filename);
      readFile(filename);
      _log.info("Read in " + _npcMap.size() + " npcs.");      
   }

   public void exportData(String fileName) throws FileNotFoundException {
      _log.info("Exporting to " + fileName);
      BufferedWriter out = null;
      try {
         out = new BufferedWriter(new FileWriter(fileName));
         
         Set<Integer> set = _npcMap.keySet();
         for (Integer key:set) {
            Mob mob = _npcMap.get(key);
            out.write(mob.getVnum() - 1 + ":");
            out.write(mob.getName() + ":");
            out.write(mob.getPlural() + ":");
            out.write(mob.getDescription());
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
            if (line.startsWith(NPC_COUNT_KEYWORD)) {
            } else if (line.startsWith(NPC_VALUE_KEYWORD)) {
               parseNpcValueLine(line, NPC_VALUE_KEYWORD);
            } else if (line.startsWith(NPC_NAME_KEYWORD)) {
               parseNpcNameLine(line, NPC_NAME_KEYWORD);
            } else if (line.startsWith(NPC_PLURAL_KEYWORD)) {
               parseNpcPluralLine(line, NPC_PLURAL_KEYWORD);
            } else if (line.startsWith(NPC_DESCRIPTION_KEYWORD)) {
               parseNpcDescriptionLine(bufferedReader, line, NPC_DESCRIPTION_KEYWORD);
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

   private void parseNpcValueLine(String line, String keyword) {
      _log.debug("Found the " + keyword + " section");
      
      String data = line.substring(keyword.length());
      String[] split = data.split(" ");
      int vnum = new Integer(split[0]);

      getOrCreateNpc(vnum);
   }
   
   private void parseNpcNameLine(String line, String keyword) {
      _log.debug("Found the " + keyword + " section");

      String data = line.substring(keyword.length());
      String[] split = data.split(" ");
      int vnum = new Integer(split[0]);
      Mob mob = getOrCreateNpc(vnum);
      
      mob.setName(getLineOfData(data));
   }
   
   private void parseNpcPluralLine(String line, String keyword) {
      _log.debug("Found the " + keyword + " section");
      
      String data = line.substring(keyword.length());
      String[] split = data.split(" ");
      int vnum = new Integer(split[0]);
      Mob mob = getOrCreateNpc(vnum);
      
      mob.setPlural(getLineOfData(data));
   }

   private void parseNpcDescriptionLine(BufferedReader bufferedReader, String line, String keyword)
   throws IOException {
      _log.debug("Found the " + keyword + " section");

      String data = line.substring(keyword.length());
      String[] split = data.split(" ");
      int vnum = new Integer(split[0]);
      Mob mob = getOrCreateNpc(vnum);

      StringBuffer buffer = new StringBuffer();
      buffer.append(getDescriptionData(data));

      while (!line.equals(new String("}"))) {
         line = bufferedReader.readLine();
         if (!line.equals(new String("}"))) {
            buffer.append(" " + line);            
         }
      }
      
      mob.setDescription(buffer.toString());
   }

   private Mob getOrCreateNpc(int vnum) {
      Mob mob = _npcMap.get(Integer.valueOf(vnum));         

      if (mob == null) {
         mob = new DefaultMob();
         mob.setVnum(vnum);
         _npcMap.put(Integer.valueOf(vnum), mob);
      }
      
      return mob;
   }

}
