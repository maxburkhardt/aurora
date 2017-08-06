(ns hologram.geometry
  (:require [clj-http.client :as client]
            [cheshire.core :refer :all])
  (:use [hologram.util]))

;; Functions related to reading the layout of the Aurora panels, and performing
;; operations that depend on that configuration

(defn layout
  "Query the aurora for its panel layout, and produce a map of panel ID -> x,y coordinates"
  []
  (into {} (map (fn [{id "panelId"
                      x "x"
                      y "y"}] {id [x y]})
    (get (parse-string
      (get (req client/get "panelLayout/layout") :body))
      "positionData"))))

(defn order-map
  "Transform a layout map (like one produced by `layout`) into an array of arrays,
  which is useful for sorting"
  [data]
  (reduce-kv (fn [init k v]
               (conj init [k v]))
             []
             data))

(defn x-desc
  "Sort a layout datastructure by X coordinate, descending."
  [layout-data]
  (sort (fn [[a-id, [a-x, a-y]] [b-id, [b-x, b-y]]] (> a-x b-x)) (order-map layout-data)))

(defn y-desc
  "Sort a layout datastructure by Y coordinate, descending."
  [layout-data]
  (sort (fn [[a-id, [a-y, a-y]] [b-id, [b-y, b-y]]] (> a-y b-y)) (order-map layout-data)))

(defn coord-range
  "Produce a list containing two values, the min and the max coordinate in one axis.
  Pass 0 as coordinate to get x-range, 1 as coordinate to get y-range."
  [panels coordinate]
  [(nth (last (first panels)) coordinate) (nth (last (last panels)) coordinate)])

(defn map-colors
  "Produce a animation string that maps a function across the aurora: the higher the
  y-coordinate, the redder you are, the higher the x-coordinate, the bluer you are."
  [layout]
  (generate-anim
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
    layout))
