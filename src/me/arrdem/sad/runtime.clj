(ns ^{:doc   "A wrapper namespace for some of the vars which compiled grammars
              rely on when executed by other clojure code. Provides various
              bindings structures for executing Sad output with hooks, semantics
              and debugging."
      :author "Reid McKenzie"
      :added "0.1.5"}
  me.arrdem.sad.runtime
  (:require (me.arrdem.sad.runtime [semantics :refer [get-semantics]]
                                   [hooks :refer [get-pre-hook get-post-hook]]
                                   [stack :refer [scope-push scope-pop]])
            [name.choi.joshua.fnparse :as fnp]))

(defmacro with-semantics [semantics-ns & forms]
  `(binding [me.arrdem.sad.runtime.semantics/*semantics-ns* ~semantics-ns]
     ~@forms))

(defmacro with-hooks [pre-hook-ns post-hook-ns & forms]
  `(binding [me.arrdem.sad.runtime.hooks/*pre-hook-ns* ~pre-hook-ns
             me.arrdem.sad.runtime.hooks/*post-hook-ns* ~post-hook-ns]
     ~@forms))

(defmacro defrule [sym form]
  `(def ~sym
     (fnp/failpoint
      (fnp/semantics
       (fnp/conc
        (fnp/effects
         (scope-push! ~(name sym))
         ((get-pre-hook (quote ~sym))))
        (fnp/semantics ~form
                       (get-semantics (quote ~sym)))
        (fnp/effects
         ((get-post-hook (quote ~sym)))
         (scope-pop!)))
       second)
      (scope-pop!))))
