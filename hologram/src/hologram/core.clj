(ns hologram.core
  (:gen-class)
  (:require [clj-http.client :as client]
            [cheshire.core :refer :all]
            [cemerick.bandalore :as sqs]
            [outpace.config :refer [defconfig]])
  (:use [hologram.geometry]
        [hologram.util]))

;; Set up configuration variables
(defconfig ^:required aws-client-id)
(defconfig ^:required aws-client-secret)
(defconfig ^:required sqs-queue-url)

(def client (sqs/create-client aws-client-id aws-client-secret))

(defn fetch-event []
  (first (map (sqs/deleting-consumer client (comp read-string :body))
              (sqs/receive client sqs-queue-url :limit 1 :wait-time-seconds 20))))

(defn brand [] (set-effect "Brand Descend"))

(defn glimpse [{animType :type palette :palette}]
  (req client/put "effects"
       {:write {
                :command :display
                :animType animType
                :colorType :HSB
                :palette palette
                :transTime {:maxValue 10 :minValue 10}
                :delayTime {:maxValue 10 :minValue 10}
                :explodeFactor 0.5
                :direction :outwards
                :loop false
               }
       }))

(defn static-glimpse [animData]
  (req client/put "effects"
       {:write {
                :command :display
                :animType :static
                :animData animData
                :loop false
                }}))

(defn -main
  "hologram activate"
  [& args]
  (let [layout-cache (layout)]
    (while true
      (let [event (fetch-event)]
        (if (not-empty event)
          (do
            (glimpse event)
            (Thread/sleep 5000)
            (brand)
          ))))
  )
)
