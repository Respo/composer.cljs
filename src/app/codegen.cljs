
(ns app.codegen
  (:require [clojure.string :as string]
            [favored-edn.core :refer [write-edn]]
            [app.util :refer [neaten-templates]])
  (:require-macros [clojure.core.strint :refer [<<]]))

(defn generate-file [templates]
  (let [header "(ns composed.templates)"
        templates-code (-> templates neaten-templates write-edn)]
    (str header templates-code)
    (<< "~{header}\n\n(def templates ~{templates-code})\n")))
