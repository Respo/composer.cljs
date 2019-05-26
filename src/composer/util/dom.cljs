
(ns composer.util.dom (:require [cumulo-util.core :refer [delay!]]))

(defn focus-element! [query]
  (delay!
   0.1
   (fn []
     (if-let [target (.querySelector js/document query)]
       (.focus target)
       (js/console.log "Found no element:" query)))))
