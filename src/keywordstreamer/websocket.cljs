(ns keywordstreamer.websocket
  (:require [cljs.core.async :refer [<! chan]]
            [re-frame.core :refer [dispatch]]
            [taoensso.sente  :as sente :refer (cb-success?)])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

;; Channel to push events from client->server over the web socket
(defonce event-chan (chan))

;; ;; ws connect
(defn setup-ws
  []
  (let [{:keys [chsk ch-recv send-fn state]}
        (sente/make-channel-socket! "/chsk" {:type :auto})]
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
    (.log js/console (str "Sending event: " evt))
    (chsk-send! evt 8000)
    (recur (<! event-chan))))
