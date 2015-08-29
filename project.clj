(defproject ohpengull "0.0.1-SNAPSHOT"
  :description "Declarative WebGL that uses glTF like a virtual DOM."
  :url "https://github.com/jdmarble/ohpengull"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "0.0-3165"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [prismatic/plumbing "0.4.4"]
                 [prismatic/schema "0.4.4"]
                 [cljs-webgl "0.1.5-SNAPSHOT"]
                 [cljs-http "0.1.37"]]

  :plugins [[lein-cljsbuild "1.0.6"]]

  :target-path "target/%s/"
  :clean-targets [:target-path]

  :profiles
  {:dev {:dependencies [[figwheel "0.3.7"]]

         :plugins [[lein-figwheel "0.3.7"]]

         :resource-paths ["src/dev/resources", "target/dev/resources"]

         :cljsbuild {:builds {:dev {:source-paths ["src/main/cljs" "src/test/cljs" "src/dev/cljs"]
                                    :compiler {:output-to "target/dev/resources/public/js/compiled/ohpengull.js"
                                               :output-dir "target/dev/resources/public/js/compiled/out"
                                               :optimizations :none
                                               :main ohpengull.dev
                                               :asset-path "js/compiled/out"
                                               :source-map true
                                               :source-map-timestamp true
                                               :cache-analysis true}}}}

         :figwheel {:css-dirs ["src/dev/resources/public/css"]
                    :server-logfile "target/figwheel.log"
                    :nrepl-port 7888}}

   :test {:cljsbuild {:builds {:test {:source-paths ["src/main/cljs" "src/test/cljs"]
                                      :compiler {:output-to "target/test/resources/public/js/compiled/ohpengull.js"
                                                 :optimizations :whitespace
                                                 :pretty-print true}}}
                      :test-commands {"unit" ["phantomjs"
                                              "src/test/resources/test.js"
                                              "src/test/resources/test.html"]}}}})
