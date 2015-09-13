(ns ohpengull.dev
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.core.async :refer [<!]]
    [cljs-webgl.context :as context]
    [cljs-webgl.typed-arrays :as ta]
    [ohpengull.core]
    [ohpengull.draw]
    [plumbing.graph-async :as graph-async]
    [figwheel.client :as fw]))

(defn my-params []
  {:gl (context/get-context (.getElementById js/document "canvas"))
   :gltf-uri "redtriangle.gltf"})

(defn my-render []
  (go
    (let [calc (graph-async/async-compile ohpengull.core/default-renderer)
          result (<! (calc (my-params)))]
      (js/console.log "calc result -> " (pr-str result)))))

(fw/start {:on-jsload my-render})

(my-render)