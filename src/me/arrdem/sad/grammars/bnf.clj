(ns ^{:doc    "An implementation of a lexer, parser and code gen for traditional
               carrot-braced BNF as used by Naur."
      :author "Reid McKenzie"
      :added  "0.1.0"}
  me.arrdem.sad.grammars.bnf
  (:require [lexington.lexer :refer :all]
            [lexington.utils.lexer :refer :all]
            [name.choi.joshua.fnparse :as fnp]
            [me.arrdem.sad.grammars.util :as gutil]
            [me.arrdem.sad.lexers.util :as lutil]))

(lutil/make-lexer bnf-base
  :ws lutil/whitespace
  :comment lutil/lisp-comment
  (lutil/deftoken equals "::=")
  (lutil/deftoken ortok "|")
  (lutil/deftoken Terminal lutil/string)
  (lutil/deftoken NonTerminal #"<.*?>")
  (lutil/deftoken dot ".")
  :chr #"."
  )

(def bnf-lexer
  (-> bnf-base
      (discard :ws)
      (discard :comment)
      (generate-for :ident   :val lutil/readerfn)
      (generate-for :string  :val lutil/readerfn)
      (generate-for :chr     :val lutil/wordfn)))

;;------------------------------------------------------------------------------
;; Declare & define productions

(declare Syntax Production Expression Term Factor)

(def Expression
  (fnp/semantics
   (fnp/conc
    Term
    (fnp/rep*
     (fnp/conc
      ortok
      Term)))
   gutil/expression-compiler))

(def Factor
  (fnp/alt
   NonTerminal
   (fnp/semantics
    Terminal
    gutil/literal-compiler)))

(def Term
  (fnp/semantics
   (fnp/rep+ Factor)
   gutil/term-compiler))

(def Production
  (fnp/semantics
   (fnp/conc
    NonTerminal
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
      bnf-lexer
      (#(gutil/fnparse-run Syntax %1))
      (#(concat (gutil/make-bnf-file-prefix) %1))))
