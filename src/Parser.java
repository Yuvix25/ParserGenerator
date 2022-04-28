import java.util.*;

class Parser {
    Lexer lexer;
    Map<String, Set<List<String>>> rules;
    public Parser(Lexer lexer1, Map<String, Set<List<String>>> rules1) {
        lexer = lexer1;
        rules = rules1;
    }

    public ParserToken parse(String input) {
        return null;
    }
}


// a*x^2 + 1/2*x^(1+2)

// atom: a-Z | 0...9 | paren | exp
// exp: add | mul | pow
// paren: (exp)
// add: mul + mul | mul - mul | mul
// mul: pow * pow | pow / pow | pow
// pow: atom ^ atom | atom

// {
//     "atom" : {["L"], ["D"], ["paren"], ["exp"]},
//     "exp" : {["add"], ["mul"], ["pow"]},
//     "add" : {["mul", "P", "mul"]}
// }


// atom * atom ^ atom + atom / atom * atom ^ (atom + atom)
// pow1 * pow2 + pow1 / pow1 * pow1 ^ (pow1 + pow1)
// mul(1 * 2) + mul(1 / 1) * mul1 ^ (mul1 + mul1)
// add(mul + mul) * mul1 ^ paren( add(mul1 + mul1) )
// aexp(mexp + mexp) * mexp ^ paren( aexp(mexp + mexp) )
// atom * atom ^ atom
// pow * pow
// mul
// exp
// atom