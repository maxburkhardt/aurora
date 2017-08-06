(ns hologram.core
  (:gen-class)
  (:require [clj-http.client :as client]
            [cheshire.core :refer :all]
            [clojure.string :as string]
            [cemerick.bandalore :as sqs]
            [outpace.config :refer [defconfig]]))

;; Set up configuration variables
(defconfig ^:required aurora-api-key)
(defconfig ^:required aurora-url)
(defconfig ^:required aws-client-id)
(defconfig ^:required aws-client-secret)
(defconfig ^:required sqs-queue-url)

;; NETWORK UTILITIES
(defn req [verb path & [body] ]
  (verb (str aurora-url "/api/v1/" aurora-api-key "/" path)
        {:form-params body
         :content-type :json}))

(def q sqs-queue-url)
(def client (sqs/create-client aws-client-id aws-client-secret))

(defn fetch-event []
  (first (map (sqs/deleting-consumer client (comp read-string :body))
              (sqs/receive client q :limit 1 :wait-time-seconds 20))))

(defn power [state]
  (req client/put "state" {:on {:value (not (not state))}}))

(defn set-effect [effect-name]
  (req client/put "effects/select"
       {:select effect-name}))

(defn layout []
  (into {} (map (fn [{id "panelId"
                      x "x"
                      y "y"}] {id [x y]})
    (get (parse-string
      (get (req client/get "panelLayout/layout") :body))
      "positionData"))))

;; helper to turn a map into an array of arrays
;; useful if you want to do any sorting
(defn order-map [data]
  (reduce-kv (fn [init k v]
               (conj init [k v]))
             []
             data))

(defn x-desc [layout-data]
  (sort (fn [[a-id, [a-x, a-y]] [b-id, [b-x, b-y]]] (> a-x b-x)) (order-map layout-data)))

(defn y-desc [layout-data]
  (sort (fn [[a-id, [a-y, a-y]] [b-id, [b-y, b-y]]] (> a-y b-y)) (order-map layout-data)))

;; coordinate: 0 for x, 1 for y
(defn coord-range [panels coordinate]
  [(nth (last (first panels)) coordinate) (nth (last (last panels)) coordinate)])

(defn generate-anim [color-fn panels]
  (str (count panels) " " (string/join " " (map color-fn panels))))

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

(defn map-colors [layout]
  (static-glimpse (generate-anim
                    (fn [[panel-id [x y]]] (str panel-id " 1 "
                                                ;; redness (corresponds to y)
                                                (let [y-range (coord-range (y-desc layout) 1)]
                                                  (int (* 255 (/ (- y (nth y-range 1)) (- (nth y-range 0) (nth y-range 1)))))
                                                  )
                                                " 0 "
                                                ;; blueness (corresponds to x)
                                                (let [x-range (coord-range (x-desc layout) 0)]
                                                  (int (* 255 (/ (- x (nth x-range 1)) (- (nth x-range 0) (nth x-range 1)))))
                                                  )
                                                " 0 20"))
                    layout)))

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
