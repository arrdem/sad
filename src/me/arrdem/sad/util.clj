(ns ^{:doc     "Implements the runtime hooks which sad parsers can use."
      :author  "Reid McKenzie"
      :version "0.1.2"}
  me.arrdem.sad.util
  (:require [clojure.pprint :refer :all]))

(def nothing
  "A varargs function which returns nil doing nothing. Used as a placeholder
for when a hook is called for and there is no registered side-effect hook."
  (fn [&  _] nil))

;; ToDo move code-pprint somewhere more general
(def code-pp
  "A wrapper around pprint which makes its code-formatting setting more
easily accessed."
  #(with-pprint-dispatch code-dispatch (pprint %)))

;;------------------------------------------------------------------------------
;;------------------------------------------------------------------------------
;; Side-effect "hook" system

(def bnf-hooks-registry (atom {}))

(defn set-prefixed-hook [prefix sym fn]
  (swap! bnf-hooks-registry
         assoc (symbol prefix (name sym))
         (if (instance? clojure.lang.IFn fn)
           fn (eval fn))))

(def set-post-hook (partial set-prefixed-hook "post"))
(def set-pre-hook  (partial set-prefixed-hook "pre"))

(defn get-prefixed-hook
  "Searches the prefix table for a prefixed symbol hook, returning the hook
if one is registered otherwise returning the function Nothing."
  [prefix sym]
  (or ((symbol prefix (name sym))
       @bnf-hooks-registry) nothing))

(def get-post-hook
  "Wrapper on get-prefixed-hook which instructs it to look for a \"post\" hook."
  (partial get-prefixed-hook "post"))

(def get-pre-hook
  "Wrapper on get-prefixed-hook which instructs it to look for a \"pre\" hook."
  (partial get-prefixed-hook "pre"))

;;------------------------------------------------------------------------------
;;------------------------------------------------------------------------------
;; Semantics manipulation system

(def bnf-semantics-registry (atom {}))

(defn get-semantics [sym]
  "Searches the semantics table for a transform function, returning
clojure.core/identity if there is no registered transform."
  (or (sym @bnf-semantics-registry) identity))

(defn set-semantics
  "Relates the function or expression fn to the symbol sym in the semantics
table, evaluating the expression with the expectation of receiving a function
if the fn argument does not already implement clojure.lang.IFn."
  [sym fn]
  (if (instance? clojure.lang.IFn fn)
    (swap! bnf-semantics-registry assoc sym fn)
    (swap! bnf-semantics-registry assoc sym (eval fn))))
