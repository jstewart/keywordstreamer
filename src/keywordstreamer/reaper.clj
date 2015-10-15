(ns keywordstreamer.reaper
  (:require [clojure.core.async :as async :refer [alt! go-loop]]
            [clojure.tools.logging :refer [info]]
            [com.stuartsierra.component :as component]))

(defn start-reaping
  [{:keys [reap shutdown]}
   {:keys [send-fn connected-uids]}]
  (go-loop []
    (alt!
      shutdown
      ([_] (info "shutting down"))

      reap
      ([{:keys [client-id results]}]
       (send-fn client-id [:ks/results results])
       (recur)))))


(defrecord Reaper [channels websocket server]
  component/Lifecycle
  (start [this]
    (info "starting")
    (assoc this :worker-thread
           (start-reaping channels (:ws server))))
  (stop [this]
    (info "stopping")
    (assoc this :worker-thread nil)))

(defn new-reaper
  []
  (map->Reaper {}))
