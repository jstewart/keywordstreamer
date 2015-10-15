(ns keywordstreamer.searcher
  (:require [clojure.core.async :as async :refer [>!! alt!! thread]]
            [clojure.tools.logging :refer [info]]
            [com.stuartsierra.component :as component]
            [keywordstreamer.search.providers :refer [start-worker]]))

(def providers [:google :bing :ddg :amazon :youtube])

(defn provider-workers [channels provider]
  [provider (doall
             (map (partial start-worker provider channels)
                  (range 0 4)))])

(defn start-workers
  [channels]
  (into {} (map (partial provider-workers channels) providers)))

(defrecord Searcher [channels]
  component/Lifecycle
  (start [this]
    (info "starting")
    (assoc this :workers (start-workers channels)))
  (stop  [this]
    (info "stopping")
    (assoc this :wokers nil)))

(defn new-searcher
  []
  (map->Searcher {}))
