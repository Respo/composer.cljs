
(ns composer.comp.workspace
  (:require [hsl.core :refer [hsl]]
            [composer.schema :as schema]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp cursor-> list-> <> span div button]]
            [respo.comp.space :refer [=<]]
            [composer.config :as config]
            [feather.core :refer [comp-icon]]
            [respo-alerts.comp.alerts :refer [comp-prompt]]
            [clojure.string :as string]
            [respo.util.list :refer [map-val]]
            [composer.comp.editor :refer [comp-editor]]
            [composer.comp.tabs :refer [comp-tabs]]
            [composer.comp.mock-data :refer [comp-mock-data]]
            [composer.comp.template-settings :refer [comp-template-settings]]
            [composer.comp.templates-list :refer [comp-templates-list]]))

(def template-tabs
  [{:value :editor, :display "Editor"}
   {:value :mocks, :display "Mocks"}
   {:value :settings, :display "Settings"}])

(defcomp
 comp-workspace
 (states templates settings focus-to focuses)
 (let [tab (:tab focus-to)
       template-id (:template-id focus-to)
       template (get templates template-id)
       focused-path (:path focus-to)
       active-templates (->> focuses
                             (map (fn [[k info]] (get-in info [:focus :template-id])))
                             (set))
       focus-in-template (->> focuses
                              (filter
                               (fn [[k info]]
                                 (= template-id (get-in info [:focus :template-id])))))
       active-names (->> focus-in-template
                         (map (fn [[k info]] (get-in info [:user :name])))
                         (string/join ", "))
       active-paths (->> focus-in-template
                         (map (fn [[k info]] (get-in info [:focus :path])))
                         (set))]
   (div
    {:style (merge ui/flex ui/row {:overflow :auto})}
    (cursor-> :list comp-templates-list states templates template-id active-templates)
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
        {:style (merge
                 ui/row-parted
                 {:border-bottom "1px solid #ddd", :padding-top "8px", :padding-right 8})}
        (comp-tabs
         template-tabs
         tab
         (fn [selected d! m!] (d! :session/focus-to {:tab (:value selected)})))
        (<> active-names {:font-family ui/font-fancy, :font-size 12, :color (hsl 0 0 70)}))
       (case (or tab :editor)
         :editor
           (cursor-> :editor comp-editor states template settings focused-path active-paths)
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
