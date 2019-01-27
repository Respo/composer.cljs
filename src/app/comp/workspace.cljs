
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
            [app.comp.template-settings :refer [comp-template-settings]]))

(defcomp
 comp-templates-list
 (states templates pointer tab)
 (let [state (or (:data states) {:name nil})]
   (div
    {:style {:padding "8px 16px"}}
    (div
     {:style ui/row-parted}
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
                         {:cursor :pointer, :padding "0px 8px", :line-height "40px"}
                         (if (= pointer (:id template)) {:background-color (hsl 0 0 90)})),
                 :on-click (fn [e d! m!] (d! :router/set-pointer (:id template)))}
                (<> (:name template)))))))))))

(def template-tabs
  [{:value :editor, :display "Editor"}
   {:value :mocks, :display "Mocks"}
   {:value :settings, :display "Settings"}])

(defcomp
 comp-workspace
 (states templates pointer-data)
 (let [pointer (:pointer pointer-data)
       tab (:tab pointer-data)
       template (get templates pointer)
       focused-path (:focused-path pointer-data)]
   (div
    {:style (merge ui/flex ui/row)}
    (div
     {:style (merge {:width 320})}
     (cursor-> :list comp-templates-list states templates pointer tab))
    (if (nil? template)
      (div
       {:style (merge
                ui/flex
                {:padding "16px",
                 :font-family ui/font-fancy,
                 :font-size 24,
                 :color (hsl 0 0 60)})}
       (<> "No template selected."))
      (div
       {:style (merge ui/flex ui/column)}
       (case tab
         :editor (comp-editor template focused-path)
         nil (comp-editor template focused-path)
         :mocks
           (cursor->
            :mock
            comp-mock-data
            states
            (:id template)
            (:focused-mock pointer-data)
            (:mock-pointer template)
            (:mocks template))
         :settings (cursor-> :settings comp-template-settings states template)
         (<> (str "Unknown tab:" (pr-str tab))))
       (div
        {}
        (comp-tabs
         template-tabs
         tab
         (fn [selected d! m!] (d! :router/set-tab (:value selected))))))))))
