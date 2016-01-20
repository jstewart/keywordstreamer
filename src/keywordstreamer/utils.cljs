(ns ^:figwheel-always keywordstreamer.utils)

(defn toggle-all [results selection]
  (->> results
       (map
        #(vector (first %)
                (-> % last (assoc :selected selection))))
       (into (sorted-map))))

(defn char-range [start end]
  (map char (range (int start) (inc (int end)))))
