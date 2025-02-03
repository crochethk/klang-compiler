grammar Klang;

// Creates import of the used types in the generated java source,
// so that we can use it in the parser rules without further ado.
@parser::header {
    import cc.crochethk.klang.ast.*;
}

start
	returns[Prog result]: definition* EOF;

definition: KW_FUN functionDef | structDef;

functionDef
	returns[FunDef result]:
	name=IDENT LPAR params? RPAR //
	(RARROW type)? LBRACE funBody=statementList RBRACE
;

structDef
	returns[StructDef result]:
	KW_STRUCT name=IDENT LBRACE (
		params TRIDASH (methodDefs+=functionDef)+
		| params? TRIDASH?
		| TRIDASH? (methodDefs+=functionDef)+
	) RBRACE
;

params: list+=parameter (COMMA list+=parameter)* COMMA?;

parameter
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
	| KW_DROP refTypeVarName=IDENT SEMI
	| (funCall | memberAccessor) SEMI
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
	// Declaration schemes:
	//      varName, type, expr
	//      varName, type, ---
	//      varName, ---, expr
	KW_LET varName=IDENT (
		(COLON type) (EQ expr)
		| COLON type
		| EQ expr
	) SEMI
	| varName=IDENT EQ expr SEMI
	| structFieldAssignStat
;

structFieldAssignStat
	returns[FieldAssignStat result]: memberAccessor EQ expr SEMI;

expr
	returns[Expr result]:
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
	| nullLit
	| varOrFunCall
	| constructorCall
	| memberAccessor
;

memberAccessor
	returns[MemberAccessChain result]:
	owner=varOrFunCall (DOT memberChain+=fieldOrMethCall)+
;
fieldOrMethCall
	returns[MemberAccess result]:
	fieldName=IDENT
	| methCall=funCall
;

constructorCall
	returns[Expr result]:
	structName=IDENT LBRACE (
		args+=expr (COMMA args+=expr)* COMMA?
	)? RBRACE
;

ternaryElseBranch
	returns[Expr result]:
	expr
	| expr QM expr COLON ternaryElseBranch
;

varOrFunCall
	returns[Expr result]:
	funCall
	// variable reference
	| varName=IDENT
;

funCall
	returns[Expr result]:
	// function call with one or more args
	name=IDENT LPAR args+=expr (COMMA args+=expr)* RPAR
	// function call without args
	| name=IDENT LPAR RPAR
;

/* Number literals */
number
	returns[Expr result]:
	num=(LIT_FLOAT | LIT_INTEGER) (KW_AS typeAnnot=numericType)?
;
numericType: T_I64 | T_F64;

bool
	returns[Expr result]: TRUE | FALSE;

string
	returns[Expr result]: LIT_STRING;

nullLit
	returns[Expr result]: KW_NULL;

// Lexer rules
LIT_INTEGER: SUB? DIGIT+;
LIT_FLOAT: SUB? DIGIT+ '.' DIGIT+;
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
TRIDASH: '---';
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