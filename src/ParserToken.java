import java.util.*;

public class ParserToken extends Token{
    List<ParserToken> children;
    public ParserToken(String name, String value, int start, int end, int line) {
        super(name, value, start, end, line);
    }

    public ParserToken(String name, String value, int start, int end, int line, List<ParserToken> children) {
        super(name, value, start, end, line);
        this.children = children;
    }
}
