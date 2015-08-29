(ns ohpengull.dev
  (:require
    [cljs-webgl.context :as context]
    [cljs-webgl.typed-arrays :as ta]
    [ohpengull.core]
    [ohpengull.draw]
    [plumbing.graph-async :as graph-async]
    [figwheel.client :as fw]))

(defn my-params []
  {:gl (context/get-context (.getElementById js/document "canvas"))
   :buffers {"redtriangle-buffer" (.-buffer (ta/int8 [1 1 0
                                                      -1 1 0
                                                      1 -1 0
                                                      0 1 2]))}
   :gltf-uri "redtriangle.gltf"})

(defn my-render []
  (let [calc (graph-async/async-compile ohpengull.core/default-renderer)]
    (calc (my-params))))

(fw/start {:on-jsload my-render})

(my-render)