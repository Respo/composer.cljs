
(ns app.comp.workspace
  (:require [hsl.core :refer [hsl]]
            [app.schema :as schema]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp cursor-> list-> <> span div button]]
            [respo.comp.space :refer [=<]]
            [app.config :as config]
            [feather.core :refer [comp-icon]]
            [respo-alerts.comp.alerts :refer [comp-prompt]]
            [clojure.string :as string]
            [respo.util.list :refer [map-val]]
            [app.comp.editor :refer [comp-editor]]
            [app.comp.tabs :refer [comp-tabs]]
            [app.comp.mock-data :refer [comp-mock-data]]
            [app.comp.template-settings :refer [comp-template-settings]]
            [app.comp.templates-list :refer [comp-templates-list]]))

(def template-tabs
  [{:value :editor, :display "Editor"}
   {:value :mocks, :display "Mocks"}
   {:value :settings, :display "Settings"}])

(defcomp
 comp-workspace
 (states templates focus-to)
 (let [tab (:tab focus-to)
       template-id (:template-id focus-to)
       template (get templates template-id)
       focused-path (:path focus-to)]
   (div
    {:style (merge ui/flex ui/row {:overflow :auto})}
    (cursor-> :list comp-templates-list states templates template-id)
    (if (nil? template)
      (div
       {:style (merge
                ui/flex
                {:padding "16px",
                 :font-family ui/font-fancy,
                 :font-size 18,
                 :color (hsl 0 0 70)})}
       (<> "No template selected."))
      (div
       {:style (merge ui/flex ui/column {:overflow :auto})}
       (div
        {:style {:border-bottom "1px solid #ddd", :padding-top "8px"}}
        (comp-tabs
         template-tabs
         tab
         (fn [selected d! m!] (d! :session/focus-to {:tab (:value selected)}))))
       (case (or tab :editor)
         :editor (cursor-> :editor comp-editor states template focused-path)
         :mocks
           (cursor->
            :mock
            comp-mock-data
            states
            (:id template)
            (:mock-id focus-to)
            (:mock-pointer template)
            (:mocks template))
         :settings (cursor-> :settings comp-template-settings states template)
         (<> (str "Unknown tab:" (pr-str tab)))))))))
