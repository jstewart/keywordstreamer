(ns keywordstreamer.search.providers
  (:require [clojure.core.async :as async :refer [alt!! thread >!!]]
            [clojure.core.cache :as cache]
            [clojure.string :as s]
            [taoensso.timbre :as timbre :refer [info]])
  (:use [keywordstreamer.search.searchers]))

(defn make-result-id [k s]
  (s/lower-case
   (str (name k) "-"
        (s/replace s #"\W" "-"))))

;; 30 minute TTL cache
;; TODO Replace with redis
(def C (atom (cache/ttl-cache-factory {} :ttl 1800000)))

(defn cache-key [k s]
  (keyword (make-result-id k s)))

(defn search-types [p]
  (p {:google :web
      :yahoo  :web
      :bing   :web
      :amazon :shopping
      :youtube :video}))

(defn create-result-map [p q res]
  {:query q
   :selected false
   :id (make-result-id p res)
   :name res
   :search-type (search-types p)})

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
           (let [q   (->> (:query data) (take 255) (apply str))
                 df  (partial create-result-map provider q)
                 ck  (cache-key provider q)
                 res (if (cache/has? @C ck)
                       (cache/hit @C ck)
                       (swap! C
                              #(cache/miss
                                % ck
                                (map df (search-fn data)))))]
             (>!! reap (assoc data :results (ck res))))
           (recur))
          :priority true)))))
