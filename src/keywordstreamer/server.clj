(ns keywordstreamer.server
  (require [com.stuartsierra.component :as component]
           [compojure.core :refer [routes defroutes GET]]
           [compojure.handler :as handler]
           [compojure.route :as route]
           [org.httpkit.server :refer [run-server]]
           [taoensso.sente :as sente]
           [taoensso.sente.server-adapters.http-kit :refer (sente-web-server-adapter)]
           [taoensso.timbre :as timbre :refer [info]]
           [ring.util.response :as resp]))

(let [{:keys [ch-recv send-fn ajax-post-fn
              ajax-get-or-ws-handshake-fn] :as sente-info}
      (sente/make-channel-socket! sente-web-server-adapter {})]
  (def ring-ajax-post   ajax-post-fn)
  (def ring-ajax-get-ws ajax-get-or-ws-handshake-fn)
  (def ch-chsk          ch-recv)
  (def chsk-send!       send-fn))

(defroutes app-routes
  (GET "/" [] (resp/resource-response "index.html" {:root "public"}))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/api app-routes))

(defn- start-server [handler port]
  (let [server (run-server handler {:port port})]
    (info (str "Started server on localhost:" port))
    server))

(defn- stop-server [server]
  (when server
    (server)))

(defrecord Server [port]
  component/Lifecycle
  (start [this]
    (assoc this :httpkit (start-server #'app port)))
  (stop [this]
    (stop-server (:httpkit this))
    (dissoc this :httpkit)))

(defn new-server [port]
  (map->Server {:port port}))
