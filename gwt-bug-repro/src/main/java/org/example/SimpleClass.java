package org.example;

/**
 * A simple class to demonstrate the bug.
 * This class itself is not the issue - the bug occurs in GWT's test infrastructure.
 */
public class SimpleClass {
    private String value;
    
    public SimpleClass(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
}
