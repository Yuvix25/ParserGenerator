# Java Written Parser Generator
## Rules Syntax:
```js
Parser:
 - startrule: <rule>
regularrule: <rule>

Lexer:
// if a something is matched for two or more tokens, it will be matched as the highest of them here, so order of appearance matters
RULENAME: `regex`


// comment (must be seperate line)
```
### Example:
```js
Parser:
// + and * can currently only be used to wrap a complete rule, so something like 'rule: rul1 rule2+ rule3' is not alowed.
 - block: variable+ // * is supported as well!
variable: GLOBAL? ID EQ expr SEMICOLON
expr: add | neg
neg: ADD mul
add: (add ADD mul) | mul
mul: (mul MUL pow) | pow
pow: (pow POW atom) | atom
paren: LPAREN expr RPAREN
atom: NUM | ID | paren


Lexer:
GLOBAL: `global`
ID: `[a-zA-Z_][a-zA-Z0-9_]*`
SEMICOLON: `;`
EQ: `=`
NUM: `[0-9]+`
ADD: `\+|\-`
MUL: `\*|\/`
POW: `\^`
LPAREN: `\(`
RPAREN: `\)`
```

## Documentation:
### `class Parser`:
```java
class Parser(String[] lexerRules, String[] parserRules);
class Parser(String ruleFileContent);
```
And to directly read from a file, use:
```java
Parser Parser.fromFile(String filePath);
```
Then, to parse a string, use:
```java
ParserToken Parser.parse(String input);
ParserToken Parser.parse(String input, List<String> skipSingle);
```
Input is the input string to be parsed, and skipSingle are parser rule names that should be skipped when only having one child, for example, if we put `mul, pow` in skipSingle, then the following parsing tree:  
`add(mul(pow(atom(NUM(1)))), ADD(+), mul(pow(atom(NUM(2)))))`  
will be translated into `add(atom(NUM(1)), ADD(+), atom(NUM(2)))`.

Example usage:
```java
Parser parser = Parser.fromFile("test/example.txt");
System.out.println(parser.parse("global x = 5 + 3; y = 2 * (3 + 4) ^ 7;", Arrays.asList("add", "mul", "pow")));
```
### `class ParserToken`:
Attributes:
```java
String name; // Rule name
String value; // Original text in the input string
int start; // Start index in the input string (not line relative)
int end; // End index in the input string (not line relative)
int line; // Line number in the input string
List<ParserToken> children; // Child tokens
```