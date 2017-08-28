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

(defn back-to-normal [] (set-effect default-effect))

(defn glimpse [animType palette]
  (req client/put "effects"
       {:write (get-display-command animType palette)}))

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
              (do
                (glimpse (:type event) (:palette event))
                (Thread/sleep 5000)
                (back-to-normal)
              ))))))))
