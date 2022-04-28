import java.util.*;

public class Tester {
    public static void main(String[] args) throws LexerError {
        Lexer lexer = new Lexer(new OrderedDict<String, String>(Arrays.asList(
            new Tuple<String, String>("ID", "\\b([A-Za-z_][A-Za-z0-9_]*)\\b"),
            new Tuple<String, String>("NUM", "[0-9]+"),
            new Tuple<String, String>("OP", "\\+|\\-|\\*|\\/|\\^")
        )));

        System.out.println(lexer.tokenize("hello1 * 10"));
    }
}
