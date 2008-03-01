package edumips64.core.parser;
import java.util.List;
import edumips64.core.ParserException;

public class ParserErrorsException extends Exception {
    protected List<ParserException> exceptions;
    protected boolean onlyWarnings;
    public ParserErrorsException(List<ParserException> list, boolean onlyWarnings) {
        exceptions = list;
        this.onlyWarnings = onlyWarnings;
    }

    public boolean hasOnlyWarnings() {
        return onlyWarnings;
    }

    public List<ParserException> getExceptionList() {
        return exceptions;
    }
}
