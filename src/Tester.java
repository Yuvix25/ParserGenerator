import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class Tester {
    public static void main(String[] args) throws LexerError, FileNotFoundException, IOException {
        Parser parser = Parser.fromFile("test/rules.txt");
        System.out.println(parser.parse("1+2*(3+1)^3", Arrays.asList("add", "mul", "pow")));
    }
}
