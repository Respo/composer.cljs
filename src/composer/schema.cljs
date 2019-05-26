
(ns composer.schema )

(def color {:id nil, :name "", :color ""})

(def color-group {:id nil, :name "", :colors (do color {})})

(def markup
  {:type :box,
   :props {},
   :attrs {},
   :layout nil,
   :presets #{},
   :style {},
   :event {},
   :children {}})

(def mock {:id nil, :data nil, :state nil, :name nil})

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
   :height 400,
   :sort-key nil})

(def user {:name nil, :id nil, :nickname nil, :avatar nil, :password nil})

(def database
  {:sessions (do session {}),
   :users (do user {}),
   :templates (do template {}),
   :saved-templates {},
   :settings {:colors-groups (do color-group {})}})

(def node-layouts
  [{:value :row, :display "Row", :kind :row}
   {:value :row-middle, :display "Row Middle", :kind :row}
   {:value :row-parted, :display "Row Parted", :kind :row}
   {:value :column, :display "Column", :kind :column}
   {:value :column-parted, :display "Column Parted", :kind :column}
   {:value :center, :display "Center", :kind :center}
   {:value :row-center, :display "Row Center", :kind :center}])

(def node-types
  {:element [:text :button :icon :input :link :image :element],
   :layout [:box :space :divider :popup],
   :control [:template :some :list :case :comment],
   :advanced [:markdown :function],
   :devtool [:inspect]})

(def props-hints
  {:box ["param"],
   :space ["width" "height"],
   :divider ["kind" "color"],
   :text ["value" "data"],
   :some ["value" "kind"],
   :button ["text" "param"],
   :link ["text" "href" "param"],
   :icon ["name" "color" "param"],
   :template ["name" "data" "state-key"],
   :list ["value"],
   :input ["value" "textarea" "param"],
   :inspect ["value"],
   :popup ["visible"],
   :case ["value" "options"],
   :element ["name"],
   :markdown ["text"],
   :image ["src" "mode" "width" "height"],
   :function ["name" "param"]})
