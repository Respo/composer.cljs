
(ns composer.comp.templates-list
  (:require [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo.comp.space :refer [=<]]
            [respo.core :refer [defcomp <> >> list-> span div button a]]
            [composer.config :as config]
            ["copy-text-to-clipboard" :as copy!]
            [favored-edn.core :refer [write-edn]]
            [composer.style :as style]
            [feather.core :refer [comp-icon]]
            [respo-alerts.core :refer [comp-prompt]]
            [clojure.string :as string]
            [respo.util.list :refer [map-val]]))

(defcomp
 comp-templates-list
 (states templates template-id active-templates)
 (let [state (or (:data states) {:name nil})]
   (div
    {:style {:padding "8px 16px", :width 160, :border-right "1px solid #eee"}}
    (div
     {:style (merge ui/row-parted {:border-bottom "1px solid #eee"})}
     (<> "Templates" {:font-family ui/font-fancy})
     (comp-prompt
      (>> states :name)
      {:trigger (comp-icon :plus {:font-size 15, :color (hsl 0 0 30), :cursor :pointer} nil)}
      (fn [result d!]
        (when-not (string/blank? result)
          (d! :template/create {:name result, :template-id template-id})))))
    (if (empty? templates)
      (div {:style {:font-family ui/font-fancy, :color (hsl 0 0 70)}} (<> "No templates"))
      (list->
       {}
       (->> templates
            (sort-by (fn [[k template]] (:sort-key template)))
            (map-val
             (fn [template]
               (div
                {:style (merge
                         {:cursor :pointer,
                          :padding "0px 8px",
                          :line-height "32px",
                          :border-bottom "1px solid #eee",
                          :border-color (hsl 0 0 94)}
                         (if (contains? active-templates (:id template))
                           {:border-color (hsl 200 80 80)})
                         (if (= template-id (:id template))
                           {:background-color (hsl 0 0 94)})),
                 :on-click (fn [e d!]
                   (d!
                    :session/focus-to
                    {:template-id (:id template), :path [], :mock-id nil})),
                 :draggable true,
                 :on-dragstart (fn [e d!]
                   (-> e :event .-dataTransfer (.setData "text" (:id template)))),
                 :on-dragover (fn [e d!] (.preventDefault (:event e))),
                 :on-drop (fn [e d!]
                   (let [drag-id (-> e :event .-dataTransfer (.getData "text"))]
                     (d! :template/move-order {:from drag-id, :to (:id template)})))}
                (<> (:name template))
                (<>
                 (:sort-key template)
                 {:color (hsl 0 0 90), :font-size 12, :margin-left 4}))))))))))
