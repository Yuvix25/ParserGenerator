public class Token {
    String name, value;
    int start, end, line;
    public Token(String name, String value, int start, int end, int line) {
        this.name = name;
        this.value = value;
        this.start = start;
        this.end = end;
        this.line = line;
    }

    public Token(String name) {
        this.name = name;
    }

    public String toString() {
        return name + "<" + (line + 1) + ":" + start + "," + end + ">";
    }
}
