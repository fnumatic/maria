(ns maria.cells.cell-list
  (:require [re-view.core :as v :refer [defview]]
            [maria.cells.code :as code]
            [clojure.core.match :refer-macros [match]]
            [cljsjs.markdown-it]
            [maria.cells.prose :as prose]
            [re-db.d :as d]
            [maria.cells.core :as Cell]))


(d/transact! [[:db/add :feature :in-place-eval true]])

(defn log-ret [label x]
  (prn label x)
  x)

(defview cell-list
  {:view/initial-state      (fn [{source :value}]
                              {:last-update source
                               :cells       (Cell/ensure-cells (Cell/from-source source))})
   :view/did-mount          (fn [{:keys [view/state]}]
                              (Cell/focus! (first (:cells @state)) :start))
   :view/will-receive-props (fn [{source :value
                                  state  :view/state}]
                              ;; normally, the source we are passed from above during editing
                              ;; is the source we last passed upwards in :will-receive-state.
                              ;; if these are different, the document has changed and we
                              ;; should re-parse from scratch.
                              (when (not= source (:last-confirmed-source @state))
                                (swap! state assoc :cells (Cell/ensure-cells (Cell/from-source source)))))
   :view/will-receive-state (fn [{:keys [view/state view/prev-state on-update]}]
                              (when-not (= (:cells @state) (:cells prev-state))
                                (let [updated-source (Cell/emit-many (:cells @state))]
                                  (v/swap-silently! state assoc :last-confirmed-source updated-source)
                                  (on-update updated-source))))}
  [{:keys [view/state]}]
  (let [{:keys [cells]} @state
        splice! #(swap! state assoc :cells (Cell/splice-by-id (:cells @state) %1 %2))]
    [:.w-100.flex-none.pv3
     (->> cells
          (map
            (fn [{:keys [id] :as cell}]
              (let [props {:id           id
                           :cell         cell
                           :cells        cells
                           :splice-self! #(:cells (swap! state update :cells Cell/splice-by-id id %))
                           :on-update    #(swap! state update :cells Cell/splice-by-id id [(Cell/->ProseCell id %)])}]
                (condp instance? cell
                  Cell/ProseCell (prose/prose-view (assoc props
                                                     :on-update #(splice! id [(Cell/->ProseCell id %)])))
                  Cell/CodeCell (code/code-view (assoc props
                                                  :on-ast #(splice! id [(Cell/->CodeCell id (:value %))]))))))))]))

