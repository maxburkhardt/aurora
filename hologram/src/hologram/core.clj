(ns hologram.core
  (:gen-class)
  (:require [clj-http.client :as client]
            [cheshire.core :refer :all]
            [cemerick.bandalore :as sqs]
            [outpace.config :refer [defconfig]])
  (:use [hologram.geometry]
        [hologram.util]
        [hologram.animations]))

;; Set up configuration variables
(defconfig ^:required aws-client-id)
(defconfig ^:required aws-client-secret)
(defconfig ^:required sqs-queue-url)
(defconfig ^:required default-effect)

(def client (sqs/create-client aws-client-id aws-client-secret))

(defn fetch-event []
  (first (map (sqs/deleting-consumer client
                                     (comp (fn [payload] (parse-string payload true)) :body))
              (sqs/receive client sqs-queue-url :limit 1 :wait-time-seconds 20))))

(defn glimpse [animType palette direction]
  (req client/put "effects"
       {:write (get-display-command animType palette direction)}))

(defn handle-operation [operation-name]
  (case operation-name
    "power-toggle" (power (not (get-power-state)))))

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
          (if (contains? event :operation)
            (handle-operation (:operation event))
            (if (contains? event :saved-effect)
              (set-effect (:saved-effect event))
              ;; else, some raw animation data has been supplied
              (let [prior-state (get-state-to-restore)]
                (glimpse (:type event) (:palette event) (:direction event))
                (Thread/sleep 5000)
                (if (and (:power prior-state) (not (nil? (:effect prior-state))))
                  (set-effect (:effect prior-state))
                  (power false))
              ))))))))
