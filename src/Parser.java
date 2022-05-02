import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

class Parser {
    Lexer lexer;
    Map<String,ParserRule> rules;
    String startRule;
    Set<String> repetitionRules = new HashSet<String>();
    
    public static Parser fromFile(String filename) throws ParserError, FileNotFoundException, IOException {
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

    
    public Parser(String content) throws ParserError {
        this(content.substring(content.indexOf("Lexer:\n")+7).split("\n"), 
             content.substring(content.indexOf("Parser:\n")+8, content.indexOf("Lexer:\n")).split("\n"));
    }

    @SuppressWarnings("unchecked")
    public Parser(String[] lexerText, String[] parserText) throws ParserError {
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
                name = name.trim().substring(1).trim();
                startRule = name;
            }

            ParserRule rule = RuleParser.parseRule(val);
            if (rule.type == RuleType.And && rule.children.size() == 1 && (rule.children.get(0).type == RuleType.Repetition1 || rule.children.get(0).type == RuleType.Repetition0)) {
                rule = rule.children.get(0);
            }
            if (rule.type == RuleType.Repetition1) { // rule+
                this.repetitionRules.add(name);
                rule = new ParserRule(RuleType.And, Arrays.asList(rule.children.get(0), new ParserRule(RuleType.Quantifier, new ParserRule(name))));
            } else if (rule.type == RuleType.Repetition0) { // rule*
                this.repetitionRules.add(name);
                rule = new ParserRule(RuleType.Quantifier, new ParserRule(RuleType.And, Arrays.asList(rule.children.get(0), new ParserRule(RuleType.Quantifier, new ParserRule(name)))));
            }
            
            
            this.rules.put(name, rule);
        }
        if (startRule == null) {
            throw new ParserError("No parser start rule found in the grammer. Please specify one by inserting a `-` before it's name.");
        }
    }

    public class RuleStateRow {
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

        ParserToken finalToken = null;
        while (true) {
            expandAllNonterminals(currentState, k);
            // System.out.println(currentState);
            stateTable.add(currentState);
            k++;

            // scan
            currentState = new ArrayList<RuleStateRow>();
            int validCount = 0;
            for (RuleStateRow row : stateTable.get(k-1)) {
                if (row.position < row.rule.children.size()) {
                    ParserRule curr = row.rule.children.get(row.position);
                    int index = k - 1 - countEmpties(row.rule, 0, row.position);
                    if (index < tokens.size() && (curr.isEmpty || tokens.get(index).name.equals(curr.name))) {
                        progress(stateTable, currentState, row, !curr.isEmpty ? new ParserToken(tokens.get(index)) : new ParserToken(), 1);
                        validCount++;
                    }
                }
            }
            if (validCount == 0)
                break;

            boolean exit = false;
            for (RuleStateRow row : currentState) {
                if (row.name.equals(startRule) && row.column == 0 && row.position == row.rule.children.size() && k-countEmpties(row.rule) == tokens.size()) {
                    finalToken = row.rule.token;
                    exit = true;
                    break;
                }
            }
            if (exit)
                break;
        }

        
        
        fixRepetitionRules(finalToken, new Stack<String>());
        removeEmptyTokens(finalToken);

        return finalToken;
    }

    private int countEmpties(ParserRule rule, int startIndex, int endIndex) {
        int count = rule.token != null ? (rule.token.isEmpty ? 1 : 0) : 0;
        if (rule.children == null || (rule.children.size() == 1 && rule.children.get(0) == rule))
            return count;
        
        for (int i = startIndex; i < endIndex; i++) {
            count += countEmpties(rule.children.get(i));
        }
        return count;
    }

    private int countEmpties(ParserRule rule) {
        return countEmpties(rule, 0, rule.children.size());
    }

    private void removeEmptyTokens(ParserToken token) {
        List<ParserToken> toRemove = new ArrayList<ParserToken>();
        if (token != null && token.children != null) {
            for (ParserToken child : token.children) {
                if (child.isEmpty) {
                    toRemove.add(child);
                } else {
                    removeEmptyTokens(child);
                }
            }
            token.children.removeAll(toRemove);
        }
    }

    private void fixRepetitionRules(ParserToken token, Stack<String> enteredReps) {
        if (token == null)
            return;
        
        boolean startedNow = false;
        if (repetitionRules.contains(token.name) && (enteredReps.empty() || !enteredReps.peek().equals(token.name))) {
            enteredReps.push(token.name);
            startedNow = true;
        }

        List<ParserToken> newChildren = new ArrayList<ParserToken>();
            for (ParserToken child : token.children) {
                if (repetitionRules.contains(child.name)) {
                    fixRepetitionRules(child, enteredReps);
                    if (!enteredReps.empty() && child.name.equals(enteredReps.peek())) {
                        newChildren.addAll(child.children);
                    } else {
                        newChildren.add(child);
                    }
                } else {
                    newChildren.add(child);
                }
            }
        
        if (!enteredReps.empty() && enteredReps.peek().equals(token.name)) {
            token.children = newChildren;
        }
        if (startedNow) {
            enteredReps.pop();
        }
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

    private void progress(List<List<RuleStateRow>> stateTable, List<RuleStateRow> state, RuleStateRow row, ParserToken token, int steps) {
        state.add(new RuleStateRow(row.name, row.rule, row.position+steps, row.column));
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
            if (tokenChildren.size() == 1 && tokenChildren.get(0).isEmpty)
                row.rule.token = new ParserToken();
            else
                row.rule.token = new ParserToken(row.name, value, tokenChildren.get(0).start, tokenChildren.get(tokenChildren.size() - 1).end, tokenChildren.get(0).line, tokenChildren);
            
            for (RuleStateRow row2 : stateTable.get(row.column)) {
                if (row2.position < row2.rule.children.size()) { //  && row.name.equals(row2.rule.children.get(row2.position).name)
                    int steps2 = row2.rule.children.get(row2.position).matches(row, row2);
                    if (steps2 != -1) {
                        progress(stateTable, state, row2.clone(), row.rule.token, steps2);
                    }
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
                    if (curr.type == RuleType.Name && rules.containsKey(curr.name)) {
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
