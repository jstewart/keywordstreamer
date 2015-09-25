(ns keywordstreamer.handlers
  (:require
   [cljs.core.async :refer [>!]]
   [keywordstreamer.db    :refer [default-value]]
   [keywordstreamer.websocket :refer [event-chan]]
   [re-frame.core         :refer [register-handler path trim-v after]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

;; Gross. Isn't there something better in clojurescript?
(defn index-of [coll pred]
  (let [v (map-indexed vector coll)
        f (first (filter pred v))]
    (if f (first f) nil)))

(defn toggle-result [coll id]
  (let [result  (first (filter #(= (:id %) id) coll))
        toggled (assoc result :selected (not (:selected result)))
        idx     (index-of coll #(= (:id (last %)) id))]
    ;; vec is needed because assoc won't work on a lazy-seq
    (assoc (vec coll) idx toggled)))

(defn toggle-all [coll selection]
  (map #(assoc % :selected selection) coll))

(defmulti handle-ws-event
  (fn [db [[op arg] evt]] op))

(defmethod handle-ws-event :default [db [[op arg] evt]]
  (.log js/console (str "unhandled event: " op))
  db)

(defmethod handle-ws-event :chsk/state [db [[op arg] evt]]
  (assoc db :ready? (:first-open? arg)))

(register-handler
 :initialize-db
 (fn [_ _]
   default-value))

(register-handler
 :ws-event
 trim-v
 handle-ws-event)

(register-handler
 :query-changed
 (fn [db [_ value]]
   (assoc db :query value)))

(register-handler
 :submit
 (fn [db _]
   (let [{:keys [query searches streaming?]} db]
     (when-not streaming?
       (go (>! event-chan
               [:ks/search {:query query :searches searches}])))
     (assoc db :streaming? (not streaming?)))))

(register-handler
 :toggle-selection
 (fn [db [_ id]]
   (assoc db
          :results
          (toggle-result (:results db) id))))

;; TODO visible
(register-handler
 :select-deselect-all
 (fn [db [_ selection]]
   ;; Get ids of visible.
   (let [])
   (assoc db
          :results
          (toggle-all (:results db) selection))))

(register-handler
 :search-type-changed
 (fn [db [_ value]]
   (merge-with merge db {:searches value})))
