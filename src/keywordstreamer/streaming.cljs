(ns ^:figwheel-always keywordstreamer.streaming
  (:require [cljs.core.async :refer [<! chan timeout alts!]]
            [re-frame.core :refer [dispatch subscribe]]
            [taoensso.sente  :as sente :refer [cb-success?]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(defn permutations
  [s]
  (if s
    (->> (seq "abcdefghijklmnopqrstuwvxyz")
         (concat [(.getFullYear (js/Date.))])
         (mapcat #(vector (str s " " %) (str % " " s))))
    []))

(defonce event-chan (chan))

(defn setup-ws
  []
  (let [{:keys [chsk ch-recv send-fn state]}
        (sente/make-channel-socket! "/chsk"
                                    {:type :auto})]
    (def chsk       chsk)
    (def ch-chsk    ch-recv)
    (def chsk-send! send-fn)
    (def chsk-state state)))

(defn setup-ws-events
  []
  ;; Receive Events
  (go-loop [evt (:event (<! ch-chsk))]
    (dispatch [:ws-event evt])
    (recur (:event (<! ch-chsk))))

  ;; Send Events
  (go-loop [evt (<! event-chan)]
    (chsk-send! evt 8000)
    (recur (<! event-chan))))


;; Each new search makes a go-loop which terminates
;; when current search in atom is finished or (subscribe ...)
;; or permutations are done
(defn handle-permuted-search
  [{:keys [query searches]}]
  (go-loop [p (permutations query)
            q query]
    ;; TODO sliding timeout window
    (<! (timeout 5000))
    (when (and (seq p)
               (= query    @(subscribe [:query]))
               (= searches @(subscribe [:searches]))
               (true?      @(subscribe [:streaming?])))
      (go (>! event-chan
              [:ks/search {:query (first p)
                           :searches @(subscribe [:searches])}]))
      (recur (rest p) query))))
