(ns ohpengull.draw
  (:require
    [ohpengull.schema.webgl :as webgl]
    [ohpengull.schema.gltf :as gltf]
    [schema.core :as s :include-macros true]
    [plumbing.core :refer-macros (defnk)]
    [ohpengull.util :refer [mapply inline-in]]
    [ohpengull.schema.common :as c]))

(def DrawCall
  {:shader webgl/Program
   :draw-mode webgl/DrawMode
   :count s/Int

   :attributes [{:buffer webgl/Buffer
                 :location s/Int
                 :components-per-vertex s/Int
                 :type webgl/DataType}]

   (s/optional-key :element-array) {:buffer webgl/Buffer
                                    :type webgl/DataType
                                    :offset s/Int}})

(defnk make-draw-calls :- [DrawCall]
  [programs :- {s/Str {:program webgl/Program
                       :attribute-locations {s/Str s/Int}}}
   buffer-views :- {s/Str webgl/Buffer}
   [:gltf meshes accessors materials techniques] :- gltf/Root]
  (for [mesh (vals meshes)
        primitive (:primitives mesh)
        :let [element-accessor (get accessors (:indices primitive))
              instance-technique (:instance-technique (get materials (:material primitive)))
              technique (get techniques (:technique instance-technique))
              instance-program (get-in technique [:passes (:pass technique) :instance-program])
              program (get programs (:program instance-program))]]
    {:shader (:program program)
     :draw-mode (:primitive primitive)
     :count (:count element-accessor)
     :attributes (for [[attribute-name parameter-name] (:attributes instance-program)
                       :let [semantic (get-in technique [:parameters parameter-name :semantic])
                             accessor (get accessors (get-in primitive [:attributes semantic]))]]
                   {:buffer (get buffer-views (:buffer-view accessor))
                    :location (get-in program [:attribute-locations attribute-name])
                    :components-per-vertex (get gltf/components-per-vertex (:type accessor))
                    :type (:component-type accessor)})
     :element-array {:buffer (get buffer-views (:buffer-view element-accessor))
                     :type (:component-type element-accessor)
                     :offset (:byte-offset element-accessor)
                     :count (:count element-accessor)}}))

(defnk execute-draw-calls! :- c/Nil
  "Send all items in the `draw-calls` sequence to the `draw!` function.

   A good value for `draw!` is `(partial cljs-webgl.buffers/draw! gl)`."
  [draw! :- c/Function
   draw-calls :- [DrawCall]]
  (doseq [draw-call draw-calls]
    (mapply draw! draw-call)))

