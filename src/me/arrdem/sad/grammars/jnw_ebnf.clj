(ns me.arrdem.sad.grammars.jnw-ebnf
  (:require [lexington.lexer       :refer :all]
            [lexington.utils.lexer :refer :all]
            [name.choi.joshua.fnparse :as fnp]
            [me.arrdem.sad.grammars.util :as util]))

(deflexer jnw-ebnf-base
  :lbracket "["
  :rbracket "]"
  :lparen   "("
  :rparen   ")"
  :lbrace   "{"
  :rbrace   "}"
  :assign   "="
  :or       "|"
  :dot      "."
  :string   #"\"[^\"]+\""
  :word     #"\w+"
  :ws       #" |\t|\r|\n"
  :chr      #".")

(def wordfn (fn [v] (apply str (:lexington.tokens/data v))))
(def strfn (fn [v] (apply str (drop 1 (butlast (:lexington.tokens/data v))))))

(def jnw-ebnf-lexer
  (-> jnw-ebnf-base
      (discard :ws)
      (generate-for :word    :val wordfn)
      (generate-for :string  :val strfn)
      (generate-for :chr     :val wordfn)
      ))

(util/deftoken NonTerminal :word)
(util/deftoken Terminal    :string)
(util/deftoken equals      :assign)
(util/deftoken dot         :dot)
(util/deftoken lparen      :lparen)
(util/deftoken rparen      :rparen)
(util/deftoken lbracket    :lbracket)
(util/deftoken rbracket    :rbracket)
(util/deftoken lbrace      :lbrace)
(util/deftoken rbrace      :rbrace)
(util/deftoken ortok       :or)

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

(def opt-expr
  (fnp/semantics
   (fnp/conc lbrace Expression rbracket)
   (fn [[_0 expr _1]]
     `(name.choi.joshua.fnparse/opt ~@expr))))

(def rep*-expr
  (fnp/semantics
   (fnp/conc lbrace Expression rbrace)
   (fn [[_0 expr _1]]
     `(name.choi.joshua.fnparse/rep* ~@expr))))

(def alt-expr
  (fnp/semantics
   (fnp/conc lparen Expression rparen)
   (fn [[_0 expr _1]]
     `(name.choi.joshua.fnparse/alt ~@expr))))

(def Factor
  (fnp/alt
   NonTerminal
   (fnp/semantics
    Terminal
    (fn [x] `(name.choi.joshua.fnparse/lit ~x)))
   opt-expr
   rep*-expr
   alt-expr))

(defn run [{:keys [srcfile]}]
  (-> (slurp srcfile)
      jnw-ebnf-lexer
      (#(util/fnparse-run Syntax %1))))
