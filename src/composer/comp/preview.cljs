
(ns composer.comp.preview
  (:require [hsl.core :refer [hsl]]
            [composer.schema :as schema]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp list-> cursor-> <> span div button input a]]
            [respo.comp.space :refer [=<]]
            [composer.config :as config]
            [respo.util.list :refer [map-val]]
            [composer.core :refer [render-markup]]
            [respo-alerts.comp.alerts :refer [comp-select]]
            [composer.comp.templates-list :refer [comp-templates-list]]
            [composer.util :refer [neaten-templates]]
            [clojure.string :as string])
  (:require-macros [clojure.core.strint :refer [<<]]))

(defn on-operation [d! op context options] (println op context (pr-str options)))

(def style-number (merge ui/input {:width 56, :min-width 56, :padding "0 4px"}))

(defcomp
 comp-preview
 (states templates focus-to shadows? focuses)
 (let [template-id (:template-id focus-to)
       template (get templates template-id)
       mock-id (:mock-pointer template)
       mock-data (if (some? mock-id) (get-in template [:mocks mock-id :data]) nil)
       mock-state (if (some? mock-id) (get-in template [:mocks mock-id :state]) nil)
       change-size! (fn [d! w h]
                      (d!
                       :template/set-preview-sizes
                       {:template-id template-id, :width w, :height h}))
       active-templates (->> focuses
                             (map (fn [[k info]] (get-in info [:focus :template-id])))
                             (set))
       focus-in-template (->> focuses
                              (filter
                               (fn [[k info]]
                                 (= template-id (get-in info [:focus :template-id])))))
       active-names (->> focus-in-template
                         (map (fn [[k info]] (get-in info [:user :name])))
                         (string/join ", "))]
   (div
    {:style (merge ui/flex ui/row {:overflow :auto})}
    (cursor-> :templates comp-templates-list states templates template-id active-templates)
    (div
     {:style (merge ui/flex ui/column)}
     (div
      {:style (merge
               ui/flex
               {:background-color (hsl 0 0 0), :overflow :auto, :display :flex})}
      (let [tmpls (neaten-templates templates)
            markup (get-in templates [template-id :markup])]
        (if (some? markup)
          (div
           {:class-name (if shadows? "dev-shadows" ""),
            :style (merge
                    ui/column
                    {:background-color (hsl 0 0 100 1),
                     :width (or (:width template) "100%"),
                     :height (or (:height template) "100%"),
                     :margin :auto,
                     :position :relative})}
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
             :template-name (:name template),
             :state-path [],
             :states mock-state}
            on-operation))
          (span
           {:style {:color (hsl 0 0 60),
                    :font-family ui/font-fancy,
                    :font-size 16,
                    :margin :auto},
            :inner-text "No selected template."}))))
     (div
      {:style ui/row-parted}
      (div
       {:style ui/row-middle}
       (input
        {:style style-number,
         :type "number",
         :value (:width template),
         :on-input (fn [e d! m!] (change-size! d! (:value e) (:height template)))})
       (=< 8 nil)
       (input
        {:style style-number,
         :type "number",
         :value (:height template),
         :on-input (fn [e d! m!] (change-size! d! (:width template) (:value e)))})
       (=< 8 nil)
       (a
        {:style ui/link,
         :inner-text "100x240",
         :on-click (fn [e d! m!] (change-size! d! 100 240))})
       (a
        {:style ui/link,
         :inner-text "240x60",
         :on-click (fn [e d! m!] (change-size! d! 240 60))})
       (a
        {:style ui/link,
         :inner-text "Full",
         :on-click (fn [e d! m!] (change-size! d! nil nil))})
       (=< 8 nil)
       (input
        {:type "checkbox",
         :style {:cursor :pointer},
         :checked shadows?,
         :on-change (fn [e d! m!] (d! :session/toggle-shadows nil))})
       (<> "shadows?" {:color (hsl 0 0 70)})
       (=< 8 nil)
       (a
        {:style ui/link,
         :inner-text "Emulate",
         :on-click (fn [e d! m!] (d! :router/change {:name :emulate, :data template-id}))}))
      (<>
       active-names
       {:font-family ui/font-fancy, :font-size 12, :color (hsl 0 0 70), :margin-right 8}))))))

(defn get-mocks [mocks] (->> mocks (map (fn [[k m]] {:value k, :display (:name m)}))))
