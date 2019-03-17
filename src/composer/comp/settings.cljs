
(ns composer.comp.settings
  (:require [hsl.core :refer [hsl]]
            [composer.schema :as schema]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp cursor-> list-> <> span div button input]]
            [respo.comp.space :refer [=<]]
            [composer.config :as config]
            [feather.core :refer [comp-i]]
            [inflow-popup.comp.popup :refer [comp-popup]]
            [respo.util.list :refer [map-val]]
            [respo-alerts.comp.alerts :refer [comp-confirm comp-prompt]]
            [clojure.string :as string]))

(defcomp
 comp-color-creator
 (states on-toggle)
 (let [state (or (:data states) {:name "", :color "", :group "theme"})]
   (div
    {:style ui/column}
    (=< nil 8)
    (div {} (<> "Add a color" {:font-family ui/font-fancy, :color (hsl 0 0 70)}))
    (input
     {:placeholder "group",
      :style ui/input,
      :value (:group state),
      :on-input (fn [e d! m!] (m! (assoc state :group (:value e))))})
    (=< nil 8)
    (input
     {:placeholder "name",
      :style ui/input,
      :value (:name state),
      :on-input (fn [e d! m!] (m! (assoc state :name (:value e))))})
    (=< nil 8)
    (input
     {:placeholder "color",
      :style ui/input,
      :value (:color state),
      :on-input (fn [e d! m!] (m! (assoc state :color (:value e))))})
    (=< nil 8)
    (div
     {:style ui/row-parted}
     (span nil)
     (button
      {:style ui/button,
       :inner-text "Add",
       :on-click (fn [e d! m!] (m! nil) (d! :settings/add-color state) (on-toggle m!))})))))

(defcomp
 comp-color-drop
 (states color)
 (div
  {:style (merge
           ui/center
           {:margin 8, :padding "8px 32px", :border "1px solid #eee", :position :relative})}
  (div {} (<> (:name color)))
  (<> (:color color) {:font-size 12, :color (hsl 0 0 70), :font-family ui/font-code})
  (cursor->
   :update
   comp-prompt
   states
   {:trigger (div
              {:style {:background-color (:color color),
                       :width 40,
                       :height 40,
                       :border "1px solid #eee"}}),
    :initial (:color color),
    :input-style {:font-family ui/font-code},
    :text "Change color"}
   (fn [result d! m!]
     (when-not (string/blank? result)
       (d! :settings/update-color {:id (:id color), :color result}))))
  (cursor->
   :remove
   comp-confirm
   states
   {:text "Sure to remove?",
    :trigger (comp-i :x 14 (hsl 0 80 70)),
    :style {:position :absolute, :right 8, :top 8}}
   (fn [e d! m!] (d! :settings/remove-color (:id color))))))

(defcomp
 comp-colors-manager
 (states colors)
 (let [grouped-colors (group-by :group (vals colors))
       state (or (:data states) {:editing-color? false})]
   (div
    {}
    (div
     {:style (merge
              ui/row-middle
              {:font-family ui/font-fancy, :font-size 20, :color (hsl 0 0 70)})}
     (<> "Colors")
     (=< 8 nil)
     (cursor->
      :create
      comp-popup
      states
      {:trigger (comp-i "plus" 20 (hsl 200 100 80))}
      (fn [on-toggle] (cursor-> :creator comp-color-creator states on-toggle))))
    (list->
     {}
     (->> grouped-colors
          (map
           (fn [[group-name colors]]
             [(or group-name "theme")
              (div
               {}
               (div
                {}
                (<>
                 (or group-name "theme")
                 {:font-family ui/font-fancy, :color (hsl 0 0 70), :font-size 16}))
               (list->
                {:style ui/row}
                (->> colors
                     (map
                      (fn [color]
                        [(:id color) (cursor-> (:id color) comp-color-drop states color)])))))])))))))

(defcomp
 comp-settings
 (states settings)
 (div
  {:style {:padding 16}}
  (cursor-> :colors comp-colors-manager states (:colors settings))))
