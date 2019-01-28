
(ns app.util )

(defn path-with-children [path] (concat [:children] (interleave path (repeat :children))))
