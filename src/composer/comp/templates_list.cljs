
(ns composer.comp.templates-list
  (:require [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo.comp.space :refer [=<]]
            [respo.core :refer [defcomp <> list-> span div cursor-> button a]]
            [composer.config :as config]
            ["copy-text-to-clipboard" :as copy!]
            [favored-edn.core :refer [write-edn]]
            [composer.style :as style]
            [feather.core :refer [comp-icon]]
            [respo-alerts.comp.alerts :refer [comp-prompt]]
            [clojure.string :as string]
            [respo.util.list :refer [map-val]]))

(defcomp
 comp-templates-list
 (states templates template-id)
 (let [state (or (:data states) {:name nil})]
   (div
    {:style {:padding "8px 16px", :width 160, :border-right "1px solid #eee"}}
    (div
     {:style (merge ui/row-parted {:border-bottom "1px solid #eee"})}
     (<> "Templates" {:font-family ui/font-fancy})
     (cursor->
      :name
      comp-prompt
      states
      {:trigger (comp-icon :plus {:font-size 15, :color (hsl 0 0 30), :cursor :pointer} nil)}
      (fn [result d! m!] (when-not (string/blank? result) (d! :template/create result)))))
    (if (empty? templates)
      (div {:style {:font-family ui/font-fancy, :color (hsl 0 0 70)}} (<> "No templates"))
      (list->
       {}
       (->> templates
            (map-val
             (fn [template]
               (div
                {:style (merge
                         {:cursor :pointer,
                          :padding "0px 8px",
                          :line-height "32px",
                          :border-bottom "1px solid #eee"}
                         (if (= template-id (:id template))
                           {:background-color (hsl 0 0 90)})),
                 :on-click (fn [e d! m!]
                   (d!
                    :session/focus-to
                    {:template-id (:id template), :path [], :mock-id nil}))}
                (<> (:name template)))))))))))
