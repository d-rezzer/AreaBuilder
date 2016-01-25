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

import java.io.FileNotFoundException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AreaBuilder {

   private static Log _log = LogFactory.getLog(AreaBuilder.class);

   public AreaBuilder() {
      TownDataReader townDataReader = new TownDataReader();
      TownDescriptionReader townDescriptionReader = new TownDescriptionReader();
      WorldDescriptionReader worldDescriptionReader = new WorldDescriptionReader();
      NumericWorldDataReader numericWorldDataReader = new NumericWorldDataReader();
      NpcDataReader npcDataReader = new NpcDataReader();
      EmoteDataReader emoteDataReader = new EmoteDataReader();
      ItemDataReader itemDataReader = new ItemDataReader();
      SpellDataReader spellDataReader = new SpellDataReader();
      TeleporterDataReader teleporterDataReader = new TeleporterDataReader();
      BarrierDataReader barrierDataReader = new BarrierDataReader();
      HelpDataReader helpDataReader = new HelpDataReader();
      MobWeaponDataReader mobWeaponDataReader = new MobWeaponDataReader();
      MobDataReader mobDataReader = new MobDataReader();
      TrapDataReader trapDataReader = new TrapDataReader();
      TreasureDataReader treasureDataReader = new TreasureDataReader();
      MessageDataReader messageDataReader = new MessageDataReader();
      try {
         townDataReader.execute();
         townDescriptionReader.execute();
         worldDescriptionReader.execute();
         numericWorldDataReader.setTownArea(townDataReader.getTownArea());
         numericWorldDataReader.execute();
         npcDataReader.execute();
         emoteDataReader.execute();
         itemDataReader.execute();
         spellDataReader.execute();
         teleporterDataReader.execute();
         barrierDataReader.execute();
         helpDataReader.setEmoteDataReader(emoteDataReader);
         helpDataReader.execute();
         mobWeaponDataReader.execute();
         mobDataReader.execute();
         trapDataReader.execute();
         treasureDataReader.execute();
         messageDataReader.execute();
      } catch (Exception e) {
         _log.error("Error while reading data.") ;
         e.printStackTrace();
      }
      
      try {
         townDataReader.exportXml("build/town.xml");
         townDescriptionReader.exportXml("build/town_room_desc.xml");
         worldDescriptionReader.exportRoomDescriptionXml("build/world_room_desc.xml");
         worldDescriptionReader.exportCommandTriggerXml("build/cmd_triggers.xml");
         numericWorldDataReader.exportXml("build/world1.xml");
         npcDataReader.exportData("build/npcs.dat");
         emoteDataReader.exportData("build/emotes.dat");
         itemDataReader.exportData("build/equipment.dat", "build/weapons.dat", "build/armor.dat");
         spellDataReader.exportData("build/spells.dat");
         teleporterDataReader.exportData("build/teleporters.dat");
         barrierDataReader.exportData("build/barriers.dat");
         helpDataReader.exportData("build/help.dat");
         mobWeaponDataReader.exportData("build/mob_weapons.dat");
         mobDataReader.exportData("build/mobs.dat");
         trapDataReader.exportData("build/traps.dat");
         treasureDataReader.exportData("build/treasures.dat");
         messageDataReader.exportData("build/tamessages.properties");
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      }
      _log.info("Done.");
   }
   
   public static void main (String [] args) throws Exception {
      new AreaBuilder() ;
   }

}
