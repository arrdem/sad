# Sad

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
atop the same template.

## Done
- Traditional BNF (no quotes, rules denoted by < > ). Invoked as "bnf".
- Jensen & Wirth EBNF as in the Pascal User Manual and Report 4th ed. Invoked as "wirth".

### ToDo
- EBNF quoted strings (double or single), repetition specifiers, regexes
- The EBNF used to specify Java 7 [here](http://docs.oracle.com/javase/specs/jls/se7/html/jls-2.html#jls-2.4)

## Compiling Grammars

Sad provides first and foremost a set of functions for transforming raw text
into parser rules, and is primarily intended for use first in generating such
rules, and secondly as a library for making interacting with generated rules
as simple and painless as possible.

As a Clojure program, sad can be built to a standalone jar and run via
`java -jar sad.jar [options]` or run from source via `lein run [options]`.
Sad accepts the following command line parameters:

    :str      - must be followed by a string,
                  will apply the selected compiler to the string.
    :srcfile  - must be followed by a string being a file path,
                  will apply the selected compiler to the file.
    :grammar  - the name of a supported grammar, being one of the supported grammar
                  strings listed above. Use of a nonstandard string will cause
                  sad to be sad and crash.

## Using Compiled Grammars

If we invoke sad directly,
```clojure
> (require 'me.arrdem.sad)
nil
> (me.arrdem.sad/-main ":str"
                       "<bar>  ::= \"bar\".
                        <o>    ::= \"o".
                        <oseq> ::= <o> <oseq> | <o> .
                        <foobar> ::= \"f\" <oseq> <bar>.")

(require '(name.choi.joshua.fnparse) '(me.arrdem.sad.util))
(def <bar>
 (name.choi.joshua.fnparse/effects
   (name.choi.joshua.fnparse/semantics
     (name.choi.joshua.fnparse/conc
       (name.choi.joshua.fnparse/lit "bar"))
     (me.arrdem.sad.util/get-semantics '<bar>))
   (me.arrdem.sad.util/get-hooks '<bar>)))
(def <o>
 (name.choi.joshua.fnparse/effects
   (name.choi.joshua.fnparse/semantics
     (name.choi.joshua.fnparse/conc (name.choi.joshua.fnparse/lit "o"))
     (me.arrdem.sad.util/get-semantics '<o>))
   (me.arrdem.sad.util/get-hooks '<o>)))
(def <oseq>
 (name.choi.joshua.fnparse/effects
   (name.choi.joshua.fnparse/semantics
     (name.choi.joshua.fnparse/alt
       (name.choi.joshua.fnparse/conc <o> <oseq>)
       (name.choi.joshua.fnparse/conc <o>))
     (me.arrdem.sad.util/get-semantics '<oseq>))
   (me.arrdem.sad.util/get-hooks '<oseq>)))
(def <foobar>
 (name.choi.joshua.fnparse/effects
   (name.choi.joshua.fnparse/semantics
     (name.choi.joshua.fnparse/conc
       (name.choi.joshua.fnparse/lit "f")
       <oseq>
       <bar>)
     (me.arrdem.sad.util/get-semantics '<foobar>))
   (me.arrdem.sad.util/get-hooks '<foobar>)))

nil
```
As you can see, even for such a simple grammar sad's output is relatively large.
As most languages contain many more rules to which side-effects and
transformations must be added sad automagically creates side-effect hooks and
fnparse semantics hooks which allow a user to separate the definition of hooks
from the generated grammar itself and for the most part frees programmers from
having to directly edit the generated grammar.

## License

Copyright © 2013 Reid "arrdem" McKenzie

Distributed under the Eclipse Public License, the same as Clojure.
