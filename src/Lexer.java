import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {
    OrderedDict<String, String> rules;
    Token[] tokens;
    public Lexer(OrderedDict<String, String> regex_rules) {
        rules = regex_rules;
        tokens = new LexerToken[rules.size()];
        int i = 0;
        for (String key : rules.keySet()) {
            tokens[i] = new Token(key);
            i++;
        }
    }

    private static List<int[]> allMatches(String reg, String input) {
        List<int[]> ret = new ArrayList<int[]>();
        Matcher m = Pattern.compile(reg).matcher(input);
        while (m.find()) {
          ret.add(new int[]{m.start(), m.end()});
        }
        return ret;
    }

    private static int comp(Tuple<int[], String> o1, Tuple<int[], String> o2) {
        return o1.key[0] - o2.key[0];
    }

    private static int getLine(String text, int start) {
        String sub = text.substring(0, start);
        return sub.length() - sub.replaceAll("\n", "").length();
    }

    public List<LexerToken> tokenize(String input) throws LexerError {
        List<Tuple<int[], String>> all = new ArrayList<Tuple<int[], String>>();
        for (String key : rules.keySet()) {
            List<int[]> matches = allMatches(rules.get(key), input);
            for (int[] se : matches) {
                if (se[0] == se[1]) {
                    
                    throw new LexerError("LexerToken '" + key + "' found an empty match at line " + (getLine(input, se[0]) + 1));
                }
                
                all.add(new Tuple<int[],String>(se, key));
            }
        }

        Collections.sort(all, (o1, o2) -> comp(o1, o2));

        
        // check for collisons and create token list
        List<LexerToken> token_list = new ArrayList<LexerToken>();

        // int prev_end = -1;
        // String prev_token = "";
        int i = 0;
        List<Integer> removes = new ArrayList<Integer>();
        for (Tuple<int[], String> match : all) {
            if (i != 0 && match.key[0] < all.get(i-1).key[1]) {
                // throw new LexerError("Collision found between token '" + prev_token + "' and token '" + match.value + "' on line " + (getLine(input, match.key[0]) + 1));
                if (rules.indexOf(match.value) < rules.indexOf(all.get(i-1).value)) {
                    removes.add(i-1);
                } else {
                    removes.add(i);
                }
            }
            int start = match.key[0];
            int end = match.key[1];
            token_list.add(new LexerToken(match.value, input.substring(start, end), start, end, getLine(input, start)));

            i++;
        }
        for (int index : removes) {
            token_list.remove(index);
        }

        return token_list;
    }
}

/*
[
    ((0, 1), "letter"),
    ((1, 4), "whitespace")
]
*/