(ns me.arrdem.sad.util)

(def bnf-semantics-registry  (atom {}))
(def bnf-hooks-registry      (atom {}))

(defn get-hooks [sym]
  (if-let [fns (sym @~'bnf-hook-registry )]
    (apply comp fns)
    identity))

(defn get-semantics [sym]
  (if-let [fns (sym @bnf-semantics-registry)]
    (apply comp fns)
    identity))

(defmacro set-hook-fn [sym fn]
  (let [gsym (gensym)]
    `(def ~gensym ~fn)
    (swap! bnf-hooks-registry assoc sym gsym)))

(defmacro set-semantic-fn [sym fn]
  (let [gsym (gensym)]
    `(def ~gensym ~fn)
    (swap! bnf-semantics-registry assoc sym gsym)))
