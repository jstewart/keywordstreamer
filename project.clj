(defproject keywordstreamer "0.1.0-SNAPSHOT"
  :description "Keyword Ideas Streamed Live to You"
  :url "http://example.com/"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main keywordstreamer.system
  :min-lein-version "2.0.0"

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.7.228"]
                 [org.clojure/core.async "0.2.374"]
                 [com.stuartsierra/component "0.3.1"]
                 [com.taoensso/encore "2.32.0"]
                 [com.taoensso/sente "1.7.0"]
                 [com.taoensso/timbre "4.2.1"]
                 [jetty/javax.servlet "5.1.12"]
                 [compojure "1.4.0"]
                 [clj-http "2.0.1"] ; Need proxy support for the client
                 [environ "1.0.1"]
                 [http-kit "2.1.19"]
                 [reagent "0.6.0-alpha"]
                 [re-frame "0.7.0-alpha"]
                 [ring/ring-anti-forgery "1.0.0"]
                 [ring.middleware.logger "0.5.0"]
                 [selmer "1.0.0"]
                 [testdouble/clojurescript.csv "0.2.0"]]

  :profiles {:dev {:plugins [[lein-cljsbuild "1.1.2"]
                             [lein-environ "1.0.1"]
                             [lein-figwheel "0.5.0-3"]
                             [lein-ancient "0.6.7"]]
                   :dependencies [[reloaded.repl "0.2.1"]]
                   :source-paths ["dev"]
                   :repl-options {:init-ns user}
                   :cljsbuild {:builds [{:id "dev"
                                         :source-paths ["src" "dev"]
                                         :figwheel { :on-jsload "keywordstreamer.core/reload" }
                                         :compiler {:main keywordstreamer.core
                                                    :asset-path "js/compiled/out-dev"
                                                    :output-to "resources/public/js/compiled/keywordstreamer-dev.js"
                                                    :output-dir "resources/public/js/compiled/out-dev"
                                                    :optimizations :none
                                                    :recompile-dependents true
                                                    :source-map true
                                                    :source-map-timestamp true }}

                                        ;; TODO: This needs to be it's own lein profile
                                        {:id "production"
                                         :source-paths ["src"]
                                         :compiler {:output-to "resources/public/js/keywordstreamer.js"
                                                    :asset-path "js/compiled/out"
                                                    :output-dir "resources/public/js/compiled/out"
                                                    :optimizations :advanced
                                                    :pretty-print false}}]}}}
  :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                    "target"]
  :figwheel {:css-dirs ["resources/public/css"] :nrepl-port 7888}

  :lis-opts {:redirect-output-to "/var/log/keywordstreamer.log"
             :properties {:host.ip "127.0.0.1" :port 8080 :app-env "production"}
             :jvm-opts ["-server"
                        "-Xss512K"
                        "-Xmx384M"]})
