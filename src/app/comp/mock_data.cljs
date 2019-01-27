
(ns app.comp.mock-data
  (:require [hsl.core :refer [hsl]]
            [app.schema :as schema]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp list-> cursor-> <> span div button textarea pre]]
            [respo.comp.space :refer [=<]]
            [app.config :as config]
            [respo.util.list :refer [map-val]]
            [feather.core :refer [comp-i]]
            [respo-alerts.comp.alerts :refer [comp-prompt]]
            [clojure.string :as string]
            [favored-edn.core :refer [write-edn]]
            [cljs.reader :refer [read-string]]))

(defcomp
 comp-mock-editor
 (states template-id mock)
 (div
  {:style (merge ui/flex ui/column)}
  (div
   {:style {:padding 8}}
   (cursor->
    :rename
    comp-prompt
    states
    {:trigger (div
               {:style ui/row-middle}
               (<> (:name mock))
               (=< 8 nil)
               (comp-i :edit 14 (hsl 200 80 60))),
     :initial (:name mock)}
    (fn [result d! m!]
      (when-not (string/blank? result)
        (d!
         :template/rename-mock
         {:template-id template-id, :mock-id (:id mock), :text result})))))
  (div
   {:style ui/flex}
   (cursor->
    :data
    comp-prompt
    states
    {:trigger (pre
               {:style (merge ui/textarea {:font-family ui/font-code, :width "100%"}),
                :disabled true,
                :inner-text (write-edn (:data mock))}),
     :style {:width "100%", :padding "0 8px"},
     :multiline? true,
     :input-style {:font-family ui/font-code},
     :initial (write-edn (:data mock))}
    (fn [result d! m!]
      (try
       (do
        (let [data (read-string result)]
          (d!
           :template/update-mock
           {:template-id template-id, :mock-id (:id mock), :data data})))
       (catch js/Error err (.error js/console err) (js/alert "Invalid data"))))))
  (div
   {:style ui/row-parted}
   (span {})
   (div
    {:style ui/row-middle}
    (button
     {:style ui/button,
      :inner-text "Use this",
      :on-click (fn [e d! m!]
        (d! :template/use-mock {:template-id template-id, :mock-id (:id mock)}))})
    (=< 8 nil)
    (button
     {:style ui/button,
      :inner-text "Remove",
      :on-click (fn [e d! m!]
        (d! :template/remove-mock {:template-id template-id, :mock-id (:id mock)}))})))))

(defcomp
 comp-mock-data
 (states template-id focused-id used-mock mocks)
 (div
  {:class-name "", :style (merge ui/flex ui/row)}
  (div
   {:style {:width 200}}
   (div
    {:style ui/row-parted}
    (<> "Mock data")
    (cursor->
     :create
     comp-prompt
     states
     {:trigger (comp-i :plus 14 (hsl 0 0 70))}
     (fn [result d! m!]
       (when-not (string/blank? result)
         (d! :template/create-mock {:template-id template-id, :text result})))))
   (if (empty? mocks)
     (div {:style {:font-family ui/font-fancy, :color (hsl 0 0 70)}} (<> "No data"))
     (list->
      {}
      (->> mocks
           (map-val
            (fn [mock]
              (div
               {:style (merge
                        ui/row-middle
                        {:cursor :pointer, :padding "0 8px"}
                        (if (= focused-id (:id mock)) {:background-color (hsl 0 0 94)})),
                :on-click (fn [e d! m!] (d! :router/set-focused-mock (:id mock)))}
               (<> (:name mock))
               (=< 8 nil)
               (if (= used-mock (:id mock)) (comp-i :star 14 (hsl 0 80 70))))))))))
  (if-let [mock (get mocks focused-id)]
    (cursor-> :editor comp-mock-editor states template-id mock)
    (div
     {:style (merge ui/flex {:font-size 18, :font-family ui/font-fancy, :padding 8})}
     (<> "Nothing")))))
