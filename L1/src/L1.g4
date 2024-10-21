grammar L1;
start
	returns[long result]: expr;

expr
	returns[long result]:
	| expr (MULT | DIV) expr
	| expr (ADD | SUB) expr
	| LPAR expr RPAR
	| zahl;

zahl
	returns[long result]: NUMBER;

NUMBER: [0-9]+;
ADD: '+';
SUB: '-';
MULT: '*';
DIV: '/';
LPAR: '(';
RPAR: ')';