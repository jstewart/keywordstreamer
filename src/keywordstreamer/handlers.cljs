(ns keywordstreamer.handlers
  (:require
   [keywordstreamer.db    :refer [default-value]]
   [re-frame.core :refer [register-handler path trim-v after]]))

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

(register-handler
 :initialize-db
 (fn [_ _]
   default-value))

(register-handler
 :query-changed
 (fn [db [_ value]]
   (assoc db :query value)))

(register-handler
 :submit
 (fn [db _]
   (assoc db :streaming
          (not (:streaming db)))))

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
