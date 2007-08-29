package org.plosone.it.jwebunit;

import com.gargoylesoftware.htmlunit.BrowserVersion;

import net.sourceforge.jwebunit.util.TestContext;

/**
 * Test context for plosone tests.
 *
 * @author Pradeep Krishnan
  */
public class PlosOneTestContext extends TestContext {
  private final BrowserVersion bv;

  /**
   * Creates a new PlosOneTestContext object.
   *
   * @param bv the browser to emulate
   */
  public PlosOneTestContext(BrowserVersion bv) {
    this.bv = bv;
  }

  /**
   * Gets the browser in use
   *
   * @return bv as BrowserVersion.
   */
  public BrowserVersion getBrowser() {
    return bv;
  }
}
