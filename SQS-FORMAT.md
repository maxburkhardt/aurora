# Sending SQS Messages to Control the Aurora

Although the core of this system is built with Clojure, the Aurora is designed
to be controllable with any stack you want. The core of its communication is an
AWS SQS stream that contains JSON messages, so anything that can output JSON
and make HTTP requests can control the panels. The JSON schema for SQS messages
is as follows:

### Keys
* `operation`: A operation to perform. Currently, the only supported value is
  `"power-toggle"`. Supplying this key will supersede any other keys provided
  in the message.
* `saved-effect`: An Aurora Saved Effect to set. Examples: `"Northern Lights"`,
  `"Forest"`, etc.
* `type`: An animation type to send to the panels. Must be combined with a
  `palette` key. The options are as follows:
  * `flow`: Wipe a color across the aurora
  * `explode`: Color emanating from the center of the Aurora
  * `wheel`: Bars of color moving across the Aurora
  * `highlight`: Randomly set panels to the palette colors, weighted with a probability
  * `random`: Randomly set panels to the palette colors
  * `fade`: Cycle through the palette, fading all panels between colors at once
* `palette`: a list of JavaScript objects containing HSB colors.

### Examples
Setting a explosion that uses white, then red, then white:
```
{
    "palette": [
        {
            "brightness": 100,
            "hue": 0,
            "saturation": 0
        },
        {
            "brightness": 100,
            "hue": 0,
            "saturation": 100
        },
        {
            "brightness": 100,
            "hue": 0,
            "saturation": 0
        }
    ],
    "type": "explode"
}
```

Toggling the power state:
```
{
	  "operation": "power-toggle"
}
```

As soon as one of these events is available on the SQS queue, it will be pulled
off by Hologram and sent to the Aurora to display.
