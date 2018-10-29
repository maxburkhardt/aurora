(defproject hologram "0.1.6"
  :description "Consumes Aurora animation data from SQS and sends it to a panel array"
  :url "https://maxb.fm"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clj-http "3.7.0"]
                 [cheshire "5.8.0"]
                 [com.cemerick/bandalore "0.0.6"]
                 [com.evocomputing/colors "1.0.4"]
                 [com.outpace/config "0.10.0"]]
  :main hologram.core
  :aot :all)
