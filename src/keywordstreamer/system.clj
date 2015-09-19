(ns keywordstreamer.system
  (require [clojure.core.async :refer [chan close!]]
           [com.stuartsierra.component :as component]
           [taoensso.timbre :as timbre :refer [info]]
           [keywordstreamer.dispatcher :as dispatcher]
           [keywordstreamer.reaper :as reaper]
           [keywordstreamer.searcher :as searcher]
           [keywordstreamer.server :as server]
           [keywordstreamer.utils :refer [on-shutdown]]
           )
  (:gen-class))

(defrecord Channels [dispatch reap
                     events shutdown
                     google bing yahooo
                     amazon facebook
                     pinterest youtube]
  component/Lifecycle
  (start [this]
    (assoc this
           :dispatch  (chan)
           :reap      (chan)
           :events    (chan)
           :google    (chan)
           :bing      (chan)
           :yahoo     (chan)
           :amazon    (chan)
           :facebook  (chan)
           :pinterest (chan)
           :youtube   (chan)
           :shutdown  (chan)))
  (stop [this]
    (close! dispatch)
    (close! reap)
    (close! shutdown)
    (assoc this
           :dispatch  nil
           :reap      nil
           :events    nil
           :google    nil
           :bing      nil
           :yahoo     nil
           :amazon    nil
           :facebook  nil
           :pinterest nil
           :youtube   nil
           :shutdown  nil)))

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
   {:channels   (map->Channels {})
    :server     (component/using (server/new-server port) ; How will channels communicate?
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
