
(ns composer.comp.dict-editor
  (:require [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo.comp.space :refer [=<]]
            [respo.core
             :refer
             [defcomp <> action-> list-> cursor-> button input span div a]]
            [clojure.string :as string]
            [composer.config :as config]
            [inflow-popup.comp.popup :refer [comp-popup]]
            [respo-alerts.comp.alerts :refer [comp-prompt]]
            [feather.core :refer [comp-i]]
            [composer.style :as style]
            [cumulo-util.core :refer [delay!]]))

(defcomp
 comp-pair-editor
 (states on-change)
 (let [state (or (:data states) {:key "", :value ""})]
   (div
    {}
    (div {} (<> "Key/value"))
    (div
     {}
     (input
      {:placeholder "key",
       :class-name "pair-key",
       :style ui/input,
       :value (:key state),
       :on-input (fn [e d! m!] (m! (assoc state :key (:value e)))),
       :autofocus true,
       :auto-focus true})
     (=< 8 nil)
     (input
      {:placeholder "value",
       :class-name "pair-value",
       :style ui/input,
       :value (:value state),
       :on-input (fn [e d! m!] (m! (assoc state :value (:value e)))),
       :on-keydown (fn [e d! m!]
         (if (= 13 (:keycode e)) (do (on-change state d! m!) (m! nil))))}))
    (=< nil 8)
    (div
     {:style ui/row-parted}
     (span {})
     (button
      {:style ui/button,
       :inner-text "Submit",
       :on-click (fn [e d! m!] (on-change state d! m!) (m! nil))})))))

(defcomp
 comp-dict-editor
 (states title dict on-change)
 (let [state (or (:data states) {:draft ""})
       do-focus! (fn []
                   (delay!
                    0.2
                    (fn []
                      (let [target (.querySelector js/document ".pair-key")]
                        (if (some? target)
                          (.focus target)
                          (js/console.warn ".pair-key not found!"))))))]
   (div
    {}
    (div
     {:style ui/row-middle}
     (<> title style/field-label)
     (=< 8 nil)
     (cursor->
      :set
      comp-popup
      states
      {:trigger (comp-i :plus 14 (hsl 200 80 70)), :on-popup (fn [e d! m!] (do-focus!))}
      (fn [on-toggle]
        (cursor->
         :pair
         comp-pair-editor
         states
         (fn [result d! m!] (on-change (merge result {:type :set}) d! m!) (on-toggle m!))))))
    (list->
     {:style {:padding-left 16}}
     (->> dict
          (map
           (fn [[k v]]
             [k
              (div
               {:style (merge ui/row-middle {:line-height "20px"})}
               (<> k {:color (hsl 0 0 70)})
               (=< 8 nil)
               (cursor->
                k
                comp-prompt
                states
                {:trigger (<> v), :text "new value", :initial v}
                (fn [result d! m!] (on-change {:type :set, :key k, :value result} d! m!)))
               (=< 8 nil)
               (span
                {:style {:cursor :pointer},
                 :on-click (fn [e d! m!] (on-change {:type :remove, :key k} d! m!))}
                (comp-i :delete 14 (hsl 200 80 70))))])))))))
