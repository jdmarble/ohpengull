(ns ohpengull.draw
  (:require
    [cljs-webgl.buffers]
    [ohpengull.schema.webgl :as webgl]
    [ohpengull.schema.gltf :as gltf]
    [schema.core :as s :include-macros true]
    [plumbing.core :refer-macros (defnk)]
    [ohpengull.util :refer [mapply]]
    [ohpengull.schema.common :as c]))

(defnk make-draw-calls :- [webgl/DrawCall]
  [programs :- {s/Str {:program webgl/Program
                       :attribute-locations {s/Str s/Int}}}
   bufferViews :- {s/Str webgl/Buffer}
   [:gltf meshes accessors materials techniques] :- gltf/Root]
  (for [mesh (vals meshes)
        primitive (:primitives mesh)
        :let [element-accessor (get accessors (:indices primitive))
              instance-technique (:instanceTechnique (get materials (:material primitive)))
              technique (get techniques (:technique instance-technique))
              instance-program (get-in technique [:passes (:pass technique) :instanceProgram])
              program (get programs (:program instance-program))]]
    {:shader (:program program)
     :draw-mode (:mode primitive)
     :count (:count element-accessor)
     :attributes (for [[attribute-name parameter-name] (:attributes instance-program)
                       :let [semantic (get-in technique [:parameters parameter-name :semantic])
                             accessor (get accessors (get-in primitive [:attributes semantic]))]]
                   {:buffer (get bufferViews (:bufferView accessor))
                    :location (get-in program [:attribute-locations attribute-name])
                    :components-per-vertex (get gltf/components-per-vertex (:type accessor))
                    :type (:componentType accessor)})
     :element-array {:buffer (get bufferViews (:bufferView element-accessor))
                     :type (:componentType element-accessor)
                     :offset (:byteOffset element-accessor)
                     :count (:count element-accessor)}}))

(defnk execute-draw-calls! :- c/Nil
  "Renders all items in the `draw-calls` sequence to the WebGL context."
  [gl :- webgl/Context
   draw-calls :- [webgl/DrawCall]]
  (doseq [draw-call draw-calls]
    (mapply (partial cljs-webgl.buffers/draw! gl) draw-call)))

