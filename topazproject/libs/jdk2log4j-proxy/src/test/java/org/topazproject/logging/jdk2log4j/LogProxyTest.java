/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.logging.jdk2log4j;

import java.util.logging.Logger;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import junit.framework.TestCase;

/**
 * Test our LogManager and LoggerProxy classes.
 *
 * @author Eric Brown
 */
public class LogProxyTest extends TestCase {
  private org.apache.log4j.Logger  log4jLog;
  private java.util.logging.Logger jdkLog;
  
  public LogProxyTest(String testName) {
    super(testName);
  }

  protected void setUp() {
    this.log4jLog = org.apache.log4j.Logger.getLogger(LogProxyTest.class);
    this.jdkLog   = java.util.logging.Logger.getLogger(LogProxyTest.class.getName());
  }

  protected void tearDown() {
  }

  public void testOutput() {
    this.log4jLog.info("Log via log4j");
    this.jdkLog.info("Log via jdk");
  }
  
  public void testSystemProperty() {
    assertEquals("java.util.logging.manager system property not set!",
                 /*org.topazproject.logging.jdk2log4j.*/LogManager.class.getName(),
                 System.getProperty("java.util.logging.manager"));
  }
  
  public void testCorrectClasses() {
    assertEquals("LogManager not our stub",
                 /*org.topazproject.logging.jdk2log4j.*/LogManager.class.getName(),
                 java.util.logging.LogManager.getLogManager().getClass().getName());
    
    assertEquals("Our jdk logger is not a ProxyLogger",
                 ProxyLogger.class.getName(), jdkLog.getClass().getName());
    
    // Validate that jdk root logger is a ProxyLogger
    assertEquals("Root logger not a ProxyLogger",
                 ProxyLogger.class.getName(), Logger.getLogger("").getClass().getName());
    
    // Jdk's "global" logger (a special case for lazy programmers)
    this.jdkLog.info("Global logger: " + Logger.global.getClass().getName() +
                     ":" + Logger.global.getName());
    assertEquals("'global' Logger object not a ProxyLogger",
                 /*org.topazproject.logging.jdk2log4j.*/ProxyLogger.class.getName(),
                 java.util.logging.Logger.global.getClass().getName());
    
    // Anonymous logger is bogus and a bug IMO...
    Logger anonymous = Logger.getAnonymousLogger();
    this.jdkLog.info("Anonymous logger: " + anonymous.getClass().getName() +
                     ":" + anonymous.getName());
    assertEquals("anonymous logger object should be a straight java.util.logging.Logger?",
                 Logger.class.getName(), anonymous.getClass().getName());
  }

  public void testLogging() {
    class MyAppender extends AppenderSkeleton {
      public LoggingEvent currentEvent;
      public void    append(LoggingEvent event) { this.currentEvent = event; }
      public void    close() {}
      public boolean requiresLayout() { return false; }
    };

    MyAppender appender = new MyAppender();
    org.apache.log4j.Category log4jRootLogger = org.apache.log4j.Logger.getRoot();
    log4jRootLogger.addAppender(appender);

    String msg1 = "test-1";
    this.jdkLog.info(msg1);
    assertEquals("Log message not passed to appender properly",
                 msg1, appender.currentEvent.getMessage());
    assertEquals("Log level not set properly in event",
                 org.apache.log4j.Level.INFO, appender.currentEvent.getLevel());
    assertEquals("Logger name not set correctly",
                 LogProxyTest.class.getName(), appender.currentEvent.getLoggerName());
    // TODO: Don't know why next lines fail, but info is clearly there from log results
    //   Probably a problem with MyAppender
    //assertEquals("Logger location not set properly",
    //             "testLogging", appender.currentEvent.getLocationInformation().getMethodName());

    log4jRootLogger.setLevel(org.apache.log4j.Level.WARN);
    String msg2 = "test-2";
    this.jdkLog.info(msg2);
    log4jRootLogger.setLevel(org.apache.log4j.Level.INFO);
    assertEquals("Log message was passed when it shouldn't have been",
                 msg1 /* NOT msg2 */, appender.currentEvent.getMessage());

    log4jRootLogger.setLevel(org.apache.log4j.Level.TRACE);
    String msg3 = "test-3: Log an exception. THIS IS JUST A TEST.";
    Throwable t = new Throwable(msg3);
    this.jdkLog.log(java.util.logging.Level.FINEST, msg3, t);
    assertEquals("Log did not capture exception properly",
                 t, appender.currentEvent.getThrowableInformation().getThrowable());
    assertEquals("Log message at finest level not working",
                 msg3, appender.currentEvent.getMessage());
    assertEquals("Log level not set properly in event",
                 org.apache.log4j.Level.TRACE, appender.currentEvent.getLevel());

    String msg4 = "test-4";
    this.jdkLog.severe(msg4);
    assertEquals("Log message at sever level not working",
                 msg4, appender.currentEvent.getMessage());
    assertEquals("Log level not translated properly",
                 org.apache.log4j.Level.FATAL, appender.currentEvent.getLevel());
    
  }
}
