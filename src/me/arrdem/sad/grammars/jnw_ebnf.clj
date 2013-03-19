(ns me.arrdem.sad.grammars.jnw-ebnf
  (:require [lexington.lexer       :refer :all]
            [lexington.utils.lexer :refer :all]
            [name.choi.joshua.fnparse :as fnp]
            [me.arrdem.sad.grammars.util :as util]
            [me.arrdem.sad.util :as runtime-util]))

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
  :string   util/good-string-re
  :word     #"[a-zA-Z\-]+"
  :ws       #" |\t|\r|\n"
  :comment  #";+.*[\n\r]+"
  :chr      #".")

(def jnw-ebnf-lexer
  (-> jnw-ebnf-base
      (discard :ws)
      (discard :comment)
      (generate-for :word    :val util/reader)
      (generate-for :string  :val util/reader)
      (generate-for :chr     :val util/wordfn)))

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

(def opt-expr
  (fnp/semantics
   (fnp/conc lbracket Expression rbracket)
   (fn [[_0 expr _1]]
     `(fnp/opt ~expr))))

(def rep*-expr
  (fnp/semantics
   (fnp/conc lbrace Expression rbrace)
   (fn [[_0 expr _1]]
     `(fnp/rep* ~expr))))

(def alt-expr
  (fnp/semantics
   (fnp/conc lparen Expression rparen)
   (fn [[_0 expr _1]]
     `(fnp/alt ~expr))))

(def Factor
  (fnp/alt
   NonTerminal
   (fnp/semantics
    Terminal
    (fn [x]
      `(fnp/lit ~x)))
   opt-expr
   rep*-expr
   alt-expr))

(def Term
  (fnp/semantics
   (fnp/rep+ Factor)
   (fn [factors]
     `(fnp/conc ~@factors))))

(def Production
  (fnp/semantics
   (fnp/conc
    NonTerminal
    equals
    Expression
    dot)
   (fn [[sym _ exp]]
     (util/register-sym sym)
     `(def ~sym (fnp/effects
                 (fnp/semantics
                  ~exp
                  (runtime-util/get-semantics (quote ~sym)))
                 (runtime-util/get-hooks (quote ~sym)))))))

(def Syntax
  (fnp/rep+ Production))

(defn run [{str ":str" srcfile ":srcfile"}]
  (-> (if str
        str
        (slurp srcfile))
      jnw-ebnf-lexer
      (#(util/fnparse-run Syntax %1))
      (#(concat (util/make-bnf-file-prefix) %1))))
