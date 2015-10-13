(ns keywordstreamer.dispatcher
  (:require [clojure.core.async :as async :refer [>! alt! go-loop]]
            [com.stuartsierra.component :as component]
            [taoensso.timbre :as timbre :refer [info]]))

(defn search-chans [m]
  (let [searches (->> (:searches m)
                      (filter (comp true? val))
                      keys)]
    (-> {:web      [:google :bing :ddg]
         :video    [:youtube]
         :shopping [:amazon]}
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
           (>!  (sc channels) data)))
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
