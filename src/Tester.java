import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class Tester {
    public static void main(String[] args) throws LexerError, ParserError, FileNotFoundException, IOException {
        Parser parser = Parser.fromFile("test/rules.txt");
        System.out.println(parser.parse("1+2*(3+1)^4", Arrays.asList("add", "mul", "pow")));
    }
}
