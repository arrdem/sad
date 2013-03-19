(ns me.arrdem.sad
  (:require [me.arrdem.sad.grammars bnf
                                    ;; ebnf
                                    jnw-ebnf
                                    ;; java-ebnf
             ])
  (:gen-class :main true))

(defn translate-text [grammar]
  (symbol (str (name grammar) "/run")))

(defn -main [& {:keys [srcfile grammar] :as opts}]
  ((translate-text grammar) opts))
