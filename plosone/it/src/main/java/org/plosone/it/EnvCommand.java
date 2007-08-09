/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plosone.it;

/**
 * A command line wrapper for Env.groovy
 *
 * @author Pradeep Krishnan
 */
public class EnvCommand {
  /**
   * Execute the command
   *
   * @param args install-location, command
   *
   * @throws Exception on an error
   */
  public static void main(String[] args) throws Exception {
    if (args.length < 1) {
      System.out.println("Usage: EnvCommand <install-location> [start/stop/install/restore]");

      return;
    }

    Env    env = new Env(args[0], null);

    String cmd = (args.length > 1) ? args[1].toLowerCase() : "start";

    if (cmd.equals("start")) {
      Object block = new Object();

      while (true) {
        synchronized (block) {
          block.wait();
        }
      }
    } else if (cmd.equals("stop"))
      env.stop();
    else if (cmd.equals("install"))
      env.install();
    else if (cmd.equals("restore"))
      env.restore();
    else
      System.out.println("Unknown command : " + cmd);
  }
}
