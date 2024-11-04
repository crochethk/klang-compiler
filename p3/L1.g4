grammar L1;

// Creates import of `Node` type in the generated java source,
// so that we can use it in the parser rules without further problems.
@parser::header {
    import cc.crochethk.compilerbau.p3.Node;
}

start
	returns[Node result]: expr;

expr
	returns[Node result]:
	| expr POW expr
	| expr (MULT | DIV) expr
	| expr (ADD | SUB) expr
	| expr AND expr
	| expr OR expr
	| LPAR expr RPAR
	| zahl
	| bool;

zahl
	returns[Node result]: NUMBER;

bool
	returns[Node result]: BOOLEAN;

// Lexer rules
NUMBER: [0-9]+;
ADD: '+';
SUB: '-';
POW: '**';
MULT: '*';
DIV: '/';
LPAR: '(';
RPAR: ')';

BOOLEAN: 'true' | 'false';
AND: '&&';
OR: '||';

WHITESPACE: [ \t\n\r]+ -> skip;