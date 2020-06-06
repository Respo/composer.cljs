
(ns composer.comp.colors-manager
  (:require [hsl.core :refer [hsl]]
            [composer.schema :as schema]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp >> list-> <> span div button input]]
            [respo.comp.space :refer [=<]]
            [composer.config :as config]
            [feather.core :refer [comp-i comp-icon]]
            [respo.util.list :refer [map-val]]
            [respo-alerts.core :refer [comp-confirm comp-prompt use-modal]]
            [clojure.string :as string]
            [composer.util.dom :refer [focus-element!]]
            [cumulo-util.core :refer [delay!]])
  (:require-macros [clojure.core.strint :refer [<<]]))

(defcomp
 comp-color-creator
 (states on-toggle color-group)
 (let [cursor (:cursor states), state (or (:data states) {:name "", :color ""})]
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
      :on-input (fn [e d!] (d! cursor (assoc state :name (:value e)))),
      :auto-focus true,
      :class-name "color-name"})
    (=< nil 8)
    (input
     {:placeholder "color",
      :style ui/input,
      :value (:color state),
      :on-input (fn [e d!] (d! cursor (assoc state :color (:value e))))})
    (=< nil 8)
    (div
     {:style ui/row-parted}
     (span nil)
     (button
      {:style ui/button,
       :inner-text "Add",
       :on-click (fn [e d!]
         (d! cursor nil)
         (d!
          :settings/add-color
          {:group-id (:id color-group), :name (:name state), :color (:color state)})
         (on-toggle d!))})))))

(defcomp
 comp-color-drop
 (states color group-id)
 (div
  {:style (merge
           ui/center
           {:margin 8, :padding "8px 32px", :border "1px solid #eee", :position :relative})}
  (div
   {}
   (comp-prompt
    (>> states :update-name)
    {:trigger (<> (:name color)),
     :initial (:name color),
     :input-style {:font-family ui/font-code},
     :text "Change name"}
    (fn [result d!]
      (when-not (string/blank? result)
        (d! :settings/update-color {:id (:id color), :group-id group-id, :name result})))))
  (<> (:color color) {:font-size 12, :color (hsl 0 0 70), :font-family ui/font-code})
  (comp-prompt
   (>> states :update)
   {:trigger (div
              {:style {:background-color (:color color),
                       :width 40,
                       :height 40,
                       :border "1px solid #eee"}}),
    :initial (:color color),
    :input-style {:font-family ui/font-code},
    :text "Change color"}
   (fn [result d!]
     (when-not (string/blank? result)
       (d! :settings/update-color {:id (:id color), :group-id group-id, :color result}))))
  (comp-confirm
   (>> states :remove)
   {:text "Sure to remove?",
    :trigger (comp-i :x 14 (hsl 0 80 70)),
    :style {:position :absolute, :right 8, :top 8}}
   (fn [e d!] (d! :settings/remove-color {:id (:id color), :group-id group-id})))))

(defcomp
 comp-color-group
 (states color-group)
 (let [creation-modal (use-modal
                       (>> states :create)
                       {:style {:padding "8px 16px", :width 300},
                        :render-body (fn [on-toggle]
                          (comp-color-creator (>> states :creator) on-toggle color-group))})]
   (div
    {:style {:border-top (str "1px solid " (hsl 0 0 96)), :margin-bottom 16, :padding 8}}
    (div
     {:style ui/row-parted}
     (div
      {:style ui/row-middle}
      (<>
       (:name color-group)
       {:font-family ui/font-fancy, :color (hsl 0 0 70), :font-size 16})
      (=< 8 nil)
      (comp-prompt
       (>> states :rename)
       {:trigger (comp-i :edit-2 14 (hsl 200 100 80)),
        :style {:display :inline-block},
        :initial (:name color-group),
        :text "New name for this group:"}
       (fn [result d!]
         (d! :settings/rename-color-group {:id (:id color-group), :name result})))
      (=< 8 nil)
      (comp-icon
       :plus
       {:color (hsl 200 100 80), :font-size 20, :cursor :pointer, :display :inline-block}
       (fn [e d!]
         ((:show creation-modal) d!)
         (delay! 0.4 (fn [] (focus-element! ".color-name"))))))
     (comp-confirm
      (>> states :remove)
      {:trigger (comp-i "x" 20 (hsl 0 100 80)),
       :style {:display :inline-block},
       :text (<< "Remove the whole group \"~(:name color-group)\"?")}
      (fn [e d!] (d! :settings/remove-color-group (:id color-group)))))
    (list->
     {:style ui/row}
     (->> (:colors color-group)
          (map-val
           (fn [color] (comp-color-drop (>> states (:id color)) color (:id color-group))))))
    (:ui creation-modal))))

(defcomp
 comp-group-creator
 (states on-toggle)
 (let [cursor (:cursor states), state (or (:data states) {:name ""})]
   (div
    {:style ui/column}
    (div {} (<> "Add group" {:font-family ui/font-fancy, :font-size 20}))
    (=< nil 16)
    (input
     {:style ui/input,
      :autofocus true,
      :placeholder "name",
      :value (:name state),
      :on-input (fn [e d!] (d! cursor (assoc state :name (:value e)))),
      :class-name "group-name"})
    (=< nil 16)
    (div
     {:style ui/row-parted}
     (span nil)
     (button
      {:style ui/button,
       :inner-text "Submit",
       :on-click (fn [e d!]
         (d! :settings/add-color-group (:name state))
         (d! cursor nil)
         (on-toggle d!))})))))

(defcomp
 comp-colors-manager
 (states color-groups)
 (let [group-modal (use-modal
                    (>> states :create-group)
                    {:style {:width 300, :padding "8px 16px"},
                     :render-body (fn [on-toggle]
                       (comp-group-creator (>> states :group-creator) on-toggle))})]
   (div
    {:style (merge ui/expand {:padding 16})}
    (div
     {:style (merge
              ui/row-middle
              {:font-family ui/font-fancy, :font-size 20, :color (hsl 0 0 70)})}
     (<> "Colors"))
    (list->
     {:style {}}
     (->> color-groups
          (map-val
           (fn [color-group] (comp-color-group (>> states (:id color-group)) color-group)))))
    (=< nil 16)
    (button
     {:style (merge ui/row-middle ui/button),
      :on-click (fn [e d!]
        ((:show group-modal) d!)
        (delay! 0.4 (fn [] (focus-element! ".group-name"))))}
     (comp-icon :plus {:font-size 20, :color (hsl 200 100 80), :vertical-align :middle} nil)
     (<> "Add group"))
    (:ui group-modal))))
