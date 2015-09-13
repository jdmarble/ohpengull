(ns ^:figwheel-always ohpengull.core
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.core.async :refer [<!]]
    [ohpengull.util :refer [http-get]]
    [schema.core :include-macros true]
    [ohpengull.schema.common :as c]
    [plumbing.core :refer-macros (defnk)]
    [ohpengull.buffers]
    [ohpengull.draw]
    [ohpengull.programs]
    [ohpengull.schema.gltf :as gltf]))

(defnk load-gltf :- gltf/Root
  [gltf-uri :- c/Uri]
  (go
    (-> gltf-uri
        http-get
        <!
        gltf/json->edn)))

(def default-renderer
  "A graph specifying a computation that outputs a sequence of WebGL draw calls."
  {:gltf load-gltf
   :buffers ohpengull.buffers/get-buffers
   :bufferViews ohpengull.buffers/create-buffer-views
   :shaders ohpengull.programs/compile-shaders
   :shader-sources ohpengull.programs/get-shader-sources
   :programs ohpengull.programs/link-programs
   :draw-calls ohpengull.draw/make-draw-calls
   :result ohpengull.draw/execute-draw-calls!})
