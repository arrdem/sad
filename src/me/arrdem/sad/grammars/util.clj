(ns ^{:doc    "A collection of utilities for the various grammars. Really a
               holding pen for code which will ultimately be better managed."
      :author "Reid McKenzie"}
  me.arrdem.sad.grammars.util
  (:require [name.choi.joshua.fnparse :as fnp]
            [clojure.set           :refer [union]]))

(defn fnparse-run
  "A wrapper to shorten fnparse's unfortunately long invocation"
  [rule tokens]
  (fnp/rule-match rule
          #(println "FAILED: " %)
          #(println "LEFTOVER: " %2)
          {:remainder tokens}))

;;------------------------------------------------------------------------------
;; emitted code prefix generation

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

(defn make-bnf-file-prefix
  "Generates the (require) statements necessary to make the symbols which the
code emitter uses available to the namespace in which the emitted code is
evaluated. Also generates the forward declarations from the set of registered
symbols."
  []
  [`(require ['name.choi.joshua.fnparse :as '~'fnp]
             ['me.arrdem.sad.util :as '~'util])
   `(declare ~@(deref sym-registry))])

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

(defn production-compiler
  "A code generation for the basic symbol definition case. Registers the symbol
with the symbol set atom so that it will be listed in the generated forward
declaration. Generates the (def) form, and provides a wrapper around the
argument rule which attaches sad's runtime hooks."
  [[sym _ exp]]
  (register-sym sym)
  `(def ~sym (~'fnp/semantics
              (~'fnp/conc
               (~'fnp/effects
                ((~'util/get-pre-hook (quote ~sym))))
               (~'fnp/semantics ~exp
                                (~'util/get-semantics (quote ~sym)))
               (~'fnp/effects
                ((~'util/get-post-hook (quote ~sym)))))
              second)))
