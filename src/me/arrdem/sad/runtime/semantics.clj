(ns me.arrdem.sad.runtime.semantics)

(def ^:dynamic *semantics-ns* nil)

(defn get-semantics [sym]
  "Searches the semantics table for a transform function, returning
clojure.core/identity if there is no registered transform."
  (or (ns-resolve *semantics-ns* sym)
      identity))
