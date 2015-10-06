(ns keywordstreamer.system
  (require [com.stuartsierra.component :as component]
           [taoensso.timbre :as timbre :refer [info]]
           [keywordstreamer.channels :as channels]
           [keywordstreamer.dispatcher :as dispatcher]
           [keywordstreamer.reaper :as reaper]
           [keywordstreamer.searcher :as searcher]
           [keywordstreamer.server :as server]
           [keywordstreamer.utils :refer [on-shutdown]]
           [keywordstreamer.ws-events :as ws-events])
  (:gen-class))

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
  (let [host (get (System/getenv "OPENSHIFT_CLOJURE_HTTP_IP") "0.0.0.0")
        port (Integer/parseInt (get (System/getenv "OPENSHIFT_CLOJURE_HTTP_PORT") "9009"))

        system (component/start (create-system port))]
    (on-shutdown
     (info "interrupted! shutting down")
     (component/stop system))))
