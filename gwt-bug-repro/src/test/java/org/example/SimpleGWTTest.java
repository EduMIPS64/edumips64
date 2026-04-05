package org.example;

import com.google.gwt.junit.client.GWTTestCase;

/**
 * Minimal GWTTestCase that triggers a failure when gwt-user and gwt-dev
 * versions are mismatched (gwt-user 2.12.2 + gwt-dev 2.13.0).
 *
 * The test content doesn't matter — the failure occurs during GWT's
 * internal Java-to-JavaScript compilation of the test infrastructure,
 * before any user test code runs.
 */
public class SimpleGWTTest extends GWTTestCase {

    @Override
    public String getModuleName() {
        return "org.example.SimpleGWTTest";
    }

    public void testSimple() {
        assertTrue(true);
    }
}
