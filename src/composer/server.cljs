
(ns composer.server
  (:require [composer.schema :as schema]
            [composer.updater :refer [updater]]
            [cljs.reader :refer [read-string]]
            [cumulo-reel.core :refer [reel-reducer refresh-reel reel-schema]]
            ["fs" :as fs]
            ["path" :as path]
            ["chalk" :as chalk]
            ["latest-version" :as latest-version]
            [composer.config :as config]
            [cumulo-util.file :refer [write-mildly! get-backup-path! merge-local-edn!]]
            [cumulo-util.core :refer [id! repeat! unix-time! delay!]]
            [composer.twig.container :refer [twig-container]]
            [recollect.diff :refer [diff-twig]]
            [recollect.twig :refer [render-twig]]
            [ws-edn.server :refer [wss-serve! wss-send! wss-each!]]
            [favored-edn.core :refer [write-edn]]
            [composer.util :refer [specified-port!]]
            [clojure.core.async :refer [go <!]]
            [cumulo-util.file :refer [chan-pick-port]]
            ["gaze" :as gaze]
            ["md5" :as md5])
  (:require-macros [clojure.core.strint :refer [<<]]))

(defonce *client-caches (atom {}))

(def storage-file (path/join js/process.env.PWD (:storage-file config/site)))

(defonce initial-db
  (merge-local-edn!
   schema/database
   storage-file
   (fn [found?] (if found? (println "Found local EDN data") (println "Found no data")))))

(defonce *reel
  (atom
   (merge
    reel-schema
    (let [db (assoc initial-db :saved-templates (:templates initial-db))]
      {:base db, :db db}))))

(defonce *reader-reel (atom @*reel))

(defonce *snapshot-md5 (atom nil))

(defn check-version! []
  (let [pkg (.parse js/JSON (fs/readFileSync (path/join js/__dirname "../package.json")))
        version (.-version pkg)]
    (-> (latest-version (.-name pkg))
        (.then
         (fn [npm-version]
           (if (= npm-version version)
             (println "Running latest version" version)
             (println
              (.yellow
               chalk
               (<<
                "New version ~{npm-version} available, current one is ~{version} . Please upgrade!\n\nyarn global add @respo/composer-app\n")))))))))

(defn persist-db! []
  (println "Saved file.")
  (let [file-content (write-edn
                      (-> (:db @*reel) (assoc :sessions {}) (dissoc :saved-templates)))]
    (write-mildly! storage-file file-content)
    (comment write-mildly! (get-backup-path!) file-content)))

(defn dispatch! [op op-data sid]
  (let [op-id (id!), op-time (unix-time!)]
    (if config/dev?
      (if (= op :snapshot/reset)
        (println "reset...")
        (println "Dispatch!" (str op) (pr-str op-data) sid)))
    (try
     (cond
       (= op :effect/persist) (do (dispatch! :template/mark-saved nil sid) (persist-db!))
       :else (reset! *reel (reel-reducer @*reel updater op op-data sid op-id op-time)))
     (catch js/Error error (js/console.error error)))))

(defn on-exit! [code]
  (persist-db!)
  (comment println "exit code is:" (pr-str code))
  (js/process.exit))

(defn on-file-change! [filepath]
  (let [content (fs/readFileSync filepath "utf8"), new-md5 (md5 content)]
    (when (not= new-md5 @*snapshot-md5)
      (println "File changed by comparing md5")
      (reset! *snapshot-md5 new-md5)
      (dispatch! :snapshot/reset (read-string content) nil))))

(defn sync-clients! [reel]
  (wss-each!
   (fn [sid socket]
     (let [db (:db reel)
           records (:records reel)
           session (get-in db [:sessions sid])
           old-store (or (get @*client-caches sid) nil)
           new-store (render-twig (twig-container db session records) old-store)
           changes (diff-twig old-store new-store {:key :id})]
       (when config/dev?
         (println (.gray chalk (str "Changes for " sid ": " (count changes)))))
       (if (not= changes [])
         (do
          (wss-send! sid {:kind :patch, :data changes})
          (swap! *client-caches assoc sid new-store)))))))

(defn render-loop! []
  (when (not (identical? @*reader-reel @*reel))
    (reset! *reader-reel @*reel)
    (sync-clients! @*reader-reel))
  (delay! 0.12 render-loop!))

(defn run-server! [port]
  (wss-serve!
   port
   {:on-open (fn [sid socket]
      (dispatch! :session/connect nil sid)
      (js/console.info "New client.")),
    :on-data (fn [sid action]
      (case (:kind action)
        :op (dispatch! (:op action) (:data action) sid)
        (println "unknown data" action))),
    :on-close (fn [sid event]
      (js/console.warn "Client closed!")
      (dispatch! :session/disconnect nil sid)),
    :on-error (fn [error] (.error js/console error))}))

(defn watch-snapshot! []
  (let [filepath storage-file]
    (reset! *snapshot-md5 (md5 (fs/readFileSync filepath "utf8")))
    (gaze
     filepath
     (fn [error watcher]
       (if (some? error)
         (js/console.error error)
         (.on ^js watcher "changed" (fn [_] (on-file-change! filepath))))))))

(defn main! []
  (println "Running mode:" (if config/dev? "dev" "release"))
  (go
   (let [port (<! (chan-pick-port (or (specified-port!) (:port config/site))))]
     (run-server! port)
     (let [link (.blue chalk (<< "http://composer.respo-mvc.org?port=~{port}"))]
       (println (<< "port ~{port} is ok, please edit on ~{link}")))))
  (render-loop!)
  (js/process.on "SIGINT" on-exit!)
  (comment repeat! 600 #(persist-db!))
  (check-version!)
  (watch-snapshot!))

(defn reload! []
  (println "Code updated.")
  (reset! *reel (refresh-reel @*reel initial-db updater))
  (sync-clients! @*reader-reel))
