package org.example;

import com.google.gwt.junit.client.GWTTestCase;

/**
 * Minimal GWTTestCase that triggers the GWT 2.13.0 compiler bug.
 * 
 * The bug occurs during compilation when GWT generates double-cast code
 * in GWTRunner.java. The test content itself doesn't matter - ANY
 * GWTTestCase will trigger this bug with GWT 2.13.0.
 */
public class SimpleGWTTest extends GWTTestCase {
    
    @Override
    public String getModuleName() {
        return "org.example.SimpleGWTTest";
    }
    
    /**
     * A minimal test that should pass.
     * With GWT 2.12.2: This test compiles and runs successfully.
     * With GWT 2.13.0: Compilation fails with InternalCompilerException.
     */
    public void testSimple() {
        SimpleClass obj = new SimpleClass("test");
        assertNotNull(obj);
        assertEquals("test", obj.getValue());
    }
    
    /**
     * Another minimal test to show the issue affects all tests.
     */
    public void testAnother() {
        SimpleClass obj = new SimpleClass("hello");
        obj.setValue("world");
        assertEquals("world", obj.getValue());
    }
}
