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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tdod.ether.ta.items.Item;
import org.tdod.ether.ta.items.armor.Armor;
import org.tdod.ether.ta.items.equipment.Equipment;
import org.tdod.ether.ta.items.weapons.Weapon;
import org.tdod.ether.taimpl.cosmos.enums.Town;
import org.tdod.ether.taimpl.items.armor.DefaultArmor;
import org.tdod.ether.taimpl.items.equipment.DefaultEquipment;
import org.tdod.ether.taimpl.items.equipment.enums.EquipmentSubType;
import org.tdod.ether.taimpl.items.equipment.enums.EquipmentType;
import org.tdod.ether.taimpl.items.weapons.DefaultWeapon;
import org.tdod.ether.taimpl.items.weapons.enums.SpecialWeaponFunction;

public class ItemDataReader extends DataReader {

   private static Log _log = LogFactory.getLog(ItemDataReader.class);

   private static final String ITEM_NAME_KEYWORD   = "INAM";
   private static final String ITEM_DESC_KEYWORD   = "IDES";
   private static final String ITEM_STATS_KEYWORD  = "ISTT";
   private static final String ITEM_EFFECT_KEYWORD = "IEFF";

   private static final int    ALL_CLASSES          = 255;
   private static final int    NO_CLASSES           = 0;
   
   private static final int    LIGHT_ARMOR_CLASSES  = 125;
   private static final int    MEDIUM_ARMOR_CLASSES = 5;
   private static final int    HEAVY_ARMOR_CLASSES  = 1;
   
   private static final int    RANGED_WEAPON        = 64;
   private static final int    SWORD_WEAPON         = 89;
   private static final int    BLUNT_WEAPON         = 53;
   private static final int    HEAVY_WEAPON         = 17;
   
   private static HashMap<Integer, Equipment> _eqMap = new HashMap<Integer, Equipment>();
   private static HashMap<Integer, Weapon>    _weaponMap = new HashMap<Integer, Weapon>();
   private static HashMap<Integer, Armor>     _armorMap = new HashMap<Integer, Armor>();

   private static HashMap<Integer, String>    _nameMap = new HashMap<Integer, String>();
   private static HashMap<Integer, String>    _descMap = new HashMap<Integer, String>();

   public ItemDataReader() {
   }
   
   public void execute() throws Exception {
      String filename = "data/TSGARN-D.MSG";
      _log.info("Reading " + filename);
      readFile(filename);
      _log.info("Read in " + _eqMap.size() + " pieces of equipment.");            
      _log.info("Read in " + _weaponMap.size() + " weapons.");
      _log.info("Read in " + _armorMap.size() + " pieces of armor.");            
   }

   public void exportData(String eqFileName, String weaponFileName, String armorFileName)
   throws FileNotFoundException {
      saveEquipment(eqFileName);
      saveWeapons(weaponFileName);
      saveArmor(armorFileName);
   }

   private void saveEquipment(String filename)
   throws FileNotFoundException {
      _log.info("Exporting to " + filename);
      BufferedWriter out = null;
      try {
         out = new BufferedWriter(new FileWriter(filename));
         
         List<Item> unsortedList = new ArrayList<Item>(_eqMap.values());
         List<Item> sortedList = getSortedList(unsortedList);
         for (int index = 0; index < sortedList.size(); index++) {
            Equipment eq = (Equipment)sortedList.get(index);
            out.write(eq.getVnum() + ":");
            out.write(eq.getName() + ":");
            out.write(eq.getLongDescription() + ":");
            out.write(eq.getCost() + " ");
            out.write(eq.getWeight() + " ");
            out.write(eq.getV2() + " ");
            out.write(eq.getMinEquipmentEffect() + " ");
            out.write(eq.getMaxEquipmentEffect() + " ");
            out.write(eq.getEquipmentType().getIndex() + " ");
            out.write(eq.getV6() + " ");
            out.write(eq.getCharges() + " ");
            out.write(eq.getEquipmentSubType().getIndex() + " ");
            out.write(eq.getRange() + " ");
            out.write(eq.getV10() + " ");
            out.write(eq.getQuestStat() + " ");
            out.write(eq.getV12() + " ");
            out.write(eq.getV13() + " ");
            out.write(eq.getLevel() + " ");
            out.write(eq.getTown().getIndex() + " ");
            out.write(eq.getAllowableClasses() + ":");
            out.write(eq.getMessage());
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
   
   private void saveWeapons(String filename) {
      _log.info("Exporting to " + filename);
      BufferedWriter out = null;
      try {
         out = new BufferedWriter(new FileWriter(filename));
         
         List<Item> unsortedList = new ArrayList<Item>(_weaponMap.values());
         List<Item> sortedList = getSortedList(unsortedList);
         for (int index = 0; index < sortedList.size(); index++) {
            Weapon weapon = (Weapon)sortedList.get(index);
            out.write(weapon.getVnum() + ":");
            out.write(weapon.getName() + ":");
            out.write(weapon.getLongDescription() + ":");
            out.write(weapon.getCost() + " ");
            out.write(weapon.getWeight() + " ");
            out.write(weapon.getRange() + " ");
            out.write(weapon.getMinDamage() + " ");
            out.write(weapon.getMaxDamage() + " ");
            out.write(weapon.getType() + " ");
            out.write(weapon.getAmmoVnum() + " ");
            out.write(weapon.getArmorRating() + " ");
            out.write(weapon.getSpecFunction().getIndex() + " ");
            out.write(weapon.getV9() + " ");
            out.write(weapon.getV10() + " ");
            out.write(weapon.getV11() + " ");
            out.write(weapon.getV12() + " ");
            out.write(weapon.getV13() + " ");
            out.write(weapon.getLevel() + " ");
            out.write(weapon.getTown().getIndex() + " ");
            out.write(weapon.getAllowableClasses() + ":");
            out.write(weapon.getMagicAttackMessage());
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
   
   private void saveArmor(String filename) {
      _log.info("Exporting to " + filename);
      BufferedWriter out = null;
      try {
         out = new BufferedWriter(new FileWriter(filename));
         
         List<Item> unsortedList = new ArrayList<Item>(_armorMap.values());
         List<Item> sortedList = getSortedList(unsortedList);
         for (int index = 0; index < sortedList.size(); index++) {
            Armor armor = (Armor)sortedList.get(index);
            out.write(armor.getVnum() + ":");
            out.write(armor.getName() + ":");
            out.write(armor.getLongDescription() + ":");
            out.write(armor.getCost() + " ");
            out.write(armor.getWeight() + " ");
            out.write(armor.getV2() + " ");
            out.write(armor.getV3() + " ");
            out.write(armor.getV4() + " ");
            out.write(armor.getType() + " ");
            out.write(armor.getArmorRating() + " ");
            out.write(armor.getV7() + " ");
            out.write(armor.getV8() + " ");
            out.write(armor.getV9() + " ");
            out.write(armor.getV10() + " ");
            out.write(armor.getV11() + " ");
            out.write(armor.getV12() + " ");
            out.write(armor.getV13() + " ");            
            out.write(armor.getLevel() + " ");
            out.write(armor.getTown().getIndex() + " ");
            out.write(armor.getAllowableClasses() + "");
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
            if (line.startsWith(ITEM_NAME_KEYWORD)) {
               parseNameLine(line, ITEM_NAME_KEYWORD);
            } else if (line.startsWith(ITEM_DESC_KEYWORD)) {
               parseDescriptionLine(line, ITEM_DESC_KEYWORD);
            } else if (line.startsWith(ITEM_STATS_KEYWORD)) {
               parseStatsLine(line, ITEM_STATS_KEYWORD);
            } else if (line.startsWith(ITEM_EFFECT_KEYWORD)) {
               parseEffectLine(line, ITEM_EFFECT_KEYWORD);
            }
         }
         setAllowableClasses();
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
      int vnum = new Integer(split[0]) - 1;
      String name = getLineOfData(data); 
      
      _nameMap.put(Integer.valueOf(vnum), name);
   }
   
   private void parseDescriptionLine(String line, String keyword) {
      _log.debug("Found the " + keyword + " section");
      
      String data = line.substring(keyword.length());
      String[] split = data.split(" ");
      int vnum = new Integer(split[0]) - 1;
      String desc = getLineOfData(data); 
      
      _descMap.put(Integer.valueOf(vnum), desc);      
   }
   
   private void parseStatsLine(String line, String keyword) {
      _log.debug("Found the " + keyword + " section");
      
      String data = line.substring(keyword.length());
      String[] split = data.split(" ");
      int vnum = new Integer(split[0]) - 1;
      
      String statLine = getLineOfData(data);
      String[] statSplit = statLine.split(" ");
      
      int itemType = Integer.valueOf(statSplit[5]);
      switch (itemType) {
      case 1:
      case 2:
      case 3:
      case 4:
      case 5:
      case 6:
      case 7:
      case 8: 
      case 9:
      case 10:
         handleWeapon(vnum, statSplit);
         break;
      case 11:
      case 12:
      case 13:
      case 14:
      case 15:
      case 16:
         handleArmor(vnum, statSplit);
         break;
      case 21:
      case 22:
      case 23:
      case 24:
      case 25:
      case 31:
      case 32:
      case 33:
      case 41:
         handleEq(vnum, statSplit);
         break;
      default:
         _log.error("Item type " + itemType + " not supported!");
         break;
      }
   }
   
   private void parseEffectLine(String line, String keyword) {
      _log.debug("Found the " + keyword + " section");

      String data = line.substring(keyword.length());
      String message = getLineOfData(data);

      if (message.isEmpty()) {
         return;
      }
      
      String[] split = data.split(" ");
      int vnum = new Integer(split[0]) - 1;
      
      Item item = null;
      
      item = _weaponMap.get(vnum);
      
      if (item == null) {
         item = _eqMap.get(vnum);
      } else {
         // Item is a weapon
        ((Weapon)item).setMagicAttackMessage(message);
        return;
      }

      if (item != null) {
         // Item is an equipment
         ((Equipment)item).setMessage(message);
         return;
      }
      
   }
   
   private void handleWeapon(int vnum, String[] statSplit) {
      Weapon weapon = new DefaultWeapon();
      
      weapon.setVnum(vnum);

      weapon.setCost(new Integer(statSplit[0]));
      weapon.setWeight(new Integer(statSplit[1]));
      weapon.setRange(new Integer(statSplit[2]));
      weapon.setMinDamage(new Integer(statSplit[3]));
      weapon.setMaxDamage(new Integer(statSplit[4]));
      weapon.setType(new Integer(statSplit[5]));
      weapon.setAmmoVnum(new Integer(statSplit[6]));
      weapon.setArmorRating(new Integer(statSplit[7]));
      weapon.setSpecFunction(SpecialWeaponFunction.getSpecialWeaponFunction(new Integer(statSplit[8])));
      weapon.setV9(new Integer(statSplit[9]));
      weapon.setV10(new Integer(statSplit[10]));
      weapon.setV11(new Integer(statSplit[11]));
      weapon.setV12(new Integer(statSplit[12]));
      weapon.setV13(new Integer(statSplit[13]));
      weapon.setLevel(new Integer(statSplit[14]));
      weapon.setTown(Town.getTown(new Integer(statSplit[15])));

      String name = _nameMap.get(vnum);
      String desc = _descMap.get(vnum);

      weapon.setName(name);
      weapon.setLongDescription(desc);
      weapon.setMagicAttackMessage("");
      
      _weaponMap.put(vnum, weapon);
   }
   
   private void handleArmor(int vnum, String[] statSplit) {
      Armor armor = new DefaultArmor();
      
      armor.setVnum(vnum);
      armor.setCost(new Integer(statSplit[0]));
      armor.setWeight(new Integer(statSplit[1]));
      armor.setV2(new Integer(statSplit[2]));
      armor.setV3(new Integer(statSplit[3]));
      armor.setV4(new Integer(statSplit[4]));
      armor.setType(new Integer(statSplit[5]));
      armor.setArmorRating(new Integer(statSplit[6]));
      armor.setV7(new Integer(statSplit[7]));
      armor.setV8(new Integer(statSplit[8]));
      armor.setV9(new Integer(statSplit[9]));
      armor.setV10(new Integer(statSplit[10]));
      armor.setV11(new Integer(statSplit[11]));
      armor.setV12(new Integer(statSplit[12]));
      armor.setV13(new Integer(statSplit[13]));
      armor.setLevel(new Integer(statSplit[14]));
      armor.setTown(Town.getTown(new Integer(statSplit[15])));

      String name = _nameMap.get(vnum);
      String desc = _descMap.get(vnum);

      armor.setName(name);
      armor.setLongDescription(desc);
      
      _armorMap.put(vnum, armor);
   }
   
   private void handleEq(int vnum, String[] statSplit) {
      Equipment equipment = new DefaultEquipment();
      
      equipment.setVnum(vnum);
      equipment.setCost(new Integer(statSplit[0]));
      equipment.setWeight(new Integer(statSplit[1]));
      equipment.setV2(new Integer(statSplit[2]));
      equipment.setMinEquipmentEffect(new Integer(statSplit[3]));
      equipment.setMaxEquipmentEffect(new Integer(statSplit[4]));
      equipment.setEquipmentType(EquipmentType.getEquipmentType(new Integer(statSplit[5])));
      equipment.setV6(new Integer(statSplit[6]));
      equipment.setCharges(new Integer(statSplit[7]));
      equipment.setEquipmentSubType(EquipmentSubType.getEquipmentSubType(new Integer(statSplit[8])));
      equipment.setRange(new Integer(statSplit[9]));
      equipment.setV10(new Integer(statSplit[10]));
      equipment.setQuestStat(new Integer(statSplit[11]));
      equipment.setV12(new Integer(statSplit[12]));
      equipment.setV13(new Integer(statSplit[13]));
      equipment.setLevel(new Integer(statSplit[14]));
      equipment.setTown(Town.getTown(new Integer(statSplit[15])));
      
      String name = _nameMap.get(vnum);
      String desc = _descMap.get(vnum);

      equipment.setName(name);
      equipment.setLongDescription(desc);
      equipment.setMessage("");
      
      _eqMap.put(vnum, equipment);
   }
   
   private List<Item> getSortedList(List<Item> list) {
      Collections.sort(list, new Comparator<Item>() {
         public int compare(Item o1, Item o2) {
            return o1.getVnum() - o2.getVnum();
         }
      });
      return list;
   }
   
   private void setAllowableClasses() {
      List<Weapon> weaponList = new ArrayList<Weapon>(_weaponMap.values());
      for (Weapon weapon:weaponList) {
         switch (weapon.getType()) {
         case 1: weapon.setAllowableClasses(ALL_CLASSES); break;
         case 3: weapon.setAllowableClasses(SWORD_WEAPON); break;
         case 2:
         case 5: weapon.setAllowableClasses(BLUNT_WEAPON); break;
         case 8:
         case 9:
         case 10: weapon.setAllowableClasses(RANGED_WEAPON); break;
         case 4:
         case 6:
         case 7:  weapon.setAllowableClasses(HEAVY_WEAPON); break;
         default: 
            weapon.setAllowableClasses(NO_CLASSES);
            _log.error("Weapon type " + weapon.getType() + " not supported!");
            break;         
         }
      }

      List<Armor> armorList = new ArrayList<Armor>(_armorMap.values());
      for (Armor armor:armorList) {
         switch (armor.getType()) {
         case 11: armor.setAllowableClasses(ALL_CLASSES); break;
         case 12:
         case 13: armor.setAllowableClasses(LIGHT_ARMOR_CLASSES); break;
         case 14: armor.setAllowableClasses(MEDIUM_ARMOR_CLASSES); break;
         case 15: 
         case 16: armor.setAllowableClasses(HEAVY_ARMOR_CLASSES); break;
         default: 
            armor.setAllowableClasses(NO_CLASSES);
            _log.error("Armor type " + armor.getType() + " not supported!");
            break;
         }
      }

      // Wow, what a fucking hack.  This is my best guess as to how allowable classes are determined for equipment.
      List<Equipment> eqList = new ArrayList<Equipment>(_eqMap.values());
      for (Equipment eq:eqList) {
         eq.setAllowableClasses(ALL_CLASSES);
         
         switch (eq.getEquipmentSubType()) {
         case NONE:

            switch (eq.getEquipmentType()) {
            case SUPPLY_WEAPON : 
               if (eq.getRange() == 3) {
                  eq.setAllowableClasses(ALL_CLASSES);
               } else if (eq.getRange() == 2) {
                  eq.setAllowableClasses(25);
               } else if (eq.getRange() == 1) {
                  eq.setAllowableClasses(HEAVY_WEAPON);
               }
            }
            
            break;
         case WAND:
            eq.setAllowableClasses(2);
            break;
         }

      }
   }
   
}
