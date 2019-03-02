
(ns app.comp.navigation
  (:require [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo.comp.space :refer [=<]]
            [respo.core :refer [defcomp <> action-> span div button a]]
            [app.config :as config]
            ["copy-text-to-clipboard" :as copy!]
            [favored-edn.core :refer [write-edn]]
            [app.style :as style]))

(defcomp
 comp-navigation
 (logged-in? count-members)
 (div
  {:style (merge
           ui/row-center
           {:height 40,
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
   (a
    {:style style/link,
     :inner-text "Save",
     :on-click (fn [e d! m!] (d! :effect/persist nil))})
   (=< 12 nil)
   (div
    {:style {:cursor "pointer"}, :on-click (action-> :router/change {:name :profile})}
    (<> (if logged-in? "Me" "Guest"))
    (=< 8 nil)
    (<> count-members)))))
