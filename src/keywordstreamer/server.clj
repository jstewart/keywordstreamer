(ns keywordstreamer.server
  (require [clojure.core.async :refer [<! go-loop]]
           [com.stuartsierra.component :as component]
           [compojure.core :refer [routes defroutes GET POST]]
           [compojure.handler :as handler]
           [compojure.route :as route]
           [org.httpkit.server :refer [run-server]]
           [taoensso.sente :as sente]
           [taoensso.sente.server-adapters.http-kit :refer [sente-web-server-adapter]]
           [taoensso.timbre :as timbre :refer [info]]
           [ring.util.response :as resp])
  (use ring.middleware.anti-forgery
       ring.middleware.session))

(defn event-loop
  "Handle inbound events"
  [ch-chsk]
  (go-loop [{:keys [client-id event] :as event-map} (<! ch-chsk)]
    (info (str client-id ":" event))
    ;; (thread (handle-event evt req))
    (recur (<! ch-chsk))))

(defn make-app-routes
  [{:keys [ajax-post-fn ajax-get-or-ws-handshake-fn]}]
  (defroutes app-routes
    (GET "/" [] (resp/resource-response "index.html" {:root "public"}))
    (route/resources "/")
    (GET  "/chsk" req (ajax-get-or-ws-handshake-fn req))
    (POST "/chsk" req (ajax-post-fn req))
    (route/not-found "Not Found")))

(defn make-handler
  [ws]
  (->> (make-app-routes ws)
       wrap-anti-forgery
       wrap-session
       handler/api))

(defn- start-server [handler port ws]
  (let [server (run-server (make-handler ws) {:port port})]
    (event-loop (:ch-recv ws))
    (info (str "Started server on localhost:" port))
    server))

(defn- stop-server [server]
  (when server
    (server)))

(defrecord Server [port]
  component/Lifecycle
  (start [this]
    (let [ws      (sente/make-channel-socket! sente-web-server-adapter {})
          handler (make-handler ws)]
      (assoc this
             :httpkit (start-server handler port ws)
             :ws      ws)))

  (stop [this]
    (info "stopping")
    (stop-server (:httpkit this))
    (dissoc this :httpkit :ws)))

(defn new-server [port]
  (map->Server {:port port}))
