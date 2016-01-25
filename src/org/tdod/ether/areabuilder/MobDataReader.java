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
import org.tdod.ether.ta.mobs.Mob;
import org.tdod.ether.taimpl.mobs.DefaultMob;

public class MobDataReader extends DataReader {

   private static Log _log = LogFactory.getLog(MobDataReader.class);

   private static final String MOB_NAME_KEYWORD   = "MNAM";
   private static final String MOB_DESC_KEYWORD   = "MDES";
   private static final String MOB_STATS_KEYWORD  = "MSTT";
   private static final String MOB_PLURAL_KEYWORD = "MPLU";
   private static final String MOB_WEAPON_KEYWORD = "MWEP";
   private static final String MOB_ATT1_KEYWORD   = "MAT1";
   private static final String MOB_ATT2_KEYWORD   = "MAT2";

   private static HashMap<Integer, Mob> _mobMap = new HashMap<Integer, Mob>();
   private static HashMap<Integer, String> _mobStatsMap = new HashMap<Integer, String>();

   public MobDataReader() {
   }

   public void execute() throws Exception {
      String filename = "data/TSGARN-D.MSG";
      _log.info("Reading " + filename);
      readFile(filename);
      _log.info("Read in " + _mobMap.size() + " mobs.");
   }

   public void exportData(String filename) throws FileNotFoundException {
      _log.info("Exporting to " + filename);
      BufferedWriter out = null;
      try {
         out = new BufferedWriter(new FileWriter(filename));
         
         Set<Integer> keySet = _mobMap.keySet();
         List<Integer> keyList = new ArrayList<Integer>(keySet);
         Collections.sort(keyList);
         for (Integer key:keyList) {
            Mob mob = _mobMap.get(key);
            out.write(mob.getVnum() + ":");
            out.write(mob.getName() + ":");
            out.write(mob.getDescription() + ":");
            out.write(_mobStatsMap.get(key) + ":");
            out.write(mob.getPlural() + ":");
            out.write(mob.getGeneralAttack().getWeapon() + ":");
            out.write(mob.getSpecialAttack().getSpecialAttackDescription() + ":");
            out.write(mob.getSpecialAbility().getSpecialAbilityDescription() + "");
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
            if (line.startsWith(MOB_NAME_KEYWORD)) {
               parseNameLine(line, MOB_NAME_KEYWORD);
            } else if (line.startsWith(MOB_DESC_KEYWORD)) {
               parseDescLine(bufferedReader, line, MOB_DESC_KEYWORD);
            } else if (line.startsWith(MOB_STATS_KEYWORD)) {
               parseStatsLine(line, MOB_STATS_KEYWORD);
            } else if (line.startsWith(MOB_PLURAL_KEYWORD)) {
               parsePluralLine(line, MOB_PLURAL_KEYWORD);
            } else if (line.startsWith(MOB_WEAPON_KEYWORD)) {
               parseWeaponLine(line, MOB_WEAPON_KEYWORD);
            } else if (line.startsWith(MOB_ATT1_KEYWORD)) {
               parseAtt1Line(line, MOB_ATT1_KEYWORD);
            } else if (line.startsWith(MOB_ATT2_KEYWORD)) {
               parseAtt2Line(line, MOB_ATT2_KEYWORD);
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
      int index = Integer.valueOf(split[0]); 
      String name = getLineOfData(data);
      Mob mob = getOrCreateMob(index);
      mob.setName(name);
   }
   
   private void parseDescLine(BufferedReader bufferedReader, String line, String keyword) 
   throws IOException {
      _log.debug("Found the " + keyword + " section");
     
      String data = line.substring(keyword.length());
      String[] split = data.split(" ");
      int index = new Integer(split[0]);

      StringBuffer buffer = new StringBuffer();
      buffer.append(getDescriptionData(data));
      
      while (!line.endsWith(new String("}"))) {
         line = bufferedReader.readLine();
         // buffer.append("\\n" + line);
         buffer.append(" " + line);
      }

      // Strip out the ending }
      String tmpStr1 = buffer.substring(0, buffer.length() - 1);
      // Replace %s with {0}
      String tmpStr2 = getModifiedStringVariableToken(tmpStr1);
      // Replace ' with ''
      // String tmpStr3 = replaceQuotes(tmpStr2);
      
      Mob mob = getOrCreateMob(index);
      mob.setDescription(tmpStr2);
   }

   private void parseStatsLine(String line, String keyword) {
      _log.debug("Found the " + keyword + " section");

      String data = line.substring(keyword.length());
      String[] split = data.split(" ");
      int index = Integer.valueOf(split[0]); 
      String stats = getLineOfData(data);
      _mobStatsMap.put(Integer.valueOf(index), stats);
   }

   private void parsePluralLine(String line, String keyword) {
      _log.debug("Found the " + keyword + " section");
      
      String data = line.substring(keyword.length());
      String[] split = data.split(" ");
      int index = Integer.valueOf(split[0]); 
      String plural = getLineOfData(data);
      Mob mob = getOrCreateMob(index);
      mob.setPlural(plural);
      
   }

   private void parseWeaponLine(String line, String keyword) {
      _log.debug("Found the " + keyword + " section");

      String data = line.substring(keyword.length());
      String[] split = data.split(" ");
      int index = Integer.valueOf(split[0]); 
      String weapon = getLineOfData(data);
      Mob mob = getOrCreateMob(index);
      mob.getGeneralAttack().setWeapon(weapon);
   }

   private void parseAtt1Line(String line, String keyword) {
      _log.debug("Found the " + keyword + " section");

      String data = line.substring(keyword.length());
      String[] split = data.split(" ");
      int index = Integer.valueOf(split[0]); 
      String att1 = getLineOfData(data);
      Mob mob = getOrCreateMob(index);
      mob.getSpecialAttack().setSpecialAttackDescription(att1);
   }

   private void parseAtt2Line(String line, String keyword) {
      _log.debug("Found the " + keyword + " section");

      String data = line.substring(keyword.length());
      String[] split = data.split(" ");
      int index = Integer.valueOf(split[0]); 
      String att2 = getLineOfData(data);
      Mob mob = getOrCreateMob(index);
      mob.getSpecialAbility().setSpecialAbilityDescription(att2);
}

   private Mob getOrCreateMob(int index) {
      Mob mob = _mobMap.get(Integer.valueOf(index));         

      if (mob == null) {
         mob = new DefaultMob();
         mob.setVnum(getVnum(index));
         _mobMap.put(Integer.valueOf(index), mob);
      }
    
      return mob;
   }

   private int getVnum(int index) {
      return index - 1;
   }
}
