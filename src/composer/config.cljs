
(ns composer.config )

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
  {:port 6011,
   :title "Composer App",
   :icon "http://cdn.tiye.me/logo/respo.png",
   :dev-ui "http://localhost:8100/main.css",
   :release-ui "http://cdn.tiye.me/favored-fonts/main.css",
   :cdn-url "http://cdn.tiye.me/composer-app/",
   :cdn-folder "tiye.me:cdn/composer-app",
   :upload-folder "tiye.me:repo/Respo/composer-app/",
   :server-folder "tiye.me:servers/composer-app",
   :theme "#eeeeff",
   :storage-key "composer-app",
   :storage-file "composer.edn"})
