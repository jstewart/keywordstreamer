(ns user
  (:require [reloaded.repl :refer [system reset stop]]
            [keywordstreamer.system]))

(reloaded.repl/set-init! #(keywordstreamer.system/create-system "0.0.0.0" 9009))
