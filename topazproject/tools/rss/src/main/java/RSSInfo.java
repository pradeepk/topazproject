/* $HeadURL:: $
 * $Id: $
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
import java.net.MalformedURLException;
import java.rmi.RemoteException;

import java.util.List;
import java.util.ArrayList;

import javax.xml.rpc.ServiceException;

import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;

import org.apache.commons.lang.text.StrMatcher;
import org.apache.commons.lang.text.StrTokenizer;

import org.topazproject.ws.article.Article;
import org.topazproject.ws.article.ArticleClientFactory;

/**
 * Return various useful RSS information from Article service. To execute
 * this using maven, please use:
 *
 *        mvn -o -f topazproject/tools/rss/pom.xml -DRSSInfo
 *                 -Dargs="-uri <Topaz article uri>"
 *
 * @author Amit Kapoor
 */
public class RSSInfo {
  private Article service;

  private static final Options options;

  // Set up the command line options
  static {
    options = new Options();
    options.addOption(OptionBuilder.withArgName("Topaz article uri").hasArg().
        isRequired(true).withValueSeparator(' ').
        withDescription("URI to access article service").create("uri"));
  }

  /**
   * Creates a new RSSInfo object.
   *
   * @param articleUri the Topaz article service uri
   *
   * @throws MalformedURLException if service's uri is not a valid URL
   * @throws ServiceException if an error occurred locating the web-service
   * @throws RemoteException if an error occurred talking to the web-service
   */
  public RSSInfo(String articleUri)
    throws MalformedURLException, ServiceException, RemoteException {
    service = ArticleClientFactory.create(articleUri);
  }

  /**
   * Queries the Topaz article service and returns the requisite information as
   * a string (XML).
   *
   * @return returns an information as a string
   *
   * @throws RemoteException if an exception occurred talking to the service
   */
  public String getFeed() throws RemoteException {
    return service.getArticles(null, null, null, null, null, false);
  }

  // Convert from string to array of strings
  private static final String[] parseArgs(String cmdLine) {
    return new StrTokenizer(cmdLine, StrMatcher.trimMatcher(),
        StrMatcher.quoteMatcher()).getTokenArray();
  }

  // Print help message
  private static final void help() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("RSSInfo", options);
    System.exit(0);
  }

  /**
   * main
   *
   * @param args the list of arguments
   */
  public static void main(String[] args) throws Exception {
    // for broken exec:java : parse the command line ourselves
    if (args.length == 1 && args[0] == null) {
      help();
    }
    if (args.length == 1 && args[0].indexOf(' ') > 0)
      args = parseArgs(args[0]);

    CommandLineParser parser = new GnuParser();
    try {
        // parse the command line arguments
      CommandLine line = parser.parse(options, args);
      RSSInfo rss = new RSSInfo(line.getOptionValue("uri"));
      String feed = rss.getFeed();
      System.out.println(feed);
    } catch( ParseException exp ) {
        System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
        help();
    }
  }
}
