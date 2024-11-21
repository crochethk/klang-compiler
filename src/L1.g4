grammar L1;

// Creates import of the used types in the generated java source,
// so that we can use it in the parser rules without further ado.
@parser::header {
    import cc.crochethk.compilerbau.praktikum.ast.*;
}

start
	returns[Prog result]: definition* EOF;

definition
	returns[FunDef result]:
	KW_FUN IDENT LPAR (funParam (COMMA funParam)*)? RPAR //
	COLON type LBRACE statementList RBRACE
;

funParam: IDENT COLON type; // "name : type"

type
	returns[TypeNode result]: primitiveType | refType;
primitiveType: T_I64 | T_BOOL | T_VOID;
refType: IDENT;

statementList
	returns[Node result]: statement*;

statement
	returns[Node result]: blockLikeStatement | basicStatement;

blockLikeStatement
	returns[Node result]: ifElse | block;

block: LBRACE statementList RBRACE;

ifElse
	returns[Node result]:
	KW_IF condition=expr then=block KW_ELSE otherwise=block
	| KW_IF condition=expr then=block
;

basicStatement
	returns[Node result]:
	// declare variable
	KW_LET IDENT COLON type SEMI
	// assign expr to variable
	| IDENT ASSIGN expr SEMI
	| KW_RETURN expr? SEMI
	//	| KW_RETURN SEMI // return "void"
;

expr
	returns[Node result]:
	expr POW expr
	| expr (MULT | DIV) expr // hier + MOD
	| expr (ADD | SUB) expr
	//
	| expr (LT | LTEQ | GT | GTEQ) expr
	| expr (EQ | NEQ) expr
	| NOT expr
	| expr AND expr
	| expr OR expr
	//
	// ternary entry point
	| expr TERNARY_QM expr COLON ternaryExpr
	| LPAR expr RPAR
	| zahl
	| bool
	| varOrFunCall
;

ternaryExpr
	returns[Node result]:
	// enables nested ternaryExpr
	expr TERNARY_QM expr COLON ternaryExpr
	| expr
;

varOrFunCall
	returns[Node result]:
	// variable value
	IDENT
	// function call without parameters
	| IDENT LPAR RPAR
	// function call with one or more parameters
	| IDENT LPAR expr (COMMA expr)* RPAR
;

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
NOT: '!';

// comparission
EQ: '==';
NEQ: '!=';
GT: '>';
GTEQ: '>=';
LT: '<';
LTEQ: '<=';

ASSIGN: '=';

LPAR: '(';
RPAR: ')';
LBRACE: '{';
RBRACE: '}';
COLON: ':';
COMMA: ',';
SEMI: ';';
TERNARY_QM: '?';

KW_FUN: 'fn';
KW_RETURN: 'return';

KW_IF: 'if';
KW_ELSE: 'else';

KW_LET: 'let';

T_I64: 'int'; //TODO change to "long"
T_BOOL: 'boolean'; //TODO change to "bool"
T_VOID: 'void';

IDENT: ID_START ID_CHAR*;
fragment ID_START: [a-zA-Z_];
fragment ID_CHAR: ID_START | [0-9];

// Ignored tokens
LINE_COMMENT: '//' .*? '\r'? '\n' -> skip;
MULTILINE_COMMENT: '/*' .*? '*/' -> skip;
WHITESPACE: [ \t\n\r]+ -> skip;
ANY: .;