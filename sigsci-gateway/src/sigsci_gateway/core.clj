(ns sigsci-gateway.core
  (:gen-class)
  (:require [uswitch.lambada.core :refer [deflambdafn]]
            [cheshire.core :refer :all]
            [clojure.java.io :as io]
            [cemerick.bandalore :as sqs]))

(def q "https://sqs.us-west-2.amazonaws.com/684104360050/security-aurora")
(def client (sqs/create-client))

(defn explode [colors]
  {:type :explode
   :palette colors})

(defn handle-flag [payload]
  (sqs/send
    client
    q
    (generate-string
      (explode
           [
            {
             :hue 0
             :saturation 0
             :brightness 100
             }
            {
             :hue 0
             :saturation 100
             :brightness 100
             }
            {
             :hue 0
             :saturation 0
             :brightness 100
             }
            ]))))

(defn handle-alert [{alert-created "created"
                     alert-type "type"
                     alert-payload "payload"}]
  (case alert-type
    "flag" (handle-flag alert-payload)
    {:response :unhandled-alert-type}))

(deflambdafn sigsci.handler [in out ctx]
  "Handle incoming Signal Sciences webhooks"
  (let [event (parse-stream (io/reader in))
        res (handle-alert event)]
    (with-open [w (io/writer out)]
      (generate-stream res w))))
