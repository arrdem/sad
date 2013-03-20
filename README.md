# Sad

```clojure 
[me.arrdem.sad "0.1.4"]
```

Sad is a Clojure parser compiler based on the venerable fnparse toolkit. The 
name is a joke on a Haskell parser generator library entitled Happy, and is an
accurate reflection of my mood upon seeing the dozens of pages which some 
grammars I have contemplated implementing span.

## Compiling Grammars
Sad provides a set of functions for transforming raw text into parser rules, and 
is designed to generate such rules, and to make interacting with generated rules
as simple and painless as possible by embedding side-effect and value 
transformation calls which can be customized after grammar generation. As a 
Clojure program, sad can be built to a standalone jar and run via
`java -jar sad.jar [options]` or run from source via `lein run [options]`.
Sad accepts the following command line parameters:

- `:str` - must be followed by a string, will apply the selected compiler to the string.
- `:srcfile` - must be followed by a string being a file path, will apply the selected compiler to the file.
- `:grammar` - the name of a supported grammar, being one of the supported grammar strings listed above. Use of a nonstandard string will cause sad to be sad and crash.

## Using Compiled Grammars
If we invoke sad directly from the Clojure repl,
```clojure
user> (require 'me.arrdem.sad)
nil
user> (def sample-grammar
        (str "<bar>    ::= \"bar\"."
             "<o>      ::= \"o\"."
             "<oseq>   ::= <o> <oseq> | <o> ."
             "<f>      ::= \"f\"."
             "<foobar> ::= <f> <oseq> <bar>."))
user/sample-grammar

;; require the actual bnf grammar compiler

user> (require 'me.arrdem.sad.grammars.bnf)
nil
user> (def compiled-sample-grammar
    (me.arrdem.sad.grammars.bnf/run {":str" sample-grammar}))
user/compiled-sample-grammar

;; compiled-sample-grammar is now a list of S expressions (Clojure code) which
;; we can either print or eval. One could pretty-print, read and coppy the code
;; via stdout, or pipe it to a file. To illustrate the point that sad kicks
;; out almost an entire namespace we will create a new namespace and eval the
;; generated code therein.

user> (eval `(do (create-ns 'user.sample-language)
                 (in-ns 'user.sample-language)
                 ;; insert the expressions in the compiled grammar
                 ~@compiled-sample-grammar
                 ;; create a test function for running the parser
                 (defn ~'run [tokens#]
                   (-> tokens#
                     ((partial assoc {} :remainder))
                     ((partial ~'fnp/rule-match
                               ~'<foobar>
                               #(println "FAILED: " %)
                               #(println "LEFTOVER: " %2)))))))
nil
#'user/sample-grammar
nil
#'user/compiled-sample-grammar
#'user.sample-language/run

;; All set up lets run this puppy....

user.sample-language> (run ["f" "o" "o" "bar"])
("f" ("o" "o") "bar")

;; Okay great! The compiled parser works a treat..
;; now just to show off pre-hooks...

user.sample-language> (util/set-pre-hook '<o> #(println "looking for an \"o\"!"))
{pre/<o> #<sample_language$eval1814$fn__1815 user.sample_language$eval1814$fn__1815@12d26c5f>}
user.sample-language> (run ["f" "o" "o" "bar"])
looking for an "o"!
looking for an "o"!
looking for an "o"!
looking for an "o"!
looking for an "o"!
looking for an "o"!
looking for an "o"!
looking for an "o"!
looking for an "o"!
looking for an "o"!
("f" ("o" "o") "bar")

;; and post-hooks... (which only trigger if the rule matched)
user.sample-language> (util/set-post-hook '<o> #(println "got an \"o\"!"))
{post/<o> #<sample_language$eval2199$fn__2200 user.sample_language$eval2199$fn__2200@6e717955>,
 pre/<o> #<sample_language$eval1814$fn__1815 user.sample_language$eval1814$fn__1815@12d26c5f>}
user.sample-language> (run ["f" "o" "o" "bar"])
looking for an "o"!
got an "o"!
looking for an "o"!
got an "o"!
looking for an "o"!
looking for an "o"!
looking for an "o"!
got an "o"!
looking for an "o"!
got an "o"!
("f" ("o" "o") "bar")

;; Awesome! Now lets just make that pesky O group one string using semantics....
user.sample-language> (util/set-semantics '<oseq> (partial apply str))
{<oseq> #<core$partial$fn__4070 clojure.core$partial$fn__4070@3cbc5edf>}
user.sample-language> (run ["f" "o" "o" "bar"])
("f" "oo" "bar")

;; And finally stringify the entire match the same way
user.sample-language> (util/set-semantics '<foobar> (partial apply str))
{<foobar> #<core$partial$fn__4070 clojure.core$partial$fn__4070@224e59d9>,
 <oseq> #<core$partial$fn__4070 clojure.core$partial$fn__4070@3cbc5edf>}
user.sample-language> (run ["f" "o" "o" "bar"])
"foobar"
```
## Project Status
Sad is currently at version 0.1.4, stable & functioning but API hugely in flux.

## Done
- Traditional BNF (no quotes, rules denoted by < > ). Invoked as "bnf".
- Jensen & Wirth EBNF as in the Pascal User Manual and Report 4th ed. Invoked as "wirth".
- Generated Grammar hooks
- Generated (require) for deps
- Generated abbreviated symbols
- Generated forward declaration for entire grammars
- Better formatting via pprint's code settings

### ToDo
- EBNF quoted strings (double or single), repetition specifiers, regexes
- The EBNF used to specify Java 7 [here](http://docs.oracle.com/javase/specs/jls/se7/html/jls-2.html#jls-2.4)

## License
Copyright © 2013 Reid "arrdem" McKenzie

Distributed under the Eclipse Public License, the same as Clojure.
