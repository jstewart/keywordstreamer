(ns ^:figwheel-always keywordstreamer.core
    (:require
     [reagent.core :as reagent]
     [re-frame.core :refer [dispatch-sync]]
     [keywordstreamer.handlers]
     [keywordstreamer.subs]
     [keywordstreamer.views]
     [keywordstreamer.streaming :refer [setup-ws setup-ws-events]]))

(defn setup
  []
  (setup-ws)
  (setup-ws-events)
  (dispatch-sync [:initialize-db])
  (reagent/render [keywordstreamer.views/keywordstreamer-app]
                  (.getElementById js/document "app")))

(defn reload
  []
  (setup))

(defn ^:export main
  []
  (setup))
