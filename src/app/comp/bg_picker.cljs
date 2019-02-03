
(ns app.comp.bg-picker
  (:require [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo.comp.space :refer [=<]]
            [respo.core :refer [defcomp <> action-> list-> cursor-> span div]]
            [app.config :as config]
            [inflow-popup.comp.popup :refer [comp-popup]]))

(def basic-colors
  [(hsl 0 0 50)
   (hsl 0 0 70)
   (hsl 0 0 90)
   (hsl 0 70 60)
   (hsl 40 70 60)
   (hsl 80 70 60)
   (hsl 120 70 60)
   (hsl 160 70 60)
   (hsl 200 70 60)])

(defcomp
 comp-bg-picker
 (states template-id path markup)
 (div
  {:style ui/row-middle}
  (<> "Background:")
  (=< 8 nil)
  (cursor->
   :bg-color
   comp-popup
   states
   {:trigger (div
              {:style {:width 24,
                       :height 24,
                       :background-color (or (get-in markup [:style "background-color"])
                                             (hsl 0 0 80))}})}
   (fn [toggle!]
     (div
      {:style {:width 320}}
      (div {} (<> "pick a color"))
      (list->
       {:style ui/row-middle}
       (->> basic-colors
            (map
             (fn [color]
               [color
                (div
                 {:style {:background-color color,
                          :width 20,
                          :height 20,
                          :margin "0 8px 8px 0",
                          :cursor :pointer},
                  :on-click (fn [e d! m!]
                    (d!
                     :template/set-node-style
                     {:template-id template-id,
                      :path path,
                      :property "background-color",
                      :value color}))})])))))))))
