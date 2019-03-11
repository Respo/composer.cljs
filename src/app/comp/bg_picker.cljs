
(ns app.comp.bg-picker
  (:require [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo.comp.space :refer [=<]]
            [respo.core
             :refer
             [defcomp <> action-> list-> cursor-> span div input button a]]
            [app.config :as config]
            [inflow-popup.comp.popup :refer [comp-popup]]
            [app.style :as style]))

(def basic-colors
  [(hsl 0 0 50)
   (hsl 0 0 70)
   (hsl 0 0 90)
   (hsl 0 70 60)
   (hsl 40 70 60)
   (hsl 80 70 60)
   (hsl 120 70 60)
   (hsl 160 70 60)
   (hsl 200 70 60)
   (hsl 0 0 100)])

(def style-label {:width 20, :height 20, :margin "0 8px 8px 0", :cursor :pointer})

(defcomp
 comp-color-panel
 (states initial-color template-id path on-toggle)
 (let [state (or (:data states) {:text initial-color})
       set-color! (fn [color d!]
                    (d!
                     :template/set-node-style
                     {:template-id template-id,
                      :path path,
                      :property "background-color",
                      :value color}))]
   (div
    {:style {:width 360}}
    (div {} (<> "Pick a color" {:font-family ui/font-fancy}))
    (list->
     {:style ui/row-middle}
     (->> basic-colors
          (map
           (fn [color]
             [color
              (div
               {:style (merge
                        style-label
                        {:background-color color, :border "1px solid #ddd"}),
                :on-click (fn [e d! m!] (set-color! color d!))})]))))
    (div
     {:style ui/row-middle}
     (input
      {:style (merge ui/input {:font-family ui/font-code, :width 160}),
       :value (:text state),
       :placeholder "A color",
       :on-input (fn [e d! m!] (m! (assoc state :text (:value e))))})
     (=< 8 nil)
     (button
      {:style style/button,
       :inner-text "Set",
       :on-click (fn [e d! m!] (set-color! (:text state) d!))})
     (=< 8 nil)
     (a
      {:style style/link,
       :inner-text "Transparent",
       :on-click (fn [e d! m!] (set-color! "transparent" d!))})))))

(defcomp
 comp-bg-picker
 (states template-id path markup)
 (let [bg-color (or (get-in markup [:style "background-color"]) (hsl 0 0 80))]
   (div
    {:style ui/row-middle}
    (<> "Background:" style/field-label)
    (=< 8 nil)
    (cursor->
     :bg-color
     comp-popup
     states
     {:trigger (div
                {:style {:width 24,
                         :height 24,
                         :background-color bg-color,
                         :border "1px solid #ddd"}})}
     (fn [on-toggle]
       (cursor-> :panel comp-color-panel states bg-color template-id path on-toggle))))))
