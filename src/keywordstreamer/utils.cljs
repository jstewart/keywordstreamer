(ns ^:figwheel-always keywordstreamer.utils)

(defn in? [coll x]
  (some #(= x %) coll))

;; Gross. Isn't there something better in clojurescript?
(defn index-of [coll pred]
  (let [v (map-indexed vector coll)
        f (first (filter pred v))]
    (if f (first f) nil)))

(defn char-range [start end]
  (map char (range (int start) (inc (int end)))))
