grammar L1;

// Creates import of the used types in the generated java source,
// so that we can use it in the parser rules without further problems.
@parser::header {
    import cc.crochethk.compilerbau.p3.ast.*;
}

start
	returns[Prog result]: definition*;

definition
	returns[FunDef result]:
	FUN IDENT LPAR (FUN_PARAM (COMMA FUN_PARAM)*)? RPAR COLON IDENT LBRACE statement RBRACE;

statement
	returns[Node result]: RETURN expr;

expr
	returns[Node result]:
	| expr POW expr
	| expr (MULT | DIV) expr
	| expr (ADD | SUB) expr
	| expr AND expr
	| expr OR expr
	| LPAR expr RPAR
	| zahl
	| bool
	| IDENT;

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

COLON: ':';
COMMA: ',';
LBRACE: '{';
RBRACE: '}';

FUN_PARAM: IDENT COLON IDENT;
FUN: 'fn';
RETURN: 'return';

IDENT: ID_START ID_CHAR*;
fragment ID_START: [a-zA-Z_];
fragment ID_CHAR: ID_START | [0-9];

LINE_COMMENT: '//' .*? '\r'? '\n' -> skip;
WHITESPACE: [ \t\n\r]+ -> skip;