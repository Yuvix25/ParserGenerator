import java.util.*;

public class ParserToken extends Token{
    List<ParserToken> children;
    public ParserToken(String name, String value, int start, int end, int line) {
        super(name, value, start, end, line);
    }
    public ParserToken(LexerToken token) {
        super(token.name, token.value, token.start, token.end, token.line);
    }

    public ParserToken(String name, String value, int start, int end, int line, List<ParserToken> children) {
        super(name, value, start, end, line);
        this.children = children;
    }

    public ParserToken clone() {
        
        if (children != null) {
            List<ParserToken> newChildren = new ArrayList<ParserToken>();
            for (ParserToken child : children)
                newChildren.add(child.clone());
            return new ParserToken(name, value, start, end, line, newChildren);
        }
        else
            return new ParserToken(name, value, start, end, line);
    }

    public String toString() {
        if (children != null)
            return name + "<" + (line + 1) + ":" + start + "," + end + ">(" + children + ")";
        else
        return name + "<" + (line + 1) + ":" + start + "," + end + ">(" + value + ")";
    }
}
