
(ns composer.schema )

(def color {:id nil, :name "", :color "", :group nil})

(def markup
  {:type :box, :props {}, :attrs {}, :layout nil, :presets #{}, :style {}, :children {}})

(def mock {:id nil, :data nil, :name nil})

(def router {:name nil, :title nil, :data {}, :router nil})

(def session
  {:user-id nil,
   :id nil,
   :nickname nil,
   :router (do router {:name :home, :data nil, :router nil}),
   :messages {},
   :copied-markup nil,
   :template-id nil,
   :focus-to {:template-id nil, :path [], :tab :editor, :mock-id []},
   :shadows? false})

(def template
  {:id nil,
   :name nil,
   :mocks (do mock {}),
   :mock-pointer nil,
   :markup (do markup nil),
   :width 400,
   :height 400})

(def user {:name nil, :id nil, :nickname nil, :avatar nil, :password nil})

(def database
  {:sessions (do session {}),
   :users (do user {}),
   :templates (do template {}),
   :saved-templates {},
   :settings {:colors {}}})
