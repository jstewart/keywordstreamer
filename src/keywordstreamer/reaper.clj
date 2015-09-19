(ns keywordstreamer.reaper
  (:require [clojure.core.async :as async :refer [alt! go-loop]]
            [com.stuartsierra.component :as component]
            [taoensso.timbre :as timbre :refer [info]]))

(defn start-reaping
  [{:keys [reap shutdown]}]
  (go-loop []
    (alt!
      shutdown
      ([_] (info "shutting down"))

      reap
      ([data] (info data)
       (recur)))))


(defrecord Reaper [channels]
  component/Lifecycle
  (start [this]
    (info "starting")
    (assoc this :worker-thread (start-reaping channels)))
  (stop [this]
    (info "stopping")
    (assoc this :worker-thread nil)))

(defn new-reaper
  []
  (map->Reaper {}))
