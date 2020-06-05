
(ns composer.comp.dict-editor
  (:require [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo.comp.space :refer [=<]]
            [respo.core :refer [defcomp <> >> list-> button input span div a]]
            [clojure.string :as string]
            [composer.config :as config]
            [respo-alerts.core :refer [comp-prompt use-modal]]
            [feather.core :refer [comp-icon comp-i]]
            [composer.style :as style]
            [cumulo-util.core :refer [delay!]]))

(defcomp
 comp-pair-editor
 (states on-change)
 (let [cursor (:cursor states), state (or (:data states) {:key "", :value ""})]
   (div
    {:style {:padding "8 16px 8px 16px"}}
    (div {} (<> "Key/value"))
    (div
     {}
     (input
      {:placeholder "key",
       :class-name "pair-key",
       :style ui/input,
       :value (:key state),
       :on-input (fn [e d!] (d! cursor (assoc state :key (:value e)))),
       :autofocus true,
       :auto-focus true})
     (=< 8 nil)
     (input
      {:placeholder "value",
       :class-name "pair-value",
       :style ui/input,
       :value (:value state),
       :on-input (fn [e d!] (d! cursor (assoc state :value (:value e)))),
       :on-keydown (fn [e d!]
         (if (= 13 (:keycode e)) (do (on-change state d!) (d! cursor nil))))}))
    (=< nil 8)
    (div
     {:style ui/row-parted}
     (span {})
     (button
      {:style ui/button,
       :inner-text "Submit",
       :on-click (fn [e d!] (on-change state d!) (d! cursor nil))})))))

(defcomp
 comp-dict-editor
 (states title dict suggests on-change)
 (let [state (or (:data states) {:draft ""})
       do-focus! (fn []
                   (delay!
                    0.2
                    (fn []
                      (let [target (.querySelector js/document ".pair-key")]
                        (if (some? target)
                          (.focus target)
                          (js/console.warn ".pair-key not found!"))))))
       props-defaults (->> suggests (map (fn [x] [x nil])) (into {}))
       editor-modal (use-modal
                     (>> states :pair)
                     {:style {:width 400},
                      :render-body (fn [on-toggle]
                        (comp-pair-editor
                         (>> states :pair)
                         (fn [result d!]
                           (on-change (merge result {:type :set}) d!)
                           (on-toggle d!))))})]
   (div
    {}
    (div
     {:style ui/row-middle}
     (<> title style/field-label)
     (=< 8 nil)
     (comp-icon
      :plus
      {:font-size 14, :color (hsl 200 80 70), :cursor :pointer}
      (fn [e d!] ((:show editor-modal) d!) (delay! 0.4 (fn [] (do-focus!))))))
    (list->
     {:style {:padding-left 16}}
     (->> (merge props-defaults dict)
          (map
           (fn [[k v]]
             [k
              (div
               {:style (merge ui/row-middle {:line-height "20px"})}
               (<> k {:color (hsl 0 0 70)})
               (=< 8 nil)
               (comp-prompt
                (>> states k)
                {:trigger (if (some? v) (<> v) (<> "nil" {:color (hsl 300 80 30 0.4)})),
                 :text "new value",
                 :initial v}
                (fn [result d!] (on-change {:type :set, :key k, :value result} d!)))
               (=< 8 nil)
               (if (some? v)
                 (span
                  {:style {:cursor :pointer},
                   :on-click (fn [e d!] (on-change {:type :remove, :key k} d!))}
                  (comp-i :delete 14 (hsl 200 80 70)))))]))))
    (:ui editor-modal))))
