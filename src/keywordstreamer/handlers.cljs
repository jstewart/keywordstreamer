(ns ^:figwheel-always keywordstreamer.handlers
  (:require [clojure.string :as s]
            [cljs.core.async           :refer [>!]]
            [keywordstreamer.db        :refer [default-value]]
            [keywordstreamer.streaming :refer [event-chan handle-permuted-search]]
            [re-frame.core             :refer [reg-event-db path
                                               trim-v after dispatch]]
            [keywordstreamer.utils :as utils])

  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn create-search [query searches]
  (let [evt-data {:query query :searches searches}]
    (go (>! event-chan
            [:ks/search evt-data]))
    (handle-permuted-search evt-data)))

(defmulti handle-ws-event
  (fn [db [[op arg] evt]] op))

(defmethod handle-ws-event :default [db [[op arg] evt]]
  ;; No-op
  db)

(defmethod handle-ws-event :chsk/state [db [[op arg] evt]]
  (assoc db :ready? (-> arg last :first-open?)))

(defmethod handle-ws-event :chsk/recv [db [[op arg] evt]]
  ;; The incoming results are embedded in the last item of arg
  (let [current-results  (:results db)
        incoming-results (-> arg last)
        results          (->> incoming-results
                              (filter #(not (utils/in?
                                             (map :id current-results)
                                             (:id %)))))]
    (merge db {:results (concat (:results db) results)})))

(reg-event-db
 :initialize-db
 (fn [_ _]
   default-value))

(reg-event-db
 :clear-results
 (fn [db _]
   (assoc db :results [])))

(reg-event-db
 :ws-event
 trim-v
 handle-ws-event)

(reg-event-db
 :query-changed
 (fn [db [_ value]]
   (dispatch [:stop-streaming])
   (assoc db :query value)))

(reg-event-db
 :dump-data
 (fn [db _]
   db))

(reg-event-db
 :submit
 (fn [db _]
   (let [{:keys [query searches streaming?]} db]
     (when-not streaming?
       (create-search query searches))
     (assoc db :streaming? (not streaming?)))))

(reg-event-db
 :stop-streaming
 (fn [db _]
   (assoc db :streaming? false)))

(reg-event-db
 :toggle-selection
 (fn [db [_ id]]
   (let [{:keys [results]} db
         filter-pred #(= id (-> % last :id))
         idx (utils/index-of results filter-pred)]
     (assoc db :results (update-in (vec results) [idx :selected] not)))))

(reg-event-db
 :focus-keyword
 (fn [db [_ query]]
   (let [{:keys [searches]} db]
     (create-search query searches))
   (set! (.-hash js/location) "#query")
   (assoc db
          :query query
          :streaming? true)))

(reg-event-db
 :select-deselect-all
 (fn [db [_ selection]]
   (assoc db
          :results
          (map  #(assoc % :selected selection) (:results db)))))

(reg-event-db
 :search-type-changed
 (fn [db [_ value]]
   (let [db (merge-with merge db {:searches value})]
     ;; When type is changed mid-stream so is the search
     (when (:streaming? db)
       (create-search (:query db) (:searches db)))
     db)))
