(ns keywordstreamer.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [reg-sub-raw subscribe]]
            [testdouble.cljs.csv :as csv]
            [keywordstreamer.utils :as utils]))

(reg-sub-raw
 :results
 (fn [db _]
   (reaction (:results @db))))

(reg-sub-raw
 :query
 (fn [db _]
   (reaction (:query @db))))

(reg-sub-raw
 :streaming?
 (fn [db _]
   (reaction (:streaming? @db))))

(reg-sub-raw
 :stream-button-verbiage
 (fn [db _]
   (reaction
    (if @(subscribe [:streaming?])
      {:label "Stop" :span :span.glyphicon.glyphicon-stop}
      {:label "Start" :span :span.glyphicon.glyphicon-play}))))

(reg-sub-raw
 :ready?
 (fn [db _]
   (reaction (:ready? @db))))

(reg-sub-raw
 :searches
 (fn [db _]
   (reaction (:searches @db))))

(reg-sub-raw
 :totals
 (fn [db _]
   (let [all (subscribe [:results])
         vis (subscribe [:visible-results])
         sel (subscribe [:selected-results])]
     (reaction
      {:all (count @all)
       :selected (count @sel)
       :visible (count @vis)}))))

(reg-sub-raw
 :selected-results
 (fn [db _]
   (let [res (subscribe [:results])]
     (reaction (filter :selected @res)))))

(reg-sub-raw
 :csv-data
 (fn [db _]
   (let [sel (subscribe [:selected-results])]
     (reaction
      (js/encodeURIComponent
       (csv/write-csv
        (map (comp vector :name) @sel)))))))

(reg-sub-raw
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
