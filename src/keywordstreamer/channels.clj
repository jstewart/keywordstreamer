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
           :ddg       (chan)
           :amazon    (chan)
           :facebook  (chan)
           :pinterest (chan)
           :youtube   (chan)
           :shutdown  (chan)))
  (stop [this]
    (info "stopping")
    ;; TODO Close these.
    ;; (close! (:ws this))
    ;; (close! (:dispatch this))
    ;; (close! (:reap this))
    ;; (close! (:events this))
    ;; (close! (:google this))
    ;; (close! (:bing this))
    ;; (close! (:ddg this))
    ;; (close! (:amazon this))
    ;; (close! (:facebook this))
    ;; (close! (:pinterest this))
    ;; (close! (:youtube this))
    ;; (close! (:shutdown this))
    (assoc this
           :ws        nil
           :dispatch  nil
           :reap      nil
           :events    nil
           :google    nil
           :bing      nil
           :ddg       nil
           :amazon    nil
           :facebook  nil
           :pinterest nil
           :youtube   nil
           :shutdown  nil)))

(defn new-channels []
  (map->Channels {}))
