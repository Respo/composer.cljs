
(ns app.comp.editor
  (:require [hsl.core :refer [hsl]]
            [app.schema :as schema]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp list-> cursor-> <> span div button a pre]]
            [respo.comp.space :refer [=<]]
            [app.config :as config]
            [respo.comp.inspect :refer [comp-inspect]]
            [respo.util.list :refer [map-val]]
            [respo-alerts.comp.alerts :refer [comp-prompt comp-confirm comp-select]]
            [app.util :refer [path-with-children]]
            [inflow-popup.comp.popup :refer [comp-popup]]
            [app.comp.presets :refer [comp-presets]]
            [app.comp.type-picker :refer [comp-type-picker]]
            [app.comp.bg-picker :refer [comp-bg-picker]]
            [app.comp.dict-editor :refer [comp-dict-editor]]
            [app.style :as style]
            [bisection-key.core :as bisection])
  (:require-macros [clojure.core.strint :refer [<<]]))

(def node-layouts
  [{:value :row, :display "Row"}
   {:value :row-middle, :display "Row Middle"}
   {:value :row-parted, :display "Row Parted"}
   {:value :row-center, :display "Row Center"}
   {:value :column, :display "Column"}
   {:value :column-parted, :display "Column Parted"}
   {:value :center, :display "Center"}])

(defcomp
 comp-layout-picker
 (states template-id path markup)
 (div
  {:style ui/row-middle}
  (<> "Layout:" style/field-label)
  (=< 8 nil)
  (cursor->
   :picker
   comp-select
   states
   (:layout markup)
   node-layouts
   {}
   (fn [result d! m!]
     (d! :template/node-layout {:template-id template-id, :path path, :layout result})))))

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
           (if (empty? (:children markup)) {:display :inline-block}))}
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
 (<> (pr-str (:mock-pointer template)))
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
     (d! :template/use-mock {:template-id (:id template), :mock-id result})))))

(defcomp
 comp-operations
 (states template-id focused-path)
 (div
  {:style (merge ui/row {})}
  (div {} (<> "Operations:" style/field-label))
  (div
   {:style (merge ui/flex)}
   (a
    {:style style/link,
     :inner-text "Append",
     :on-click (fn [e d! m!]
       (d! :template/append-markup {:template-id template-id, :path focused-path})
       (d! :router/move-append nil))})
   (=< 8 nil)
   (a
    {:style style/link,
     :inner-text "After",
     :on-click (fn [e d! m!]
       (d! :template/after-markup {:template-id template-id, :path focused-path})
       (d! :router/move-after nil))})
   (=< 8 nil)
   (a
    {:style style/link,
     :inner-text "Prepend",
     :on-click (fn [e d! m!]
       (d! :template/prepend-markup {:template-id template-id, :path focused-path})
       (d! :router/move-prepend nil))})
   (=< 8 nil)
   (a
    {:style style/link,
     :inner-text "Before",
     :on-click (fn [e d! m!]
       (d! :template/before-markup {:template-id template-id, :path focused-path})
       (d! :router/move-before nil))})
   (=< 8 nil)
   (cursor->
    :remove
    comp-confirm
    states
    {:trigger (a {:style ui/link, :inner-text "Remove"})}
    (fn [e d! m!]
      (d! :template/remove-markup {:template-id template-id, :path focused-path})
      (d! :session/focus-to {:path (vec (butlast focused-path))})))
   (=< 8 nil)
   (a
    {:style style/link,
     :inner-text "Wrap",
     :on-click (fn [e d! m!]
       (d! :template/wrap-markup {:template-id template-id, :path focused-path})
       (d! :session/focus-to {:path (conj focused-path bisection/mid-id)}))})
   (=< 8 nil)
   (a
    {:style style/link,
     :inner-text "Spread",
     :on-click (fn [e d! m!]
       (d! :template/spread-markup {:template-id template-id, :path focused-path})
       (d! :router/move-before nil))})
   (=< 8 nil)
   (a
    {:style style/link,
     :inner-text "Copy",
     :on-click (fn [e d! m!]
       (d! :session/copy-markup {:template-id template-id, :path focused-path}))})
   (=< 8 nil)
   (a
    {:style style/link,
     :inner-text "Paste",
     :on-click (fn [e d! m!]
       (d! :session/paste-markup {:template-id template-id, :path focused-path}))}))))

(def style-mock-data
  {:margin 0,
   :padding "4px 8px",
   :font-size 12,
   :font-family ui/font-code,
   :background-color (hsl 0 0 94),
   :white-space :normal,
   :line-height "18px"})

(defcomp
 comp-editor
 (states template focused-path)
 (div
  {:style (merge ui/flex ui/row {:overflow :auto})}
  (div
   {:style (merge ui/flex ui/column {:overflow :auto})}
   (cursor-> :operations comp-operations states (:id template) (or focused-path []))
   (div {:style {:height 1, :background-color (hsl 0 0 90)}})
   (div
    {:style (merge ui/flex {:overflow :auto, :padding 8})}
    (comp-markup (:markup template) [] focused-path)
    (when config/dev? (comp-inspect "Markup" (:markup template) {:bottom 0}))))
  (let [child (get-in (:markup template) (interleave (repeat :children) focused-path))
        template-id (:id template)
        mock-id (:mock-pointer template)
        mock-data (if (nil? mock-id) nil (get-in template [:mocks mock-id :data]))]
    (div
     {:style (merge ui/flex {:overflow :auto, :padding 8})}
     (cursor-> :type comp-type-picker states template-id focused-path child)
     (cursor-> :layout comp-layout-picker states template-id focused-path child)
     (cursor-> :background comp-bg-picker states template-id focused-path child)
     (when config/dev? (comp-inspect "Node" child {:bottom 0}))
     (cursor-> :presets comp-presets states (:presets child) template-id focused-path)
     (=< nil 8)
     (cursor-> :mocks comp-mock-picker states template)
     (pre {:inner-text (pr-str mock-data), :style style-mock-data})
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
