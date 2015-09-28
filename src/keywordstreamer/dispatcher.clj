(ns keywordstreamer.dispatcher
  (:require [clojure.core.async :as async :refer [>! alt! go-loop]]
            [com.stuartsierra.component :as component]
            [taoensso.timbre :as timbre :refer [info]]
            [keywordstreamer.utils :refer [char-range current-year]]))

;; TODO: Move this to the client
;; (def permutations
;;   (flatten
;;    [(range 1 10) (char-range \a \z) (current-year)]))

(defn search-chans [m]
  (let [searches (->> (:searches m)
                      (filter (comp true? val))
                      keys)]
    (-> {:web      [:google :bing :yahoo]
         :video    [:youtube]
         :shopping [:amazon]
         :social   []}
        (select-keys searches)
        vals
        flatten)))

(defn start-dispatching
  [{:keys [dispatch shutdown] :as channels}]
  (go-loop []
    (alt!
      shutdown
      ([_] (info "shutting down"))

      dispatch
      ([data]
       (when data
         (doseq [sc (search-chans data)]
           (>!  (sc channels) data)
           ;; (let [searches (conj (map (partial str (:query data) " ") permutations)
           ;;                      (:query data))
           ;;       chan     (sc channels)]
           ;;   (doseq [s searches]
           ;;     ))
           )
         )
       (recur))
      :priority true)))


(defrecord Dispatcher [channels]
  component/Lifecycle
    (start [this]
      (info "starting")
      (assoc this :worker-thread (start-dispatching channels)))
    (stop [this]
      (info "stopping")
      (assoc this :worker-thread nil)))

(defn new-dispatcher
  []
  (map->Dispatcher {}))
