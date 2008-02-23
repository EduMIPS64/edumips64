package edumips64;
import java.io.*;
import edumips64.core.parser.*;

public class ParserTest {
    public static void main(String[] args) throws Exception {
        // Scanner.main(args);
        BufferedReader reader = new BufferedReader(new FileReader(args[0]));
        Scanner scanner = new Scanner(reader);
        Parser p = Parser.getInstance();
        p.parse(scanner);
    }
}
