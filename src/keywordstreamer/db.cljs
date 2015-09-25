(ns keywordstreamer.db)

(def default-value
  {:query      ""
   :searches   {:web true :shopping false :social false :video false} ;; Search types
   :streaming? false
   :ready?     false
   :results    [{:query "Test" :selected false :id "purple" :name "Purple widget" :search-type :web}
                {:query "Test" :selected false :id "black" :name "Black widget" :search-type :shopping}
                {:query "Test" :selected true :id "red" :name "Red widget" :search-type :web}
                {:query "Test" :selected false :id "green" :name "Green widgets are super fucking awesome mang" :search-type :video}]})
