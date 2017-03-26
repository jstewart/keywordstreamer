(ns keywordstreamer.utils)

(defn add-shutdown-hook
  [f]
  (.addShutdownHook (java.lang.Runtime/getRuntime)
                    (Thread. ^Runnable f)))

(defmacro on-shutdown
  [& body]
  `(add-shutdown-hook (fn [] ~@body)))

(defn ?assoc
  "Same as assoc, but skip the assoc if v is nil"
  [m & kvs]
  (->> kvs
       (partition 2)
       (filter second)
       (map vec)
       (into m)))

(defn char-range [start end]
  (map char (range (int start) (inc (int end)))))

(defn current-year []
  (.format (java.text.SimpleDateFormat. "yyyy") (java.util.Date.)))
