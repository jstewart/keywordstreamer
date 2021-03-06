(ns keywordstreamer.system
  (require [taoensso.timbre :refer [info]]
           [com.stuartsierra.component :as component]
           [environ.core :refer [env]]
           [keywordstreamer.channels :as channels]
           [keywordstreamer.dispatcher :as dispatcher]
           [keywordstreamer.reaper :as reaper]
           [keywordstreamer.searcher :as searcher]
           [keywordstreamer.server :as server]
           [keywordstreamer.utils :refer [on-shutdown]]
           [keywordstreamer.ws-events :as ws-events])
  (:gen-class))

(def default-host
  (or (env :host-ip) "127.0.0.1"))

(def default-port
  (Integer/parseInt
   (or (env :port) "8080")))

(defrecord KeywordStreamer []
  component/Lifecycle
  (start [this]
    (info "Starting Keyword Streamer")
    (component/start-system this))
  (stop [this]
    (info "Stopping Keyword Streamer")
    (component/stop-system this)))

(defn create-system [host port]
  (component/system-map
   :server     (server/new-server host port)
   :channels   (component/using (channels/new-channels)
                                [:server])
   :dispatcher (component/using (dispatcher/new-dispatcher)
                                [:channels])
   :searcher   (component/using (searcher/new-searcher)
                                [:channels])
   :ws-events  (component/using (ws-events/new-ws-events)
                                [:channels])
   :reaper     (component/using (reaper/new-reaper)
                                [:channels :server])))

(defn -main [& args]
  (let [host default-host
        port default-port
        system (component/start (create-system host port))]
    (on-shutdown
     (info "interrupted! shutting down")
     (component/stop system))))
