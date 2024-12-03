package org.edumips64.utils.io;

// Writer implementation that writes to standard output.
public class StdOutWriter implements Writer {
    @Override
    public void write(String s) throws WriteException {
        System.out.print(s);
    }
}