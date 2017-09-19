(ns maria.frames.live-actions
  (:require [cljs.core.match :refer-macros [match]]
            [re-db.d :as d]
            [maria.persistence.local :as local]
            [maria.persistence.github :as github]
            [maria.commands.doc :as doc]))

(defn handle-message [_ message]
  (match message
         [:db/transactions txs] (d/transact! txs)
         [:db/copy-local from-id to-id] (local/init-storage to-id (d/get from-id :local))
         [:project/move-local! from-id to-id] (do
                                                (local/local-put! to-id (local/local-get from-id))
                                                (local/local-put! from-id nil)
                                                (doc/locals-remove! :local/recents from-id))))