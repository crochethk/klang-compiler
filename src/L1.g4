grammar L1;

// Creates import of the used types in the generated java source,
// so that we can use it in the parser rules without further ado.
@parser::header {
    import cc.crochethk.compilerbau.praktikum.ast.*;
    import cc.crochethk.compilerbau.praktikum.ast.types.*;
}

start
	returns[Prog result]: definition* EOF;

definition
	returns[FunDef result]:
	KW_FUN IDENT LPAR (funParam (COMMA funParam)*)? RPAR //
	COLON type LBRACE statement RBRACE;

funParam: IDENT COLON type; // "name : type"

type
	returns[Type result]: primitiveType; // | customType
primitiveType: T_I64 | T_BOOL | T_VOID;
// customType: IDENT;

statement
	returns[Node result]:
	| ifElse
	// basically statementList: one or more statements separated by SEMI
	| basicStatement (SEMI | SEMI statement);

basicStatement
	returns[Node result]:
	// declare variable
	| KW_LET IDENT COLON type
	// assign expr to variable
	| IDENT ASSIGN expr
	| KW_RETURN // return "void"
	| KW_RETURN expr;

ifElse
	returns[IfElseStat result]:
	| KW_IF expr LBRACE statement RBRACE KW_ELSE LBRACE statement RBRACE
	| KW_IF expr LBRACE statement RBRACE;

expr
	returns[Node result]:
	| expr POW expr
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
	| varOrFunCall;

ternaryExpr
	returns[Node result]:
	// enables nested ternaryExpr
	| expr TERNARY_QM expr COLON ternaryExpr
	| expr;

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