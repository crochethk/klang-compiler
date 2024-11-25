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
	KW_FUN funName=IDENT LPAR (funParam (COMMA funParam)* COMMA?)? RPAR //
	COLON type LBRACE funBody=statementList RBRACE
;

funParam: name=IDENT COLON type;

type
	returns[TypeNode result]: primitiveType | refType;
primitiveType: T_I64 | T_F64 | T_BOOL | T_VOID;
refType: IDENT;

statementList
	returns[StatementList result]: statement*;

statement
	returns[Node result]:
	blockLikeStatement
	| varDeclarationOrAssignment
	| KW_RETURN expr? SEMI
;

blockLikeStatement
	returns[Node result]: ifElse | block;

block
	returns[Node result]: LBRACE statementList RBRACE;

ifElse
	returns[Node result]:
	KW_IF condition=expr then=block KW_ELSE otherwise=block
	| KW_IF condition=expr then=block
;

varDeclarationOrAssignment
	returns[Node result]:
	KW_LET varName=IDENT COLON type ASSIGN expr SEMI
	// KW_LET varName=IDENT (COLON type)? SEMI  // optional type annotation
	| KW_LET varName=IDENT COLON type SEMI
	| varName=IDENT ASSIGN expr SEMI
;

expr
	returns[Node result]:
	// arithmetic expr
	negationOp=SUB expr // negation // TODO <------------ implement UnaryOpExpr.neg 
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
	| expr QM expr COLON ternaryElseBranch
	// TODO <---- modified ternary
	| LPAR exprInParens=expr RPAR
	| number
	| bool
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

number
	returns[Node result]: //
	num=LIT_FLOAT litTypeSuffix?
	| num=LIT_INTEGER litTypeSuffix?
;
litTypeSuffix: USCORE? (T_F64 | T_I64);

bool
	returns[Node result]: TRUE | FALSE;

// Lexer rules
LIT_INTEGER: DIGIT+;
LIT_FLOAT: DIGIT+ '.' DIGIT+;
fragment DIGIT: [0-9];

TRUE: 'true';
FALSE: 'false';
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
USCORE: '_';

KW_FUN: 'fn';
KW_RETURN: 'return';

KW_IF: 'if';
KW_ELSE: 'else';

KW_LET: 'let';

T_I64: 'i64';
T_BOOL: 'bool';
T_VOID: 'void';
T_F64: 'f64';

IDENT: ID_START ID_CHAR*;
fragment ID_START: [a-zA-Z_];
fragment ID_CHAR: ID_START | [0-9];

// Ignored tokens
LINE_COMMENT: '//' .*? '\r'? '\n' -> skip;
MULTILINE_COMMENT: '/*' .*? '*/' -> skip;
WHITESPACE: [ \t\n\r]+ -> skip;
ANY: .;