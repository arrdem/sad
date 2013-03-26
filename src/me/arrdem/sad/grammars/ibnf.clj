(ns ^{:doc    "An implementation of a lexer, parser and code gen for an
               indentation and newline based BNF grammar as used here:
               www2.informatik.uni-halle.de/lehre/pascal/sprache/pas_bnf.html"
      :author "Reid McKenzie"
      :added  "0.1.5"}
  me.arrdem.sad.grammars.ibnf
  (:require [lexington.lexer :refer :all]
            [lexington.utils.lexer :refer :all]
            [name.choi.joshua.fnparse :as fnp]
            [me.arrdem.sad.grammars.util :as gutil]
            [me.arrdem.sad.lexers.util :as lutil]))

;;------------------------------------------------------------------------------
;; Set up the lexer for the IBNF gramar

(lutil/make-lexer ibnf-base
  :ws #"[ \t]"
  :comment lutil/lisp-comment
  (lutil/deftoken equals ":\n")
  (lutil/deftoken dot "\n\n")
  (lutil/deftoken ortok "\n")
  (lutil/deftoken Terminal lutil/string)
  (lutil/deftoken NonTerminal #"[a-zA-Z][a-zA-Z0-9\-]*")
  :chr #"."
  )

(def ibnf-lexer
  (-> ibnf-base

      ;; Ditch junk tokens
      (discard :ws)
      (discard :comment)

      ;; Process special symbols
      (generate-for :ident       :val lutil/readerfn)
      (generate-for :Terminal    :val lutil/readerfn)
      (generate-for :NonTerminal :val lutil/readerfn)
      (generate-for :chr         :val lutil/wordfn)))

(defmacro p [& rest]
  `(fn [& _#]
     (println ";" ~@rest)))

;;------------------------------------------------------------------------------
;; Declare & define productions

(declare Syntax Production Expression Term Factor)

(def Expression
  (fnp/semantics
   (fnp/conc
    (fnp/failpoint
     Term
     (p "[Expression] no term found"))
    (fnp/failpoint
     (fnp/rep*
      (fnp/conc
       ortok
       Term))
     (p "[Expression] no expression tail found")))
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
    (fnp/failpoint
     NonTerminal
     (p "[Production] no production naming nonterminal"))
    (fnp/failpoint
     equals
     (p "[Production] no production assignment operator"))
    (fnp/failpoint
     Expression
     (p "[Production] no production expression part found"))
    (fnp/failpoint
     dot
     (fn [& rest] "; [Production] no production terminator found\n;" rest)))
   gutil/production-compiler))

(def Syntax
  (fnp/semantics
   (fnp/rep+
    (fnp/alt
     (fnp/constant-semantics
      (fnp/alt ortok dot)
      nil)
     Production))
   (partial remove nil?)))

;;------------------------------------------------------------------------------
;; And throw a run interface on this grammar

(defn run [{str ":str" srcfile ":srcfile"}]
  (-> (if str str
        (slurp srcfile))
      ibnf-lexer
      (#(fnp/rule-match Syntax prn prn {:remainder %1}))
      (#(concat (gutil/make-bnf-file-prefix) %1))))
