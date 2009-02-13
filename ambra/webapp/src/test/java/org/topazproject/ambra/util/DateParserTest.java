/* $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2009 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.topazproject.ambra.util;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import static org.testng.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.text.ParseException;

/**
 * @author Dragisa Krsmanovic
 */
public class DateParserTest {

  private SimpleDateFormat format;

  @DataProvider(name = "dates")
  public String[][] createData() {
    return new String[][] {
        {"1997-07-16T19:20:30.45-02:00", "07/16/97 19:20:30.450 -0200"},
        {"1997-07-16T19:20:30+01:00",    "07/16/97 19:20:30.000 +0100"},
        {"1997-07-16T12:20:30-06:00",    "07/16/97 12:20:30.000 -0600"},
        {"1997-07-16T19:20",             "07/16/97 19:20:00.000 +0000"},
        {"1997-07-16",                   "07/16/97 00:00:00.000 +0000"},
        {"1997-07",                      "07/01/97 00:00:00.000 +0000"},
        {"1997",                         "01/01/97 00:00:00.000 +0000"}
    };
  }

  @DataProvider(name = "brokenDates")
  public String[][] createBrokenData() {
    return new String[][] {
        {"1997*07*16"},
        {"1997/07/16"},
        {"1997/07/16T19:20:30.45-02:00"},
    };
  }

  @BeforeClass
  public void setUp() {
    format = new SimpleDateFormat("MM/dd/yy HH:mm:ss.SSS Z");
  }

  @Test(dataProvider = "dates")
  public void testParse(String date, String expectedDate) throws InvalidDateException, ParseException{
    assertEquals(DateParser.parse(date), format.parse(expectedDate), "Dates missmatch");
  }

  @Test(dataProvider = "brokenDates", expectedExceptions = InvalidDateException.class)
  public void testParseMalformedDate(String date) throws InvalidDateException, ParseException{
    DateParser.parse(date);
  }

}
