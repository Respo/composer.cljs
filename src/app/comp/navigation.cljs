
(ns app.comp.navigation
  (:require [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo.comp.space :refer [=<]]
            [respo.core :refer [defcomp <> action-> span div button]]
            [app.config :as config]
            ["copy-text-to-clipboard" :as copy!]
            [app.util :refer [neaten-templates]]
            [favored-edn.core :refer [write-edn]]))

(defcomp
 comp-navigation
 (logged-in? count-members templates)
 (div
  {:style (merge
           ui/row-center
           {:height 48,
            :justify-content :space-between,
            :padding "0 16px",
            :font-size 16,
            :border-bottom (str "1px solid " (hsl 0 0 0 0.1)),
            :font-family ui/font-fancy})}
  (div
   {:style ui/row-middle}
   (div
    {:on-click (action-> :router/change {:name :home}), :style {:cursor :pointer}}
    (<> (:title config/site) nil))
   (=< 16 nil)
   (div
    {:on-click (action-> :router/change {:name :preview}), :style {:cursor :pointer}}
    (<> "Preview" nil)))
  (div
   {:style ui/row}
   (button
    {:style ui/button,
     :inner-text "Copy",
     :on-click (fn [e d! m!] (copy! (write-edn (neaten-templates templates))))})
   (=< 8 nil)
   (button
    {:style ui/button,
     :inner-text "Save",
     :on-click (fn [e d! m!] (d! :effect/persist nil))})
   (=< 12 nil)
   (div
    {:style {:cursor "pointer"}, :on-click (action-> :router/change {:name :profile})}
    (<> (if logged-in? "Me" "Guest"))
    (=< 8 nil)
    (<> count-members)))))
