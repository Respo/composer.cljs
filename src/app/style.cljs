
(ns app.style (:require [hsl.core :refer [hsl]] [respo-ui.core :as ui]))

(def button (-> ui/button (dissoc :background-color) (merge {:line-height "28px"})))

(def field-label {:color (hsl 0 0 60), :font-weight :bold, :font-family ui/font-fancy})

(def link
  {:text-decoration :underline,
   :cursor :pointer,
   :color (hsl 240 80 80),
   :font-family ui/font-fancy})
