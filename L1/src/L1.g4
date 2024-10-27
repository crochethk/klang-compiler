grammar L1;
start
	returns[long result]: expr;

expr
	returns[long result]:
	| expr POW expr
	| expr (MULT | DIV) expr
	| expr (ADD | SUB) expr
	| LPAR expr RPAR
	| zahl;

zahl
	returns[long result]: NUMBER;

NUMBER: [0-9]+;
ADD: '+';
SUB: '-';
POW: '**';
MULT: '*';
DIV: '/';
LPAR: '(';
RPAR: ')';