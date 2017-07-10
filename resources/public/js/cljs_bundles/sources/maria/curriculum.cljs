(ns maria.curriculum
  (:require [clojure.set :as set]))

(def modules-by-path {"intro" "6121050c023ad640688e1d0220c0f50d"})
(def modules-by-id (set/map-invert modules-by-path))

(def modules-owner {:username  "modules"
                    :maria-url "/modules"})