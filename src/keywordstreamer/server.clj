(ns keywordstreamer.server
  (require [clojure.core.async :refer [<! go-loop]]
           [com.stuartsierra.component :as component]
           [compojure.core :refer [routes defroutes GET POST]]
           [compojure.handler :as handler]
           [compojure.route :as route]
           [org.httpkit.server :refer [run-server]]
           [selmer.parser :as parser]
           [taoensso.sente :as sente]
           [taoensso.sente.server-adapters.http-kit :refer [sente-web-server-adapter]]
           [taoensso.timbre :as timbre :refer [info]]
           [ring.util.codec :refer [url-decode]]
           [ring.util.response :refer [response header content-type]])
  (use ring.middleware.anti-forgery
       ring.middleware.session
       ring.util.anti-forgery))

(defn render [template & [params]]
  (-> template
      (parser/render-file
       (assoc params
              :page template))
      response
      (content-type "text/html; charset=utf-8")))

(defn make-app-routes
  [{:keys [ajax-post-fn ajax-get-or-ws-handshake-fn]}]
  (defroutes app-routes
    (GET "/" [] (render "public/index.html"
                        {:anti-forgery-token *anti-forgery-token*
                         :app-env (get (System/getenv) "APP_ENV" "dev")}))
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
  (->> (make-app-routes ws)
       wrap-anti-forgery
       wrap-session
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
