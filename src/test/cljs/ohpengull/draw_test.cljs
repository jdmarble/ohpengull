(ns ohpengull.draw-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]
    [cljs-webgl.constants.draw-mode :as draw-mode]
    [ohpengull.draw :as draw]
    [cljs-webgl.constants.data-type :as data-type]
    [cljs-webgl.constants.buffer-object :as buffer-object]
    [cljs-webgl.constants.shader :as shader]))

(def test-gltf
  {:accessors {"my-position-accessor" {:buffer-view "my-array-view"
                                       :byte-offset 0
                                       :component-type data-type/float
                                       :count 1
                                       :type :vec3}
               "my-index-accessor" {:buffer-view "my-element-view"
                                    :byte-offset 0
                                    :component-type data-type/unsigned-short
                                    :count 1
                                    :type :scalar}}
   :buffers {"my-array-buffer" {:uri "my-array-buffer.bin"
                                :type :arraybuffer}
             "my-element-buffer" {:uri "my-element-buffer.bin"
                                  :type :arraybuffer}}
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

(deftest make-draw-calls-test
  (is (not (nil? (draw/make-draw-calls {:loaded-programs {}, :gl-buffers {}, :gltf test-gltf})))
      "check schema")
  (is (= (count (:meshes test-gltf))
         (count (:draws (draw/make-draw-calls {:loaded-programs {}, :gl-buffers {}, :gltf test-gltf}))))
      "temporary draw every mesh once"))

(def smallest-drawcall
  {:shader nil
   :draw-mode draw-mode/triangles
   :count 1
   :attributes []})

(defn- test-input [input]
  (let [output (atom [])]
    (draw/execute-draw-calls!
      {:draw! (fn [& rest]
         (swap! output conj (apply hash-map rest)))
       :draw-calls input})
    @output))

(deftest execute-draw-calls!-test

  (is (empty? (test-input []))
      "no calls to `draw!` with empty `draw-calls`")

  (is (= [smallest-drawcall] (test-input [smallest-drawcall]))
      "passes contents of `draw-calls` maps as keyword args to `draw!`")

  (is (= [smallest-drawcall smallest-drawcall] (test-input [smallest-drawcall smallest-drawcall]))
      "multiple calls"))
