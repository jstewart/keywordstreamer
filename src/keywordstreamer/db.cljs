(ns keywordstreamer.db)

(def default-value
  {:query      ""
   :searches   {:web true :shopping false :video false} ;; Search types
   :streaming? false
   :ready?     false
   :results    (sorted-map)})
