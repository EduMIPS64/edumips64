package org.edumips64.client;

import org.edumips64.core.parser.ParserException;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;

@JsType
public class ParserError {
    public int row;
    public int column;
    public boolean isWarning;
    public String description;

    @JsIgnore
    private ParserError(int row, int column, boolean isWarning, String description) {
        this.row = row;
        this.column = column;
        this.isWarning = isWarning;
        this.description = description;
    }

    @JsIgnore
    public String asSerializedJsonString() {
        FluentJsonObject json = new FluentJsonObject();
        return json
            .put("row", row)
            .put("column", column)
            .put("isWarning", isWarning)
            .put("description", description)
            .toString();
    }

    @JsIgnore
    public static ParserError FromParserException(ParserException e) {
        // Gets row, column, line, description.
        String[] exceptionData = e.getStringArray();
        return new ParserError(
            Integer.parseInt(exceptionData[0]),
            Integer.parseInt(exceptionData[1]),
            !e.isError(),
            exceptionData[3]);
    }
    
}