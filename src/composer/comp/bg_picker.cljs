
(ns composer.comp.bg-picker
  (:require [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo.comp.space :refer [=<]]
            [respo.core :refer [defcomp <> >> list-> span div input button a]]
            [composer.config :as config]
            [respo-alerts.core :refer [use-modal]]
            [composer.style :as style]
            [respo.util.list :refer [map-val]]))

(defcomp
 comp-color-panel
 (states color-groups initial-color set-color! on-toggle)
 (let [state (or (:data states) {:text initial-color})]
   (div
    {:style {:width 360}}
    (div {} (<> "Pick a color" {:font-family ui/font-fancy}))
    (list->
     {}
     (->> color-groups
          (map-val
           (fn [color-group]
             (div
              {}
              (div {} (<> (or (:name color-group) "theme") {:font-family ui/font-fancy}))
              (list->
               {:style ui/row}
               (->> (:colors color-group)
                    (map-val
                     (fn [color]
                       (div
                        {:style (merge
                                 ui/center
                                 {:background-color (:color color),
                                  :width 32,
                                  :height 32,
                                  :margin 4,
                                  :cursor :pointer,
                                  :border "1px solid #ddd"}),
                         :on-click (fn [e d!] (set-color! (:color color) d!))}
                        (<> (:name color) {:color :white, :font-size 10})))))))))))
    (div
     {}
     (a
      {:style ui/link,
       :inner-text "Add colors",
       :on-click (fn [e d!] (d! :router/change {:name :settings, :data {:tab :colors}}))})))))

(defcomp
 comp-bg-picker
 (states template-id path markup color-groups)
 (let [bg-color (or (get-in markup [:style "background-color"]) (hsl 0 0 100))
       set-color! (fn [color d!]
                    (d!
                     :template/set-node-style
                     {:template-id template-id,
                      :path path,
                      :property "background-color",
                      :value color}))
       bg-modal (use-modal
                 (>> states :bg-colors)
                 {:style {:width 400, :padding "8px 16px"},
                  :render-body (fn [on-toggle]
                    (comp-color-panel
                     (>> states :panel)
                     color-groups
                     bg-color
                     set-color!
                     on-toggle))})]
   (div
    {:style ui/row-middle}
    (<> "Background:" style/field-label)
    (=< 8 nil)
    (div
     {:style {:width 24,
              :height 24,
              :background-color bg-color,
              :border "1px solid #ddd",
              :cursor :pointer},
      :on-click (fn [e d!] (println "click") ((:show bg-modal) d!))})
    (:ui bg-modal))))

(defcomp
 comp-font-color-picker
 (states template-id path markup color-groups)
 (let [init-color (or (get-in markup [:style "color"]) (hsl 0 0 100))
       set-color! (fn [color d!]
                    (d!
                     :template/set-node-style
                     {:template-id template-id, :path path, :property "color", :value color}))
       font-modal (use-modal
                   (>> states :font-color)
                   {:style {:width 400, :padding "8px 16px"},
                    :render-body (fn [on-toggle]
                      (comp-color-panel
                       (>> states :panel)
                       color-groups
                       init-color
                       set-color!
                       on-toggle))})]
   (div
    {:style ui/row-middle}
    (<> "Font color:" style/field-label)
    (=< 8 nil)
    (div
     {:style {:width 24,
              :height 24,
              :background-color init-color,
              :border "1px solid #ddd",
              :cursor :pointer},
      :on-click (fn [e d!] ((:show font-modal) d!))})
    (:ui font-modal))))
