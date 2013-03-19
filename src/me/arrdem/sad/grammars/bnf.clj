(ns me.arrdem.sad.grammars.bnf
  (:require [lexington.lexer       :refer :all]
            [lexington.utils.lexer :refer :all]
            [name.choi.joshua.fnparse :as fnp]
            [me.arrdem.sad.grammars.util :as util]))

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

(def reader (comp read-string #(apply str %) :lexington.tokens/data))
(def wordfn (fn [v] (apply str (:lexington.tokens/data v))))
(def strfn (fn [v] (apply str (drop 1 (butlast (:lexington.tokens/data v))))))

(def bnf-lexer
  (-> bnf-base
      (discard :ws)
      (generate-for :ident   :val reader)
      (generate-for :string  :val reader)
      (generate-for :chr     :val wordfn)))

(util/deftoken ortok  :or)
(util/deftoken equals :assign)
(util/deftoken Terminal :string)
(util/deftoken NonTerminal :ident)
(util/deftoken dot    :dot)

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
   `(def ~sym ~@exp))))

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
       `(name.choi.joshua.fnparse/alt ~t ~@terms)))))

(def Term
  (fnp/semantics
   (fnp/rep+ Factor)
   (fn [factors]
     `(name.choi.joshua.fnparse/conc ~@factors))))

(def Factor
  (fnp/alt
   NonTerminal
   (fnp/semantics
    Terminal
    (fn [x] `(name.choi.joshua.fnparse/lit ~x))
    )))

(defn run [{:keys [srcfile]}]
  (-> (slurp srcfile)
      bnf-lexer
      (#(util/fnparse-run Syntax %1))))
