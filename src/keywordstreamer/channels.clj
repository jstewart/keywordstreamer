(ns keywordstreamer.channels
  (:require [clojure.core.async :refer [chan close!]]
            [com.stuartsierra.component :as component]))

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

(defn new-channels []
  map->Channels)
