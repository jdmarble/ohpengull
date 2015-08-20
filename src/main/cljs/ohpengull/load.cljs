(ns ohpengull.load
  (:require
    [ohpengull.schema.webgl :as webgl]
    [ohpengull.schema.gltf :as gltf]
    [schema.core :as s :include-macros true]
    [plumbing.core :refer-macros (defnk)]
    [cljs-webgl.shaders :as shaders]
    [ohpengull.util :as util]))

(defnk load-shaders :- {s/Str webgl/Shader}
  [gl :- webgl/Context
   shader-sources :- {s/Str s/Str}
   [:gltf shaders] :- gltf/Root]
  (util/map-vals
    shaders
    (fn [shader-desc]
      (shaders/create-shader gl
                             (:type shader-desc)
                             (get shader-sources (:uri shader-desc))))))

(defnk load-programs :- {s/Str {:program webgl/Program
                                :attribute-locations {s/Str s/Int}}}
  [gl :- webgl/Context
   shaders :- {s/Str webgl/Shader}
   [:gltf programs] :- gltf/Root]
  (util/map-vals
    programs
    (fn [program-desc]
      (let [program (shaders/create-program gl
                                            (get shaders (:vertex-shader program-desc))
                                            (get shaders (:fragment-shader program-desc)))]
        {:program program
         :attribute-locations
         (into {} (for [attrib (:attributes program-desc)]
                    [attrib (shaders/get-attrib-location gl program attrib)]))}))))