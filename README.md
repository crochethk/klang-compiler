# Klang compiler
This project is a compiler for the "Klang" programming language, developed as 
part of a Compiler Construction course. The project involves multiple stages of 
compilation, including lexical analysis, parsing, abstract syntax tree (AST) 
generation, type checking, and code generation for both Java Byte Code (JBC) and
 GNU Assembly (ASM). Another implemented optional stage is the pretty printer, 
which allows converting the AST back into its source code representation.
The AST utilizes the visitor pattern: Nodes are "Visitables" and the various 
compilation stages traversing and modifying it are "Visitors".
