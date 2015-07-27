(defproject ohpengull "0.0.1-SNAPSHOT"
  :description "Declarative WebGL that uses glTF like a virtual DOM."
  :url "https://github.com/jdmarble/ohpengull"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2850"]
                 [cljs-webgl "0.1.5-SNAPSHOT"]]

  :plugins [[lein-cljsbuild "1.0.4"]]

  :profiles {:dev {:dependencies [[figwheel "0.2.5-SNAPSHOT"]]

                   :plugins [[lein-figwheel "0.2.5-SNAPSHOT"]]

                   :resource-paths ["src/dev/resources", "target/dev/resources"]

                   :cljsbuild {
                               :builds [{:id "dev"
                                         :source-paths ["src/main/cljs" "src/dev/cljs"]
                                         :compiler {:output-to "target/dev/resources/public/js/compiled/ohpengull.js"
                                                    :output-dir "target/dev/resources/public/js/compiled/out"
                                                    :optimizations :none
                                                    :main ohpengull.dev
                                                    :asset-path "js/compiled/out"
                                                    :source-map true
                                                    :source-map-timestamp true
                                                    :cache-analysis true }}]}

                   :figwheel {:css-dirs ["src/dev/resources/public/css"]
                              :server-logfile "target/figwheel.log"}}})
