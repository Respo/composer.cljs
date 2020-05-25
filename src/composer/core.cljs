
(ns composer.core
  (:require [respo.core
             :refer
             [defcomp list-> <> div button textarea span a i input create-list-element img]]
            [respo.comp.space :refer [=<]]
            [hsl.core :refer [hsl]]
            ["feather-icons" :as icons]
            [respo-ui.core :as ui]
            [clojure.string :as string]
            [cljs.reader :refer [read-string]]
            [respo.util.detect :refer [component? element?]]
            [respo-md.comp.md :refer [comp-md-block]]
            [composer.util :refer [use-string-keys index-of]])
  (:require-macros [clojure.core.strint :refer [<<]]))

(declare render-some)

(declare render-popup)

(declare render-children)

(declare render-box)

(declare read-token)

(declare render-element)

(declare render-template)

(declare read-by-marks)

(declare render-case)

(declare render-list)

(declare render-markup)

(declare render-function)

(defcomp
 comp-invalid
 (title props)
 (span
  {:style {:color :white,
           :cursor :pointer,
           :background-color (hsl 0 80 50),
           :font-size 13,
           :padding "4px 4px",
           :font-family ui/font-fancy},
   :inner-text title,
   :on-click (fn [e d! m!] (js/console.log (clj->js props)))}))

(defn read-token [x scope state]
  (if (string? x)
    (cond
      (string/starts-with? x "@")
        (let [chunks (filter (fn [x] (not (string/blank? x))) (string/split (subs x 1) " "))]
          (read-by-marks chunks scope))
      (string/starts-with? x "#")
        (let [chunks (filter (fn [x] (not (string/blank? x))) (string/split (subs x 1) " "))]
          (read-by-marks chunks state))
      (string/starts-with? x ":") (keyword (subs x 1))
      (string/starts-with? x "~") (read-string (subs x 1))
      (string/starts-with? x "|") (subs x 1)
      (string/starts-with? x "\"") (subs x 1)
      (re-matches (re-pattern "[\\d\\.]+") x) (js/parseFloat x)
      :else x)
    nil))

(defn read-by-marks [xs scope]
  (if (nil? scope)
    nil
    (if (empty? xs)
      scope
      (let [x (first xs), v (read-token x scope {})] (recur (rest xs) (get scope v))))))

(defn eval-attrs [attrs data state]
  (->> attrs (map (fn [[k v]] [k (read-token v data state)])) (into {})))

(defn extract-templates [db]
  (->> db
       :templates
       vals
       (map (fn [template] [(:name template) (:markup template)]))
       (into {})))

(defn get-layout [layout]
  (use-string-keys
   (case layout
     :row ui/row
     :row-center ui/row-center
     :center ui/center
     :row-middle ui/row-middle
     :row-parted ui/row-parted
     :column ui/column
     :column-parted ui/column-parted
     {})))

(defn get-template-props [data-prop attrs data state]
  (if (some? data-prop)
    (read-token data-prop data state)
    (->> attrs (map (fn [[k v]] [(keyword k) (read-token v data state)])) (into {}))))

(defn read-styles [style data state]
  (->> style
       (map
        (fn [[k v]]
          [k
           (cond
             (re-matches #"^\#[0-9a-f]{3}$" v)
               (do (js/console.warn "Outdated color syntax" v) v)
             (re-matches #"^\#[0-9a-f]{6}$" v)
               (do (js/console.warn "Outdated color syntax" v) v)
             read-token v
             data state)]))
       (into {})))

(defn style-presets [preset-ids presets]
  (->> preset-ids
       (map
        (fn [preset-id]
          (let [preset (get presets preset-id)]
            (if (some? preset) (:style preset) (js/console.warn "Unknown preset:" preset-id)))))
       (apply merge)))

(defn render-button [markup context on-action]
  (let [props (:props markup)
        states (:states context)
        text (read-token (get props "text") (:data context) (:data states))
        param (read-token (get props "param") (:data context) (:data states))
        event-map (->> (:event markup)
                       (map
                        (fn [[name action]]
                          [name
                           (fn [e d! m!]
                             (on-action
                              d!
                              (read-token action (:data context) (:data states))
                              context
                              {:event (:event e), :props props, :param param}))]))
                       (into {}))]
    (button
     (merge
      (eval-attrs (:attrs markup) (:data context) (:data states))
      {:style (merge
               ui/button
               (style-presets (:presets markup) (:presets context))
               (read-styles (:style markup) (:data context) (:data states))),
       :inner-text (or text "Submit"),
       :on event-map}))))

(defn render-divider [markup context]
  (let [props (:props markup)
        states (:states context)
        vertical? (contains?
                   #{:vertical :v}
                   (read-token (get props "kind") (:data context) (:data states)))
        color (or (read-token (get props "color") (:data context) (:data states)) "#eee")]
    (div
     {:style (if vertical?
        {:background-color color, :width 1, :height "auto"}
        {:background-color color, :height 1, :width "auto"})})))

(defn render-icon [markup context on-action]
  (let [props (:props markup)
        states (:states context)
        icon-name (or (read-token (get props "name") (:data context) (:data states))
                      :feather)
        size (js/parseFloat (get props "size" "16"))
        color (get props "color" (hsl 200 80 70))
        obj (aget (.-icons icons) (name icon-name))
        param (read-token (get props "param") (:data context) (:data states))
        event-map (->> (:event markup)
                       (map
                        (fn [[name action]]
                          [name
                           (fn [e d! m!]
                             (on-action
                              d!
                              (read-token action (:data context) (:data states))
                              context
                              {:event (:event e), :props props, :param param}))]))
                       (into {}))]
    (if (some? obj)
      (i
       {:style (merge {"display" :inline-block, "cursor" :pointer} (:style markup)),
        :innerHTML (.toSvg obj (clj->js {:width size, :height size, :color color})),
        :on event-map})
      (comp-invalid (str "No icon: " icon-name) props))))

(defn render-image [markup context]
  (let [props (:props markup)
        states (:states context)
        src (read-token (get props "src") (:data context) (:data states))
        mode (read-token (get props "mode") (:data context) (:data states))
        width (or (read-token (get props "width") (:data context) (:data states)) 80)
        height (or (read-token (get props "height") (:data context) (:data states)) 80)]
    (cond
      (nil? src) (comp-invalid (<< "Bad image src: ~(pr-str src)") props)
      (= mode :img)
        (img
         (merge
          {:src src, :width width, :height height}
          (eval-attrs (:attrs markup) (:data context) (:data states))
          {:style (merge
                   (get-layout (:layout markup))
                   (style-presets (:presets markup) (:presets context))
                   (read-styles (:style markup) (:data context) (:data states)))}))
      (contains? #{:contain :cover} mode)
        (div
         (merge
          (eval-attrs (:attrs markup) (:data context) (:data states))
          {:style (merge
                   (use-string-keys
                    {:background-image (<< "url(~{src})"),
                     :background-size mode,
                     :width width,
                     :height height,
                     :background-position :center,
                     :background-repeat :no-repeat})
                   (get-layout (:layout markup))
                   (style-presets (:presets markup) (:presets context))
                   (read-styles (:style markup) (:data context) (:data states)))}))
      :else (comp-invalid (<< "Bad image mode: ~(pr-str mode)") props))))

(defn render-input [markup context on-action]
  (let [props (:props markup)
        states (:states context)
        value (read-token (get props "value") (:data context) (:data states))
        textarea? (some? (get props "textarea"))
        param (read-token (get props "param") (:data context) (:data states))
        attrs (eval-attrs (:attrs markup) (:data context) (:data states))
        event-map (->> (:event markup)
                       (map
                        (fn [[name action]]
                          [name
                           (fn [e d! m!]
                             (on-action
                              d!
                              (read-token action (:data context) (:data states))
                              context
                              {:props props,
                               :value (:value e),
                               :event (:event e),
                               :param param}))]))
                       (into {}))]
    (if textarea?
      (textarea
       (merge
        attrs
        {:value value,
         :style (merge
                 (use-string-keys ui/textarea)
                 (style-presets (:presets markup) (:presets context))
                 (read-styles (:style markup) (:data context) (:data states))),
         :on event-map}))
      (input
       (merge
        attrs
        {:value value,
         :style (merge
                 (use-string-keys ui/input)
                 (style-presets (:presets markup) (:presets context))
                 (read-styles (:style markup) (:data context) (:data states))),
         :on event-map})))))

(def style-inspect
  {:background-color (hsl 200 80 60),
   :color :white,
   :padding "0 8px",
   :font-size 12,
   :font-family ui/font-code,
   :line-height "20px",
   :min-height "20px",
   :display :inline-block,
   :cursor :pointer,
   :border (str "1px solid " (hsl 0 90 64)),
   :max-width 400,
   :max-height 120,
   :overflow :auto})

(defn render-inspect [markup context]
  (let [props (:props markup)
        states (:states context)
        value (read-token (get props "value") (:data context) (:data states))]
    (span
     {:inner-text (pr-str value),
      :style style-inspect,
      :on-click (fn [e d! m!]
        (js/console.log (clj->js (:data context)) (clj->js (:state context))))})))

(defn render-link [markup context on-action]
  (let [props (:props markup)
        states (:data context)
        text (read-token (get props "text") (:data context) (:data states))
        href (read-token (get props "href") (:data context) (:data states))
        param (read-token (get props "param") (:data context) (:data states))
        event-map (->> (:event markup)
                       (map
                        (fn [[name action]]
                          [name
                           (fn [e d! m!]
                             (on-action
                              d!
                              (read-token action (:data context) (:data states))
                              context
                              {:event (:event e), :props props, :param param}))]))
                       (into {}))]
    (a
     (merge
      (eval-attrs (:attrs markup) (:data context) (:data states))
      {:style (merge
               (use-string-keys ui/link)
               (style-presets (:presets markup) (:presets context))
               (read-styles (:style markup) (:data context) (:data states))),
       :inner-text (or text "Submit"),
       :href (or href "#"),
       :on event-map}))))

(defn render-markdown [markup context]
  (let [props (:props markup)
        states (:states context)
        value (read-token (get props "text") (:data context) (:data states))
        class-name (get props "class")]
    (comp-md-block
     value
     {:style (merge
              (style-presets (:presets markup) (:presets context))
              (read-styles (:style markup) (:data context) (:data states))),
      :class-name class-name})))

(defn render-space [markup context]
  (let [props (:props markup)
        states (:state context)
        width (read-token (get props "width") (:data context) (:data states))
        height (read-token (get props "height") (:data context) (:data states))]
    (if (and (nil? width) (nil? height)) (comp-invalid "<Space nil>" props) (=< width height))))

(defn render-text [markup context]
  (let [props (:props markup)
        states (:states context)
        value (read-token (get props "value") (:data context) (:data states))
        data (read-token (get props "data") (:data context) (:data states))]
    (<>
     (if (some? value)
       (if (and (string? value) (some? data)) (string/replace value "~{data}" data) value)
       "TEXT")
     (merge
      (style-presets (:presets markup) (:presets context))
      (read-styles (:style markup) (:data context) (:data states))))))

(def style-unknown {"font-size" 12, "color" :red})

(defn render-template [markup context on-action]
  (let [templates (:templates context)
        data (:data context)
        states (:states context)
        props (:props markup)
        template-name (read-token (get props "name") (:data context) (:data states))
        state-key (read-token (get props "state-key") (:data context) (:data states))
        data-prop (get props "data")]
    (cond
      (> (:level context) 10) (comp-invalid "<Bad template: too much levels>" props)
      (not (string? template-name))
        (comp-invalid (<< "<Invalid template name: ~(pr-str template-name)>") props)
      (and (nil? data-prop) (empty? (:attrs markup)))
        (comp-invalid (<< "<template data missing, no data, no attrs>") props)
      :else
        (let [template-props (get-template-props
                              data-prop
                              (:attrs markup)
                              data
                              (:data states))
              state-fn (get-in context [:state-fns template-name])]
          (render-markup
           (get templates template-name)
           (-> context
               (assoc :data template-props)
               (update :level inc)
               (assoc :template-name template-name)
               (update
                :state-path
                (fn [path] (if (some? state-key) (conj (vec path) state-key) (vec path))))
               (update
                :states
                (fn [states]
                  (let [states-here (if (some? state-key) (get states state-key) states)]
                    (update
                     states-here
                     :data
                     (fn [state] (if (fn? state-fn) (state-fn template-props state) state)))))))
           on-action)))))

(defn render-some [markup context on-action]
  (let [props (:props markup)
        states (:states context)
        value (read-token (get props "value") (:data context) (:data states))
        kind (read-token (get props "kind") (:data context) (:data states))
        child-pair (->> (:children markup) (sort-by first) (vals))
        result (case kind
                 :list (empty? value)
                 :map (empty? value)
                 :boolean (or (= value false) (nil? value))
                 :string (string/blank? value)
                 :value (nil? value)
                 nil (nil? value)
                 (nil? value))]
    (cond
      (not= (count child-pair) 2) (comp-invalid "<Some wants 2 children>" props)
      (nil? (get props "value")) (comp-invalid "<Some requires a value>" props)
      :else
        (if result
          (render-markup (first child-pair) context on-action)
          (render-markup (last child-pair) context on-action)))))

(defn render-popup [markup context on-action]
  (let [props (:props markup)
        states (:states context)
        value (read-token (get props "visible") (:data context) (:data states))
        backdrop-click-action (get-in markup [:event "backdrop-click"])
        param (read-token (get props "param") (:data context) (:data states))]
    (if (:hide-popup? context)
      (comp-invalid "Popup is hidden in dev" props)
      (if value
        (div
         {:style {:position :fixed,
                  :top 0,
                  :left 0,
                  :width "100%",
                  :height "100%",
                  :display :flex,
                  :overflow :auto,
                  :padding 32,
                  :background-color (hsl 0 0 0 0.7)},
          :on-click (fn [e d! m!]
            (if (some? backdrop-click-action)
              (on-action
               d!
               (read-token backdrop-click-action (:data context) (:data states))
               context
               {:event (:event e), :props props, :param param})))}
         (list->
          (merge
           {:on-click (fn [e d! m!] )}
           (eval-attrs (:attrs markup) (:data context) (:data states))
           {:style (merge
                    (use-string-keys
                     {:margin :auto,
                      :min-width 320,
                      :min-height 200,
                      :background-color (hsl 0 0 100)})
                    (get-layout (:layout markup))
                    (style-presets (:presets markup) (:presets context))
                    (read-styles (:style markup) (:data context) (:data states)))})
          (render-children (:children markup) context on-action)))
        (span {})))))

(defn render-markup [markup context on-action]
  (case (:type markup)
    :box (render-box markup context on-action)
    :space (render-space markup context)
    :divider (render-divider markup context)
    :button (render-button markup context on-action)
    :icon (render-icon markup context on-action)
    :link (render-link markup context on-action)
    :text (render-text markup context)
    :some (render-some markup context on-action)
    :template (render-template markup context on-action)
    :input (render-input markup context on-action)
    :list (render-list markup context on-action)
    :popup (render-popup markup context on-action)
    :inspect (render-inspect markup context)
    :element (render-element markup context on-action)
    :markdown (render-markdown markup context)
    :image (render-image markup context)
    :case (render-case markup context on-action)
    :function (render-function markup context on-action)
    :comment (span nil)
    (div
     {:style style-unknown}
     (comp-invalid (str "Bad type: " (pr-str (:type markup))) markup))))

(defn render-list [markup context on-action]
  (let [props (:props markup)
        states (:states context)
        value (read-token (get props "value") (:data context) (:data states))
        only-child (first (vals (:children markup)))]
    (cond
      (not (or (sequential? value) (map? value)))
        (comp-invalid (<< "<Bad list value: ~(pr-str value)>") props)
      (> (count (:children markup)) 1) (comp-invalid "<Bad list: too many children>" props)
      (nil? only-child) (comp-invalid (<< "<Bad list: no children>") props)
      :else
        (list->
         (merge
          (eval-attrs (:attrs markup) (:data context) (:data states))
          {:style (merge
                   (get-layout (:layout markup))
                   (style-presets (:presets markup) (:presets context))
                   (read-styles (:style markup) (:data context) (:data states)))})
         (let [pairs (if (map? value)
                       (->> value (sort-by (fn [[k v]] k)))
                       (->> value (map-indexed (fn [idx v] [idx v]))))]
           (->> pairs
                (map
                 (fn [[k x]]
                   [k
                    (let [new-context (assoc
                                       context
                                       :data
                                       {:index k, :outer (:data context), :item x})]
                      (render-markup only-child new-context on-action))]))))))))

(defn render-function [markup context on-action]
  (let [props (:props markup)
        states (:states context)
        param (read-token (get props "param") (:data context) (:data states))
        func-name (read-token (get props "name") (:data context) (:data states))
        styles (merge
                (get-layout (:layout markup))
                (style-presets (:presets markup) (:presets context))
                (read-styles (:style markup) (:data context) (:data states)))
        func (get-in context [:functions func-name])
        children (render-children (:children markup) context on-action)]
    (cond
      (nil? func-name) (comp-invalid "No function defined" (:props markup))
      (not (fn? func))
        (comp-invalid (<< "No function for ~(pr-str func-name)") (:functions context))
      :else (func param styles on-action children))))

(defn render-element [markup context on-action]
  (let [props (:props markup)
        states (:states context)
        value (read-token (get props "name") (:data context) (:data states))
        tag-name (keyword (or value "div"))
        param (read-token (get props "param") (:data context) (:data states))
        event-map (->> (:event markup)
                       (map
                        (fn [[name action]]
                          [name
                           (fn [e d! m!]
                             (on-action
                              d!
                              (read-token action (:data context) (:data states))
                              context
                              {:event (:event e), :props props, :param param}))]))
                       (into {}))]
    (create-list-element
     tag-name
     (merge
      (eval-attrs (:attrs markup) (:data context) (:data states))
      {:style (merge
               (get-layout (:layout markup))
               (style-presets (:presets markup) (:presets context))
               (read-styles (:style markup) (:data context) (:data states))),
       :on event-map})
     (render-children (:children markup) context on-action))))

(defn render-children [children context on-action]
  (->> children
       (sort-by first)
       (map (fn [[k child]] [k (render-markup child context on-action)]))))

(defn render-case [markup context on-action]
  (let [props (:props markup)
        states (:states context)
        value (read-token (get props "value") (:data context) (:data states))
        options (read-token (get props "options") (:data context) (:data states))
        options-size (count options)
        children-size (count (:children markup))
        children-vec (->> (:children markup) (sort-by first) (map last) (vec))]
    (cond
      (not (vector? options)) (comp-invalid "Options need to be a vector" props)
      (not= children-size (inc options-size))
        (comp-invalid
         (<< "Children size ~{children-size} not matchhing 1+~{options-size}")
         props)
      :else
        (let [idx (index-of options value)]
          (if (nil? idx)
            (render-markup (first children-vec) context on-action)
            (render-markup (get children-vec (inc idx)) context on-action))))))

(defn render-box [markup context on-action]
  (let [props (:props markup)
        states (:states context)
        param (read-token (get props "param") (:data context) (:data states))
        event-map (->> (:event markup)
                       (map
                        (fn [[name action]]
                          [name
                           (fn [e d! m!]
                             (on-action
                              d!
                              (read-token action (:data context) (:data states))
                              context
                              {:param param, :event (:event e), :props props}))]))
                       (into {}))]
    (list->
     (merge
      (eval-attrs (:attrs markup) (:data context) (:data states))
      {:style (merge
               (get-layout (:layout markup))
               (style-presets (:presets markup) (:presets context))
               (read-styles (:style markup) (:data context) (:data states))),
       :on event-map})
     (render-children (:children markup) context on-action))))
