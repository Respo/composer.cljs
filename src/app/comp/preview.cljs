
(ns app.comp.preview
  (:require [hsl.core :refer [hsl]]
            [app.schema :as schema]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp list-> cursor-> <> span div button input a]]
            [respo.comp.space :refer [=<]]
            [app.config :as config]
            [respo.util.list :refer [map-val]]
            [respo-composer.core :refer [render-markup]]
            [app.util :refer [neaten-templates]]
            [respo-alerts.comp.alerts :refer [comp-select]])
  (:require-macros [clojure.core.strint :refer [<<]]))

(defcomp
 comp-template-list
 (templates router-data)
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
            (<> (:name template)))))))))

(defn get-mocks [mocks] (->> mocks (map (fn [[k m]] {:value k, :display (:name m)}))))

(def style-number (merge ui/input {:width 56, :min-width 56, :padding "0 4px"}))

(defcomp
 comp-preview
 (states templates router-data)
 (let [template-id (:pointer router-data)
       mock-id (:focused-mock router-data)
       template (get templates template-id)
       mock-data (get-in template [:mocks mock-id :data])]
   (div
    {:style (merge ui/flex ui/row {:padding "0 8px"})}
    (comp-template-list templates router-data)
    (div
     {:style (merge ui/flex ui/column)}
     (div
      {:style (merge ui/flex ui/center {:background-color (hsl 0 0 0), :overflow :auto})}
      (div
       {:style {:background-color (hsl 0 0 100 0.2),
                :width (or (:width router-data) "100%"),
                :height (or (:height router-data) "100%")}}
       (let [tmpls (neaten-templates templates)]
         (render-markup
          (get-in templates [(:pointer router-data) :markup])
          {:data mock-data, :templates tmpls, :level 0}
          (fn [op op-data] (println op op-data))))))
     (div
      {:style ui/row-middle}
      (div
       {}
       (cursor->
        :mock
        comp-select
        states
        (:focused-mock router-data)
        (get-mocks (get-in templates [(:pointer router-data) :mocks]))
        {:text "Select mock data"}
        (fn [result d! m!] (d! :router/set-focused-mock result))))
      (=< 8 nil)
      (input
       {:style style-number,
        :type "number",
        :value (:width router-data),
        :on-input (fn [e d! m!]
          (d! :router/set-preview-sizes {:width (:value e), :height (:height router-data)}))})
      (input
       {:style style-number,
        :type "number",
        :value (:height router-data),
        :on-input (fn [e d! m!]
          (d! :router/set-preview-sizes {:width (:width router-data), :height (:value e)}))})
      (a
       {:style ui/link,
        :inner-text "100x400",
        :on-click (fn [e d! m!] (d! :router/set-preview-sizes {:width 100, :height 400}))})
      (a
       {:style ui/link,
        :inner-text "400x100",
        :on-click (fn [e d! m!] (d! :router/set-preview-sizes {:width 400, :height 100}))})
      (a
       {:style ui/link,
        :inner-text "full",
        :on-click (fn [e d! m!] (d! :router/set-preview-sizes {:width nil, :height nil}))}))))))
