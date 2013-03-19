(ns me.arrdem.sad.grammars.util
  (:require [lexington.lexer       :refer :all]
            [lexington.utils.lexer :refer :all]
            [name.choi.joshua.fnparse :as fnp]))

(defmacro deftoken [symbol val]
  `(def ~symbol
     (fnp/semantics
      (fnp/term
       #(= (:lexington.tokens/type %1) ~val))
      :val)))

(def whitespace-re    #" |\t|\r|\n")
(def simple-string-re #"\"[^\"]+\"")
(def good-string-re   #"\"[^\"\\]*(?:\\.[^\"\\]*)*\"")

(def strfn
  (fn [v]
    (-> (:lexington.tokens/data v)
        butlast
        (#(drop 1 %1))
        (#(apply str %1)))))

(defn fnparse-run [rule tokens]
  (apply vector
         (fnp/rule-match
          rule
          #(println "FAILED: " %)
          #(println "LEFTOVER: " %2)
          {:remainder tokens})))

(defn make-bnf-file-prefix []
  ['(require '(name.choi.joshua.fnparse)
             '(me.arrdem.sad.util))])

(def reader (comp read-string #(apply str %) :lexington.tokens/data))
(def wordfn (fn [v] (apply str (:lexington.tokens/data v))))
(def strfn (fn [v] (apply str (drop 1 (butlast (:lexington.tokens/data v))))))
