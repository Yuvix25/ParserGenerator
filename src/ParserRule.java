import java.util.*;
enum RuleType {
    Name,
    And,
    Or,
    Repetition0, // RULE*
    Repetition1, // RULE+
    Quantifier // RULE?
}

public class ParserRule {
    // type = -1: NAME, 0: AND, 1: OR, 2: RULE?, 3: RULE+
    // int type = -1;

    RuleType type = RuleType.Name;
    List<ParserRule> children;
    String name;
    ParserToken token;
    boolean isEmpty = false;
    boolean isDummy = false;

    
    public ParserRule(RuleType type, List<ParserRule> children) {
        this.type = type;
        this.children = children;
    }
    public ParserRule(RuleType type, ParserRule child) {
        this.type = type;
        this.children = Arrays.asList(child);
    }
    public ParserRule(String name) {
        this.name = name;
        this.children = new ArrayList<ParserRule>(Arrays.asList(this));
    }
    public ParserRule(boolean emptyRule) {
        this.isEmpty = emptyRule;
        this.children = new ArrayList<ParserRule>(Arrays.asList(this));
    }

    public List<ParserRule> splitByOr() throws ParserError {
        List<ParserRule> res = new ArrayList<ParserRule>();
        switch(type) {
            case And: // And
                for (ParserRule child : children) {
                    List<ParserRule> splittedChild = child.splitByOr();
                    if (res.size() == 0)
                        res.addAll(splittedChild);
                    else
                        res = ruleProduct(res, splittedChild);
                }
                break;
            case Or: // Or
                for (ParserRule child : children) {
                    if (child.type == RuleType.Quantifier) {
                        throw new ParserError("Cannot use '?' within an 'Or' statement. (example: x|y?|z is not allowed)");
                    }
                    res.addAll(child.splitByOr());
                }
                break;
            case Quantifier: // Rule?
                res.addAll(children.get(0).splitByOr());
                res.add(new ParserRule(true));
                break;
            case Repetition1: // Rule+
            case Repetition0: // Rule*
            case Name: // Name
                res.add(this);
                break;
        }

        return res;
    }

    private List<ParserRule> ruleProduct(List<ParserRule> x, List<ParserRule> y) {
        if (x.size() == 0)
            return new ArrayList<ParserRule>(y);
        else if (y.size() == 0)
            return new ArrayList<ParserRule>(x);
        
        List<ParserRule> prod = new ArrayList<ParserRule>();
        for (ParserRule first : x) {
            for (ParserRule second : y) {
                List<ParserRule> newChildren = new ArrayList<ParserRule>();
                if (first.children != null)
                    for (ParserRule child : first.children)
                        if (!child.isEmpty)
                            newChildren.add(child);
                
                if (second.children != null)
                    for (ParserRule child : second.children)
                        if (!child.isEmpty)
                            newChildren.add(child);

                prod.add(new ParserRule(RuleType.And, newChildren));
            }
        }
        return prod;
    }

    public int matches(Parser.RuleStateRow toMatch, Parser.RuleStateRow parentRow) {
        if (type == RuleType.Name) {
            if (toMatch.name.equals(name)) {
                return 1;
            }
            return -1;
        } else if (type == RuleType.And || type == RuleType.Or) {
            return children.size() == 0 || children == null || isEmpty ? 0 : -1;
        } else if (type == RuleType.Quantifier) {
            return -1;
        }

        return -1;
    }

    public void clearTokens() {
        this.token = null;
        if (type != RuleType.Name)
            for (ParserRule x : children)
                x.clearTokens();
    }

    public ParserRule clone() {
        if (isEmpty)
            return new ParserRule(true);
        List<ParserRule> newChildren = new ArrayList<ParserRule>();
        if (type != RuleType.Name)
            for (ParserRule child : children)
                newChildren.add(child.clone());
        else
            newChildren.add(this);
        ParserRule newRule = new ParserRule(type, newChildren);
        newRule.name = this.name;
        if (token != null)
            newRule.token = this.token.clone();
        return newRule;
    }


    public String toString() {
        if (isEmpty)
            return "EmptyRule";
        else if (type == RuleType.Name)
            return name;
        else
            return  (type == RuleType.And ? "And<" : (type == RuleType.Or ? "Or<" : "")) + children.toString().substring(type == RuleType.Quantifier ? 1 : 0, type == RuleType.Quantifier ? children.toString().length() - 1 : children.toString().length()) + (type == RuleType.Quantifier ? "?" : ">");
    }
}
