# me.arrdem.sad

Sad is a Clojure parser generator based on the venerable fnparse toolkit.
Implementing a parser or other AST builder comes down to two fundimental
steps: the lexer and the parser. Lexers take characters, and generate tokens
which the parser is designed to "wire" together into abstract syntax trees
by transforming the token stream.

Typically as programmers we do not just go out and create a grammar (although
someone ultimately must), rather we are often working to implement an existing
grammar so that our programs can read some standard text format such as XML,
HTML, CSS, or even other program source code in the case of compilers.

Sad is a tool for generating parser combinators from human-readable and
human-authored text in some of the many BNF grammar description languages.

## BNF Syntaxes
Sad provides support for the "original" Noam Chomsky BNF, a template based
description of the EBNF grammar, and implementations of various EBNFs
atop the same template

- Traditional BNF (no quotes, rules denoted by < > )
- EBNF quoted strings (double or single), repetition specifiers
- Jensen & Wirth EBNF as in the Pascal User Manual and Report 4th ed.
- The EBNF used to specify Java 7 [here](http://docs.oracle.com/javase/specs/jls/se7/html/jls-2.html#jls-2.4)

## Usage

Sad provides first and formost a set of functions for transforming raw text
into parser rules.

## License

Copyright © 2013 Reid "arrdem" McKenzie

Distributed under the Eclipse Public License, the same as Clojure.
