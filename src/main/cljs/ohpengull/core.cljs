(ns ^:figwheel-always ohpengull.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.core.async :refer [<!]]
    [clojure.data :refer [diff]]
    [cljs-http.client :as http]
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

(defn- dissoc-nils [m]
  "Returns the given map with nil values removed.
  From http://stackoverflow.com/a/3938151"
  (apply dissoc m (for [[k v] m
                        :when (nil? v)]
                    k)))

(defn- request-shaders! [render-state]
  (for [[shader-name shader-desc] (seq (get-in render-state [:gltf :shaders]))]
    (.log js/console (pr-str shader-name shader-desc))))

(defn- load-shader! [render-state gl shader-name {:keys [type uri]}]
  (go (let [response (<! (http/get uri))]
        (swap! render-state assoc-in [:shaders shader-name]
               (shaders/create-shader gl type (:body response))))))

(defn- update-shaders! [_ render-state
                        {_gltf :gltf}
                        {:keys [gl gltf shaders]}]
  (request-shaders! @render-state)
  (when-not (= (:shaders _gltf) (:shaders gltf))
    (doseq [[shader-name shader-desc] (:shaders gltf)]
      (load-shader! render-state gl shader-name shader-desc))))

(defn- shader?
  "Tests whether a given `value` is a WebGL shader."
  [value]
  (= (type value) js/WebGLShader))

(defn- load-program! [gl shaders {:keys [fragment-shader vertex-shader]}]
  (let [fs (get shaders fragment-shader)
        vs (get shaders vertex-shader)]
    (when (and (shader? fs)
               (shader? vs))
      (shaders/create-program gl fs vs))))

(defn- update-programs! [_ render-state
                         {_gltf :gltf, _shaders :shaders}
                         {:keys [gl gltf shaders]}]
  (when-not (and (= (:programs _gltf) (:programs gltf))
                 (= _shaders shaders))
    (swap! render-state assoc :programs
           (dissoc-nils (map-vals (:programs gltf) #(load-program! gl shaders %))))))

(defn- load-buffer! [{:keys [uri type]}]
  (type uri))

(defn- update-buffers! [_ render-state
                        {_gltf :gltf}
                        {:keys [gltf]}]
  (when-not (= (:buffers _gltf) (:buffers gltf))
    (swap! render-state assoc :buffers
           (map-vals (:buffers gltf) #(load-buffer! %)))))

(defn- load-buffer-view! [gl buffers {:keys [buffer byteOffset byteLength target]}]
  (let [buffer-val (get buffers buffer)]
    (when (not (nil? buffer-val))
      (let [view (js/DataView. (.-buffer buffer-val) byteOffset byteLength)]
        (buffers/create-buffer gl view target buffer-object/static-draw)))))

(defn- update-buffer-views! [_ render-state
                             {_gltf :gltf, _buffers :buffers}
                             {:keys [gl gltf buffers]}]
  (when-not (and (= (:buffer-views _gltf) (:buffer-views gltf))
                 (= _buffers buffers))
    (swap! render-state assoc :buffer-views
           (map-vals (:buffer-views gltf) #(load-buffer-view! gl buffers %)))))

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

(defn- update-draws! [_ render-state
                      {_gltf :gltf, _programs :programs, _buffer-views :buffer-views}
                      {:keys [gl gltf programs buffer-views]}]
  (when-not (and (= _gltf gltf)
                 (= _programs programs)
                 (= _buffer-views buffer-views))
    (swap! render-state assoc :draws
           (for [mesh (vals (:meshes gltf))
                 prim (:primitives mesh)
                 :let [material (get-in gltf [:materials (:material prim)])
                       technique-name (get-in material [:instance-technique :technique])
                       technique (get-in gltf [:techniques technique-name])
                       pass (get-in technique [:passes (:pass technique)])
                       program-name (get-in pass [:instance-program :program])
                       program (get programs program-name)
                       attributes (for [[attribute-name parameter-name] (get-in pass [:instance-program :attributes])
                                        :let [semantic (get-in technique [:parameters parameter-name :semantic])
                                              accessor-name (get-in prim [:attributes semantic])]]
                                    (assoc-attribute gl gltf buffer-views program attribute-name accessor-name))
                       element-array (assoc-element-array gltf buffer-views (:indices prim))]
                 :when (not (or (nil? program)
                                (nil? (:buffer element-array))
                                (some nil? (map :buffer attributes))))]
             {:shader program
              :draw-mode (:primitive prim)
              :count (get-in gltf [:accessors (:indices prim) :count])
              :attributes attributes
              :element-array element-array}))))

(defn- draw! [_ _ old-state {:keys [gl draws]}]
  "Draws a single frame to the context."
  (when-not (= (:draws old-state) draws)
    (doseq [draw draws]
      (.log js/console "draw!" (pr-str draw))
      (mapply buffers/draw! gl draw))))

(defn render [gltf canvas]
  (let [gl (context/get-context canvas)
        render-state (atom {:gl gl
                            :gltf {}
                            :shaders {}
                            :programs {}
                            :buffers {}
                            :buffer-views {}
                            :draws []})]
    #_(add-watch render-state :log #(.log js.console (pr-str (take 2 (diff %3 %4)))))
    (add-watch render-state :shaders update-shaders!)
    (add-watch render-state :programs update-programs!)
    (add-watch render-state :buffer-views update-buffer-views!)
    (add-watch render-state :buffers update-buffers!)
    (add-watch render-state :draws update-draws!)
    (add-watch render-state :draw draw!)
    (swap! render-state assoc :gltf gltf)))