(ns me.arrdem.sad.runtime.hooks)

(defn- nothing [] nil)

(def ^:dynamic *pre-hook-ns* nil)
(def ^:dynamic *post-hook-ns* nil)

(defn get-pre-hook
  "Wrapper on get-prefixed-hook which instructs it to look for a \"pre\" hook."
  [sym]
  (if (and *pre-hook-ns* (symbol? *pre-hook-ns*))
    (ns-resolve *pre-hook-ns* sym)
    nothing))

(defn get-post-hook
  "Wrapper on get-prefixed-hook which instructs it to look for a \"post\" hook."
  [sym]
  (if (and *post-hook-ns* (symbol? *post-hook-ns*))
    (ns-resolve *post-hook-ns* sym)
    nothing))
