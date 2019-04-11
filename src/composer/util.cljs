
(ns composer.util )

(defn path-includes? [xs ys]
  (if (empty? ys)
    true
    (if (empty? xs) false (if (= (first xs) (first ys)) (recur (rest xs) (rest ys)) false))))

(defn filter-path-set [paths p]
  (->> paths (filter (fn [path] (path-includes? path p))) (set)))

(defn neaten-templates [templates]
  (->> templates vals (map (fn [template] [(:name template) (:markup template)])) (into {})))

(defn path-with-children [path] (concat [:children] (interleave path (repeat :children))))

(defn use-string-keys [x] (->> x (map (fn [[k v]] [(name k) v])) (into {})))
