
(ns app.config )

(def cdn?
  (cond
    (exists? js/window) false
    (exists? js/process) (= "true" js/process.env.cdn)
    :else false))

(def dev?
  (let [debug? (do ^boolean js/goog.DEBUG)]
    (if debug?
      (cond
        (exists? js/window) true
        (exists? js/process) (not= "true" js/process.env.release)
        :else true)
      false)))

(def site
  {:port 11017,
   :title "Deviser Respo",
   :icon "http://cdn.tiye.me/logo/erigeron.png",
   :dev-ui "http://localhost:8100/main.css",
   :release-ui "http://cdn.tiye.me/favored-fonts/main.css",
   :cdn-url "http://cdn.tiye.me/deviser-respo/",
   :cdn-folder "tiye.me:cdn/deviser-respo",
   :upload-folder "tiye.me:repo/Erigeron/deviser-respo/",
   :server-folder "tiye.me:servers/deviser-respo",
   :theme "#eeeeff",
   :storage-key "deviser-respo",
   :storage-file "storage.edn"})
