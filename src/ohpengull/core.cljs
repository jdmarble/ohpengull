(ns ^:figwheel-always ohpengull.core
  (:require-macros [dommy.core :refer [sel sel1]])
  (:require
    [cljs-webgl.context :as context]
    [cljs-webgl.shaders :as shaders]
    [cljs-webgl.constants.draw-mode :as draw-mode]
    [cljs-webgl.constants.data-type :as data-type]
    [cljs-webgl.constants.buffer-object :as buffer-object]
    [cljs-webgl.constants.shader :as shader]
    [cljs-webgl.buffers :as buffers]
    [cljs-webgl.typed-arrays :as ta]))

(defn mapply [f & args]
  "Pass a map to a function as keyword arguments.
  From http://stackoverflow.com/a/19430023/336925"
  (apply f (apply concat (butlast args) (last args))))

(defn map-vals [m f]
  "Transform one map of values to another map with the same keys but with a function applied to the values.
  From http://stackoverflow.com/a/1677927/336925"
  (into {} (for [[k v] m] [k (f v)])))

(def my-vertex-shader-source
  "attribute vec3 vertex_position;
   void main() {
     gl_Position = vec4(vertex_position, 1);
   }")

(def my-fragment-shader-source
  "void main() {
     gl_FragColor = vec4(1, 1, 1, 0);
   }")

(def input
  {:accessors {"my-position-accessor" {:buffer-view "my-array-buffer-view"
                                       :components-per-vertex 3
                                       :type data-type/float}
               "my-index-accessor" {:buffer-view "my-element-array-buffer-view"
                                    :count 3
                                    :type data-type/unsigned-short
                                    :offset 0}}
   :buffer-views {"my-array-buffer-view" (ta/float32 [0.5 -0.5 0.0
                                                      0.0 0.5 0.0
                                                      -0.5 -0.5 0.0])
                  "my-element-array-buffer-view" (ta/unsigned-int16 [0 1 2])}
   :programs {"my-program" {:attributes nil                 ;TODO
                            :fragment-shader "my-fragment-shader"
                            :vertex-shader "my-vertex-shader"}}
   :shaders {"my-vertex-shader" {:type shader/vertex-shader
                                 :uri my-vertex-shader-source}
             "my-fragment-shader" {:type shader/fragment-shader
                                   :uri my-fragment-shader-source}}
   })

(defonce render-state
         (atom {:last-input {}
                :programs {}
                :shaders {}
                :draws []}))

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

(defn- make-draw-calls! [gl programs buffer-views accessors]
  (let [program (get programs "my-program")
        vertex-buffer (buffers/create-buffer gl (get buffer-views "my-array-buffer-view")
                                             buffer-object/array-buffer
                                             buffer-object/static-draw)
        element-buffer (buffers/create-buffer gl (get buffer-views "my-element-array-buffer-view")
                                              buffer-object/element-array-buffer
                                              buffer-object/static-draw)]
    [{:shader program
      :draw-mode draw-mode/triangles
      :count 3
      :attributes [(assoc (get accessors "my-position-accessor")
                     :buffer vertex-buffer
                     :location (shaders/get-attrib-location gl program "vertex_position"))]
      :element-array (assoc (get accessors "my-index-accessor")
                       :buffer element-buffer)}]))

(defn update-render-state! [old-state gl input]
  (if (= (:last-input old-state) input)
    old-state
    ; TODO: Unload removed shaders, programs, and buffers
    (let [shaders (load-shaders! gl (:shaders input))
          programs (load-programs! gl shaders (:programs input))]
      {:last-input input
       :shaders shaders
       :programs programs
       :draws (make-draw-calls! gl programs (:buffer-views input) (:accessors input))})))

(let [gl (context/get-context (sel1 :#glcanvas))]
  (swap! render-state update-render-state! gl input)        ;TODO swap! function must not have side-effects
  (buffers/clear-color-buffer gl 0 0 0 1)
  (doseq [draw-args (:draws @render-state)]
    (mapply buffers/draw! gl draw-args)))
