(ns maria.commands.which-key
  (:require [re-view.core :as v :refer [defview]]
            [re-db.d :as d]
            [lark.commands.registry :as registry]
            [lark.commands.exec :as exec]
            [clojure.set :as set]
            [maria.views.bottom-bar :as bottom-bar]
            [maria.util :as util]))

(defn show-namespace-commands [modifiers-down [namespace hints]]
  (let [row-height 24]
    [:.avoid-break.ph3.pv2
     {:style {:max-width 200}}
     [:.b.mb2 namespace]
     [:.flex.flex-column.bg-near-white.ph2

      (for [{:keys [display-name name bindings]} hints]
        [:.flex.items-center.ws-nowrap.pointer.hover-bg-near-white.br2
         {:on-mouse-down #(exec/exec-command-name name)
          :style         {:height row-height}}
         display-name
         [:.flex-auto]
         [:.dib.ph1.br2.pv05.gray
          (-> (registry/binding-string->vec (first bindings))
              (set)
              (set/difference modifiers-down)
              (registry/keyset-string))]])]]))

(defview show-commands
  {:view/state         exec/state
   :update             (fn [{:keys [view/state view/prev-state]}]
                         (let [{active? :which-key/active?
                                :keys   [modifiers-down]} @state]
                           (bottom-bar/add-bottom-bar! :eldoc/which-key (when active?
                                                                          (let [commands (seq (exec/keyset-commands modifiers-down (exec/get-context)))]
                                                                            [:.bg-white.sans-serif.relative
                                                                             [:.pb0.f-body.absolute.left-0.top-0.inline-flex.items-center.bg-white.b--light-gray.ph3
                                                                              {:style {:padding-top  10
                                                                                       :border-width 1
                                                                                       :border-style "solid solid none solid"
                                                                                       :height       30
                                                                                       :margin-top   -30}}

                                                                              (registry/keyset-string modifiers-down)
                                                                              util/space
                                                                              [:span.gray "-"]]
                                                                             [:.f7.hint-columns.pv2
                                                                              (if commands (->> commands
                                                                                                (group-by :display-namespace)
                                                                                                (map (partial show-namespace-commands modifiers-down)))
                                                                                           [:.gray.ph2 "No commands"])]])))))
   :view/did-mount     #(.update %)
   :view/should-update (constantly true)
   :view/did-update    #(.update %)}
  []
  nil)