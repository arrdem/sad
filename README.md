# Sad

```clojure
[me.arrdem.sad "0.2.0"]
```

Sad is a Clojure parser compiler based on the venerable fnparse toolkit. The
name is a joke on a Haskell parser generator library entitled Happy, and is an
accurate reflection of my mood upon seeing the dozens of pages which some
grammars I have contemplated implementing span.

## REPL usage
```clojure
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
user.sample-language> (run ["f" "bar"])
FAILED:  {:remainder [f bar]}
nil
```

## Command line usage
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


## Runtime support for grammars
Grammars compiled by sad use macro symbol `me.arrdem.sad.runtime/deftoken` which
creates bindings to side-effect hooks and semantic transformations. As of 0.2.0, hooks
and transformations are not stored in global state, but rather are stored in the
following rebindable vars:

- `me.arrdem.sad.runtime.hooks/*pre-hook-ns*`
- `me.arrdem.sad.runtime.hooks/*post-hook-ns*`
- `me.arrdem.sad.runtime.semantics/*semantics-ns*`

When a rule created with `(defrule r ...)` is invoked, the hooks system attempts to
resolve the symbol `r` in symbol namespace bound to `*pre-hook-ns*`. If there is a
namespace symbol bound there, sad attempts to resolve a var named `r` in that namespace
and execute it. If there is no bound namespace, or no such function r no action is taken
and fnparse will process the rule normally as if there was no wrapper for hooks and
semantics.

When the rule r is invoked and succeeds, more wrapper code will attempt to resolve the
symbol `r` in the namespace bound to `*semantics-ns*`, and as with the hooks will
execute the symbol bound there as if it were in an fnparse `(semantics)` group with the
rule matched.

## Project Status
Sad is currently at version 0.2.0, stable & functioning but API hugely in flux.

### Done
- Traditional BNF (no quotes, rules denoted by < > ). Invoked as "bnf".
- Jensen & Wirth EBNF as in the Pascal User Manual and Report 4th ed. Invoked as "wirth".
- Generated Grammar hooks
- Generated (require) for deps
- Generated abbreviated symbols
- Generated forward declaration for entire grammars
- Better formatting via pprint's code settings

### In the works
- An api (partially implemented) for tracking where in the grammar is being executed. A trivial implementation exists and the stack is accessible at `me.arrdem.sad.runtime.stack/rule-stack` but as it is currently a global atom usage is possible only in single-threaded cases.

### ToDo
- EBNF quoted strings (double or single), repetition specifiers, regexes
- The EBNF used to specify Java 7 [here](http://docs.oracle.com/javase/specs/jls/se7/html/jls-2.html#jls-2.4)

## License
Copyright © 2013 Reid "arrdem" McKenzie

Distributed under the Eclipse Public License, the same as Clojure.
