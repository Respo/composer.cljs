
(ns composer.comp.editor
  (:require [hsl.core :refer [hsl]]
            [composer.schema :as schema]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp list-> cursor-> <> span div button a pre]]
            [respo.comp.space :refer [=<]]
            [composer.config :as config]
            [respo.comp.inspect :refer [comp-inspect]]
            [respo.util.list :refer [map-val]]
            [respo-alerts.comp.alerts :refer [comp-prompt comp-confirm comp-select]]
            [composer.util :refer [path-with-children]]
            [inflow-popup.comp.popup :refer [comp-popup]]
            [composer.comp.presets :refer [comp-presets]]
            [composer.comp.type-picker :refer [comp-type-picker]]
            [composer.comp.bg-picker :refer [comp-bg-picker comp-font-picker]]
            [composer.comp.dict-editor :refer [comp-dict-editor]]
            [composer.style :as style]
            [bisection-key.core :as bisection]
            [favored-edn.core :refer [write-edn]])
  (:require-macros [clojure.core.strint :refer [<<]]))

(def node-layouts
  [{:value :row, :display "Row", :kind :row}
   {:value :row-middle, :display "Row Middle", :kind :row}
   {:value :row-parted, :display "Row Parted", :kind :row}
   {:value :column, :display "Column", :kind :column}
   {:value :column-parted, :display "Column Parted", :kind :column}
   {:value :center, :display "Center", :kind :center}
   {:value :row-center, :display "Row Center", :kind :center}])

(defcomp
 comp-layout-name
 (layout-name)
 (let [layout (->> node-layouts (filter (fn [item] (= layout-name (:value item)))) first)]
   (if (some? layout)
     (<> (:display layout) {:padding "2px 8px", :background-color (hsl 0 0 94)})
     (<> "Nothing" {:color (hsl 0 0 80), :font-family ui/font-fancy}))))

(defcomp
 comp-layout-picker
 (states template-id path markup)
 (let [on-pick (fn [layout d!]
                 (d!
                  :template/node-layout
                  {:template-id template-id, :path path, :layout layout}))]
   (div
    {:style ui/row-middle}
    (<> "Layout:" style/field-label)
    (=< 8 nil)
    (cursor->
     :popup
     comp-popup
     states
     {:trigger (comp-layout-name (:layout markup))}
     (fn [on-toggle]
       (div
        {:style ui/column}
        (let [render-list (fn [kind]
                            (list->
                             {:style ui/column}
                             (->> node-layouts
                                  (filter (fn [item] (= kind (:kind item))))
                                  (map
                                   (fn [item]
                                     [(:value item)
                                      (div
                                       {:style {:margin "4px 0", :cursor :pointer},
                                        :on-click (fn [e d! m!]
                                          (on-pick (:value item) d!)
                                          (on-toggle m!))}
                                       (comp-layout-name (:value item)))])))))]
          (div
           {:style ui/row}
           (render-list :row)
           (=< 16 nil)
           (render-list :column)
           (=< 16 nil)
           (render-list :center)))
        (div
         {:style ui/row-parted}
         (span nil)
         (a
          {:style ui/link,
           :inner-text "Clear",
           :on-click (fn [e d! m!] (on-pick nil d!) (on-toggle m!))}))))))))

(def style-element
  {:display :inline-block,
   :cursor :pointer,
   :padding "0 8px",
   :margin-bottom 4,
   :background-color (hsl 0 0 88),
   :color :white,
   :border-radius "4px",
   :vertical-align :top,
   :line-height "24px"})

(defcomp
 comp-markup
 (markup path focused-path)
 (div
  {:style (merge
           {:padding "4px 4px 1px 4px",
            :border (<< "1px solid ~(hsl 0 0 88)"),
            :border-bottom nil,
            :border-right nil}
           (if (empty? (:children markup)) {:display :inline-block})
           (if (empty? path) {:border :none, :padding 0}))}
  (div
   {:style (merge
            style-element
            (if (= path focused-path) {:background-color (hsl 200 80 70)})),
    :on-click (fn [e d! m!] (d! :session/focus-to {:path path}))}
   (<> (:type markup)))
  (list->
   {:style (merge
            {:padding-left 8, :margin-left 8}
            (let [amount (count (:children markup))]
              (if (or (<= amount 1)
                      (and (<= amount 5 )
                           (every? (fn [x] (empty? (:children markup))) (:children markup))))
                {:display :inline-block})))}
   (->> (:children markup)
        (sort-by (fn [[k child-markup]] k))
        (map
         (fn [[k child-markup]] [k (comp-markup child-markup (conj path k) focused-path)]))))))

(defn get-mocks [mocks] (->> mocks (map (fn [[k m]] {:value k, :display (:name m)}))))

(defcomp
 comp-mock-picker
 (states template)
 (div
  {:style ui/row-middle}
  (<> "Mock:" style/field-label)
  (=< 8 nil)
  (cursor->
   :mock
   comp-select
   states
   (:mock-pointer template)
   (get-mocks (:mocks template))
   {:text "Select mock"}
   (fn [result d! m!]
     (if (some? result)
       (d! :template/use-mock {:template-id (:id template), :mock-id result}))))))

(defcomp
 comp-operations
 (states template-id focused-path)
 (div
  {:style (merge ui/row {:padding "0 8px"})}
  (div {} (<> "Operations:" style/field-label))
  (div
   {:style (merge ui/flex)}
   (a
    {:style style/link,
     :inner-text "Append",
     :on-click (fn [e d! m!]
       (d! :template/append-markup {:template-id template-id, :path focused-path})
       (d! :router/move-append nil))})
   (a
    {:style style/link,
     :inner-text "After",
     :on-click (fn [e d! m!]
       (d! :template/after-markup {:template-id template-id, :path focused-path})
       (d! :router/move-after nil))})
   (a
    {:style style/link,
     :inner-text "Prepend",
     :on-click (fn [e d! m!]
       (d! :template/prepend-markup {:template-id template-id, :path focused-path})
       (d! :router/move-prepend nil))})
   (a
    {:style style/link,
     :inner-text "Before",
     :on-click (fn [e d! m!]
       (d! :template/before-markup {:template-id template-id, :path focused-path})
       (d! :router/move-before nil))})
   (cursor->
    :remove
    comp-confirm
    states
    {:trigger (a {:style ui/link, :inner-text "Remove"})}
    (fn [e d! m!]
      (d! :template/remove-markup {:template-id template-id, :path focused-path})
      (d! :session/focus-to {:path (vec (butlast focused-path))})))
   (a
    {:style style/link,
     :inner-text "Wrap",
     :on-click (fn [e d! m!]
       (d! :template/wrap-markup {:template-id template-id, :path focused-path})
       (d! :session/focus-to {:path (conj focused-path bisection/mid-id)}))})
   (a
    {:style style/link,
     :inner-text "Spread",
     :on-click (fn [e d! m!]
       (d! :template/spread-markup {:template-id template-id, :path focused-path})
       (d! :router/move-before nil))})
   (a
    {:style style/link,
     :inner-text "Copy",
     :on-click (fn [e d! m!]
       (d! :session/copy-markup {:template-id template-id, :path focused-path}))})
   (a
    {:style style/link,
     :inner-text "Paste",
     :on-click (fn [e d! m!]
       (d! :session/paste-markup {:template-id template-id, :path focused-path}))}))))

(def props-hints
  {:box ["param"],
   :space ["width" "height"],
   :divider ["kind" "color"],
   :text ["value"],
   :some ["value" "kind"],
   :button ["text" "param"],
   :link ["text" "href" "param"],
   :icon ["name" "color" "param"],
   :template ["name" "data"],
   :list ["value"],
   :input ["value" "textarea" "param"],
   :slot ["dom"],
   :inspect ["value"],
   :popup ["visible"],
   :case ["value"],
   :element ["name"],
   :markdown ["text"],
   :image ["src" "mode" "width" "height"]})

(defcomp
 comp-props-hint
 (type)
 (div
  {:style ui/row-middle}
  (<> "Support props: " style/field-label)
  (=< 8 nil)
  (list->
   {}
   (->> (get props-hints type)
        (map
         (fn [name]
           [name
            (span
             {:inner-text name,
              :style {:margin "0 4px",
                      :color (hsl 0 0 60),
                      :font-size 12,
                      :font-family ui/font-code}})]))))))

(def style-mock-data
  {:margin 0,
   :padding "4px 8px",
   :font-size 10,
   :font-family ui/font-code,
   :border "1px solid #ddd",
   :background-color (hsl 0 0 98),
   :white-space :pre,
   :line-height "15px",
   :max-height 120,
   :overflow :auto,
   :border-radius "4px",
   :word-break :break-all,
   :min-height 48})

(defcomp
 comp-editor
 (states template settings focused-path)
 (div
  {:style (merge ui/flex ui/row {:overflow :auto})}
  (div
   {:style (merge ui/flex ui/column {:overflow :auto})}
   (=< nil 4)
   (cursor-> :operations comp-operations states (:id template) (or focused-path []))
   (div
    {:style (merge ui/flex {:overflow :auto, :padding "0 8px"})}
    (comp-markup (:markup template) [] focused-path)
    (when config/dev? (comp-inspect "Markup" (:markup template) {:bottom 0}))))
  (div {:style {:width 1, :background-color "#eee"}})
  (let [child (get-in (:markup template) (interleave (repeat :children) focused-path))
        template-id (:id template)
        mock-id (:mock-pointer template)
        mock-data (if (nil? mock-id) nil (get-in template [:mocks mock-id :data]))]
    (div
     {:style (merge ui/flex {:overflow :auto, :padding 8})}
     (cursor-> :type comp-type-picker states template-id focused-path child)
     (cursor-> :layout comp-layout-picker states template-id focused-path child)
     (cursor->
      :font-color
      comp-font-picker
      states
      template-id
      focused-path
      child
      (:colors settings))
     (cursor->
      :background
      comp-bg-picker
      states
      template-id
      focused-path
      child
      (:colors settings))
     (when config/dev? (comp-inspect "Node" child {:bottom 0}))
     (cursor-> :presets comp-presets states (:presets child) template-id focused-path)
     (=< nil 8)
     (cursor-> :mocks comp-mock-picker states template)
     (pre {:inner-text (write-edn mock-data), :style style-mock-data})
     (comp-props-hint (:type child))
     (cursor->
      :props
      comp-dict-editor
      states
      "Props:"
      (:props child)
      (fn [change d! m!]
        (d!
         :template/node-props
         (merge {:template-id template-id, :path focused-path} change))))
     (cursor->
      :event
      comp-dict-editor
      states
      "Event:"
      (:event child)
      (fn [change d! m!]
        (d!
         :template/node-event
         (merge {:template-id template-id, :path focused-path} change))))
     (cursor->
      :attrs
      comp-dict-editor
      states
      "Attrs:"
      (:attrs child)
      (fn [change d! m!]
        (d!
         :template/node-attrs
         (merge {:template-id template-id, :path focused-path} change))))
     (cursor->
      :style
      comp-dict-editor
      states
      "Style:"
      (:style child)
      (fn [change d! m!]
        (d!
         :template/node-style
         (merge {:template-id template-id, :path focused-path} change))))))))
