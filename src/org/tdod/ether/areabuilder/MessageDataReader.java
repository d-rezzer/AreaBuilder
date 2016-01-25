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
import org.tdod.ether.util.InvalidFileException;

public class MessageDataReader extends DataReader {

   private static Log _log = LogFactory.getLog(MessageDataReader.class);

   private static HashMap<String, String> _messageMap = new HashMap<String, String>();

   public MessageDataReader() {
   }
 
   public void execute() throws Exception {
      String filename = "data/TSGARN-M.MSG";
      _log.info("Reading " + filename);
      readFile(filename);
      _log.info("Read in " + _messageMap.size() + " messages.");
   }

   public void exportData(String fileName) throws FileNotFoundException {
      _log.info("Exporting to " + fileName);
      BufferedWriter out = null;
      try {
         out = new BufferedWriter(new FileWriter(fileName));
         
         Set<String> set = _messageMap.keySet();
         for (String key:set) {
            String message = _messageMap.get(key);
            out.write(key + "=" + message);
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
            if (line.trim().startsWith("/*")) {
            } else if (line.length() == 0) {
            } else if (line.contains("{")) {
               getData(bufferedReader, line);
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

   private void getData(BufferedReader bufferedReader, String line) throws IOException, InvalidFileException {
      StringBuffer buffer = new StringBuffer();

      int index = line.indexOf("{");
      String key = line.substring(0, index);
      String subString = line.substring(index + 1, line.length());
      buffer.append(subString);
      
      while (!buffer.toString().contains(new String("}"))) {
         line = bufferedReader.readLine();
         buffer.append("\\n\\" + "\n" + line);
      }
      
      // Remove the last }
      String tmpMsg1 = buffer.substring(0, buffer.toString().indexOf("}"));
      // Convert %s to {0}
      String tmpMsg2 = getModifiedStringVariableToken(tmpMsg1);
      // If the string contains {0}, then replace any ' with ''. MessageFormat gets confused if there are single '.
      if (tmpMsg2.contains("{0}")) {
         tmpMsg2 = replaceQuotes(tmpMsg2);
      }
      
      if (tmpMsg2.startsWith(" ")) {
         tmpMsg2 = replaceInitialSpacing(tmpMsg2);
      }
      
      _messageMap.put(key.trim(), tmpMsg2);
   }

   private String replaceInitialSpacing(String str) {
      StringBuffer buffer = new StringBuffer();

      boolean isInitialSpacing = true;
      for (int index = 0; index < str.length(); index++) {
         if (str.charAt(index) == ' ' && isInitialSpacing) {
            buffer.append("\\u0020");            
         } else {
            isInitialSpacing = false;
            buffer.append(str.charAt(index));
         }
      }
      
      return buffer.toString();
   }
}
