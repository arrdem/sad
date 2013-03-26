(ns me.arrdem.sad.runtime.hooks)

(defn- nothing [] nil)

(def ^:dynamic *pre-hook-ns* nil)
(def ^:dynamic *post-hook-ns* nil)

(defn get-pre-hook
  "Wrapper on get-prefixed-hook which instructs it to look for a \"pre\" hook."
  [sym]
  (or (ns-resolve *pre-hook-ns* sym) nothing))

(defn get-post-hook
  "Wrapper on get-prefixed-hook which instructs it to look for a \"post\" hook."
  [sym]
  (or (ns-resolve *post-hook-ns* sym) nothing))
