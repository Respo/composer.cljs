
(ns composer.updater
  (:require [composer.updater.session :as session]
            [composer.updater.user :as user]
            [composer.updater.router :as router]
            [composer.updater.template :as template]
            [composer.updater.settings :as settings]
            [composer.schema :as schema]
            [respo-message.updater :refer [update-messages]]))

(defn updater [db op op-data sid op-id op-time]
  (let [f (case op
            :session/connect session/connect
            :session/disconnect session/disconnect
            :session/remove-message session/remove-message
            :session/copy-markup session/copy-markup
            :session/paste-markup session/paste-markup
            :session/focus-to session/focus-to
            :session/toggle-shadows session/toggle-shadows
            :user/log-in user/log-in
            :user/sign-up user/sign-up
            :user/log-out user/log-out
            :router/change router/change
            :router/move-append router/move-append
            :router/move-prepend router/move-prepend
            :router/move-after router/move-after
            :router/move-before router/move-before
            :template/create template/create-template
            :template/rename template/rename-template
            :template/remove template/remove-template
            :template/create-mock template/create-mock
            :template/update-mock template/update-mock
            :template/remove-mock template/remove-mock
            :template/rename-mock template/rename-mock
            :template/use-mock template/use-mock
            :template/append-markup template/append-markup
            :template/prepend-markup template/prepend-markup
            :template/remove-markup template/remove-markup
            :template/after-markup template/after-markup
            :template/before-markup template/before-markup
            :template/wrap-markup template/wrap-markup
            :template/spread-markup template/spread-markup
            :template/node-type template/set-node-type
            :template/node-layout template/set-node-layout
            :template/set-node-style template/set-node-style
            :template/node-preset template/update-node-preset
            :template/node-style template/update-node-style
            :template/node-props template/update-node-props
            :template/node-event template/update-node-event
            :template/node-attrs template/update-node-attrs
            :template/set-preview-sizes template/set-preview-sizes
            :template/mark-saved template/mark-saved
            :settings/add-color settings/add-color
            :settings/remove-color settings/remove-color
            :settings/update-color settings/update-color
            (do (println "Unknown op:" op) identity))]
    (f db op-data sid op-id op-time)))
