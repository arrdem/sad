(ns me.arrdem.sad.util
  (:require [clojure.pprint :refer :all]))

(def nothing (fn [&  _] nil))
(def code-pp #(with-pprint-dispatch code-dispatch (pprint %)))

(def bnf-semantics-registry  (atom {}))
(def bnf-hooks-registry      (atom {}))

(defn get-prefixed-hook [prefix sym]
  (or ((symbol prefix (name sym))
       @bnf-hooks-registry) nothing))

(def get-post-hook (partial get-prefixed-hook "post"))
(def get-pre-hook (partial get-prefixed-hook "pre"))

(defn get-semantics [sym]
  (or (sym @bnf-semantics-registry) identity))

(defn set-prefixed-hook [prefix sym fn]
  (swap! bnf-hooks-registry
         assoc (symbol prefix (name sym))
         (if (instance? clojure.lang.IFn fn)
           fn (eval fn))))

(def set-post-hook (partial set-prefixed-hook "post"))
(def set-pre-hook  (partial set-prefixed-hook "pre"))

(defn set-semantics [sym fn]
  (if (instance? clojure.lang.IFn fn)
    (swap! bnf-semantics-registry assoc sym fn)
    (swap! bnf-semantics-registry assoc sym (eval fn))))
