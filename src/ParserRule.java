import java.util.*;

public class ParserRule {
    int type = 2;
    List<ParserRule> children;
    String name;
    ParserToken token;

    /* type - 0: AND, 1: OR, 2: NAME
    */
    public ParserRule(int type, List<ParserRule> children) {
        this.type = type;
        this.children = children;
    }
    public ParserRule(String name) {
        this.name = name;
        this.children = new ArrayList<ParserRule>(Arrays.asList(this));
    }

    public List<ParserRule> splitByOr() {
        List<ParserRule> res = new ArrayList<ParserRule>();
        switch(type) {
            case 0:
                for (ParserRule child : children) {
                    List<ParserRule> splittedChild = child.splitByOr();
                    if (res.size() == 0) {
                        res.addAll(splittedChild);
                    } else {
                        List<ParserRule> prod = new ArrayList<ParserRule>();
                        for (ParserRule split : splittedChild) {
                            for (ParserRule prev : res) {
                                List<ParserRule> newChildren;
                                if (prev.type == 2) {
                                    newChildren = new ArrayList<ParserRule>(Arrays.asList(prev));
                                } else {
                                    newChildren = new ArrayList<ParserRule>(prev.children);
                                }
                                if (split.children == null) {
                                    newChildren.add(split);
                                } else {
                                    newChildren.addAll(split.children);
                                }
                                prod.add(new ParserRule(0, newChildren));
                            }
                        }
                        res = prod;
                    }
                    
                }
                break;
            case 1:
                for (ParserRule child : children) {
                    res.addAll(child.splitByOr());
                }
                break;
            case 2:
                res.add(this);
                break;
        }

        return res;
    }

    public void clearTokens() {
        this.token = null;
        if (type != 2)
            for (ParserRule x : children)
                x.clearTokens();
    }

    public ParserRule clone() {
        List<ParserRule> newChildren = new ArrayList<ParserRule>();
        if (type != 2)
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
        if (type == 2)
            return name;
        else
            return (type == 0 ? "And" : "Or") + "<" + children.toString() + ">";
    }
}
