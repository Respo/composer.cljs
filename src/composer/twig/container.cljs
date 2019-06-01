
(ns composer.twig.container
  (:require [recollect.twig :refer [deftwig]]
            [composer.twig.user :refer [twig-user]]
            ["randomcolor" :as color]))

(deftwig
 twig-members
 (sessions users)
 (->> sessions
      (map (fn [[k session]] [k (get-in users [(:user-id session) :name])]))
      (into {})))

(deftwig
 twig-container
 (db session records)
 (let [logged-in? (some? (:user-id session))
       router (:router session)
       base-data {:logged-in? logged-in?, :session session, :reel-length (count records)}
       user (get-in db [:users (:user-id session)])]
   (merge
    base-data
    (if logged-in?
      {:user (twig-user user),
       :router (assoc
                router
                :data
                (case (:name router)
                  :home {}
                  :preview {}
                  :profile (twig-members (:sessions db) (:users db))
                  :settings (:data router)
                  {})),
       :count (count (:sessions db)),
       :color (color/randomColor),
       :templates (:templates db),
       :templates-modified? (not (identical? (:templates db) (:saved-templates db))),
       :settings (:settings db),
       :focuses (->> (:sessions db)
                     (filter (fn [[k s]] (not= k (:id session))))
                     (map (fn [[k s]] [k {:user (twig-user user), :focus (:focus-to s)}]))
                     (into {}))}
      nil))))
