(ns hologram.util
  (:require [clj-http.client :as client]
            [cheshire.core :refer :all]
            [clojure.string :as string]
            [com.evocomputing.colors :refer :all]
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

(defn get-power-state
  "Return a boolean representing whether the power is on"
  []
  (:value (:on (:state (parse-string (:body (req client/get "")) true)))))

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

(defn effect-set?
  "Returns true if an effect has been set, false if an internal mode is being used (Static or Dynamic)"
  [effect-string]
  (not (and
    (= \* (get effect-string 0))
    (= \* (get effect-string (- (count effect-string) 1))))))

(defn get-state-to-restore
  "Get the power and effect state of the aurora for restoration after a glimpse."
  []
  (let [full-state (parse-string (:body (req client/get "/")))
        effect-string (get-in full-state ["effects" "select"])]
    {:power (get-in full-state ["state" "on" "value"])
     :effect (if (effect-set? effect-string) effect-string nil)}))

(defn hsb-to-rgb
  "Convert an HSB map into an RGB array that the static effect generator can take."
  [hsb]
  (let [{h :hue s :saturation l :brightness} hsb]
    (take 3 (:rgba (create-color {:h h :s s :l l})))))
