(ns hologram.util
  (:require [clj-http.client :as client]
            [cheshire.core :refer :all]
            [clojure.string :as string]
            [outpace.config :refer [defconfig]]))

;; Set up configuration variables
(defconfig ^:required aurora-api-key)
(defconfig ^:required aurora-url)

(defn req
  "Make a request to the Aurora API and return its results"
  [verb path & [body]]
  (verb (str aurora-url "/api/v1/" aurora-api-key "/" path)
        {:form-params body
         :content-type :json}))

(defn power
  "Turn the Aurora on or off by passing a truthy or falsey value"
  [state]
  (req client/put "state" {:on {:value (not (not state))}}))

(defn set-effect
  "Switch to an effect that is saved on the Aurora."
  [effect-name]
  (req client/put "effects/select"
       {:select effect-name}))

(defn generate-anim
  "Generate a static animation string, given details for each panel."
  [color-fn panels]
  (str (count panels) " " (string/join " " (map color-fn panels))))
