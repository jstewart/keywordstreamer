(ns ^:figwheel-always keywordstreamer.utils)

;; taken from medley.core
(defn distinct-by
  "Returns a lazy sequence of the elements of coll, removing any elements that
  return duplicate values when passed to a function f."
  [f coll]
  (let [step (fn step [xs seen]
               (lazy-seq
                ((fn [[x :as xs] seen]
                   (when-let [s (seq xs)]
                     (let [fx (f x)]
                       (if (contains? seen fx)
                         (recur (rest s) seen)
                         (cons x (step (rest s) (conj seen fx)))))))
                 xs seen)))]
    (step coll #{})))

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

(defn char-range [start end]
  (map char (range (int start) (inc (int end)))))
