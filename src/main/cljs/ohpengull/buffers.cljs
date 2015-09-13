(ns ohpengull.buffers
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.core.async :refer [<!]]
    [ohpengull.util :refer [http-get-arraybuffer]]
    [ohpengull.schema.webgl :as webgl]
    [ohpengull.schema.gltf :as gltf]
    [ohpengull.schema.common :as c]
    [schema.core :as s :include-macros true]
    [plumbing.core :refer [map-vals] :refer-macros (fnk defnk for-map)]
    [cljs-webgl.buffers :as buffers]
    [cljs-webgl.constants.buffer-object :as buffer-object]))

(defnk get-buffers :- {s/Str s/Any}
  "Downloads binary buffers required by the gltf."
  [[:gltf buffers] :- gltf/Root]
  (go
    (for-map [{:keys [uri]} (vals buffers)
              :let [response (<! (http-get-arraybuffer uri))]]
             urie
             (do
               (js/console.log "response -> " (pr-str (type response)))
               (:body response)))))


(defnk create-buffer-views :- {s/Str c/TypedArray}
  [gl :- webgl/Context
   buffers :- {s/Str s/Any}
   [:gltf bufferViews] :- gltf/Root]
  (map-vals
    bufferViews
    (fnk [buffer byteOffset byteLength target]
      (buffers/create-buffer
        gl
        (js/Uint8Array. (get buffers buffer) byteOffset byteLength)
        target
        buffer-object/static-draw))))
