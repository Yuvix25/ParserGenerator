import java.util.*;

public class RuleParser {
    final static List<Character> seperators = Arrays.asList('|', ' ', '(', ')', '?');

    public static ParserRule parseRule(List<String> seperated) {
        ParserRule root = new ParserRule(0, new ArrayList<ParserRule>());
        for (int i = 0; i < seperated.size(); i++) {
            String x = seperated.get(i);
            switch (x) {
                case "(":
                    int end = findClosingParentheses(seperated, i);
                    List<String> parenContent = seperated.subList(i+1, end);
                    root.children.add(parseRule(parenContent));
                    i = end;
                    break;
                case "|":
                    root.type = 1;
                    break;
                case "?":
                    if (root.children.size() > 0)
                        root.children.set(root.children.size()-1, new ParserRule(2, Arrays.asList(root.children.get(root.children.size()-1))));
                    break;
                case " ":
                    break;
                default:
                    root.children.add(new ParserRule(x));
                    break;
            }
        }
        return root;
    }

    public static ParserRule parseRule(String rule) {
        List<String> seperated = new ArrayList<String>();
        
        String tmp = "";
        for (int i = 0; i < rule.length(); i++) {
            char c = rule.charAt(i);
            if (seperators.contains(c)) {
                if (!tmp.equals(""))
                    seperated.add(tmp);
                seperated.add(c + "");
                tmp = "";
            } else {
                tmp += c;
            }
        }
        if (!tmp.equals(""))
            seperated.add(tmp);

        return parseRule(seperated);
    }

    private static int findClosingParentheses(List<String> arr, int openIndex) {
        int balance = 1;
        for (int i = openIndex+1; i < arr.size(); i++) {
            String x = arr.get(i);
            if (x.equals("("))
                balance++;
            else if (x.equals(")"))
                balance--;
            
            if (balance == 0)
                return i;
        }
        return -1;
    }
}
