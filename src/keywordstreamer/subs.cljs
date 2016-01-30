(ns keywordstreamer.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub subscribe]]
            [testdouble.cljs.csv :as csv]
            [keywordstreamer.utils :as utils]))

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
 :stream-button-verbiage
 (fn [db _]
   (reaction
    (if @(subscribe [:streaming?])
      {:label "Stop" :span :span.glyphicon.glyphicon-stop}
      {:label "Start" :span :span.glyphicon.glyphicon-play}))))

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
   (let [all (subscribe [:results])
         vis (subscribe [:visible-results])
         sel (subscribe [:selected-results])]
     (reaction
      {:all (count @all)
       :selected (count @sel)
       :visible (count @vis)}))))

(register-sub
 :selected-results
 (fn [db _]
   (let [res (subscribe [:results])]
     (reaction (filter :selected @res)))))

(register-sub
 :csv-data
 (fn [db _]
   (let [sel (subscribe [:selected-results])]
     (reaction
      (js/encodeURIComponent
       (csv/write-csv
        (map (comp vector :name) @sel)))))))

(register-sub
 :visible-results
 (fn [db [_]]
   (let [searches (subscribe [:searches])
         results  (subscribe [:results])
         showing  (reaction  (->> @searches
                                  (filter second)
                                  (map (comp keyword first))))]
     (reaction
      (doall
       (filter
        #(utils/in? @showing (:search-type %)) @results))))))
