(ns me.arrdem.sad.grammars.bnf
  (:require [lexington.lexer :refer :all]
            [lexington.utils.lexer :refer :all]
            [name.choi.joshua.fnparse :as fnp]
            [me.arrdem.sad.grammars.util :as util]
            [me.arrdem.sad.util :as runtime-util]))

;; syntax        ::=  { rule }
;; rule          ::=  identifier  "::="  expression "."
;; expression    ::=  term { "|" term }
;; term          ::=  factor { factor }
;; factor        ::=  identifier |
;;                    quoted_symbol
;; identifier    ::=  "<" letter { letter | digit } ">"
;; quoted_symbol ::= """ { any_character } """

(deflexer bnf-base
  :assign   "::="
  :dot      "."
  :or       "|"
  :string   #"\"[^\"]+\""
  :ident    #"<\w+>"
  :ws       #" |\t|\r|\n"
  :chr      #".")

(def bnf-lexer
  (-> bnf-base
      (discard :ws)
      (generate-for :ident   :val util/reader)
      (generate-for :string  :val util/reader)
      (generate-for :chr     :val util/wordfn)))

(util/deftoken ortok       :or)
(util/deftoken equals      :assign)
(util/deftoken Terminal    :string)
(util/deftoken NonTerminal :ident)
(util/deftoken dot         :dot)

(declare Syntax Production Expression Term Factor)

(def Syntax
  (fnp/rep+ Production))

(def Production
  (fnp/semantics
   (fnp/conc
    NonTerminal
    equals
    Expression
    dot)
   (fn [[sym _ exp]]
     `(def ~sym (fnp/effects
                 (fnp/semantics
                  ~exp
                  (runtime-util/get-semantics (quote ~sym)))
                 (runtime-util/get-hooks (quote ~sym)))))))

(def Expression
  (fnp/semantics
   (fnp/conc
    Term
    (fnp/rep*
     (fnp/conc
      ortok
      Term)))
   (fn [[t terms]]
     (let [terms (map second terms)]
       (if-not (empty? terms)
         `(fnp/alt ~t ~@terms)
         t)))))

(def Term
  (fnp/semantics
   (fnp/rep+ Factor)
   (fn [factors]
     (if (< 1 (count factors))
       `(fnp/conc ~@factors)
       (first factors)))))

(def Factor
  (fnp/alt
   NonTerminal
   (fnp/semantics
    Terminal
    (fn [x] `(fnp/lit ~x))
    )))

(defn run [{:keys [srcfile str]}]
  (-> (if str
        str
        (slurp srcfile))
      bnf-lexer
      (#(util/fnparse-run Syntax %1))
      (#(concat (util/make-bnf-file-prefix) %1))))
