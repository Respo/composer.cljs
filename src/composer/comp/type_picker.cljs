
(ns composer.comp.type-picker
  (:require [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo.comp.space :refer [=<]]
            [respo.core :refer [defcomp >> list-> <> action-> span div a]]
            [composer.config :as config]
            [respo-alerts.core :refer [comp-select]]
            [composer.style :as style]
            [inflow-popup.comp.popup :refer [comp-popup]]
            [clojure.string :as string]
            [composer.schema :refer [node-types]]))

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
  (<> (let [y (name x)] (str (string/upper-case (first y)) (subs y 1))))))

(defn render-title [title]
  (div {:style {:font-family ui/font-fancy, :color (hsl 0 0 70), :margin-top 20}} (<> title)))

(defcomp
 comp-type-picker
 (states template-id focused-path markup)
 (div
  {:style ui/row-middle}
  (<> "Node Type:" style/field-label)
  (=< 8 nil)
  (comp-popup
   (>> states :popup)
   {:trigger (<> (name (:type markup)))}
   (fn [on-toggle]
     (let [on-pick (fn [result d! m!]
                     (d!
                      :template/node-type
                      {:template-id template-id, :path focused-path, :type result})
                     (on-toggle m!))
           render-list (fn [types]
                         (list-> {} (->> types (map (fn [x] [x (comp-node-type x on-pick)])))))]
       (div
        {:style (merge ui/row {:width 480})}
        (div {:style ui/flex} (render-title "Elements") (render-list (:element node-types)))
        (=< 16 nil)
        (div
         {:style ui/flex}
         (render-title "Layout")
         (render-list (:layout node-types))
         (render-title "DevTool")
         (render-list (:devtool node-types)))
        (=< 16 nil)
        (div
         {:style ui/flex}
         (render-title "Controls")
         (render-list (:control node-types))
         (render-title "Advanced")
         (render-list (:advanced node-types)))))))
  (=< 8 nil)
  (if (= :icon (:type markup)) (comp-icon-site))))

(defn find-option [x options]
  (if (empty? options)
    nil
    (let [x0 (first options)] (if (= x (:value x0)) x0 (recur x (rest options))))))
