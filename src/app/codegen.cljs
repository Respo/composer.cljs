
(ns app.codegen
  (:require [clojure.string :as string] [favored-edn.core :refer [write-edn]])
  (:require-macros [clojure.core.strint :refer [<<]]))

(defn generate-file [templates]
  (let [header "(ns composed.templates)\n"
        defs (->> templates
                  (sort-by first)
                  vals
                  (map
                   (fn [template]
                     (let [data-str (write-edn (:markup template))]
                       (<< "\n(def ~(:name template) ~{data-str})\n"))))
                  (string/join ""))]
    (str header defs)))
