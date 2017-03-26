(defproject keywordstreamer "0.1.0-SNAPSHOT"
  :description "Keyword Ideas Streamed Live to You"
  :url "http://example.com/"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main keywordstreamer.system
  :min-lein-version "2.0.0"

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.494"]
                 [org.clojure/core.async "0.3.442"]
                 [com.stuartsierra/component "0.3.2"]
                 [com.taoensso/encore "2.90.1"]
                 [com.taoensso/sente "1.11.0"]
                 [com.taoensso/timbre "4.8.0"]
                 [jetty/javax.servlet "5.1.12"]
                 [compojure "1.5.2"]
                 [clj-http "2.3.0"]
                 [environ "1.1.0"]
                 [http-kit "2.2.0"]
                 [reagent "0.6.1"]
                 [re-frame "0.9.2"]
                 [ring/ring-anti-forgery "1.0.1"]
                 [ring.middleware.logger "0.5.0"]
                 [testdouble/clojurescript.csv "0.2.0"]]
  :plugins [[lein-cljsbuild "1.1.5"]]

  :prep-tasks [["cljsbuild" "once" "production"] ["compile"]]

  ;; Cljs Build
  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src" "dev"]
                        :figwheel { :on-jsload "keywordstreamer.core/reload" }
                        :compiler {:main keywordstreamer.core
                                   :asset-path "js/compiled/out-dev"
                                   :output-to "resources/public/js/keywordstreamer.js"
                                   :output-dir "resources/public/js/compiled/out-dev"
                                   :optimizations :none
                                   :recompile-dependents true
                                   :source-map true
                                   :source-map-timestamp true }}

                       {:id "production"
                        :source-paths ["src"]
                        :jar true
                        :compiler {:output-to "resources/public/js/keywordstreamer.js"
                                   :asset-path "js/compiled/out"
                                   :output-dir "resources/public/js/compiled/out"
                                   ;; Can't use :advanced until this is fixed:
                                   ;; http://dev.clojure.org/jira/browse/CLJS-1954
                                   ;; https://github.com/google/closure-compiler/issues/2336
                                   ;;:optimizations :advanced
                                   :optimizations :whitespace
                                   :pretty-print false}}]}

  :profiles {:dev {:plugins [[lein-environ "1.1.0"]
                             [lein-figwheel "0.5.9"]
                             [lein-ancient "0.6.10"]]
                   :dependencies [[reloaded.repl "0.2.3"]]
                   :source-paths ["dev"]
                   :repl-options {:init-ns user}}
             :uberjar {:aot :all}}

  :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                    "resources/public/js/keywordstreamer.js"
                                    "target"]

  ;; Figwheel Options
  :figwheel {:css-dirs ["resources/public/css"] :nrepl-port 7888}

  ;; Init Script
  :lis-opts {:redirect-output-to "/var/log/keywordstreamer.log"
             :properties {:host.ip "127.0.0.1" :port 8080 :app-env "production"}
             :jvm-opts ["-server"
                        "-Xss512K"
                        "-Xmx384M"]})
