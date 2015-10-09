(ns ^:figwheel-always keywordstreamer.handlers
  (:require [cljs.core.async           :refer [>!]]
            [keywordstreamer.db        :refer [default-value]]
            [keywordstreamer.streaming :refer [event-chan handle-permuted-search]]
            [re-frame.core             :refer [register-handler path
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
  (assoc db :ready? (:first-open? arg)))

(defmethod handle-ws-event :chsk/recv [db [[op arg] evt]]
  ;; event is embedded in the recv event
  (assoc db :results
         (utils/distinct-by :id
          (concat (last arg) (:results db)))))

(register-handler
 :initialize-db
 (fn [_ _]
   default-value))

(register-handler
 :clear-results
 (fn [db _]
   (assoc db :results [])))

(register-handler
 :ws-event
 trim-v
 handle-ws-event)

(register-handler
 :query-changed
 (fn [db [_ value]]
   (dispatch [:stop-streaming])
   (assoc db :query value)))

(register-handler
 :dump-data
 (fn [db _]
   (.log js/console (str (filter :selected (:results db))))
   db))

(register-handler
 :submit
 (fn [db _]
   (let [{:keys [query searches streaming?]} db]
     (when-not streaming?
       (create-search query searches))
     (assoc db :streaming? (not streaming?)))))

(register-handler
 :stop-streaming
 (fn [db _]
   (assoc db :streaming? false)))

(register-handler
 :toggle-selection
 (fn [db [_ id]]
   (assoc db
          :results
          (utils/toggle-result (:results db) id))))

(register-handler
 :focus-keyword
 (fn [db [_ query]]
   (let [{:keys [searches]} db]
     (create-search query searches))
   (set! (.-hash js/location) "#query")
   (assoc db
          :query query
          :streaming? true)))

(register-handler
 :select-deselect-all
 (fn [db [_ selection]]
   (let [])
   (assoc db
          :results
          (utils/toggle-all (:results db) selection))))

(register-handler
 :search-type-changed
 (fn [db [_ value]]
   (let [db (merge-with merge db {:searches value})]
     ;; When type is changed mid-stream so is the search
     (when (:streaming? db)
       (create-search (:query db) (:searches db)))
     db)))
