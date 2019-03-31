
(ns composer.util )

(defn neaten-templates [templates]
  (->> templates vals (map (fn [template] [(:name template) (:markup template)])) (into {})))

(defn path-with-children [path] (concat [:children] (interleave path (repeat :children))))

(defn use-string-keys [x] (->> x (map (fn [[k v]] [(name k) v])) (into {})))
