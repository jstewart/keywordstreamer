(defproject keywordstreamer "0.1.0-SNAPSHOT"
  :description "Keyword Ideas Streamed Live to You"
  :url "http://example.com/"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main keywordstreamer.system

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.122"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [com.stuartsierra/component "0.2.3"]
                 [com.taoensso/sente "1.6.0"]
                 [com.taoensso/timbre "4.1.1"]
                 [jetty/javax.servlet "5.1.12"]
                 [compojure "1.4.0"]
                 [http-kit "2.1.19"]
                 [reagent "0.5.1"]
                 [re-frame "0.4.1"]]

  :profiles {:dev {:plugins [[lein-cljsbuild "1.0.6"]
                             [lein-figwheel "0.3.7"]
                             [lein-ancient "0.6.7"]]
                   :dependencies [[reloaded.repl "0.2.0"]]
                   :source-paths ["dev"]
                   :cljsbuild {:builds [{:source-paths ["src" "dev"]
                                         :figwheel { :on-jsload "keywordstreamer.core/reload" }
                                         :compiler {:main keywordstreamer.core
                                                    :asset-path "js/compiled/out"
                                                    :output-to "resources/public/js/compiled/keywordstreamer.js"
                                                    :output-dir "resources/public/js/compiled/out"
                                                    :optimizations :none
                                                    :recompile-dependents true
                                                    :source-map true
                                                    :source-map-timestamp true }}]}}}
  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]
  :figwheel {:css-dirs ["resources/public/css"] :nrepl-port 7888})
