
(ns composer.comp.emulate
  (:require [respo.core :refer [defcomp <> div input button span]]
            [respo.comp.space :refer [=<]]
            [respo.comp.inspect :refer [comp-inspect]]
            [respo-ui.core :as ui]
            [composer.schema :as schema]
            [composer.style :as style]
            [composer.config :as config]
            [composer.util :refer [neaten-templates]]
            [composer.core :refer [render-markup]]
            [hsl.core :refer [hsl]]
            [feather.core :refer [comp-icon]]))

(defcomp
 comp-emulate
 (templates template-id)
 (let [tmpls (neaten-templates templates)
       template (get templates template-id)
       markup (get-in templates [template-id :markup])
       mock-id (:mock-pointer template)
       mock-data (if (some? mock-id) (get-in template [:mocks mock-id :data]) nil)]
   (div
    {}
    (render-markup
     markup
     {:data mock-data,
      :templates tmpls,
      :level 0,
      :functions {:preview (fn [param style on-action children]
                    (println "funcition" param style on-action children)
                    (<>
                     "No preview for function"
                     {:color 'white, :background-color (hsl 200 80 80)}))},
      :template-name (:name template)}
     (fn [d! op context options] (println op context (pr-str options))))
    (comp-icon
     :x
     {:font-size 14, :color (hsl 0 0 80), :position :fixed, :bottom 8, :right 8}
     (fn [e d!] (d! :router/change {:name :preview}))))))
