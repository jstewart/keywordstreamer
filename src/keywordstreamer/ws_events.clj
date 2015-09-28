(ns keywordstreamer.ws-events
  (:require [clojure.core.async :refer [<! >! go go-loop alt!]]
            [com.stuartsierra.component :as component]
            [taoensso.timbre :as timbre :refer [info]]))

(defmulti handle-ws-event
  (fn [{:keys [client-id event]} dispatch] (first event)))

(defmethod handle-ws-event :default [{:keys [client-id event]} dispatch]
  (info (str "Unhandled event: " (first event))))

(defmethod handle-ws-event :ks/search [{:keys [client-id event]} dispatch]
  (let [[_ data] event]
    (go (>! dispatch (assoc data :client-id client-id)))))

(defn start-event-loop
  "Incoming events from websocket"
  [{:keys [ws shutdown dispatch]}]
  (go-loop []
    (alt!
      shutdown
      ([_] (info "shutting down"))

      ws
      ([event-map]
       (handle-ws-event event-map dispatch)
       (recur)))))

(defrecord WsEvents [channels]
  component/Lifecycle
  (start [this]
    (info "starting")
    (assoc this :event-loop (start-event-loop channels)))
  (stop [this]
    (info "stopping")
    (assoc this :event-loop nil)))

(defn new-ws-events
  []
  (map->WsEvents {}))
