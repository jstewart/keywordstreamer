(ns ^:figwheel-always keywordstreamer.core
    (:require
     [reagent.core :as reagent :refer [atom]]
     [re-frame.core :refer [dispatch dispatch-sync]]
     [taoensso.sente  :as sente :refer (cb-success?)]
     [keywordstreamer.handlers]
     [keywordstreamer.subs]
     [keywordstreamer.views]))

;; ws connect
(defn setup-ws
  []
  (let [{:keys [chsk ch-recv send-fn state]}
        (sente/make-channel-socket! "/chsk" {:type :auto})]
    (def chsk       chsk)
    (def ch-chsk    ch-recv)
    (def chsk-send! send-fn)
    (def chsk-state state)))

(defn setup
  []
  ;;(setup-ws)
  (dispatch-sync [:initialize-db])
  (reagent/render [keywordstreamer.views/keywordstreamer-app]
                  (.getElementById js/document "app")))

(defn reload
  []
  (setup))

(defn ^:export main
  []
  (setup))
