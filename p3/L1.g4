grammar L1;

// Creates import of the used types in the generated java source,
// so that we can use it in the parser rules without further ado.
@parser::header {
    import cc.crochethk.compilerbau.p3.ast.*;
}

start
	returns[Prog result]: definition*;

definition
	returns[FunDef result]:
	KW_FUN IDENT LPAR (FUN_PARAM (COMMA FUN_PARAM)*)? RPAR //
	COLON IDENT // return type
	LBRACE statement RBRACE; // this coould be replaced by a block rule and node type

statement
	returns[Node result]: KW_RETURN expr;

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
	| varOrFunCall;

varOrFunCall
	returns[Node result]:
	// variable value
	IDENT
	// function call without parameters
	| IDENT LPAR RPAR
	// function call with one or more parameters
	| IDENT LPAR expr (COMMA expr)* RPAR;

zahl
	returns[Node result]: NUMBER;

bool
	returns[Node result]: BOOLEAN;

// Lexer rules
NUMBER: [0-9]+;
BOOLEAN: 'true' | 'false';
ADD: '+';
SUB: '-';
POW: '**';
MULT: '*';
DIV: '/';
AND: '&&';
OR: '||';

LPAR: '(';
RPAR: ')';
LBRACE: '{';
RBRACE: '}';
COLON: ':';
COMMA: ',';

FUN_PARAM: IDENT COLON IDENT; // "name : type"
KW_FUN: 'fn';
KW_RETURN: 'return';

IDENT: ID_START ID_CHAR*;
fragment ID_START: [a-zA-Z_];
fragment ID_CHAR: ID_START | [0-9];

// Ignored tokens
LINE_COMMENT: '//' .*? '\r'? '\n' -> skip;
WHITESPACE: [ \t\n\r]+ -> skip;