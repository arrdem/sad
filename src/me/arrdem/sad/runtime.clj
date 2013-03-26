(ns ^{:doc   "A wrapper namespace for some of the vars which compiled grammars
              rely on when executed by other clojure code. Provides various
              bindings structures for executing Sad output with hooks, semantics
              and debugging."
      :author "Reid McKenzie"
      :added "0.1.5"}
  me.arrdem.sad.runtime
  (:require [me.arrdem.sad.runtime.semantics]
            [me.arrdem.sad.runtime.hooks]
            [me.arrdem.sad.runtime.stack]))

(defmacro with-semantics [semantics-ns & forms]
  `(binding [me.arrdem.sad.runtime.semantics/*semantics-ns* ~semantics-ns]
     ~@forms))

(defmacro with-hooks [pre-hook-ns post-hook-ns & forms]
  `(binding [me.arrdem.sad.runtime.hooks/*pre-hook-ns* ~pre-hook-ns
             me.arrdem.sad.runtime.hooks/*post-hook-ns* ~post-hook-ns]
     ~@forms))
