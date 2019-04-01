
(ns composer.core
  (:require [respo.core
             :refer
             [defcomp
              cursor->
              list->
              <>
              div
              button
              textarea
              span
              a
              i
              input
              create-list-element
              img]]
            [respo.comp.space :refer [=<]]
            [hsl.core :refer [hsl]]
            ["feather-icons" :as icons]
            [respo-ui.core :as ui]
            [clojure.string :as string]
            [cljs.reader :refer [read-string]]
            [respo.util.detect :refer [component? element?]]
            [respo-md.comp.md :refer [comp-md-block]]
            [composer.util :refer [use-string-keys]])
  (:require-macros [clojure.core.strint :refer [<<]]))

(declare render-some)

(declare render-popup)

(declare render-children)

(declare render-box)

(declare read-token)

(declare render-element)

(declare render-template)

(declare read-by-marks)

(declare render-list)

(declare render-markup)

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

(defn read-token [x scope]
  (if (string? x)
    (cond
      (string/starts-with? x "@")
        (let [chunks (filter (fn [x] (not (string/blank? x))) (string/split (subs x 1) " "))]
          (read-by-marks chunks scope))
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
      (let [x (first xs), v (read-token x scope)] (recur (rest xs) (get scope v))))))

(defn eval-attrs [attrs data]
  (->> attrs (map (fn [[k v]] [k (read-token v data)])) (into {})))

(defn extract-templates [db]
  (->> db
       :templates
       vals
       (map (fn [template] [(:name template) (:markup template)]))
       (into {})))

(defn get-layout [layout]
  (case layout
    :row ui/row
    :row-center ui/row-center
    :center ui/center
    :row-middle ui/row-middle
    :row-parted ui/row-parted
    :column ui/column
    :column-parted ui/column-parted
    {}))

(defn get-preset [preset]
  (case preset
    :flex ui/flex
    :expand (merge ui/flex {:scroll :auto})
    :font-code {:font-family ui/font-code}
    :font-fancy {:font-family ui/font-fancy}
    :font-normal {:font-family ui/font-normal}
    :fullscreen ui/fullscreen
    :scroll {:overflow :auto}
    :global ui/global
    :base-padding {:padding "4px 8px"}
    (do (js/console.warn (str "Unknown preset: " preset)) nil)))

(defn style-presets [presets] (->> presets (map get-preset) (apply merge)))

(defn render-button [markup context on-action]
  (let [props (:props markup)
        text (read-token (get props "text") (:data context))
        param (read-token (get props "param") (:data context))
        event-map (->> (:event markup)
                       (map
                        (fn [[name action]]
                          [name
                           (fn [e d! m!]
                             (on-action
                              d!
                              (read-token action (:data context))
                              param
                              {:event (:event e), :props props, :data (:data context)}))]))
                       (into {}))]
    (button
     (merge
      (eval-attrs (:attrs markup) (:data context))
      {:style (merge ui/button (style-presets (:presets markup)) (:style markup)),
       :inner-text (or text "Submit"),
       :on event-map}))))

(defn render-divider [markup context]
  (let [props (:props markup)
        vertical? (contains?
                   #{:vertical :v}
                   (read-token (get props "kind") (:data context)))
        color (get props "color" "#eee")]
    (div
     {:style (if vertical?
        {:background-color color, :width 1, :height "auto"}
        {:background-color color, :height 1, :width "auto"})})))

(defn render-icon [markup context on-action]
  (let [props (:props markup)
        icon-name (get props "name" "feather")
        size (js/parseFloat (get props "size" "16"))
        color (get props "color" (hsl 200 80 70))
        obj (aget (.-icons icons) icon-name)
        param (read-token (get props "param") (:data context))
        event-map (->> (:event markup)
                       (map
                        (fn [[name action]]
                          [name
                           (fn [e d! m!]
                             (on-action
                              d!
                              (read-token action (:data context))
                              param
                              {:event (:event e), :props props, :data (:data context)}))]))
                       (into {}))]
    (if (some? obj)
      (i
       {:style (merge {:display :inline-block, :cursor :pointer} (:style markup)),
        :innerHTML (.toSvg obj (clj->js {:width size, :height size, :color color})),
        :on event-map})
      (comp-invalid (str "No icon: " icon-name) props))))

(defn render-image [markup context]
  (let [props (:props markup)
        src (read-token (get props "src") (:data context))
        mode (read-token (get props "mode") (:data context))
        width (or (read-token (get props "width") (:data context)) 80)
        height (or (read-token (get props "height") (:data context)) 80)]
    (cond
      (nil? src) (comp-invalid (<< "Bad image src: ~(pr-str src)") props)
      (= mode :img)
        (img
         (merge
          {:src src, :width width, :height height}
          (eval-attrs (:attrs markup) (:data context))
          {:style (merge
                   (get-layout (:layout markup))
                   (style-presets (:presets markup))
                   (:style markup))}))
      (contains? #{:contain :cover} mode)
        (div
         (merge
          (eval-attrs (:attrs markup) (:data context))
          {:style (merge
                   {:background-image (<< "url(~{src})"),
                    :background-size mode,
                    :width width,
                    :height height,
                    :background-position :center,
                    :background-repeat :no-repeat}
                   (get-layout (:layout markup))
                   (style-presets (:presets markup))
                   (:style markup))}))
      :else (comp-invalid (<< "Bad image mode: ~(pr-str mode)") props))))

(defn render-input [markup context on-action]
  (let [props (:props markup)
        value (read-token (get props "value") (:data context))
        textarea? (some? (get props "textarea"))
        param (read-token (get props "param") (:data context))
        attrs (eval-attrs (:attrs markup) (:data context))
        event-map (->> (:event markup)
                       (map
                        (fn [[name action]]
                          [name
                           (fn [e d! m!]
                             (on-action
                              d!
                              (read-token action (:data context))
                              param
                              {:props props,
                               :value (:value e),
                               :event (:event e),
                               :data (:data context)}))]))
                       (into {}))]
    (if textarea?
      (textarea
       (merge
        attrs
        {:value value,
         :style (merge
                 (use-string-keys ui/textarea)
                 (style-presets (:presets markup))
                 (:style markup)),
         :on event-map}))
      (input
       (merge
        attrs
        {:value value,
         :style (merge
                 (use-string-keys ui/input)
                 (style-presets (:presets markup))
                 (:style markup)),
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
  (let [props (:props markup), value (read-token (get props "value") (:data context))]
    (span
     {:inner-text (pr-str value),
      :style style-inspect,
      :on-click (fn [e d! m!] (js/console.log (clj->js (:data context))))})))

(defn render-link [markup context on-action]
  (let [props (:props markup)
        text (read-token (get props "text") (:data context))
        href (read-token (get props "href") (:data context))
        param (read-token (get props "param") (:data context))
        event-map (->> (:event markup)
                       (map
                        (fn [[name action]]
                          [name
                           (fn [e d! m!]
                             (on-action
                              d!
                              (read-token action (:data context))
                              param
                              {:event (:event e), :props props, :data (:data context)}))]))
                       (into {}))]
    (a
     (merge
      (eval-attrs (:attrs markup) (:data context))
      {:style (merge ui/link (:style markup)),
       :inner-text (or text "Submit"),
       :href (or href "#"),
       :on event-map}))))

(defn render-markdown [markup context]
  (let [props (:props markup)
        value (read-token (get props "text") (:data context))
        class-name (get props "class")]
    (comp-md-block
     value
     {:style (merge (style-presets (:presets markup)) (:style markup)),
      :class-name class-name})))

(defn render-slot [markup context on-action]
  (let [props (:props markup), dom (or (get props "dom") (:dom props))]
    (cond
      (component? dom) dom
      (element? dom) dom
      (some? dom) (<> (str "<Bad slot: " (pr-str dom) ">"))
      :else (comp-invalid "<Empty slot>" props))))

(defn render-space [markup context]
  (let [props (:props markup)
        width (read-token (get props "width") (:data context))
        height (read-token (get props "height") (:data context))]
    (if (and (nil? width) (nil? height)) (comp-invalid "<Space nil>" props) (=< width height))))

(defn render-text [markup context]
  (let [props (:props markup), value (read-token (get props "value") (:data context))]
    (<> (or value "TEXT") (merge (style-presets (:presets markup)) (:style markup)))))

(def style-unknown {"font-size" 12, "color" :red})

(defn render-template [markup context on-action]
  (let [templates (:templates context)
        data (:data context)
        props (:props markup)
        template-name (read-token (get props "name") (:data context))]
    (cond
      (> (:level context) 10) (comp-invalid "<Bad template: too much levels>" props)
      (not (string? template-name))
        (comp-invalid (<< "<Invalid template name: ~(pr-str template-name)>") props)
      :else
        (render-markup
         (get templates template-name)
         (-> context (assoc :data (read-token (get props "data") data)) (update :level inc))
         on-action))))

(defn render-some [markup context on-action]
  (let [props (:props markup)
        value (read-token (get props "value") (:data context))
        kind (read-token (get props "kind") (:data context))
        child-pair (->> (:children markup) (sort-by first) (vals))
        result (case kind
                 :list (empty? value)
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
        value (read-token (get props "visible") (:data context))
        action (get props "action" "popup-close")]
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
          :on-click (fn [e d! m!] (on-action d! action props nil))}
         (list->
          (merge
           {:on-click (fn [e d! m!] )}
           (eval-attrs (:attrs markup) (:data context))
           {:style (merge
                    {:margin :auto,
                     :min-width 320,
                     :min-height 200,
                     :background-color (hsl 0 0 100)}
                    (get-layout (:layout markup))
                    (style-presets (:presets markup))
                    (:style markup))})
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
    :slot (render-slot markup context on-action)
    :popup (render-popup markup context on-action)
    :inspect (render-inspect markup context)
    :element (render-element markup context)
    :markdown (render-markdown markup context)
    :image (render-image markup context)
    (div
     {:style style-unknown}
     (comp-invalid (str "Bad type: " (pr-str (:type markup))) markup))))

(defn render-list [markup context on-action]
  (let [props (:props markup)
        value (read-token (get props "value") (:data context))
        only-child (first (vals (:children markup)))]
    (cond
      (not (sequential? value)) (comp-invalid (<< "<Bad list: ~(pr-str value)>") props)
      (> (count (:children markup)) 1) (comp-invalid "<Bad list: too many children>" props)
      (nil? only-child) (comp-invalid (<< "<Bad list: no children>") props)
      :else
        (list->
         (merge
          (eval-attrs (:attrs markup) (:data context))
          {:style (merge
                   (get-layout (:layout markup))
                   (style-presets (:presets markup))
                   (:style markup))})
         (->> value
              (map-indexed
               (fn [idx x]
                 [idx
                  (let [new-context (assoc
                                     context
                                     :data
                                     {:index idx, :outer (:data context), :item x})]
                    (render-markup only-child new-context on-action))])))))))

(defn render-element [markup context on-action]
  (let [props (:props markup)
        value (read-token (get props "name") (:data context))
        tag-name (keyword (or value "div"))
        param (read-token (get props "param") (:data context))
        event-map (->> (:event markup)
                       (map
                        (fn [[name action]]
                          [name
                           (fn [e d! m!]
                             (on-action
                              d!
                              (read-token action (:data context))
                              param
                              {:event (:event e), :props props, :data (:data context)}))]))
                       (into {}))]
    (create-list-element
     tag-name
     (merge
      (eval-attrs (:attrs markup) (:data context))
      {:style (merge
               (get-layout (:layout markup))
               (style-presets (:presets markup))
               (:style markup)),
       :on event-map})
     (render-children (:children markup) context on-action))))

(defn render-children [children context on-action]
  (->> children
       (sort-by first)
       (map (fn [[k child]] [k (render-markup child context on-action)]))))

(defn render-box [markup context on-action]
  (let [props (:props markup)
        param (read-token (get props "param") (:data context))
        event-map (->> (:event markup)
                       (map
                        (fn [[name action]]
                          [name
                           (fn [e d! m!]
                             (on-action
                              d!
                              (read-token action (:data context))
                              param
                              {:event (:event e), :props props, :data (:data context)}))]))
                       (into {}))]
    (list->
     (merge
      (eval-attrs (:attrs markup) (:data context))
      {:style (merge
               (get-layout (:layout markup))
               (style-presets (:presets markup))
               (:style markup)),
       :on event-map})
     (render-children (:children markup) context on-action))))
