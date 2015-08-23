(ns ohpengull.dev
  (:require
    [cljs-webgl.context :as context]
    [cljs-webgl.constants.buffer-object :as buffer-object]
    [cljs-webgl.constants.draw-mode :as draw-mode]
    [cljs-webgl.constants.data-type :as data-type]
    [cljs-webgl.constants.shader :as shader]
    [cljs-webgl.typed-arrays :as ta]
    [ohpengull.buffers]
    [ohpengull.draw]
    [ohpengull.programs]
    [plumbing.graph :as graph]
    [figwheel.client :as fw]))

(def my-gltf
  {:accessors {"my-position-accessor" {:buffer-view "my-array-view"
                                       :byte-offset 0
                                       :component-type data-type/float
                                       :count 3
                                       :type :vec3}
               "my-index-accessor" {:buffer-view "my-element-view"
                                    :byte-offset 0
                                    :component-type data-type/unsigned-short
                                    :count 3
                                    :type :scalar}}
   :buffers {"my-buffer" {:uri [0.5 -0.5 0.0
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
                                                         :type :vec3}}
                                :pass "my-pass"
                                :passes {"my-pass" {:instance-program {:attributes {"vertex_position" "position"}
                                                                       :program "my-program"}}}}}
   :shaders {"my-vertex-shader" {:type shader/vertex-shader
                                 :uri "my-VS.glsl"}
             "my-fragment-shader" {:type shader/fragment-shader
                                   :uri "my-FS.glsl"}}
   })

(def gl (context/get-context (.getElementById js/document "canvas")))

(defn my-params []
  {:shader-sources {"my-VS.glsl" "attribute vec3 vertex_position;
                                  void main() {gl_Position = vec4(vertex_position, 1);}"
                    "my-FS.glsl" "void main() {gl_FragColor = vec4(1, 0, 0, 1);}"}
   :gl gl
   :buffers {"my-array-buffer" (.-buffer (ta/float32 [1.0 1.0 0.0
                                                      -1.0 1.0 0.0
                                                      1.0 -1.0 0.0]))
             "my-element-buffer" (.-buffer (ta/unsigned-int16 [0 1 2]))}
   :gltf my-gltf})

(defn my-render []
  (let [calc (graph/compile {:buffer-views ohpengull.buffers/create-buffer-views
                             :shaders ohpengull.programs/compile-shaders
                             :programs ohpengull.programs/link-programs
                             :draw-calls ohpengull.draw/make-draw-calls})
        result (calc (my-params))]
    (js/console.log "result -> " (pr-str result))
    (ohpengull.draw/execute-draw-calls!
      (assoc result
        :draw! (partial cljs-webgl.buffers/draw! gl)))))

(fw/start {:on-jsload my-render})

(my-render)