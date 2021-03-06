(ns keywordstreamer.channels
  (:require [clojure.core.async :refer [chan close!]]
            [taoensso.timbre :refer [info]]
            [com.stuartsierra.component :as component]))

(defrecord Channels [server]
  component/Lifecycle
  (start [this]
    (info "starting")
    (assoc this
           :ws        (get-in server [:ws :ch-recv])
           :dispatch  (chan)
           :reap      (chan)
           :events    (chan)
           :google    (chan)
           :bing      (chan)
           :yahoo     (chan)
           :amazon    (chan)
           :youtube   (chan)
           :wikipedia (chan)
           :shutdown  (chan)))
  (stop [this]
    (info "stopping")
    (assoc this
           :ws        nil
           :dispatch  nil
           :reap      nil
           :events    nil
           :google    nil
           :bing      nil
           :yahoo     nil
           :amazon    nil
           :facebook  nil
           :youtube   nil
           :wikipedia nil
           :shutdown  nil)))

(defn new-channels []
  (map->Channels {}))
