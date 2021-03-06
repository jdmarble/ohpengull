(ns ohpengull.programs
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.core.async :refer [<!]]
    [cljs-http.client :as http]
    [ohpengull.schema.webgl :as webgl]
    [ohpengull.schema.gltf :as gltf]
    [schema.core :as s :include-macros true]
    [plumbing.core :refer-macros (defnk for-map)]
    [cljs-webgl.shaders :as shaders]
    [ohpengull.util :as util]))

(defnk get-shader-sources :- {s/Str s/Str}
  [[:gltf shaders]]
  (go
    (for-map [{:keys [uri]} (vals shaders)]
      uri
      ; TODO: error handling
      (:body (<! (http/get uri))))))

(defnk compile-shaders :- {s/Str webgl/Shader}
  [gl :- webgl/Context
   shader-sources :- {s/Str s/Str}
   [:gltf shaders] :- gltf/Root]
  (util/map-vals
    shaders
    (fn [shader-desc]
      (shaders/create-shader gl
                             (:type shader-desc)
                             (get shader-sources (:uri shader-desc))))))

(defnk link-programs :- {s/Str {:program webgl/Program
                                :attribute-locations {s/Str s/Int}}}
  [gl :- webgl/Context
   shaders :- {s/Str webgl/Shader}
   [:gltf programs] :- gltf/Root]
  (util/map-vals
    programs
    (fn [program-desc]
      (let [program (shaders/create-program gl
                                            (get shaders (:vertexShader program-desc))
                                            (get shaders (:fragmentShader program-desc)))]
        {:program program
         :attribute-locations
         (into {} (for [attrib (:attributes program-desc)]
                    [attrib (shaders/get-attrib-location gl program attrib)]))}))))