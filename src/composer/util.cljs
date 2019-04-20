
(ns composer.util
  (:require ["net" :as net])
  (:require-macros [clojure.core.strint :refer [<<]]))

(defn path-includes? [xs ys]
  (if (empty? ys)
    true
    (if (empty? xs) false (if (= (first xs) (first ys)) (recur (rest xs) (rest ys)) false))))

(defn filter-path-set [paths p]
  (->> paths (filter (fn [path] (path-includes? path p))) (set)))

(defn index-of
  ([xs y] (index-of 0 xs y))
  ([idx xs y]
   (cond (empty? xs) nil (= (first xs) y) idx :else (recur (inc idx) (rest xs) y))))

(defn neaten-templates [templates]
  (->> templates vals (map (fn [template] [(:name template) (:markup template)])) (into {})))

(defn path-with-children [path] (concat [:children] (interleave path (repeat :children))))

(defn port-taken? [port next-fn]
  (let [tester (.createServer net)]
    (.. tester
        (once
         "error"
         (fn [err]
           (if (not= (.-code err) "EADDRINUSE") (next-fn err false) (next-fn nil true))))
        (once
         "listening"
         (fn [] (.. tester (once "close" (fn [] (next-fn nil false))) (close))))
        (listen port))))

(defn pick-port! [port next-fn]
  (port-taken?
   port
   (fn [err taken?]
     (if (some? err)
       (do (.error js/console err) (.exit js/process 1))
       (if taken?
         (do (println (<< "port ~{port} is in use.")) (pick-port! (inc port) next-fn))
         (next-fn port))))))

(defn specified-port! []
  (let [raw js/process.env.port] (if (some? raw) (js/parseInt raw) nil)))

(defn use-string-keys [x] (->> x (map (fn [[k v]] [(name k) v])) (into {})))
