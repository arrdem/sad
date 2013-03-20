(ns ^{:doc    "An implementation of a lexer, parser and code gen for EBNF as
               used in The Pascal User Manual and Report 4th ed. by Wirth and
               Jensen. Hence the name jnw-ebnf."
      :author "Reid McKenzie"
      :added  "0.1.0"}
  me.arrdem.sad.grammars.jnw-ebnf
  (:require [lexington.lexer :refer :all]
            [lexington.utils.lexer :refer :all]
            [name.choi.joshua.fnparse :as fnp]
            [me.arrdem.sad.grammars.util :as gutil]
            [me.arrdem.sad.lexers.util :as lutil :refer [make-lexer
                                                           deftoken]]))

(lutil/make-lexer jnw-ebnf-base
  :ws lutil/whitespace
  :comment lutil/comment
  (deftoken lbracket "[")
  (deftoken rbracket "]")
  (deftoken lparen "(")
  (deftoken rparen ")")
  (deftoken lbrace "{")
  (deftoken rbrace "}")
  (deftoken equals "=")
  (deftoken ortok "|")
  (deftoken dot ".")
  (deftoken Terminal lutil/string)
  (deftoken NonTerminal lutil/word)
  :chr #"."
  )

(def jnw-ebnf-lexer
  (-> jnw-ebnf-base
      (discard :ws)
      (discard :comment)
      (generate-for :word    :val lutil/readerfn)
      (generate-for :string  :val lutil/readerfn)
      (generate-for :chr     :val lutil/wordfn)))

;;------------------------------------------------------------------------------
;; Declare & define productions

(declare Syntax Production Expression Term Factor)

(def Expression
  (fnp/semantics
   (fnp/conc Term
             (fnp/rep*
              (fnp/conc ortok
                        Term)))
   gutil/expression-compiler))

(def opt-expr
  (fnp/semantics
   (fnp/conc lbracket
             Expression
             rbracket)
   (fn [[_0 expr _1]]
     `(~'fnp/opt ~expr))))

(def rep*-expr
  (fnp/semantics
   (fnp/conc lbrace
             Expression
             rbrace)
   (fn [[_0 expr _1]]
     `(~'fnp/rep* ~expr))))

(def alt-expr
  (fnp/semantics
   (fnp/conc lparen
             Expression
             rparen)
   (fn [[_0 expr _1]]
     `(~'fnp/alt ~expr))))

(def Factor
  (fnp/alt
   NonTerminal
   (fnp/semantics
    Terminal
    gutil/literal-compiler)
   opt-expr
   rep*-expr
   alt-expr))

(def Term
  (fnp/semantics
   (fnp/rep+ Factor)
   gutil/term-compiler))

(def Production
  (fnp/semantics
   (fnp/conc NonTerminal
             equals
             Expression
             dot)
   gutil/production-compiler))

(def Syntax
  (fnp/rep+ Production))

;;------------------------------------------------------------------------------
;; And throw a run interface on this grammar

(defn run [{str ":str" srcfile ":srcfile"}]
  (-> (if str str
        (slurp srcfile))
      jnw-ebnf-lexer
      (#(gutil/fnparse-run Syntax %1))
      (#(concat (gutil/make-bnf-file-prefix) %1))))
