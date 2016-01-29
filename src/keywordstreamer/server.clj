(ns keywordstreamer.server
  (require [clojure.core.async :refer [<! go-loop]]
           [taoensso.timbre :refer [info]]
           [com.stuartsierra.component :as component]
           [compojure.core :refer [routes defroutes GET POST]]
           [compojure.handler :as handler]
           [compojure.route :as route]
           [environ.core :refer [env]]
           [org.httpkit.server :refer [run-server]]
           [taoensso.sente :as sente]
           [taoensso.sente.server-adapters.http-kit :refer [sente-web-server-adapter]]
           [ring.util.codec :refer [url-decode]]
           [ring.middleware.logger :refer [wrap-with-logger]]
           [ring.util.response :refer [response resource-response
                                       header content-type set-cookie]])
  (use ring.middleware.anti-forgery
       ring.middleware.cookies
       ring.middleware.session
       ring.util.anti-forgery))

(defn make-app-routes
  [{:keys [ajax-post-fn ajax-get-or-ws-handshake-fn]}]
  (defroutes app-routes
    (GET "/" []
         (-> (resource-response "public/index.html")
             (set-cookie "csrf-token" *anti-forgery-token*)
             (content-type "text/html; charset=utf-8")))
    (route/resources "/")
    (GET  "/chsk" req (ajax-get-or-ws-handshake-fn req))
    (POST  "/download" [data]
           (-> (response (url-decode data))
               (header "Content-Disposition" "attachment; filename=keywords.csv")
               (content-type "text/csv; charset=utf-8")))
    (POST "/chsk" req (ajax-post-fn req))
    (route/not-found "Not Found")))

(defn make-handler
  [ws]
  (-> (make-app-routes ws)
      wrap-cookies
      wrap-anti-forgery
      wrap-session
      wrap-with-logger
      handler/api))

(defn uid-fn
  [ring-req]
  (:client-id ring-req))

(defn- start-server [handler host port ws]
  (let [server (run-server (make-handler ws) {:ip host :port port})]
    (info (str "Started server on localhost:" port))
    server))

(defn- stop-server [server]
  (when server
    (server)))

(defrecord Server [host port]
  component/Lifecycle
  (start [this]
    (let [ws      (sente/make-channel-socket! sente-web-server-adapter
                                              {:user-id-fn uid-fn})
          handler (make-handler ws)]
      (assoc this
             :httpkit (start-server handler host port ws)
             :ws      ws)))

  (stop [this]
    (info "stopping")
    (stop-server (:httpkit this))
    (dissoc this :httpkit :ws)))

(defn new-server [host port]
  (map->Server {:host host :port port}))
