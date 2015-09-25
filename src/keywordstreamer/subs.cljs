(ns keywordstreamer.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub subscribe]]))

(defn in?
  [coll x]
  (some #(= x %) coll))

(register-sub
 :results
 (fn [db _]
   (reaction (:results @db))))

(register-sub
 :query
 (fn [db _]
   (reaction (:query @db))))

(register-sub
 :streaming?
 (fn [db _]
   (reaction (:streaming? @db))))

(register-sub
 :ready?
 (fn [db _]
   (reaction (:ready? @db))))

(register-sub
 :searches
 (fn [db _]
   (reaction (:searches @db))))

(register-sub
 :totals
 (fn [db _]
   (let [all (reaction  (:results @db))
         vis (subscribe [:visible-results])]
     (reaction
      {:all (count @all)
       :visible (count @vis)}))))

(register-sub
 :visible-results
 (fn [db [_]]
   (let [searches (subscribe [:searches])
         results  (reaction (:results @db))
         showing  (reaction
                   (->> @searches
                        (filter second)
                        (map (comp keyword first))))]
     (reaction
      (doall
       (filter
        #(in? @showing (:search-type %)) @results))))))
