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
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tdod.ether.ta.cosmos.Emote;

/**
 * TODO Eventually, I'd like to save the help data in an XML file instead of the proprietary one I created.
 */
public class HelpDataReader extends DataReader {

   private static Log _log = LogFactory.getLog(HelpDataReader.class);

   private static HashMap<String, String> _helpKeywordMap = new HashMap<String, String>();
   private static HashMap<String, String> _helpMap = new HashMap<String, String>();
   
   private EmoteDataReader emoteDataReader;

   public HelpDataReader() {
      // Lower case help entries are viewable by the player.
      // Any capital cases in the help entries makes that entry only obtainable through the code.
      _helpKeywordMap.put("GENHLP", "default");
      _helpKeywordMap.put("ARNHLP", "arena");
      _helpKeywordMap.put("BNKHLP", "vaults");
      _helpKeywordMap.put("DOCHLP", "docks");
      _helpKeywordMap.put("GLDHLP", "guild");
      _helpKeywordMap.put("SHPHLP", "shops");
      _helpKeywordMap.put("TAVHLP", "tavern");
      _helpKeywordMap.put("TMPHLP", "temple");
      _helpKeywordMap.put("ARCHLP", "archers");
      _helpKeywordMap.put("HUNHLP", "hunters");
      _helpKeywordMap.put("ROGHLP", "rogues");
      _helpKeywordMap.put("COMHLP", "combat");
      _helpKeywordMap.put("GLSHLP", "glossary");
      _helpKeywordMap.put("GRPHLP", "groups");
      _helpKeywordMap.put("INFHLP", "info");
      _helpKeywordMap.put("ITMHLP", "items");
      _helpKeywordMap.put("MOVHLP", "movement");
      _helpKeywordMap.put("ORDHLP", "orders");
      _helpKeywordMap.put("SPLHLP", "spells");
      _helpKeywordMap.put("MSCHLP", "misc");
      _helpKeywordMap.put("EXPHLP1", "exp1");
      _helpKeywordMap.put("EXPHLP2", "exp2");
      _helpKeywordMap.put("ARNINT1", "general1");
      _helpKeywordMap.put("ARNINT2", "general2");
      _helpKeywordMap.put("ARNINT3", "general3");
      _helpKeywordMap.put("actions", "actions");
      _helpKeywordMap.put("general4", "general4");
      
      _helpKeywordMap.put("ENTRTA", "ENTRTA");
      _helpKeywordMap.put("EXITTA", "EXITTA");
      _helpKeywordMap.put("INVTOP", "INVTOP");

   }

   public void execute() throws Exception {
      String filename = "data/TSGARN-C.MSG";
      _log.info("Reading " + filename);
      readFile(filename);
      _log.info("Read in " + _helpMap.size() + " help entries.");      
   }

   public void exportData(String fileName) throws FileNotFoundException {
      _log.info("Exporting to " + fileName);
      BufferedWriter out = null;
      try {
         out = new BufferedWriter(new FileWriter(fileName));
         
         List<String> list = new ArrayList<String>(_helpKeywordMap.values());
         Collections.sort(list);

         for (String key:list) {
            out.write(key + "~");
            out.write(_helpMap.get(key) + "~\n");
         }
         
         out.write("#$");
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

   public void setEmoteDataReader(EmoteDataReader emoteDataReader) {
      this.emoteDataReader = emoteDataReader;
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
            // if (line.contains(HELP1_KEYWORD) || line.contains(HELP2_KEYWORD)) {
            if (line.contains("{")) {
               parseHelpData(bufferedReader, line);
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
      insertCredits();
      insertEmotes();
   }

   private void parseHelpData(BufferedReader bufferedReader, String line)
   throws IOException {
      int index = line.indexOf(" ");
      if (index == -1) {
         return;
      }
      String data = line.substring(index);
      String[] split = line.split(" ");
      String keyword = split[0];

      _log.debug("Found the " + keyword + " section");

if ("INVTOP".equals(keyword)) {
   System.out.println();
} 
// System.out.println(keyword);      
      StringBuffer buffer = new StringBuffer();
      buffer.append("\n" + getDescriptionData(data));
      
      while (!line.contains(new String("}"))) {
         line = bufferedReader.readLine();
         if (!line.contains(new String("}"))) {
            buffer.append("\n" + line);            
         } else {
            break;
         }
      }

      
      String helpToken = _helpKeywordMap.get(keyword);
      _helpMap.put(helpToken, fixString(buffer.toString()) + "\n");
      
   }

   private void insertCredits() {
      String credits =
         "\n\n" +
         "The following is a list of people who contributed to the production of\n" +
         "Tele-Arena version 5.6b:\n" +
         "\n" +
         "  Sean Ferrell:       Author/Programmer\n" +
         "\n" +
         "  Jim Sweeny,\n" +
         "  Elizabeth Casino:   Design Assitants/Playtesters\n" +
         "\n" +
         "\n" +
         "  For helpful information about the game and some possible commands for the\n" +
         "different areas in the arena type 'HELP' or '?' from anywhere in the game.\n\n";
      
      _helpMap.put(_helpKeywordMap.get("general4"), credits);
   }

   private void insertEmotes() {
      if (emoteDataReader == null) {
         _log.error("EmoteDataReader is null!");
         return;
      }
      
      List<Emote> emoteList = emoteDataReader.getSortedList();
      int count = 1;
      int maxColumns = 5;
      StringBuffer tempStringBuffer = new StringBuffer();
      StringBuffer buffer = new StringBuffer();
      buffer.append("\n\n");
      for (int index = 0; index < 27; index++) {
         buffer.append("\u0020");
      }
      buffer.append("Action List\n");
      buffer.append("------------------------------------------------------------------\n");
      for (Emote emote:emoteList) {
         if (count++ == maxColumns) {
            tempStringBuffer.append(emote.getKeyword());
            buffer.append(tempStringBuffer + "\n");
            count = 1;
            tempStringBuffer = new StringBuffer();
         } else {
            Formatter formatter = new Formatter(tempStringBuffer, Locale.US);
            formatter.format("%-15s", emote.getKeyword());
         }
      }
      _helpMap.put(_helpKeywordMap.get("actions"), buffer.toString() + "\n");
   }
   
   private String fixString(String data) {
      StringBuffer buffer = new StringBuffer();
      int subCount = 0;
      
      for (int strIndex = 0; strIndex < data.length(); strIndex++) {
         if (data.charAt(strIndex) == '&') {
            buffer.append("&&");
            strIndex = strIndex + 1;
            subCount++;
         }
         
         buffer.append(data.charAt(strIndex));
      }
      
      return buffer.toString();      
   }
}
