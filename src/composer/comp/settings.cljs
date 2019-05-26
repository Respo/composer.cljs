
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
            [clojure.string :as string]
            [composer.util.dom :refer [focus-element!]])
  (:require-macros [clojure.core.strint :refer [<<]]))

(defcomp
 comp-color-creator
 (states on-toggle color-group)
 (let [state (or (:data states) {:name "", :color ""})]
   (div
    {:style ui/column}
    (div
     {}
     (<>
      (<< "Add a color in ~(:name color-group)")
      {:font-family ui/font-fancy, :color (hsl 0 0 70)}))
    (=< nil 8)
    (input
     {:placeholder "name",
      :style ui/input,
      :value (:name state),
      :on-input (fn [e d! m!] (m! (assoc state :name (:value e)))),
      :auto-focus true,
      :class-name "color-name"})
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
       :on-click (fn [e d! m!]
         (m! nil)
         (d!
          :settings/add-color
          {:group-id (:id color-group), :name (:name state), :color (:color state)})
         (on-toggle m!))})))))

(defcomp
 comp-color-drop
 (states color group-id)
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
       (d! :settings/update-color {:id (:id color), :group-id group-id, :color result}))))
  (cursor->
   :remove
   comp-confirm
   states
   {:text "Sure to remove?",
    :trigger (comp-i :x 14 (hsl 0 80 70)),
    :style {:position :absolute, :right 8, :top 8}}
   (fn [e d! m!] (d! :settings/remove-color {:id (:id color), :group-id group-id})))))

(defcomp
 comp-color-group
 (states color-group)
 (div
  {}
  (div
   {:style ui/row-parted}
   (div
    {:style ui/row-middle}
    (<>
     (:name color-group)
     {:font-family ui/font-fancy, :color (hsl 0 0 70), :font-size 16})
    (=< 8 nil)
    (cursor->
     :rename
     comp-prompt
     states
     {:trigger (comp-i :edit 20 (hsl 200 100 80)), :style {:display :inline-block}}
     (fn [result d! m!]
       (d! :settings/rename-color-group {:id (:id color-group), :name result})))
    (=< 8 nil)
    (cursor->
     :create
     comp-popup
     states
     {:trigger (comp-i "plus" 20 (hsl 200 100 80)),
      :style {:display :inline-block},
      :on-popup (fn [e d! m!] (focus-element! ".color-name"))}
     (fn [on-toggle] (cursor-> :creator comp-color-creator states on-toggle color-group))))
   (cursor->
    :remove
    comp-confirm
    states
    {:trigger (comp-i "x" 20 (hsl 0 100 80)), :style {:display :inline-block}}
    (fn [e d! m!] (d! :settings/remove-color-group (:id color-group)))))
  (list->
   {:style ui/row}
   (->> (:colors color-group)
        (map-val
         (fn [color] (cursor-> (:id color) comp-color-drop states color (:id color-group))))))))

(defcomp
 comp-group-creator
 (states on-toggle)
 (let [state (or (:data states) {:name ""})]
   (div
    {:style ui/column}
    (div {} (<> "Add group" {:font-family ui/font-fancy, :font-size 20}))
    (=< nil 16)
    (input
     {:style ui/input,
      :autofocus true,
      :placeholder "name",
      :value (:name state),
      :on-input (fn [e d! m!] (m! (assoc state :name (:value e)))),
      :class-name "group-name"})
    (=< nil 16)
    (div
     {:style ui/row-parted}
     (span nil)
     (button
      {:style ui/button,
       :inner-text "Submit",
       :on-click (fn [e d! m!]
         (d! :settings/add-color-group (:name state))
         (m! nil)
         (on-toggle m!))})))))

(defcomp
 comp-colors-manager
 (states color-groups)
 (div
  {}
  (div
   {:style (merge
            ui/row-middle
            {:font-family ui/font-fancy, :font-size 20, :color (hsl 0 0 70)})}
   (<> "Colors")
   (=< 8 nil)
   (cursor->
    :create-group
    comp-popup
    states
    {:trigger (comp-i "plus" 20 (hsl 200 100 80)),
     :on-popup (fn [e d! m!] (focus-element! ".group-name"))}
    (fn [on-toggle] (cursor-> :group-creator comp-group-creator states on-toggle))))
  (list->
   {:style {:margin-left 16}}
   (->> color-groups
        (map-val
         (fn [color-group] (cursor-> (:id color-group) comp-color-group states color-group)))))))

(defcomp
 comp-settings
 (states settings)
 (div
  {:style {:padding 16}}
  (cursor-> :colors comp-colors-manager states (:color-groups settings))))
