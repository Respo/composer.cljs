
(ns composer.comp.navigation
  (:require [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo.comp.space :refer [=<]]
            [respo.core :refer [defcomp <> span div button a]]
            [composer.config :as config]
            ["copy-text-to-clipboard" :as copy!]
            [favored-edn.core :refer [write-edn]]
            [composer.style :as style]))

(defcomp
 comp-entry
 (title router-name router router-data)
 (div
  {:on-click (fn [e d!] (d! :router/change {:name router-name, :data router-data})),
   :style (merge
           {:cursor :pointer, :color (hsl 0 0 70)}
           (if (= router-name (:name router)) {:color :black}))}
  (<> title nil)))

(defcomp
 comp-navigation
 (logged-in? count-members router modified?)
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
   (comp-entry (:title config/site) :home router nil)
   (=< 16 nil)
   (comp-entry "Preview" :preview router nil)
   (=< 16 nil)
   (comp-entry "Overview" :overview router nil)
   (=< 16 nil)
   (comp-entry "Settings" :settings router {:tab :colors}))
  (div
   {:style ui/row-middle}
   (a
    {:style (merge style/link (if modified? {:color (hsl 200 80 50)} {:color (hsl 0 0 86)})),
     :inner-text "Save",
     :on-click (fn [e d!] (d! :effect/persist nil))})
   (=< 12 nil)
   (div
    {:style {:cursor :pointer}, :on-click (fn [e d!] (d! :router/change {:name :profile}))}
    (<> (if logged-in? "Me" "Guest"))
    (=< 4 nil)
    (<> count-members)))))
