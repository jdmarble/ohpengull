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
                                                         :type 35665 #_(FLOAT_VEC3)}}
                                :pass "my-pass"
                                :passes {"my-pass" {:instance-program {:attributes {"vertex_position" "position"}
                                                                       :program "my-program"}}}}}
   :shaders {"my-vertex-shader" {:type shader/vertex-shader
                                 :uri my-vertex-shader-source}
             "my-fragment-shader" {:type shader/fragment-shader
                                   :uri my-fragment-shader-source}}
   })

(defonce render-state
         (atom {:last-input {}
                :programs {}
                :shaders {}
                :buffers {}
                :buffer-views {}}))

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

(defn- assoc-attribute [gl render-state program attribute-name accessor-name]
  (let [accessor (get-in render-state [:last-input :accessors accessor-name])
        buffer-view (get-in render-state [:buffer-views (:buffer-view accessor)])]
    (assoc accessor
      :buffer buffer-view
      :location (shaders/get-attrib-location gl program attribute-name))))

(defn- assoc-element-array [render-state accessor-name]
  (let [accessor (get-in render-state [:last-input :accessors accessor-name])
        buffer-view (get-in render-state [:buffer-views (:buffer-view accessor)])]
    (assoc accessor
      :buffer buffer-view)))

(defn update-render-state! [old-state gl input]
  (if (= (:last-input old-state) input)
    old-state
    ; TODO: Unload removed shaders, programs, and buffers
    (let [shaders (load-shaders! gl (:shaders input))
          programs (load-programs! gl shaders (:programs input))
          buffers (load-buffers! (:buffers input))
          buffer-views (load-buffer-views! gl buffers (:buffer-views input))]
      {:last-input input
       :shaders shaders
       :programs programs
       :buffers buffers
       :buffer-views buffer-views})))

(defn- make-draw-calls [gl render-state]
  (for [mesh (vals (get-in render-state [:last-input :meshes]))
        prim (:primitives mesh)
        :let [material (get-in render-state [:last-input :materials (:material prim)])
              technique-name (get-in material [:instance-technique :technique])
              technique (get-in render-state [:last-input :techniques technique-name])
              pass (get-in technique [:passes (:pass technique)])
              program-name (get-in pass [:instance-program :program])
              program (get-in render-state [:programs program-name])
              attributes (get-in pass [:instance-program :attributes])]]
    {:shader program
     :draw-mode (:primitive prim)
     :count (get-in render-state [:last-input :accessors (:indices prim) :count])
     :attributes (for [[attribute-name parameter-name] attributes
                       :let [semantic (get-in technique [:parameters parameter-name :semantic])
                             accessor-name (get-in prim [:attributes semantic])]]
                   (assoc-attribute gl render-state program attribute-name accessor-name))
     :element-array (assoc-element-array render-state (:indices prim))}))

(let [gl (context/get-context (sel1 :#glcanvas))]
  (swap! render-state update-render-state! gl input)        ;TODO swap! function must not have side-effects
  (buffers/clear-color-buffer gl 0 0 0 1)
  (doseq [draw-args (make-draw-calls gl @render-state)]
    (mapply buffers/draw! gl draw-args)))