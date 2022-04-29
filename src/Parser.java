import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

class Parser {
    Lexer lexer;
    Map<String,ParserRule> rules;
    String startRule;
    
    public static Parser fromFile(String filename) throws FileNotFoundException, IOException {
        FileReader in = new FileReader(filename);
        BufferedReader br = new BufferedReader(in);
        String content = "";
        while (true) {
            String line = br.readLine();
            if (line == null)
                break;
            String stripped = line.replaceAll(" ", "").replaceAll("\t", "");
            if (stripped != "" && !stripped.startsWith("//"))
                content += line + "\n";
        }
        br.close();

        return new Parser(content);
    }

    @SuppressWarnings("unchecked")
    public Parser(String content) {
        String[] parserText = content.substring(content.indexOf("Parser:\n")+8, content.indexOf("Lexer:\n")).split("\n");
        String[] lexerText = content.substring(content.indexOf("Lexer:\n")+8).split("\n");

        Tuple<String,String>[] lexerRules = (Tuple<String,String>[]) new Tuple[lexerText.length];
        for (int i = 0; i < lexerText.length; i++) {
            String line = lexerText[i];
            if (line.length() == 0)
                continue;
            String name = line.substring(0, line.indexOf(":"));
            String val = line.substring(line.indexOf("`")+1, line.lastIndexOf("`"));
            lexerRules[i] = new Tuple<String,String>(name, val);
        }
        this.lexer = new Lexer(lexerRules);

        this.rules = new HashMap<String,ParserRule>();
        for (int i = 0; i < parserText.length; i++) {
            String line = parserText[i];
            if (line.length() == 0)
                continue;
            String name = line.substring(0, line.indexOf(":"));
            String val = line.substring(line.indexOf(":")+1).replaceAll("\\s+", " ")
                                                                .replaceAll("\\s*\\|\\s*", "|")
                                                                .replaceAll("\\s*\\(\\s*", "(")
                                                                .replaceAll("\\s*\\)\\s*", ")");
            val = val.trim();
            boolean start = name.trim().startsWith("-");
            if (start) {
                name = name.trim().substring(name.indexOf("-")).trim();
                startRule = name;
            }

            ParserRule rule = RuleParser.parseRule(val);
            
            this.rules.put(name, rule);
        }
    }

    private class RuleStateRow {
        String name;
        ParserRule rule;
        int position;
        int column;
        public RuleStateRow(String name, ParserRule rule, int position, int column) {
            this.name = name;
            this.rule = rule;
            this.position = position;
            this.column = column;
        }

        public RuleStateRow clone() {
            return new RuleStateRow(name, rule.clone(), position, column);
        }

        public String toString() {
            return name + " -> " + rule + " : " + position + "(" + column + ")";
        }
    }

    public ParserToken parse(String input) throws LexerError, ParserError {
        List<LexerToken> tokens = lexer.tokenize(input);
        List<List<RuleStateRow>> stateTable = new ArrayList<List<RuleStateRow>>();
        int k = 0;

        List<RuleStateRow> currentState = new ArrayList<RuleStateRow>();
        addRule(currentState, startRule, 0);

        while (true) {
            expandAllNonterminals(currentState, k);
            // System.out.println(currentState);
            stateTable.add(currentState);
            k++;
            if (k == tokens.size()+1)
                break;

            // scan
            currentState = new ArrayList<RuleStateRow>();
            for (RuleStateRow row : stateTable.get(k-1)) {
                if (row.position < row.rule.children.size()) {
                    ParserRule curr = row.rule.children.get(row.position);
                    if (tokens.get(k-1).name.equals(curr.name)) {
                        progress(stateTable, currentState, row, new ParserToken(tokens.get(k-1)));
                    }
                }
            }
        }

        for (RuleStateRow row : currentState) {
            if (row.name.equals(startRule) && row.column == 0 && row.position == row.rule.children.size()) {
                return row.rule.token;
            }
        }

        return null;
    }

    public ParserToken parse(String input, List<String> skipSingle) throws LexerError, ParserError {
        ParserToken token = parse(input);
        if (token != null)
            return removeSingles(token, skipSingle);
        return token;
    }

    private ParserToken removeSingles(ParserToken token, List<String> which) {
        if (which.contains(token.name) && token.children != null && token.children.size() == 1) {
            token = removeSingles(token.children.get(0), which);
        } else if (token.children != null) {
            for (int i = 0; i < token.children.size(); i++) {
                token.children.set(i, removeSingles(token.children.get(i), which));
            }
        }
        return token;
    }

    private void progress(List<List<RuleStateRow>> stateTable, List<RuleStateRow> state, RuleStateRow row, ParserToken token) {
        state.add(new RuleStateRow(row.name, row.rule, row.position+1, row.column));
        if (row.position + 1 == row.rule.children.size()) {
            row.rule.children.get(row.position).token = token;

            List<ParserToken> tokenChildren = new ArrayList<ParserToken>();
            String value = "";
            for (ParserRule child : row.rule.children) {
                if (child.token != null) {
                    tokenChildren.add(child.token);
                } else {
                    tokenChildren.add(token);
                }
                value += tokenChildren.get(tokenChildren.size() - 1).value;
            }
            row.rule.token = new ParserToken(row.name, value, tokenChildren.get(0).start, tokenChildren.get(tokenChildren.size() - 1).end, tokenChildren.get(0).line, tokenChildren);
            for (RuleStateRow row2 : stateTable.get(row.column)) {
                if (row2.position < row2.rule.children.size() && row.name.equals(row2.rule.children.get(row2.position).name)) {
                    progress(stateTable, state, row2.clone(), row.rule.token);
                }
            }
        } else {
            row.rule.children.get(row.position).token = token;
        }
    }

    private void expandAllNonterminals(List<RuleStateRow> state, int column) throws ParserError {
        Set<String> toAdd = new HashSet<String>();
        Set<String> alreadyAdded = new HashSet<String>();
        int prevSize = -1;
        while (true) {
            prevSize = toAdd.size();
            for (RuleStateRow row : state) {
                if (row.position < row.rule.children.size()) {
                    ParserRule curr = row.rule.children.get(row.position);
                    if (curr.type == -1 && rules.containsKey(curr.name)) {
                        toAdd.add(curr.name);
                    }
                }
            }
            if (toAdd.size() == prevSize)
                break;

            for (String name : toAdd) {
                if (!alreadyAdded.contains(name)) {
                    alreadyAdded.add(name);
                    addRule(state, name, column);
                }
            }
        }
        
        
    }

    private void addRule(List<RuleStateRow> state, String rule, int column) throws ParserError {
        for (ParserRule split : rules.get(rule).splitByOr()) {
            split = split.clone();
            state.add(new RuleStateRow(rule, split, 0, column));
        }
    }
}
