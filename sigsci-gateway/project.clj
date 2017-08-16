(defproject sigsci-gateway "0.1.1"
  :description "AWS Lambda gateway to turn SigSci webhooks into aurora colors"
  :url "https://maxb.fm"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.json "0.2.6"]
                 [com.amazonaws/aws-lambda-java-core "1.1.0"]
                 [cheshire "5.7.1"]
                 [uswitch/lambada "0.1.2"]
                 [com.cemerick/bandalore "0.0.6"]]
  :java-source-paths ["src/java"]
  :aot :all)
