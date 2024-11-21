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
	varDeclarationOrAssignment
	| KW_RETURN expr? SEMI
;

varDeclarationOrAssignment
	returns[Node result]:
	// KW_LET varName=IDENT COLON type ASSIGN expr SEMI // declare and assign at once
	// KW_LET varName=IDENT (COLON type)? SEMI  // optional type annotation
	KW_LET varName=IDENT COLON type SEMI
	| varName=IDENT ASSIGN expr SEMI
;

expr
	returns[Node result]:
	// arithmetic expr
	SUB expr // negation
	| lhs=expr POW rhs=expr
	| lhs=expr (MULT | DIV) rhs=expr // hier + MOD
	| lhs=expr (ADD | SUB) rhs=expr
	// comparison expr
	| lhs=expr (LT | LTEQ | GT | GTEQ) rhs=expr
	| lhs=expr (EQ | NEQ) rhs=expr
	// boolean expr
	| NOT expr
	| lhs=expr AND rhs=expr
	| lhs=expr OR rhs=expr
	// ternary conditional
	| expr QM expr COLON expr ( QM expr COLON expr)*
	| LPAR expr RPAR
	| number
	| bool
	| varOrFunCall
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

number
	returns[Node result]: FLOAT | INTEGER;

bool
	returns[Node result]: BOOLEAN;

// Lexer rules
INTEGER: [0-9]+;
FLOAT: (DIGIT+ '.' DIGIT*) | (DIGIT* '.' DIGIT+);
fragment DIGIT: [0-9];

BOOLEAN: 'true' | 'false';
ADD: '+';
SUB: '-';
POW: '**';
MULT: '*';
DIV: '/';

AND: '&&';
OR: '||';
NOT: '!';

// comparison
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
QM: '?';

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