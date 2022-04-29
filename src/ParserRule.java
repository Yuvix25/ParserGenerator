import java.util.*;

public class ParserRule {
    int type = -1;
    List<ParserRule> children;
    String name;
    ParserToken token;
    boolean isEmpty = false;

    /* type -> -1: NAME, 0: AND, 1: OR, 2: EXP?
    */
    public ParserRule(int type, List<ParserRule> children) {
        this.type = type;
        this.children = children;
    }
    public ParserRule(String name) {
        this.name = name;
        this.children = new ArrayList<ParserRule>(Arrays.asList(this));
    }
    public ParserRule(boolean emptyRule) {
        this.isEmpty = emptyRule;
    }

    public List<ParserRule> splitByOr() throws ParserError {
        List<ParserRule> res = new ArrayList<ParserRule>();
        switch(type) {
            case 0:
                for (ParserRule child : children) {
                    List<ParserRule> splittedChild = child.splitByOr();
                    if (res.size() == 0)
                        res.addAll(splittedChild);
                    else
                        res = ruleProduct(res, splittedChild);
                }
                break;
            case 1:
                for (ParserRule child : children) {
                    if (child.type == 2) {
                        throw new ParserError("Cannot use '?' within an 'Or' statement. (example: x|y?|z is not allowed)");
                    }
                    res.addAll(child.splitByOr());
                }
                break;
            case 2:
                res.addAll(children.get(0).splitByOr());
                res.add(new ParserRule(true));
                break;
            case -1:
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

                prod.add(new ParserRule(0, newChildren));
            }
        }
        return prod;
    }

    public void clearTokens() {
        this.token = null;
        if (type != -1)
            for (ParserRule x : children)
                x.clearTokens();
    }

    public ParserRule clone() {
        List<ParserRule> newChildren = new ArrayList<ParserRule>();
        if (type != -1)
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
        if (type == -1)
            return name;
        else
            return  (type == 0 ? "And<" : (type == 1 ? "Or<" : "?")) + children.toString().substring(type == 2 ? 1 : 0, type == 2 ? children.toString().length() - 1 : children.toString().length()) + (type == 2 ? "" : ">");
    }
}
