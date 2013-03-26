(ns ^{:doc    "An implementation of a lexer, parser and code gen for traditional
               carrot-braced BNF as used by Naur."
      :author "Reid McKenzie"
      :added  "0.1.0"}
  me.arrdem.sad.grammars.ibnf
  (:require [lexington.lexer :refer :all]
            [lexington.utils.lexer :refer :all]
            [name.choi.joshua.fnparse :as fnp]
            [me.arrdem.sad.grammars.util :as gutil]
            [me.arrdem.sad.lexers.util :as lutil]))

(lutil/make-lexer bnf-base
  :ws #"[ \t]"
  :comment lutil/comment
  (lutil/deftoken equals ":\n")
  (lutil/deftoken dot "\n\n")
  (lutil/deftoken ortok "\n")
  (lutil/deftoken Terminal lutil/string)
  (lutil/deftoken NonTerminal #"[a-z\-]+")
  :chr #"."
  )

(def bnf-lexer
  (-> bnf-base
      (discard :ws)
      (discard :comment)
      (generate-for :ident       :val lutil/readerfn)
      (generate-for :Terminal    :val lutil/readerfn)
      (generate-for :NonTerminal :val lutil/readerfn)
      (generate-for :chr         :val lutil/wordfn)))

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
      (#(fnp/rule-match Syntax prn prn {:remainder %1}))
      (#(concat (gutil/make-bnf-file-prefix) %1))))