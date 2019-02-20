
(ns app.comp.preview
  (:require [hsl.core :refer [hsl]]
            [app.schema :as schema]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp list-> cursor-> <> span div button]]
            [respo.comp.space :refer [=<]]
            [app.config :as config]
            [respo.util.list :refer [map-val]]
            [respo-composer.core :refer [render-markup]]
            [app.util :refer [neaten-templates]]
            [respo-alerts.comp.alerts :refer [comp-select]])
  (:require-macros [clojure.core.strint :refer [<<]]))

(defn get-mocks [mocks] (->> mocks (map (fn [[k m]] {:value k, :display (:name m)}))))

(defcomp
 comp-preview
 (states templates router-data)
 (let [template-id (:pointer router-data)
       mock-id (:focused-mock router-data)
       template (get templates template-id)
       mock-data (get-in template [:mocks mock-id :data])]
   (div
    {:style (merge ui/flex ui/row {:padding "0 8px"})}
    (div
     {:style (merge ui/column {:width 240, :padding 8})}
     (div {:style {:font-family ui/font-fancy}} (<> "Templates"))
     (list->
      {}
      (->> templates
           (map-val
            (fn [template]
              (div
               {:style (merge
                        {:line-height "40px", :cursor :pointer, :padding "0 8px"}
                        (if (= (:pointer router-data) (:id template))
                          {:background-color (hsl 0 0 90)})),
                :on-click (fn [e d! m!] (d! :router/set-pointer (:id template)))}
               (<> (:name template))))))))
    (div
     {:style (merge ui/flex ui/column)}
     (div
      {:style (merge ui/flex ui/center {:background-color (hsl 0 0 88), :overflow :auto})}
      (let [tmpls (neaten-templates templates)]
        (render-markup
         (get-in templates [(:pointer router-data) :markup])
         {:data mock-data, :templates tmpls, :level 0}
         (fn [op op-data] (println op op-data)))))
     (div
      {:style ui/row-parted}
      (div
       {}
       (cursor->
        :mock
        comp-select
        states
        (:focused-mock router-data)
        (get-mocks (get-in templates [(:pointer router-data) :mocks]))
        {:text "Select mock data"}
        (fn [result d! m!] (d! :router/set-focused-mock result)))))))))
