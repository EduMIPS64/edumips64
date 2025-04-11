package org.edumips64.client;

import org.edumips64.utils.io.Reader;

/**
 * BrowserWindowReader
 * 
 * A Reader implementation that reads input from a browser window
 * in the context of a web worker.
 * 
 * It uses messaages to communicate with the browser main thread and fetch
 * additional bytes as required, and buffers locally the data read.
 */
public class BrowserWindowReader implements Reader {
    // The blocking queue is used to wait for data to come from the UI thread.
    // It will be filled with a string of the required size.
    private String input = "";
    private Worker worker;

    public BrowserWindowReader(Worker worker) {
        this.worker = worker;
    }

    @Override
    public void close() {}

    @Override 
    public int read(char[] buffer, int count) {
        worker.readData(count);
        
        // DOES NOT WORK - DOES NOT WORK - DOES NOT WORK - DOES NOT WORK - DOES NOT WORK -
        //
        // How do we wait here for the data to become available?
        // The worker is single-threaded, so we can't do things like using a BlockingQueue or Thread.sleep().
        // Those classes are just not available in GWT.
        // So, how do we wait for the data to be available?
        //
        // DOES NOT WORK - DOES NOT WORK - DOES NOT WORK - DOES NOT WORK - DOES NOT WORK -
        while (input.isEmpty()) ;
        worker.info("[GWT] got stdin data: " + input);

        for (int i = 0; i < count && i < input.length(); i++) {
            buffer[i] = input.charAt(i);
        }
        input = "";
        return count;
    }
    
    // Invoked when the worker receives a message from the main thread.
    // This is the only way to read data from the browser window.
    public void addData(String data) {
        input = data;
    }
}
