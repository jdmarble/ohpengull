(ns ohpengull.buffers
  (:require
    [ohpengull.schema.webgl :as webgl]
    [ohpengull.schema.gltf :as gltf]
    [ohpengull.schema.common :as c]
    [schema.core :as s :include-macros true]
    [plumbing.core :refer-macros (fnk defnk)]
    [ohpengull.util :as util]
    [cljs-webgl.buffers :as buffers]
    [cljs-webgl.constants.buffer-object :as buffer-object]))

(defnk create-buffer-views :- {s/Str c/TypedArray}
  [gl :- webgl/Context
   buffers :- {s/Str s/Any}
   [:gltf buffer-views] :- gltf/Root]
  (util/map-vals
    buffer-views
    (fnk [buffer byteOffset byteLength target]
      (buffers/create-buffer
        gl
        (js/Uint8Array. (get buffers buffer) byteOffset byteLength)
        target
        buffer-object/static-draw))))
