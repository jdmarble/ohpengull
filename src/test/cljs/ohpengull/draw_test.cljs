(ns ohpengull.draw-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]
    [cljs-webgl.constants.draw-mode :as draw-mode]
    [ohpengull.draw :as draw]
    [cljs-webgl.constants.data-type :as data-type]
    [cljs-webgl.constants.buffer-object :as buffer-object]
    [cljs-webgl.constants.shader :as shader]))

(def test-gltf
  {:accessors {"my-position-accessor" {:bufferView "my-array-view"
                                       :byteOffset 0
                                       :componentType data-type/float
                                       :count 1
                                       :type :vec3}
               "my-index-accessor" {:bufferView "my-element-view"
                                    :byteOffset 0
                                    :componentType data-type/unsigned-short
                                    :count 1
                                    :type :scalar}}
   :buffers {"my-array-buffer" {:uri "my-array-buffer.bin"
                                :type :arraybuffer}
             "my-element-buffer" {:uri "my-element-buffer.bin"
                                  :type :arraybuffer}}
   :bufferViews {"my-array-view" {:buffer "my-array-buffer"
                                  :byteOffset 0
                                  :byteLength 36
                                  :target buffer-object/array-buffer}
                 "my-element-view" {:buffer "my-element-buffer"
                                    :byteOffset 0
                                    :byteLength 6
                                    :target buffer-object/element-array-buffer}}
   :materials {"my-material" {:instanceTechnique {:technique "my-technique"}}}
   :meshes {"my-mesh" {:primitives [{:attributes {:POSITION "my-position-accessor"}
                                     :indices "my-index-accessor"
                                     :material "my-material"
                                     :mode draw-mode/triangles}]}}
   :programs {"my-program" {:attributes ["vertex_position"]
                            :fragmentShader "my-fragment-shader"
                            :vertexShader "my-vertex-shader"}}
   :techniques {"my-technique" {:parameters {"position" {:semantic :POSITION
                                                         :type :vec3}}
                                :pass "my-pass"
                                :passes {"my-pass" {:instanceProgram {:attributes {"vertex_position" "position"}
                                                                      :program "my-program"}}}}}
   :shaders {"my-vertex-shader" {:type shader/vertex-shader
                                 :uri "redtriangle-VS.glsl"}
             "my-fragment-shader" {:type shader/fragment-shader
                                   :uri "redtriangle-FS.glsl"}}
   })

(deftest make-draw-calls-test
  (is (not (nil? (draw/make-draw-calls {:programs {}, :bufferViews {}, :gltf test-gltf})))
      "check schema")
  (is (= (count (:meshes test-gltf))
         (count (:draws (draw/make-draw-calls {:programs {}, :bufferViews {}, :gltf test-gltf}))))
      "temporary draw every mesh once"))

