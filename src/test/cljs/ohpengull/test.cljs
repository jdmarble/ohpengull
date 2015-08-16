(ns ohpengull.test
  (:require [cljs.test :refer-macros [run-all-tests]]
            [schema.core]
            [ohpengull.draw-test]
            [ohpengull.util-test]))

(defn ^:export run []
  (enable-console-print!)
  (schema.core/set-fn-validation! true)                     ; Enable schema checking during tests
  (run-all-tests #"ohpengull.*-test")                       ; Run tests
  0                                                         ; Return exit code for success
  )
