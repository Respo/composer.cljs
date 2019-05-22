
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
            [composer.comp.bg-picker :refer [comp-bg-picker comp-font-color-picker]]
            [composer.comp.dict-editor :refer [comp-dict-editor]]
            [composer.style :as style]
            [bisection-key.core :as bisection]
            [favored-edn.core :refer [write-edn]]
            [composer.util :refer [filter-path-set]]
            [composer.comp.operations :refer [comp-operations]]
            [feather.core :refer [comp-icon]]
            [clojure.string :as string])
  (:require-macros [clojure.core.strint :refer [<<]]))

(def fontface-choices
  [{:value ui/font-fancy, :display "Fancy"}
   {:value ui/font-code, :display "Code"}
   {:value ui/font-normal, :display "Normal"}])

(defcomp
 comp-fontface-picker
 (states fontface on-select)
 (div
  {:style ui/row-middle}
  (<> "Font family:" style/field-label)
  (=< 8 nil)
  (cursor->
   :font-color
   comp-select
   states
   fontface
   fontface-choices
   {:text "Select fontface"}
   (fn [result d! m!] (on-select result d! m!)))))

(defcomp
 comp-layout-name
 (layout-name)
 (let [layout (->> schema/node-layouts
                   (filter (fn [item] (= layout-name (:value item))))
                   first)]
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
                             (->> schema/node-layouts
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

(defn display-events [xs] (->> xs (map last) (string/join "; ")))

(defn display-props [xs] (->> xs (map (fn [[k v]] v)) (string/join "; ")))

(def style-element
  {:display :inline-block,
   :cursor :pointer,
   :padding "0 8px",
   :margin-bottom 4,
   :background-color (hsl 160 0 94),
   :color (hsl 0 0 50),
   :border-radius "0px",
   :vertical-align :top,
   :line-height "24px"})

(defcomp
 comp-markup
 (markup path focused-path active-paths)
 (div
  {:style (merge
           {:padding "4px 4px 1px 4px",
            :border-style "solid",
            :border-width "1px 0 0 1px",
            :border-color (hsl 0 0 94)}
           (if (contains? active-paths path) {:border-color (hsl 200 80 80)})
           (if (empty? (:children markup)) {:display :inline-block})
           (if (and false (empty? path)) {:border :none, :padding 0}))}
  (div
   {:style (merge
            style-element
            (if (= path focused-path) {:background-color (hsl 200 80 76), :color :white})),
    :on-click (fn [e d! m!] (d! :session/focus-to {:path path}))}
   (<> (name (:type markup)))
   (=< 8 nil)
   (<> (display-props (:props markup)) {:font-size 10, :font-family ui/font-code})
   (if (not (empty? (:event markup)))
     (<>
      (display-events (:event markup))
      {:font-size 10,
       :font-family ui/font-code,
       :margin-left 8,
       :background-color (hsl 280 80 60),
       :color :white,
       :padding "0 4px"}))
   (if (and (= :template (:type markup)) (string? (get-in markup [:props "name"])))
     (comp-icon
      :map-pin
      (merge
       {:font-size 12,
        :color (hsl 200 80 50),
        :cursor :pointer,
        :margin-left "4px",
        :vertical-align :middle}
       (if (= path focused-path) {:color "white"}))
      (fn [e d! m!] (d! :session/jump-template (get-in markup [:props "name"]))))))
  (list->
   {:style (merge
            {:padding-left 8, :margin-left 8}
            (let [amount (count (:children markup))]
              (if (or (<= amount 1)
                      (and (<= amount 5)
                           (every? (fn [x] (empty? (:children markup))) (:children markup))))
                {:display :inline-block})))}
   (->> (:children markup)
        (sort-by first)
        (map
         (fn [[k child-markup]]
           (let [next-path (conj path k)]
             [k
              (comp-markup
               child-markup
               next-path
               focused-path
               (filter-path-set active-paths next-path))])))))))

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
       (d! :template/use-mock {:template-id (:id template), :mock-id result}))))
  (=< 8 nil)
  (comp-icon
   :edit
   {:font-size 14, :color (hsl 200 80 80), :cursor :pointer}
   (fn [e d! m!] (d! :session/focus-to {:tab :mocks, :mock-id (:mock-pointer template)})))))

(defcomp
 comp-props-hint
 (type)
 (div
  {:style ui/row-middle}
  (<> "Support props: " style/field-label)
  (=< 8 nil)
  (list->
   {}
   (->> (get schema/props-hints type)
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
   :overflow :auto,
   :border-radius "4px",
   :word-break :break-all,
   :min-height 48})

(defcomp
 comp-editor
 (states template settings focused-path active-paths)
 (div
  {:style (merge ui/flex ui/row {:overflow :auto})}
  (div
   {:style (merge ui/flex ui/column {:overflow :auto})}
   (=< nil 4)
   (cursor-> :operations comp-operations states (:id template) (or focused-path []))
   (div
    {:style (merge ui/flex {:overflow :auto, :padding "0 8px"})}
    (comp-markup (:markup template) [] focused-path active-paths)
    (when config/dev? (comp-inspect "Markup" (:markup template) {:bottom 0}))))
  (div {:style {:width 1, :background-color "#eee"}})
  (let [child (get-in (:markup template) (interleave (repeat :children) focused-path))
        template-id (:id template)
        mock-id (:mock-pointer template)
        mock-data (if (nil? mock-id) nil (get-in template [:mocks mock-id :data]))
        mock-state (if (nil? mock-id) nil (get-in template [:mocks mock-id :state]))]
    (div
     {:style (merge ui/flex ui/column {:overflow :auto})}
     (div
      {:style (merge ui/flex ui/row {:overflow :auto})}
      (div
       {:style (merge ui/expand {:padding 8})}
       (cursor-> :type comp-type-picker states template-id focused-path child)
       (cursor-> :layout comp-layout-picker states template-id focused-path child)
       (when config/dev? (comp-inspect "Node" child {:bottom 0}))
       (cursor-> :presets comp-presets states (:presets child) template-id focused-path)
       (cursor->
        :props
        comp-dict-editor
        states
        "Props:"
        (:props child)
        (get schema/props-hints (:type child))
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
        nil
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
        nil
        (fn [change d! m!]
          (d!
           :template/node-attrs
           (merge {:template-id template-id, :path focused-path} change))))
       (comp-props-hint (:type child)))
      (div
       {:style (merge ui/expand {:border-left "1px solid #f4f4f4", :padding 8})}
       (cursor->
        :font-color
        comp-font-color-picker
        states
        template-id
        focused-path
        child
        (:colors settings))
       (cursor->
        :fontface
        comp-fontface-picker
        states
        (get-in child [:style "font-family"])
        (fn [result d! m!]
          (d!
           :template/node-style
           (merge
            {:template-id template-id, :path focused-path}
            {:type :set, :key "font-family", :value result}))))
       (cursor->
        :background
        comp-bg-picker
        states
        template-id
        focused-path
        child
        (:colors settings))
       (cursor->
        :style
        comp-dict-editor
        states
        "Style:"
        (:style child)
        nil
        (fn [change d! m!]
          (d!
           :template/node-style
           (merge {:template-id template-id, :path focused-path} change))))))
     (div
      {:style (merge
               ui/flex
               ui/column
               {:padding 8, :overflow :auto, :border-top (str "1px solid " (hsl 0 0 94))})}
      (cursor-> :mocks comp-mock-picker states template)
      (pre
       {:inner-text (write-edn mock-data),
        :style (merge style-mock-data {:overflow :auto, :flex 2})})
      (pre
       {:inner-text (write-edn mock-state),
        :style (merge ui/flex style-mock-data {:overflow :auto})}))))))
