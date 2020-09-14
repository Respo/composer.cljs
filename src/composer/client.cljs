
(ns composer.client
  (:require [respo.core :refer [render! clear-cache! realize-ssr!]]
            [respo.cursor :refer [update-states]]
            [composer.comp.container :refer [comp-container]]
            [cljs.reader :refer [read-string]]
            [composer.schema :as schema]
            [composer.config :as config]
            [ws-edn.client :refer [ws-connect! ws-send!]]
            [recollect.patch :refer [patch-twig]]
            [cumulo-util.core :refer [on-page-touch]]
            ["url-parse" :as url-parse])
  (:require-macros [clojure.core.strint :refer [<<]]))

(declare dispatch!)

(declare connect!)

(declare simulate-login!)

(defonce *states (atom {:states {}}))

(defonce *store (atom nil))

(defn simulate-login! []
  (let [raw (.getItem js/localStorage (:storage-key config/site))]
    (if (some? raw)
      (do (println "Found storage.") (dispatch! :user/log-in (read-string raw)))
      (do (println "Found no storage.")))))

(defn dispatch! [op op-data]
  (when (and config/dev? (not= op :states)) (println "Dispatch" op op-data))
  (case op
    :states (reset! *states (update-states @*states op-data))
    :effect/connect (connect!)
    (ws-send! {:kind :op, :op op, :data op-data})))

(defn connect! []
  (let [obj (url-parse js/location.href true)
        options (js->clj (.-query obj) :keywordize-keys true)
        host (or (:host options) "127.0.0.1")
        port (or (:port options) (:port config/site))]
    (ws-connect!
     (<< "ws://~{host}:~{port}")
     {:on-open (fn [] (simulate-login!)),
      :on-close (fn [event] (reset! *store nil) (js/console.error "Lost connection!")),
      :on-data (fn [data]
        (case (:kind data)
          :patch
            (let [changes (:data data)]
              (when config/dev? (js/console.log "Changes" (clj->js changes)))
              (reset! *store (patch-twig @*store changes)))
          (println "unknown kind:" data)))})))

(def mount-target (.querySelector js/document ".app"))

(defn on-window-keydown [event]
  (comment js/console.log event)
  (when (and (= "s" (.-key event)) (.-metaKey event))
    (.preventDefault event)
    (dispatch! :effect/persist nil)))

(defn render-app! [renderer]
  (renderer mount-target (comp-container (:states @*states) @*store) dispatch!))

(def ssr? (some? (.querySelector js/document "meta.respo-ssr")))

(defn main! []
  (println "Running mode:" (if config/dev? "dev" "release"))
  (if ssr? (render-app! realize-ssr!))
  (render-app! render!)
  (connect!)
  (add-watch *store :changes #(render-app! render!))
  (add-watch *states :changes #(render-app! render!))
  (on-page-touch #(if (nil? @*store) (connect!)))
  (.addEventListener js/window "keydown" #(on-window-keydown %))
  (println "App started!"))

(defn ^:dev/after-load
  reload!
  []
  (clear-cache!)
  (render-app! render!)
  (println "Code updated."))
