(ns keywordstreamer.search.providers
  (:require [clojure.core.async :as async :refer [alt!! thread >!!]]
            [taoensso.timbre :as timbre :refer [info]])
  (:use [keywordstreamer.search.searchers]))

(defn start-worker [provider {:keys [shutdown reap] :as channels} n]
  (let [search-fn (condp = provider
                    :google  google-search
                    :bing    bing-search
                    :yahoo   yahoo-search
                    :amazon  amazon-search
                    :youtube youtube-search)]
    (info (str "starting " (name provider) " search worker " n))
    (thread
      (loop []
        (alt!!
          shutdown
          ([_] (info "shutting down"))

          (provider channels)
          ([data]
           (>!! reap (assoc data :results (search-fn data)))
           (recur)))))))
