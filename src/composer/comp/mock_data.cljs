
(ns composer.comp.mock-data
  (:require [hsl.core :refer [hsl]]
            [composer.schema :as schema]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp list-> cursor-> <> span div button textarea pre a]]
            [respo.comp.space :refer [=<]]
            [composer.config :as config]
            [respo.util.list :refer [map-val]]
            [feather.core :refer [comp-i]]
            [respo-alerts.comp.alerts :refer [comp-prompt comp-confirm]]
            [clojure.string :as string]
            [favored-edn.core :refer [write-edn]]
            [cljs.reader :refer [read-string]]
            [inflow-popup.comp.popup :refer [comp-popup]]))

(def style-code {:font-family ui/font-code, :font-size 12, :line-height "18px"})

(defcomp
 comp-data-editor
 (states data on-submit)
 (let [state (or (:data states) {:draft (write-edn data), :error nil})]
   (div
    {:style ui/column}
    (textarea
     {:style (merge ui/textarea style-code {:height 240, :width 600}),
      :placeholder "EDN data",
      :value (:draft state),
      :on-input (fn [e d! m!] (m! (assoc state :draft (:value e))))})
    (div
     {:style (merge ui/row-parted {:margin-top 8})}
     (if (some? (:error state))
       (div
        {:style {:color :red, :max-width 360, :line-height "20px"}}
        (<> (:error state) {:color :red}))
       (span nil))
     (button
      {:inner-text "Submit",
       :style ui/button,
       :on-click (fn [e d! m!]
         (try
          (do (on-submit (read-string (:draft state)) d! m!) (m! nil))
          (catch js/Error err (.error js/console err) (m! (assoc state :error err)))))})))))

(defcomp
 comp-mock-editor
 (states template-id mock)
 (let [base-op-data {:template-id template-id, :mock-id (:id mock)}]
   (div
    {:style (merge ui/flex ui/column)}
    (div
     {:style {:padding "4px 8px"}}
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
           {:template-id template-id, :mock-id (:id mock), :text result}))))
     (=< 40 nil)
     (a
      {:style ui/link,
       :inner-text "Use it",
       :on-click (fn [e d! m!]
         (d! :template/use-mock {:template-id template-id, :mock-id (:id mock)}))})
     (=< 8 nil)
     (cursor->
      :fork
      comp-prompt
      states
      {:trigger (a {:style ui/link, :inner-text "Fork"}), :text "Fork with new name:"}
      (fn [result d! m!]
        (when-not (string/blank? result)
          (d!
           :template/fork-mock
           {:template-id template-id, :mock-id (:id mock), :name result}))))
     (=< 8 nil)
     (cursor->
      :remove
      comp-confirm
      states
      {:trigger (a {:style ui/link, :inner-text "Remove"}),
       :text "Sure to remove mock data?"}
      (fn [e d! m!]
        (d! :template/remove-mock {:template-id template-id, :mock-id (:id mock)}))))
    (div
     {:style {:padding "0px 8px", :max-height 320, :overflow :auto}}
     (pre
      {:style (merge
               style-code
               {:width "100%",
                :margin 0,
                :background-color (hsl 0 0 96),
                :padding "4px 8px"}),
       :disabled true,
       :inner-text (write-edn (:data mock))}))
    (div
     {:style {:padding 8}}
     (cursor->
      :edit
      comp-popup
      states
      {:trigger (a {:style ui/link, :inner-text "Edit data"}), :style nil}
      (fn [on-toggle]
        (cursor->
         :edit-data
         comp-data-editor
         states
         (:data mock)
         (fn [data d! m!]
           (println "edit data" data)
           (d! :template/update-mock (merge base-op-data {:data data}))
           (on-toggle m!)))))))))

(defcomp
 comp-mock-data
 (states template-id focused-id used-mock mocks)
 (div
  {:class-name "", :style (merge ui/flex ui/row)}
  (div
   {:style {:width 160, :border-right "1px solid #eee"}}
   (div
    {:style (merge
             ui/row-parted
             {:padding "4px 8px",
              :border-bottom "1px solid #eee",
              :font-family ui/font-fancy})}
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
     (div
      {:style {:font-family ui/font-fancy, :color (hsl 0 0 70), :padding 8}}
      (<> "No data"))
     (list->
      {}
      (->> mocks
           (map-val
            (fn [mock]
              (div
               {:style (merge
                        ui/row-middle
                        {:cursor :pointer,
                         :padding "0 8px",
                         :border-bottom "1px solid #eee",
                         :line-height "32px",
                         :height "32px"}
                        (if (= focused-id (:id mock)) {:background-color (hsl 0 0 94)})),
                :on-click (fn [e d! m!] (d! :session/focus-to {:mock-id (:id mock)}))}
               (<> (or (:name mock) "-"))
               (=< 8 nil)
               (if (= used-mock (:id mock)) (comp-i :star 14 (hsl 0 80 70))))))))))
  (if-let [mock (get mocks focused-id)]
    (cursor-> :editor comp-mock-editor states template-id mock)
    (div
     {:style (merge
              ui/flex
              {:font-size 16, :font-family ui/font-fancy, :padding 8, :color (hsl 0 0 70)})}
     (<> "Nothing")))))
