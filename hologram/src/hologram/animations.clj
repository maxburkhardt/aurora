(ns hologram.animations)

;; Wipe a color across the aurora
(defn get-flow [palette]
  {
   :animType :flow
   :palette palette
   :flowFactor 2.5
   :direction :down
   })

;; Color emanating from the center of the Aurora
(defn get-explode [palette]
  {
   :animType :explode
   :palette palette
   :explodeFactor 0.5
   :direction :outwards
  })

;; Bars of color moving across the Aurora
(defn get-wheel [palette]
  {
   :animType :wheel
   :palette palette
   :windowSize 2
   :direction :right
   })

;; Randomly set panels to the palette colors, weighted with a probability
(defn get-highlight [palette]
  {
   :animType :highlight
   :palette (map (fn [color] (merge color {:probability 80})) palette)
   :brightnessRange {:maxValue 100 :minValue 25}
   })

;; Randomly set panels to the palette colors
(defn get-random [palette]
  {
   :animType :random
   :palette palette
   :brightnessRange {:maxValue 100 :minValue 25}
   })

;; Cycle through the palette, fading all panels between colors at once
(defn get-fade [palette]
  {
   :animType :fade
   :palette palette
   :brightnessRange {:maxValue 100 :minValue 25}
   })

(defn get-display-command [animType palette]
  (merge
    {
     :command :display
     :colorType :HSB
     :transTime {:maxValue 10 :minValue 10}
     :delayTime {:maxValue 10 :minValue 10}
     :loop false
    }
    ((case animType
    :explode get-explode
    :flow get-flow
    :wheel get-wheel
    :highlight get-highlight
    :random get-random
    :fade get-fade) palette)))
