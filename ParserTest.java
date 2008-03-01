package edumips64;
import java.io.*;
import edumips64.core.parser.*;
import edumips64.core.Memory;

public class ParserTest {
    public static void main(String[] args) throws Exception {
        // Scanner.main(args);
        if(args.length > 1) {
            System.out.println("sc");
            // Opzione!
            Scanner.main(args);
        }
        else {
            BufferedReader reader = new BufferedReader(new FileReader(args[0]));
            Scanner scanner = new Scanner(reader);
            Parser p = Parser.getInstance();
            p.parse(scanner);
            Memory m = Memory.getInstance();
            System.out.println(m.toString());
        }
    }
}
