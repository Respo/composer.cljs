
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

(def style-number (merge ui/input {:width 56, :min-width 56, :padding "0 4px"}))

(defcomp
 comp-preview
 (states templates router-data)
 (let [template-id (:pointer router-data)
       template (get templates template-id)
       mock-id (:mock-pointer template)
       mock-data (if (some? mock-id) (get-in template [:mocks mock-id :data]) nil)]
   (div
    {:style (merge ui/flex ui/row {:padding "0 8px"})}
    (comp-template-list templates router-data)
    (div
     {:style (merge ui/flex ui/column)}
     (div
      {:style (merge
               ui/flex
               {:background-color (hsl 0 0 0), :overflow :auto, :display :flex})}
      (let [tmpls (neaten-templates templates)
            markup (get-in templates [(:pointer router-data) :markup])]
        (if (some? markup)
          (div
           {:style {:background-color (hsl 0 0 100 0.2),
                    :width (or (:width router-data) "100%"),
                    :height (or (:height router-data) "100%"),
                    :margin :auto}}
           (render-markup
            markup
            {:data mock-data, :templates tmpls, :level 0}
            (fn [op op-data] (println op op-data))))
          (span
           {:style {:color (hsl 0 0 60),
                    :font-family ui/font-fancy,
                    :font-size 16,
                    :margin :auto},
            :inner-text "No selected template."}))))
     (div
      {:style ui/row-middle}
      (input
       {:style style-number,
        :type "number",
        :value (:width router-data),
        :on-input (fn [e d! m!]
          (d! :router/set-preview-sizes {:width (:value e), :height (:height router-data)}))})
      (=< 8 nil)
      (input
       {:style style-number,
        :type "number",
        :value (:height router-data),
        :on-input (fn [e d! m!]
          (d! :router/set-preview-sizes {:width (:width router-data), :height (:value e)}))})
      (=< 8 nil)
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

(defn get-mocks [mocks] (->> mocks (map (fn [[k m]] {:value k, :display (:name m)}))))
