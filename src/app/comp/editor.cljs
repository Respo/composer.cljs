
(ns app.comp.editor
  (:require [hsl.core :refer [hsl]]
            [app.schema :as schema]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp list-> cursor-> <> span div button]]
            [respo.comp.space :refer [=<]]
            [app.config :as config]
            [respo.comp.inspect :refer [comp-inspect]]
            [respo.util.list :refer [map-val]]
            [respo-alerts.comp.alerts :refer [comp-prompt comp-confirm comp-select]]
            [app.util :refer [path-with-children]]
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
                       :background-color (or (get-in markup [:style :background-color])
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
                     :template/node-style
                     {:template-id template-id,
                      :path path,
                      :property :background-color,
                      :value color}))})])))))))))

(def node-layouts
  [{:value :row, :display "Row"}
   {:value :column, :display "Column"}
   {:value :center, :display "Center"}
   {:value :row-center, :display "Row Center"}
   {:value :row-middle, :display "Row Middle"}
   {:value :row-parted, :display "Row Parted"}
   {:value :column-parted, :display "Column Parted"}])

(defcomp
 comp-layout-picker
 (states template-id path markup)
 (div
  {:style ui/row-middle}
  (<> "Layout:")
  (=< 8 nil)
  (cursor->
   :picker
   comp-select
   states
   (:layout markup)
   node-layouts
   {}
   (fn [result d! m!]
     (if (some? result)
       (d! :template/node-layout {:template-id template-id, :path path, :layout result}))))))

(defcomp
 comp-markup
 (markup path focused-path)
 (div
  {:class-name "no-shadows",
   :style (merge
           {:padding-left 8}
           (if (empty? (:children markup)) {:border-left "1px solid #eee"}))}
  (div
   {:style (merge
            {:display :inline-block,
             :cursor :pointer,
             :padding "0 8px",
             :margin-bottom 8,
             :background-color (hsl 0 0 88),
             :color :white,
             :border-radius "8px",
             :vertical-align :top}
            (if (= path focused-path) {:background-color (hsl 200 80 70)})),
    :on-click (fn [e d! m!] (d! :router/set-focused-path path))}
   (<> (:type markup))
   (=< 8 nil)
   (<> (:id markup)))
  (list->
   {:style (merge
            {:padding-left 8, :margin-left 8}
            (let [amount (count (:children markup))]
              (if (or (<= amount 1)
                      (and (<= amount 5 )
                           (every? (fn [x] (empty? (:children markup))) (:children markup))))
                {:display :inline-block})))}
   (->> (:children markup)
        (sort-by (fn [[k child-markup]] k))
        (map-val
         (fn [child-markup]
           (comp-markup child-markup (conj path (:id child-markup)) focused-path)))))))

(defcomp
 comp-operations
 (states template-id focused-path)
 (div
  {:style (merge ui/column {:width 300})}
  (div {} (<> "Operations"))
  (div
   {:style {}}
   (button
    {:style ui/button,
     :inner-text "Append",
     :on-click (fn [e d! m!]
       (d! :template/append-markup {:template-id template-id, :path focused-path})
       (d! :router/move-append nil))})
   (=< 8 nil)
   (button
    {:style ui/button,
     :inner-text "After",
     :on-click (fn [e d! m!]
       (d! :template/after-markup {:template-id template-id, :path focused-path})
       (d! :router/move-after nil))})
   (=< 8 nil)
   (button
    {:style ui/button,
     :inner-text "Prepend",
     :on-click (fn [e d! m!]
       (d! :template/prepend-markup {:template-id template-id, :path focused-path})
       (d! :router/move-prepend nil))})
   (button
    {:style ui/button,
     :inner-text "Before",
     :on-click (fn [e d! m!]
       (d! :template/before-markup {:template-id template-id, :path focused-path})
       (d! :router/move-before nil))})
   (=< 8 nil)
   (cursor->
    :remove
    comp-confirm
    states
    {:trigger (button {:style ui/button, :inner-text "Remove"})}
    (fn [e d! m!]
      (d! :template/remove-markup {:template-id template-id, :path focused-path})
      (d! :router/set-focused-path (vec (butlast focused-path))))))
  (=< nil 16)
  (div {} (<> "Changes"))
  (div
   {}
   (button
    {:style ui/button,
     :inner-text "Move",
     :on-click (fn [e d! m!] (d! :router/move-prepend nil))}))))

(def node-types
  [{:value :box, :display "Box"}
   {:value :text, :display "Text"}
   {:value :space, :display "Space"}
   {:value :icon, :display "Icon"}
   {:value :if, :display "if expression"}
   {:value :value, :display "value expression"}])

(defcomp
 comp-type-picker
 (states template-id focused-path markup)
 (div
  {:style ui/row-middle}
  (<> "Node Type:")
  (=< 8 nil)
  (cursor->
   :type
   comp-select
   states
   (:type markup)
   node-types
   {}
   (fn [result d! m!]
     (d! :template/node-type {:template-id template-id, :path focused-path, :type result})))))

(defcomp
 comp-editor
 (states template focused-path)
 (div
  {:style (merge ui/flex ui/row)}
  (div
   {:style (merge ui/flex {:overflow :auto, :padding 8})}
   (comp-markup (:markup template) [] focused-path)
   (comp-inspect "Markup" (:markup template) {}))
  (let [child (get-in (:markup template) (interleave (repeat :children) focused-path))]
    (div
     {:style {}}
     (cursor-> :type comp-type-picker states (:id template) focused-path child)
     (cursor-> :operations comp-operations states (:id template) (or focused-path []))
     (cursor-> :layout comp-layout-picker states (:id template) focused-path child)
     (cursor-> :background comp-bg-picker states (:id template) focused-path child)
     (comp-inspect "Node" child {})))))
