grammar Klang;

// Creates import of the used types in the generated java source,
// so that we can use it in the parser rules without further ado.
@parser::header {
    import cc.crochethk.compilerbau.praktikum.ast.*;
}

start
	returns[Prog result]: definition* EOF;

definition: functionDef | structDef;

functionDef
	returns[FunDef result]:
	KW_FUN name=IDENT LPAR (param (COMMA param)* COMMA?)? RPAR //
	(RARROW type)? LBRACE funBody=statementList RBRACE
;

structDef
	returns[StructDef result]:
	KW_STRUCT name=IDENT LBRACE (param (COMMA param)* COMMA?)? RBRACE
;

param
	returns[Parameter result]: name=IDENT COLON type;

type
	returns[TypeNode result]: builtinType | customType;
builtinType: numericType | T_BOOL | T_VOID | T_STRING;
customType: IDENT;

statementList
	returns[StatementList result]: statement*;

statement
	returns[Node result]:
	blockLikeStatement
	| varDeclarationOrAssignment
	| KW_RETURN expr? SEMI
	| KW_BREAK SEMI
;

blockLikeStatement
	returns[Node result]: ifElse | loop | block;

block
	returns[StatementList result]: LBRACE statementList RBRACE;

ifElse
	returns[Node result]:
	KW_IF condition=expr then=block KW_ELSE otherwise=block
	| KW_IF condition=expr then=block
;

/**
 * Simple, condition-free loop that can be escaped using "break".
 * Break shall always only stop the loop it is called from, i.e. it
 * shall not end any potential parent loops.
 */
loop
	returns[Node result]: KW_LOOP block;

varDeclarationOrAssignment
	returns[Node result]:
	KW_LET varName=IDENT COLON type EQ expr SEMI
	// KW_LET varName=IDENT (COLON type)? SEMI  // optional type annotation
	| KW_LET varName=IDENT COLON type SEMI
	| varName=IDENT EQ expr SEMI
;

expr
	returns[Node result]:
	// arithmetic expr
	negationOp=SUB expr // negation
	| lhs=expr POW rhs=expr
	| lhs=expr (MULT | DIV) rhs=expr // hier + MOD
	| lhs=expr (ADD | SUB) rhs=expr
	// comparison expr
	| lhs=expr (LT | LTEQ | GT | GTEQ) rhs=expr
	| lhs=expr (EQEQ | NEQ) rhs=expr
	// boolean expr
	| NOT expr
	| lhs=expr AND rhs=expr
	| lhs=expr OR rhs=expr
	// ternary conditional
	| expr QM expr COLON ternaryElseBranch
	| LPAR exprInParens=expr RPAR
	| number
	| bool
	| string
	| varOrFunCall
;

ternaryElseBranch
	returns[Node result]:
	expr
	| expr QM expr COLON ternaryElseBranch
;

varOrFunCall
	returns[Node result]:
	// function call with one or more args
	IDENT LPAR expr (COMMA expr)* RPAR
	// function call without args
	| IDENT LPAR RPAR
	// variable reference
	| IDENT
;

/* Number literals */
number
	returns[Node result]:
	num=(LIT_FLOAT | LIT_INTEGER) (KW_AS typeAnnot=numericType)?
;
numericType: T_I64 | T_F64;

bool
	returns[Node result]: TRUE | FALSE;

string
	returns[Node result]: LIT_STRING;

// Lexer rules
LIT_INTEGER: DIGIT+;
LIT_FLOAT: DIGIT+ '.' DIGIT+;
fragment DIGIT: [0-9];

/**
 * String literal.
 * - '\' starts an escape sequence (which is evaluated later)
 * - SPECIAL_CHARs must be escaped using '\' in order to get their literal
 * - Multiline strings are allowed without further ado
 */
LIT_STRING: DQUOTE (ESCAPE_SEQ | NOT_SPECIAL_CHAR)* DQUOTE;
/** "\" escapes any character */
fragment ESCAPE_SEQ: '\\' .;
fragment NOT_SPECIAL_CHAR: ~["\\];
DQUOTE: '"';

TRUE: 'true';
FALSE: 'false';
INCREMENT: '++';
ADD: '+';
DECREMENT: '--';
SUB: '-';
POW: '**';
MULT: '*';
DIV: '/';

AND: '&&';
OR: '||';
NOT: '!';

// comparison
EQEQ: '==';
NEQ: '!=';
GT: '>';
GTEQ: '>=';
LT: '<';
LTEQ: '<=';

RARROW: '->';

EQ: '=';

LPAR: '(';
RPAR: ')';
LBRACE: '{';
RBRACE: '}';
COLON: ':';
COMMA: ',';
SEMI: ';';
QM: '?';
DOT: '.';

KW_FUN: 'fn';
KW_RETURN: 'return';

KW_IF: 'if';
KW_ELSE: 'else';

KW_LET: 'let';
KW_DROP: 'drop';
KW_AS: 'as';

KW_LOOP: 'loop';
KW_BREAK: 'break';

KW_STRUCT: 'struct';
KW_NULL: 'null';

T_BOOL: 'bool';
T_VOID: 'void';
T_I64: 'i64';
T_F64: 'f64';

T_STRING: 'string';

IDENT: ID_START ID_CHAR*;
fragment ID_START: [a-zA-Z_];
fragment ID_CHAR: ID_START | [0-9];

// Ignored tokens
// line comments MUST end with a newline symbol
LINE_COMMENT: '//' .*? '\r'? '\n' -> skip;
BLOCK_COMMENT: '/*' .*? '*/' -> skip;
WHITESPACE: [ \t\n\r]+ -> skip;
ANY: .;