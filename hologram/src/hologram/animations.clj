(ns hologram.animations
  (:use [hologram.geometry]
        [hologram.util]))

;; Wipe a color across the aurora
(defn get-flow [palette direction]
  {
   :animType :flow
   :palette palette
   :flowFactor 2.5
   :direction (if (nil? direction) :down direction)
   })

;; Color emanating from the center of the Aurora
(defn get-explode [palette direction]
  {
   :animType :explode
   :palette palette
   :explodeFactor 0.5
   :direction (if (nil? direction) :outwards direction)
  })

;; Bars of color moving across the Aurora
(defn get-wheel [palette direction]
  {
   :animType :wheel
   :palette palette
   :windowSize 2
   :direction (if (nil? direction) :right direction)
   })

;; Randomly set panels to the palette colors, weighted with a probability
(defn get-highlight [palette direction]
  {
   :animType :highlight
   :palette (map (fn [color] (merge color {:probability 80})) palette)
   :brightnessRange {:maxValue 100 :minValue 25}
   })

;; Randomly set panels to the palette colors
(defn get-random [palette direction]
  {
   :animType :random
   :palette palette
   :brightnessRange {:maxValue 100 :minValue 25}
   })

;; Cycle through the palette, fading all panels between colors at once
(defn get-fade [palette direction]
  {
   :animType :fade
   :palette palette
   :brightnessRange {:maxValue 100 :minValue 25}
   })

(defn get-display-command [layout animType palette direction values]
  (merge
    {
     :command :display
     :loop false
     }
    (case animType
      ;; Custom animations
      "progress-horizontal" {:animType :static :animData
                             (progress layout values "x" (hsb-to-rgb (first palette)))}
      "progress-vertical" {:animType :static :animData
                             (progress layout values "y" (hsb-to-rgb (first palette)))}
      ;; All the builtin animation types
      (merge
        {
         :colorType :HSB
         :transTime {:maxValue 10 :minValue 10}
         :delayTime {:maxValue 10 :minValue 10}
        }
        ((case animType
        "explode" get-explode
        "flow" get-flow
        "wheel" get-wheel
        "highlight" get-highlight
        "random" get-random
        "fade" get-fade) palette direction)))))
