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


(defn phrases [n text]
  "chunk text in to n sized phrases."
  )

(defn kw-density [phrase text]
  "Calculates keyword density for phrase in text"
  ;; extract text from page.
  ;; split on phrase terminators
  ;; get number of times phrase is used in the page (wp)
  ;; Density = your keyword density
  ;; Nkr = how many times you repeated a specific key-phrase
  ;; Nwp = number of words in your key-phrase
  ;; Tkn = total words in the analyzed text
  ;; Density = (Nkr x (Nwp / Tkn)) x 100
  ;; Density = ( Nkr / ( Tkn -( Nkr * ( Nwp-1 ) ) ) ) * 100
  )
