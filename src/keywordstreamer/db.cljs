(ns keywordstreamer.db)

(def default-value
  {:query      ""
   :searches   {:web true :shopping false :video false :wikipedia false}
   :streaming? false
   :ready?     false
   :results    []})
