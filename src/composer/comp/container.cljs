
(ns composer.comp.container
  (:require [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp <> div span action-> cursor-> button]]
            [respo.comp.inspect :refer [comp-inspect]]
            [respo.comp.space :refer [=<]]
            [composer.comp.navigation :refer [comp-navigation]]
            [composer.comp.profile :refer [comp-profile]]
            [composer.comp.login :refer [comp-login]]
            [respo-message.comp.messages :refer [comp-messages]]
            [cumulo-reel.comp.reel :refer [comp-reel]]
            [composer.config :refer [dev?]]
            [composer.schema :as schema]
            [composer.config :as config]
            [composer.comp.workspace :refer [comp-workspace]]
            [composer.comp.preview :refer [comp-preview]]
            [composer.comp.overflow :refer [comp-overview]]
            [composer.comp.settings :refer [comp-settings]]))

(defcomp
 comp-offline
 ()
 (div
  {:style (merge
           ui/global
           ui/fullscreen
           ui/column-dispersive
           {:background-color (:theme config/site)})}
  (div {:style {:height 0}})
  (div
   {:style {:background-image (str "url(" (:icon config/site) ")"),
            :width 128,
            :height 128,
            :background-size :contain}})
  (div
   {:style {:cursor :pointer, :line-height "32px"},
    :on-click (action-> :effect/connect nil)}
   (<> "No connection..." {:font-family ui/font-fancy, :font-size 24}))))

(defcomp
 comp-status-color
 (color)
 (div
  {:style (let [size 24]
     {:width size,
      :height size,
      :position :absolute,
      :bottom 60,
      :left 8,
      :background-color color,
      :border-radius "50%",
      :opacity 0.6,
      :pointer-events :none})}))

(defcomp
 comp-container
 (states store)
 (let [state (:data states)
       session (:session store)
       router (:router store)
       router-data (:data router)
       templates (:templates store)
       focus-to (:focus-to session)]
   (if (nil? store)
     (comp-offline)
     (div
      {:style (merge ui/global ui/fullscreen ui/column)}
      (comp-navigation
       (:logged-in? store)
       (:count store)
       router
       (:templates-modified? store))
      (if (:logged-in? store)
        (case (:name router)
          :home (cursor-> :workspace comp-workspace states templates focus-to)
          :preview
            (cursor-> :preview comp-preview states templates focus-to (:shadows? session))
          :overview (comp-overview templates)
          :profile (comp-profile (:user store) (:data router))
          :settings (cursor-> :settings comp-settings states (:settings store))
          (<> router))
        (comp-login states))
      (comp-status-color (:color store))
      (when dev?
        (comp-inspect "Settings" (:settings store) {:bottom 0, :left 0, :max-width "100%"}))
      (comp-messages
       (get-in store [:session :messages])
       {}
       (fn [info d! m!] (d! :session/remove-message info)))
      (when dev? (comp-reel (:reel-length store) {:bottom 60}))))))

(def style-body {:padding "8px 16px"})
