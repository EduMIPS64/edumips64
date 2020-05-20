package org.edumips64.client;

import org.edumips64.core.parser.ParserException;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

class ParserErrorFactory {
    public static ParserError FromParserException(ParserException e) {
        // Gets row, column, line, description.
        String[] exceptionData = e.getStringArray();

        ParserError p = new ParserError();
        p.row = Integer.parseInt(exceptionData[0]);
        p.column = Integer.parseInt(exceptionData[1]);
        p.isWarning = !e.isError();
        p.description = exceptionData[3];
        return p;
    }
}

@JsType(namespace=JsPackage.GLOBAL, name="Object", isNative=true)
public class ParserError {
    public int row;
    public int column;
    public boolean isWarning;
    public String description;
}