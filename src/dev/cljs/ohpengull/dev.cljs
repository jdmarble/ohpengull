(ns ohpengull.dev
  (:require
    [cljs-webgl.constants.buffer-object :as buffer-object]
    [cljs-webgl.constants.draw-mode :as draw-mode]
    [cljs-webgl.constants.data-type :as data-type]
    [cljs-webgl.constants.shader :as shader]
    [cljs-webgl.typed-arrays :as ta]
    [ohpengull.core :as ohpengull]
    [figwheel.client :as fw]))

(def my-vertex-shader-source
  "attribute vec3 vertex_position;
   void main() {
     gl_Position = vec4(vertex_position, 1);
   }")

(def my-fragment-shader-source
  "void main() {
     gl_FragColor = vec4(1, 1, 1, 0);
   }")

(def my-gltf
  {:accessors {"my-position-accessor" {:buffer-view "my-array-view"
                                       :components-per-vertex 3
                                       :type data-type/float}
               "my-index-accessor" {:buffer-view "my-element-view"
                                    :count 3
                                    :type data-type/unsigned-short
                                    :offset 0}}
   :buffers {"my-array-buffer" {:uri [0.5 -0.5 0.0
                                      0.0 0.5 0.0
                                      -0.5 -0.5 0.0]
                                :type ta/float32}
             "my-element-buffer" {:uri [0 1 2]
                                  :type ta/unsigned-int16}}
   :buffer-views {"my-array-view" {:buffer "my-array-buffer"
                                   :byteOffset 0
                                   :byteLength 36
                                   :target buffer-object/array-buffer}
                  "my-element-view" {:buffer "my-element-buffer"
                                     :byteOffset 0
                                     :byteLength 6
                                     :target buffer-object/element-array-buffer}}
   :materials {"my-material" {:instance-technique {:technique "my-technique"}}}
   :meshes {"my-mesh" {:primitives [{:attributes {:POSITION "my-position-accessor"}
                                     :indices "my-index-accessor"
                                     :material "my-material"
                                     :primitive draw-mode/triangles}]}}
   :programs {"my-program" {:attributes ["vertex_position"]
                            :fragment-shader "my-fragment-shader"
                            :vertex-shader "my-vertex-shader"}}
   :techniques {"my-technique" {:parameters {"position" {:semantic :POSITION
                                                         :type 35665 #_data-type/float-vec3}}
                                :pass "my-pass"
                                :passes {"my-pass" {:instance-program {:attributes {"vertex_position" "position"}
                                                                       :program "my-program"}}}}}
   :shaders {"my-vertex-shader" {:type shader/vertex-shader
                                 :uri my-vertex-shader-source}
             "my-fragment-shader" {:type shader/fragment-shader
                                   :uri my-fragment-shader-source}}
   })

(defn my-render []
  (ohpengull/render my-gltf (js/document.getElementById "glcanvas")))

(fw/start {:on-jsload my-render})