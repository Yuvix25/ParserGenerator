public class LexerError extends Exception {
    public LexerError(String message) {
        super("Lexer Error: " + message);
    }
}
