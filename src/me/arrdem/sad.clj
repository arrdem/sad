(ns me.arrdem.sad
  (:require [me.arrdem.sad.grammars bnf
                                    ibnf
                                    ;; ebnf
                                    jnw-ebnf
                                    ;; java-ebnf
             ]
            [clojure.pprint :refer :all])
  (:gen-class :main true))

(defn -main [& {grammar ":grammar" :as opts}]
  (doseq [expr
          (case grammar
            "wirth" (me.arrdem.sad.grammars.jnw-ebnf/run opts)
            "ibnf"  (me.arrdem.sad.grammars.ibnf/run opts)
            "bnf"   (me.arrdem.sad.grammars.bnf/run opts))]
         (with-pprint-dispatch code-dispatch (pprint expr)))
  (println ""))
