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
import org.tdod.ether.ta.spells.Spell;
import org.tdod.ether.taimpl.spells.DefaultSpell;
import org.tdod.ether.taimpl.spells.enums.CureCondition;
import org.tdod.ether.taimpl.spells.enums.ManaEffect;
import org.tdod.ether.taimpl.spells.enums.MiscTargetEffect;
import org.tdod.ether.taimpl.spells.enums.MiscTargetEffect2;
import org.tdod.ether.taimpl.spells.enums.PoisonTarget;
import org.tdod.ether.taimpl.spells.enums.SpellTarget;
import org.tdod.ether.taimpl.spells.enums.SpellType;
import org.tdod.ether.taimpl.spells.enums.StatModifier;

public class SpellDataReader extends DataReader {

   private static Log _log = LogFactory.getLog(SpellDataReader.class);

   private static HashMap<Integer, Spell> _spellMap = new HashMap<Integer, Spell>();
   
   private static final String SPELL_NAME_KEYWORD = "SNAM";
   private static final String SPELL_DESC_KEYWORD = "SDES";
   private static final String SPELL_STAT_KEYWORD = "SSTT";

   public SpellDataReader() {
   }

   public void execute() throws Exception {
      String filename = "data/TSGARN-D.MSG";
      _log.info("Reading " + filename);
      readFile(filename);
      _log.info("Read in " + _spellMap.size() + " spells.");
   }

   public void exportData(String fileName) throws FileNotFoundException {
      _log.info("Exporting to " + fileName);
      BufferedWriter out = null;
      try {
         out = new BufferedWriter(new FileWriter(fileName));
         
         Set<Integer> set = _spellMap.keySet();
         for (Integer key:set) {
            Spell spell = _spellMap.get(key);
            out.write(spell.getName() + ":");
            out.write(spell.getMessage() + ":");
            
            out.write(spell.getSpellType().getIndex() + " ");
            out.write(spell.getMana() + " ");
            out.write(spell.getMinSpellEffect() + " ");
            out.write(spell.getMaxSpellEffect() + " ");
            out.write((spell.scalesWithLevel() ? 1 : 0) + " ");
            out.write(spell.getCost() + " ");
            out.write(spell.getSpellTarget().getIndex() + " ");
            out.write(spell.getArmorModifier() + " ");
            out.write(spell.getCureCondition().getIndex() + " ");
            out.write(spell.getPoisonTarget().getIndex() + " ");
            out.write(spell.getManaEffect().getIndex() + " ");
            out.write(spell.getStatPenalty().getIndex() + " ");
            out.write(spell.getStatBonus().getIndex() + " ");
            out.write(spell.getMiscTargetEffect().getIndex() + " ");
            out.write(spell.getMiscTargetEffect2().getIndex() + "");

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
            if (line.startsWith(SPELL_NAME_KEYWORD)) {
               parseNameLine(line, SPELL_NAME_KEYWORD);
            } else if (line.startsWith(SPELL_DESC_KEYWORD)) {
               parseDescriptionLine(line, SPELL_DESC_KEYWORD);
            } else if (line.startsWith(SPELL_STAT_KEYWORD)) {
               parseStatLine(line, SPELL_STAT_KEYWORD);
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
      Spell spell = getOrCreateSpell(index);
      spell.setName(name);
   }

   private void parseDescriptionLine(String line, String keyword) {
      _log.debug("Found the " + keyword + " section");

      String data = line.substring(keyword.length());
      String[] split = data.split(" ");
      int index = Integer.valueOf(split[0]); 

      String message = getLineOfData(data);
      Spell spell = getOrCreateSpell(index);
      spell.setMessage(message);
   }

   private void parseStatLine(String line, String keyword) {
      _log.debug("Found the " + keyword + " section");

      String data = line.substring(keyword.length());
      String[] split = data.split(" ");
      int index = Integer.valueOf(split[0]); 
      
      String statLine = getLineOfData(data);
      String[] statSplit = statLine.split(" ");
      
      Spell spell = getOrCreateSpell(index);
      
      spell.setSpellType(SpellType.getSpellType(Integer.valueOf(statSplit[0])));
      spell.setMana(Integer.valueOf(statSplit[1]));
      spell.setMinSpellEffect(Integer.valueOf(statSplit[2]));
      spell.setMaxSpellEffect(Integer.valueOf(statSplit[3]));
      spell.setScalesWithLevel(Integer.valueOf(statSplit[4]));
      spell.setCost(Integer.valueOf(statSplit[5]));
      spell.setSpellTarget(SpellTarget.getSpellTarget(Integer.valueOf(statSplit[6])));
      spell.setArmorModifier(Integer.valueOf(statSplit[7]));
      spell.setCureCondition(CureCondition.getCureCondition(Integer.valueOf(statSplit[8])));
      spell.setPoisonTarget(PoisonTarget.getPoisonTarget(Integer.valueOf(statSplit[9])));
      spell.setManaEffect(ManaEffect.getManaEffect(Integer.valueOf(statSplit[10])));
      spell.setStatPenalty(StatModifier.getStatModifier(Integer.valueOf(statSplit[11])));
      spell.setStatBonus(StatModifier.getStatModifier(Integer.valueOf(statSplit[12])));
      spell.setMiscTargetEffect(MiscTargetEffect.getMiscTargetEffect(Integer.valueOf(statSplit[13])));
      spell.setMiscTargetEffect2(MiscTargetEffect2.getMiscTargetEffect2(Integer.valueOf(statSplit[14])));
   }

   private Spell getOrCreateSpell(int index) {
      Spell spell = _spellMap.get(Integer.valueOf(index));
      
      if (spell == null) {
         spell = new DefaultSpell();
         _spellMap.put(Integer.valueOf(index), spell);
      }

      return spell;
   }

}
