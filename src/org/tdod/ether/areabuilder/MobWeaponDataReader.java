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
import org.tdod.ether.ta.mobs.MobWeapon;
import org.tdod.ether.ta.mobs.enums.MobWeaponType;
import org.tdod.ether.taimpl.mobs.DefaultMobWeapon;

public class MobWeaponDataReader extends DataReader {

   private static Log _log = LogFactory.getLog(MobWeaponDataReader.class);

   private static final String MOB_WEAPON_NAME_KEYWORD   = "WNAM";
   private static final String MOB_WEAPON_STATS_KEYWORD  = "WSTT";

   private static HashMap<Integer, MobWeapon> _mobWeaponMap = new HashMap<Integer, MobWeapon>();
   
   public MobWeaponDataReader() {
   }

   public void execute() throws Exception {
      String filename = "data/TSGARN-D.MSG";
      _log.info("Reading " + filename);
      readFile(filename);
      _log.info("Read in " + _mobWeaponMap.size() + " mob weapons.");
   }

   public void exportData(String filename) throws FileNotFoundException {
      _log.info("Exporting to " + filename);
      BufferedWriter out = null;
      try {
         out = new BufferedWriter(new FileWriter(filename));
         
         Set<Integer> set = _mobWeaponMap.keySet();
         for (Integer key:set) {
            MobWeapon mobWeapon = _mobWeaponMap.get(key);
            out.write(mobWeapon.getName() + ":");
            out.write(mobWeapon.getMobWeaponType().getIndex() + " ");
            out.write(mobWeapon.getV1() + " ");
            out.write(mobWeapon.getMinDamage() + " ");
            out.write(mobWeapon.getMaxDamage() + "");
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
            if (line.startsWith(MOB_WEAPON_NAME_KEYWORD)) {
               parseNameLine(line, MOB_WEAPON_NAME_KEYWORD);
            } else if (line.startsWith(MOB_WEAPON_STATS_KEYWORD)) {
               parseWeaponStatsLine(line, MOB_WEAPON_STATS_KEYWORD);
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
      MobWeapon mobWeapon = getOrCreateMobWeapon(index);
      mobWeapon.setName(name);
   }
   
   private void parseWeaponStatsLine(String line, String keyword) {
      _log.debug("Found the " + keyword + " section");
      String data = line.substring(keyword.length());
      String[] split = data.split(" ");
      String statLine = getLineOfData(data);
      String[] statSplit = statLine.split(" ");

      int index = Integer.valueOf(split[0]); 
      MobWeapon mobWeapon = getOrCreateMobWeapon(index);
      mobWeapon.setMobWeaponType(MobWeaponType.getMobWeaponType(Integer.valueOf(statSplit[0])));
      mobWeapon.setV1(Integer.valueOf(statSplit[1]));
      mobWeapon.setMinDamage(Integer.valueOf(statSplit[2]));
      mobWeapon.setMaxDamage(Integer.valueOf(statSplit[3]));
   }
   
   private MobWeapon getOrCreateMobWeapon(int index) {
      MobWeapon mobWeapon = _mobWeaponMap.get(Integer.valueOf(index));         

      if (mobWeapon == null) {
         mobWeapon = new DefaultMobWeapon();
         _mobWeaponMap.put(Integer.valueOf(index), mobWeapon);
      }
    
      return mobWeapon;
   }

}
