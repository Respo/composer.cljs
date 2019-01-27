
(ns app.updater
  (:require [app.updater.session :as session]
            [app.updater.user :as user]
            [app.updater.router :as router]
            [app.updater.template :as template]
            [app.schema :as schema]
            [respo-message.updater :refer [update-messages]]))

(defn updater [db op op-data sid op-id op-time]
  (let [f (case op
            :session/connect session/connect
            :session/disconnect session/disconnect
            :session/remove-message session/remove-message
            :user/log-in user/log-in
            :user/sign-up user/sign-up
            :user/log-out user/log-out
            :router/change router/change
            :router/set-pointer router/set-pointer
            :router/set-tab router/set-tab
            :router/set-focused-mock router/set-focused-mock
            :router/set-focused-path router/set-focused-path
            :template/create template/create-template
            :template/rename template/rename-template
            :template/remove template/remove-template
            :template/create-mock template/create-mock
            :template/update-mock template/update-mock
            :template/remove-mock template/remove-mock
            :template/rename-mock template/rename-mock
            :template/use-mock template/use-mock
            :template/append-markup template/append-markup
            (do (println "Unknown op:" op) identity))]
    (f db op-data sid op-id op-time)))
