(ns ^:figwheel-always ohpengull.core
  (:require
    [cljs-webgl.context :as context]
    [cljs-webgl.shaders :as shaders]
    [cljs-webgl.constants.buffer-object :as buffer-object]
    [cljs-webgl.buffers :as buffers]))

(defn- mapply [f & args]
  "Pass a map to a function as keyword arguments.
  From http://stackoverflow.com/a/19430023/336925"
  (apply f (apply concat (butlast args) (last args))))

(defn- map-vals [m f]
  "Transform one map of values to another map with the same keys but with a function applied to the values.
  From http://stackoverflow.com/a/1677927/336925"
  (into {} (for [[k v] m] [k (f v)])))

(defn- load-shader! [gl {:keys [type uri]}]
  ;TODO dereference URI instead of using URI as source
  (shaders/create-shader gl type uri))

(defn- load-shaders! [gl descs]
  (map-vals descs #(load-shader! gl %)))

(defn- load-program! [gl shaders {:keys [fragment-shader vertex-shader]}]
  (shaders/create-program gl
                          (get shaders fragment-shader)
                          (get shaders vertex-shader)))

(defn- load-programs! [gl shaders descs]
  (map-vals descs #(load-program! gl shaders %)))

(defn- load-buffer! [{:keys [uri type]}]
  (type uri))

(defn- load-buffers! [descs]
  (map-vals descs #(load-buffer! %)))

(defn- load-buffer-view! [gl buffers {:keys [buffer byteOffset byteLength target]}]
  (let [view (js/DataView. (.-buffer (get buffers buffer)) byteOffset byteLength)]
    (buffers/create-buffer gl view target buffer-object/static-draw)))

(defn- load-buffer-views! [gl buffers descs]
  (map-vals descs #(load-buffer-view! gl buffers %)))

(defn- assoc-attribute [gl gltf buffer-views program attribute-name accessor-name]
  (let [accessor (get-in gltf [:accessors accessor-name])
        buffer-view (get buffer-views (:buffer-view accessor))]
    (assoc accessor
      :buffer buffer-view
      :location (shaders/get-attrib-location gl program attribute-name))))

(defn- assoc-element-array [gltf buffer-views accessor-name]
  (let [accessor (get-in gltf [:accessors accessor-name])
        buffer-view (get buffer-views (:buffer-view accessor))]
    (assoc accessor
      :buffer buffer-view)))

(defn- make-draw-calls! [gl gltf]
  (let [shaders (load-shaders! gl (:shaders gltf))
        programs (load-programs! gl shaders (:programs gltf))
        buffers (load-buffers! (:buffers gltf))
        buffer-views (load-buffer-views! gl buffers (:buffer-views gltf))]
    (for [mesh (vals (:meshes gltf))
          prim (:primitives mesh)
          :let [material (get-in gltf [:materials (:material prim)])
                technique-name (get-in material [:instance-technique :technique])
                technique (get-in gltf [:techniques technique-name])
                pass (get-in technique [:passes (:pass technique)])
                program-name (get-in pass [:instance-program :program])
                program (get programs program-name)
                attributes (get-in pass [:instance-program :attributes])]]
      {:shader program
       :draw-mode (:primitive prim)
       :count (get-in gltf [:accessors (:indices prim) :count])
       :attributes (for [[attribute-name parameter-name] attributes
                         :let [semantic (get-in technique [:parameters parameter-name :semantic])
                               accessor-name (get-in prim [:attributes semantic])]]
                     (assoc-attribute gl gltf buffer-views program attribute-name accessor-name))
       :element-array (assoc-element-array gltf buffer-views (:indices prim))})))

(defn render [gltf canvas]
  (let [gl (context/get-context canvas)]
    (buffers/clear-color-buffer gl 0 0 0 1)
    (doseq [draw-args (make-draw-calls! gl gltf)]
      (mapply buffers/draw! gl draw-args))))