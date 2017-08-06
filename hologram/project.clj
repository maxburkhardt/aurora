(defproject hologram "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clj-http "3.6.1"]
                 [cheshire "5.7.1"]
                 [com.cemerick/bandalore "0.0.6"]
                 [com.outpace/config "0.10.0"]]
  :main hologram.core
  :aot :all)
