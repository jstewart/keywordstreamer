(ns keywordstreamer.search.searchers
  (:require [clojure.data.json :as json]
            [clj-http.client :as c]
            [keywordstreamer.utils :refer [?assoc char-range current-year]]))

;; TODO Implement these. The suggest api is shutting down
(defn scrape-google-for [s])
(defn scrape-yt-for [s])

(defn prep-query
  [m]
  (str (:query m) " "))

(defn google-autosuggest-for [s & [hl ds]]
  (let [params  (?assoc {:client "firefox"} :hl hl :ds ds)]
    (-> (c/get "http://suggestqueries.google.com/complete/search"
                 {:query-params (assoc params :q s)
                  :as "ISO-8859-1"})
      :body
      json/read-str
      last)))

(defn google-search [m]
  (google-autosuggest-for (prep-query m)))

(defn bing-search [m]
  (-> (c/get "http://api.bing.com/osjson.aspx"
             {:query-params {:query (prep-query m)}}
             {:as :json})
      :body
      json/read-str
      last))

(defn ddg-search [m]
  (->> (c/get "https://duckduckgo.com/ac/"
             {:query-params {:q (prep-query m)}})
      :body
      (json/read-str)
      (map vals)
      flatten))

(defn amazon-search [m]
  (-> (c/get "https://completion.amazon.com/search/complete"
             {:query-params {:method "completion"
                             :search-alias "aps"
                             :client "keyword-streamer"
                             :mkt 1
                             :q (prep-query m)}})
      :body
      (json/read-str)
      second))

(defn youtube-search [m]
  (google-autosuggest-for (:query m) "en" "yt"))
