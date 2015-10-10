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

;; 15 minute TTL cache
;; TODO Replace with redis
(def C (atom (cache/ttl-cache-factory {} :ttl 900000)))

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

(defn perform-search [{:keys [provider data]}]
  (let [search-fn (condp = provider
                    :google  google-search
                    :bing    bing-search
                    :yahoo   yahoo-search
                    :amazon  amazon-search
                    :youtube youtube-search)]

    (try
      (search-fn data)
      (catch Exception e
        (info (str "Caught exception searching "
                   provider ": "
                   data " | "
                   (.getMessage e)))
        []))))

(defn cache-miss [{:keys [cache-key data query provider] :as m}]
  (swap!
   C
   (fn [res]
     (cache/miss
      res
      cache-key
      (map (partial create-result-map provider query)
           (perform-search m))))))

(defn cache-result
  [{:keys [cache-key] :as m}]
  (if (cache/has? @C cache-key)
    (cache/hit @C cache-key)
    (cache-miss m)))

(defn start-worker [provider {:keys [shutdown reap] :as channels} n]
  (info (str "starting " (name provider) " search worker " n))
  (thread
    (loop []
      (alt!!
        shutdown
        ([_] (info "shutting down"))

        (provider channels)
        ([data]
         (let [q   (->> (:query data) (take 500) (apply str))
               ck (cache-key provider q)
               res (cache-result  {:cache-key ck
                                   :data data
                                   :query q
                                   :provider provider})]
           (>!! reap (assoc data :results (ck res))))
         (recur))
        :priority true))))
