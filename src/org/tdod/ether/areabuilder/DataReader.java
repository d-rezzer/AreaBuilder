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

public abstract class DataReader {

   /**
    * Returns an array of data between brackets.
    * "1    {0 3}" = [0, 3]
    * @param data the data to manipulate.
    * @return an array of strings from the data line.
    */
   protected String[] getData(String data) {
      int index = data.indexOf("{");
      String string = data.substring(index + 1, data.length() - 1);
      String[] dataSplit = string.split(" ");
      return dataSplit;
   }

   /**
    * Returns any data between brackets.
    * "1    {shop keeper}" = "shop keeper"
    * @param data the string to manipulate.
    * @return the  modified string.
    */
   protected String getLineOfData(String data) {
      int index = data.indexOf("{");
      String string = data.substring(index + 1, data.length() - 1);
      return string;
   }

   /**
    * Change "%s is doing something to %s" to "{0} is doing something to {1}"
    * @param action the string to modify.
    * @return the modified string.
    */
   protected String getModifiedStringVariableToken(String action) {
      StringBuffer buffer = new StringBuffer();
      int subCount = 0;
      
      for (int strIndex = 0; strIndex < action.length(); strIndex++) {
         if (action.charAt(strIndex) == '%') {
            buffer.append("{" + subCount + "}");
            strIndex = strIndex + 1;
            subCount++;
         } else {
            if (strIndex < action.length()) {
               buffer.append(action.charAt(strIndex));            
            }            
         }
         
      }
      
      return buffer.toString();
   }

   /**
    * Replaces ' with ''
    * @param str the string to modify.
    * @return the modified string.
    */
   protected String replaceQuotes(String str) {
      StringBuffer buffer = new StringBuffer();
      int subCount = 0;
      
      for (int strIndex = 0; strIndex < str.length(); strIndex++) {
         if (str.charAt(strIndex) == '\'') {
            buffer.append("''");
            strIndex = strIndex + 1;
            subCount++;
         }
         
         buffer.append(str.charAt(strIndex));
      }
      
      return buffer.toString();      
   }
   
   protected String getDescriptionData(String data) {
      int index = data.indexOf("{");
      String string = data.substring(index + 1, data.length());
      return string;      
   }


}
