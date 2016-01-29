(ns keywordstreamer.search.providers
  (:require [clojure.core.async :as async :refer [alt!! thread >!!]]
            [clojure.core.cache :as cache]
            [clojure.string :as s]
            [taoensso.timbre :refer [info]])
  (:use [keywordstreamer.search.searchers]))

(defn make-result-id [s]
  (s/lower-case
   (s/replace s #"\W" "-")))

(def C (atom (cache/ttl-cache-factory {} :ttl 900000)))

(defn cache-key [provider suggestion]
  (->> (make-result-id suggestion)
       (str (name provider) "-")
       keyword))

(defn search-types [p]
  (p {:google    :web
      :yahoo     :web
      :bing      :web
      :amazon    :shopping
      :wikipedia :wikipedia
      :youtube   :video}))

(defn create-result-map [p q res]
  {:query q
   :selected false
   :id (make-result-id res)
   :name res
   :search-type (search-types p)})

(defn perform-search [{:keys [provider data]}]
  (let [search-fn (condp = provider
                    :google    google-search
                    :wiki      wikipedia-search
                    :bing      bing-search
                    :yahoo     yahoo-search
                    :amazon    amazon-search
                    :youtube   youtube-search
                    :wikipedia wikipedia-search)]

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
        ([{:keys [client-id query] :as data}]
         (let [q   (->> query (take 500) (apply str))
               ck (cache-key provider q)
               res (cache-result  {:cache-key ck
                                   :data data
                                   :query q
                                   :provider provider})]
           (>!! reap {:client-id client-id :results (ck res)}))
         (recur))
        :priority true))))
