import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class Tester {
    public static void main(String[] args) throws LexerError, ParserError, FileNotFoundException, IOException {
        Parser parser = Parser.fromFile("test/example.txt");
        System.out.println(parser.parse("global x = 5 + 3; y = 2 * (3 + 4) ^ 7;", Arrays.asList("add", "mul", "pow")));
    }
}
