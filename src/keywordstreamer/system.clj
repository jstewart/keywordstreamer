(ns keywordstreamer.system
  (require [com.stuartsierra.component :as component]
           [taoensso.timbre :as timbre :refer [info]]
           [keywordstreamer.channels :as channels]
           [keywordstreamer.dispatcher :as dispatcher]
           [keywordstreamer.reaper :as reaper]
           [keywordstreamer.searcher :as searcher]
           [keywordstreamer.server :as server]
           [keywordstreamer.utils :refer [on-shutdown]]
           )
  (:gen-class))

(defrecord KeywordStreamer []
  component/Lifecycle
  (start [this]
    (info "Starting Keyword Streamer")
    (component/start-system this))
  (stop [this]
    (info "Stopping Keyword Streamer")
    (component/stop-system this)))

(defn create-system [port]
  (map->KeywordStreamer
   {:channels   (channels/new-channels)
    :server     (component/using (server/new-server port)
                                 [:channels])
    :dispatcher (component/using (dispatcher/new-dispatcher)
                                 [:channels])
    :searcher   (component/using (searcher/new-searcher)
                                 [:channels])
    :reaper     (component/using (reaper/new-reaper)
                                 [:channels])}))

(defn -main [& args]
  (let [system (.start (create-system 9009))]
    (on-shutdown
     (info "interrupted! shutting down")
     (component/stop system))))
