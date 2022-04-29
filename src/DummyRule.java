public class DummyRule extends ParserRule {
    public DummyRule(ParserRule plus) {
        super(false);
        this.isDummy = true;
    }
}
