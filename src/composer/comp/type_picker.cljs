
(ns composer.comp.type-picker
  (:require [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo.comp.space :refer [=<]]
            [respo.core :refer [defcomp >> list-> <> span div a]]
            [composer.config :as config]
            [respo-alerts.core :refer [comp-select use-modal]]
            [composer.style :as style]
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
   :on-click (fn [e d!] (on-pick x d!))}
  (<> (let [y (name x)] (str (string/upper-case (first y)) (subs y 1))))))

(defn render-title [title]
  (div {:style {:font-family ui/font-fancy, :color (hsl 0 0 70), :margin-top 20}} (<> title)))

(defcomp
 comp-type-picker
 (states template-id focused-path markup)
 (let [cursor (:cursor states)
       type-modal (use-modal
                   (>> states :type)
                   {:style {:padding "8px 16px"},
                    :render-body (fn [on-close]
                      (let [on-pick (fn [result d!]
                                      (d!
                                       :template/node-type
                                       {:template-id template-id,
                                        :path focused-path,
                                        :type result})
                                      (on-close d!))
                            render-list (fn [types]
                                          (list->
                                           {}
                                           (->> types
                                                (map
                                                 (fn [x] [x (comp-node-type x on-pick)])))))]
                        (div
                         {:style (merge ui/row)}
                         (div
                          {:style ui/flex}
                          (render-title "Elements")
                          (render-list (:element node-types)))
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
                          (render-list (:advanced node-types))))))})]
   (div
    {:style ui/row-middle}
    (<> "Node Type:" style/field-label)
    (=< 8 nil)
    (span
     {:inner-text (name (:type markup)),
      :style {:cursor :pointer},
      :on-click (fn [e d!] ((:show type-modal) d!))})
    (=< 8 nil)
    (if (= :icon (:type markup)) (comp-icon-site))
    (:ui type-modal))))

(defn find-option [x options]
  (if (empty? options)
    nil
    (let [x0 (first options)] (if (= x (:value x0)) x0 (recur x (rest options))))))
