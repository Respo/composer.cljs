
(ns app.comp.type-picker
  (:require [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo.comp.space :refer [=<]]
            [respo.core :refer [defcomp cursor-> list-> <> action-> span div a]]
            [app.config :as config]
            [respo-alerts.comp.alerts :refer [comp-select]]
            [app.style :as style]
            [inflow-popup.comp.popup :refer [comp-popup]]))

(defcomp
 comp-icon-site
 ()
 (a
  {:href "http://repo.respo-mvc.org/feather/",
   :inner-text "Built-in icons",
   :target "_blank",
   :style {:font-family ui/font-fancy}}))

(defcomp
 comp-node-type
 (x on-pick)
 (div
  {:style {:padding "0 12px",
           :cursor :pointer,
           :background-color (hsl 0 0 96),
           :line-height "32px",
           :margin-bottom 8},
   :on-click (fn [e d! m!] (on-pick x d! m!))}
  (<> (:display x))))

(defn find-option [x options]
  (if (empty? options)
    nil
    (let [x0 (first options)] (if (= x (:value x0)) x0 (recur x (rest options))))))

(def node-types
  [{:value :box, :kind :layout, :display "Box"}
   {:value :space, :kind :layout, :display "Space"}
   {:value :button, :kind :element, :display "Button"}
   {:value :link, :kind :element, :display "Link"}
   {:value :icon, :kind :element, :display "Icon"}
   {:value :text, :kind :element, :display "Text"}
   {:value :input, :kind :element, :display "Input"}
   {:value :some, :kind :control, :display "Some"}
   {:value :template, :kind :control, :display "Template"}
   {:value :list, :kind :control, :display "List"}
   {:value :slot, :kind :control, :display "Slot"}
   {:value :inspect, :kind :devtool, :display "Inspect"}
   {:value :popup, :kind :layout, :display "Popup"}
   {:value :element, :kind :element, :display "Element"}])

(defn render-title [title]
  (div {:style {:font-family ui/font-fancy, :color (hsl 0 0 70), :margin-top 20}} (<> title)))

(defcomp
 comp-type-picker
 (states template-id focused-path markup)
 (div
  {:style ui/row-middle}
  (<> "Node Type:" style/field-label)
  (=< 8 nil)
  (cursor->
   :popup
   comp-popup
   states
   {:trigger (<>
              (let [v (find-option (:type markup) node-types)]
                (if (nil? v) "Nothing" (:display v))))}
   (fn [on-toggle]
     (let [on-pick (fn [result d! m!]
                     (d!
                      :template/node-type
                      {:template-id template-id, :path focused-path, :type (:value result)})
                     (on-toggle m!))]
       (div
        {:style (merge ui/row {:width 400})}
        (div
         {:style ui/flex}
         (render-title "Layout")
         (list->
          {}
          (->> node-types
               (filter (fn [x] (= :layout (:kind x))))
               (map (fn [x] [(:value x) (comp-node-type x on-pick)]))))
         (render-title "Elements")
         (list->
          {}
          (->> node-types
               (filter (fn [x] (= :element (:kind x))))
               (map (fn [x] [(:value x) (comp-node-type x on-pick)])))))
        (=< 16 nil)
        (div
         {:style ui/flex}
         (render-title "Controls")
         (list->
          {}
          (->> node-types
               (filter (fn [x] (= :control (:kind x))))
               (map (fn [x] [(:value x) (comp-node-type x on-pick)]))))
         (render-title "DevTool")
         (list->
          {}
          (->> node-types
               (filter (fn [x] (= :devtool (:kind x))))
               (map (fn [x] [(:value x) (comp-node-type x on-pick)])))))))))
  (=< 8 nil)
  (if (= :icon (:type markup)) (comp-icon-site))))
