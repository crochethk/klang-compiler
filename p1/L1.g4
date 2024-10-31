grammar L1;
// "start" == rule name
start
	// returns[<TYP> <CTX_FIELD_NAME>]
	returns[long result]: expr;

expr
	returns[long result]:
	| expr POW expr
	| expr (MULT | DIV) expr
	| expr (ADD | SUB) expr
	| LPAR expr RPAR
	| zahl;

// [COPILOT]
// 1. **Rule Name and Return Type**:
//    - `zahl`: The name of the rule.
//    - `returns[long result]`: Specifies that this rule returns a value of type `long`.
// 
// 2. **Rule Definition**:
//    - `NUMBER`: This is the token that the rule matches.
//         It represents a numeric value and is defined elsewhere in the grammar.
zahl
	returns[long result]: NUMBER;

NUMBER: [0-9]+;
ADD: '+';
SUB: '-';
POW: '**';
MULT: '*';
DIV: '/';
LPAR: '(';
RPAR: ')';