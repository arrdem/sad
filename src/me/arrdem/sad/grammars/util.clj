(ns ^{:doc    "A collection of utilities for the various grammars. Really a
               holding pen for code which will ultimately be better managed."
      :author "Reid McKenzie"}
  me.arrdem.sad.grammars.util
  (:require (clojure [set :refer [union]]
                     [string :refer [replace]])
            [name.choi.joshua.fnparse :as fnp]))

(defn fnparse-run
  "A wrapper to shorten fnparse's unfortunately long invocation"
  [rule tokens]
  (fnp/rule-match rule
          #(println "FAILED: " %)
          #(println "LEFTOVER: " %2)
          {:remainder tokens}))

;;------------------------------------------------------------------------------
;; FNParse code emitters

(defn expression-compiler
  "A code gen for the 'expression' rule form in BNF, being a sequence of legal
alternatives. Compiles to an fnp/alt call in the multi case, otherwise skips
the superfluous (alt) call in favor of the literal first argument."
  [[t terms]]
  (let [terms (map second terms)]
    (if-not (empty? terms)
      `(~'fnp/alt ~t ~@terms)
      t)))

(defn literal-compiler
  "A code gen for literals"
  [x]
  `(~'fnp/lit ~x))

(defn term-compiler
  "A code gen for a sequence of sequentially requisite rules. Builds to a (conc)
form in the case of multiple arguments, otherwise gives the first of its
arguments, assuming that the usage was in the context of a one or more match."
  [factors]
  (if (< 1 (count factors))
    `(~'fnp/conc ~@factors)
    (first factors)))

(declare register-sym)

(defn production-compiler
  "A code generation for the basic symbol definition case. Registers the symbol
with the symbol set atom so that it will be listed in the generated forward
declaration. Generates the (def) form, and provides a wrapper around the
argument rule which attaches sad's runtime hooks."
  [[sym _ exp]]
  (register-sym sym)
  `(~'defrule ~sym
     ~exp))

;;------------------------------------------------------------------------------
;; Symbol registry and registry manipulation

(def ^{:public false :static false} sym-registry
  "The set of symbols which the grammar being parsed contains. Used to generate
forward-declarations at code emission time in order to resolve any circular rule
dependence issues and as opposed to doing a lot of work to resolve the dependance
order of the rules at hand."
  (atom #{}))

(defn register-sym
  "Adds a symbol to the symbol registry"
  [sym]
  (swap! sym-registry union #{sym}))

(defn clear-symbols
  "Clears the symbol registry"
  []
  (reset! sym-registry #{}))

;;------------------------------------------------------------------------------
;; Literal registry and manipulation

(def ^:private lit-registry (atom {}))
(def ^:private lit-code-registry (atom '()))

(defn make-name
  "Processes a literal and tries to generate a sane name for the matcher rule"
  [lit]
  (-> lit
      ;; prefix all generated names..
      (replace #"^"      "tok_")

      (replace #"[ \t]+" "_")
      (replace #"\?"     "_QMARK_")
      (replace #"/"      "_FSLASH_")
      (replace #"\\"     "_BSLASH_")
      (replace #"\+"     "_P_")
      (replace #"\-"     "_SUB_")
      (replace #"\*"     "_STAR_")
      (replace #"#"      "_POUND_")
      (replace #"\$"     "_DOL_")
      (replace #"@"      "_AT_")
      (replace #"!"      "_BANG_")
      (replace #"\."     "_DOT_")
      (replace #"<"      "_L_")
      (replace #">"      "_G_")
      (replace #"="      "_EQ_")
      (replace #":"      "_COL_")
      (replace #";"      "_SEMI_")
      (replace #"\("     "_LP_")
      (replace #"\)"     "_RP_")
      (replace #"\["     "_LB_")
      (replace #"\]"     "_RB_")
      (replace #"\^"     "_UP_")
      (replace #","      "_COMMA_")

      ;; clean up some mess
      (replace #"_+"     "_")
      (replace #"_*$"    "")
      ))

(defn install-lit [lit]
  (let [sym (symbol (make-name lit))
        code (production-compiler [sym nil (literal-compiler lit)])]
    (if-not (contains? @lit-registry lit)
      (do (swap! lit-registry assoc lit sym)
          (swap! lit-code-registry conj code)))
    sym))

;;------------------------------------------------------------------------------
;; emitted code prefix generation

(defn make-bnf-file-prefix
  "Generates the (require) statements necessary to make the symbols which the
code emitter uses available to the namespace in which the emitted code is
evaluated. Also generates the forward declarations from the set of registered
symbols."
  []
  `((require ['name.choi.joshua.fnparse :as '~'fnp]
             ['me.arrdem.sad.runtime :refer ['~'defrule]])
    (declare ~@(deref sym-registry))
    ~@(deref lit-code-registry)))
