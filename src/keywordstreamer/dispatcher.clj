(ns keywordstreamer.dispatcher
  (:require [clojure.core.async :as async :refer [>! alt! go-loop]]
            [com.stuartsierra.component :as component]
            [taoensso.timbre :as timbre :refer [info]]))


(defn start-dispatching
  [{:keys [dispatch shutdown reap]}]
  (go-loop []
    (println "go loop")
    (alt!
      shutdown
      ([_] (info "shutting down"))

      dispatch
      ([data]
       (when data
         (>! reap data))
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
