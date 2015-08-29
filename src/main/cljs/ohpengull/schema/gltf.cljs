(ns ohpengull.schema.gltf
  (:require
    [plumbing.core :refer [map-keys]]
    [schema.core :as s :include-macros true]
    [schema.coerce :as coerce]
    [ohpengull.schema.common :as c]
    [ohpengull.schema.webgl :as webgl]))

(def TypeKeyword
  (s/enum 5120
          5121
          5122
          5123
          5124
          5125
          5126
          35664
          35665
          35666
          35667
          35668
          35669
          35670
          35671
          35672
          35673
          35674
          35675
          35676
          35678))

(def components-per-vertex
  {"SCALAR" 1
   "VEC2" 2
   "VEC3" 3
   "VEC4" 4
   "MAT2" 4
   "MAT3" 9
   "MAT4" 16})

(def ArrayValues
  (s/either [s/Num] [s/Bool] [s/Str]))

(def Ref
  s/Str)

(def ChildOfRoot
  {(s/optional-key :name) s/Str})

(def Accessor
  (assoc ChildOfRoot
    :bufferView Ref
    :byteOffset c/NonnegInt
    (s/optional-key :byteStride) c/UnsignedByte
    :componentType (s/enum 5120 5121 5122 5123 5126)
    :count c/PosInt
    :type (s/enum "SCALAR" "VEC2" "VEC3" "VEC4" "MAT2" "MAT3" "MAT4") ;TODO: Use keywords
    (s/optional-key :max) [s/Num]
    (s/optional-key :min) [s/Num]))

(def Buffer
  (assoc ChildOfRoot
    :uri c/Uri
    (s/optional-key :byteLength) c/NonnegInt
    :type (s/enum "arraybuffer" "text")                     ;TODO: Use keywords
    ))

(def BufferView
  (assoc ChildOfRoot
    :buffer Ref
    :byteOffset c/NonnegInt
    (s/optional-key :byteLength) c/NonnegInt
    (s/optional-key :target) webgl/Target))

(def Material
  (assoc ChildOfRoot
    :instanceTechnique {:technique Ref
                        (s/optional-key :values) {s/Str s/Any}}))

(def Mesh
  (assoc ChildOfRoot
    :primitives [{:attributes {s/Str s/Str}
                  :indices Ref
                  :material Ref
                  :mode webgl/DrawMode}]))

(def Program
  (assoc ChildOfRoot
    :attributes [s/Str]
    :fragmentShader Ref
    :vertexShader Ref))

(def Technique
  (assoc ChildOfRoot
    :parameters {s/Str {(s/optional-key :count) c/PosInt
                        :type TypeKeyword
                        (s/optional-key :semantic) s/Str
                        (s/optional-key :node) Ref
                        (s/optional-key :value) (s/either s/Num s/Bool s/Str ArrayValues)}}
    :pass Ref
    :passes {s/Str {(s/optional-key :details) s/Any         ;TODO create techniquePassDetails schema
                    :instanceProgram {(s/optional-key :attributes) {s/Str s/Str}
                                      :program Ref
                                      (s/optional-key :uniforms) {s/Str s/Str}}
                    (s/optional-key :states) s/Any          ;TODO create techniquePassStates schema
                    }}))

(def Shader
  (assoc ChildOfRoot
    :type webgl/ShaderType
    :uri c/Uri))

(def Root
  {(s/optional-key :allExtensions) [s/Str]
   (s/optional-key :accessors) {s/Str Accessor}
   (s/optional-key :animations) {s/Str s/Any}               ;TODO create animation schema
   (s/optional-key :asset) {s/Keyword s/Any}                ;TODO create asset schema
   (s/optional-key :buffers) {s/Str Buffer}
   (s/optional-key :bufferViews) {s/Str BufferView}
   (s/optional-key :cameras) {s/Str s/Any}                  ;TODO create camera schema
   (s/optional-key :images) {s/Str s/Any}                   ;TODO create image schema
   (s/optional-key :lights) {s/Str s/Any}                   ;TODO create light schema
   (s/optional-key :materials) {s/Str Material}
   (s/optional-key :meshes) {s/Str Mesh}
   (s/optional-key :nodes) {s/Str s/Any}                    ;TODO create node schema
   (s/optional-key :programs) {s/Str Program}
   (s/optional-key :samplers) {s/Str s/Any}                 ;TODO create sampler schema
   :scene s/Str
   (s/optional-key :scenes) {s/Str s/Any}                   ;TODO create scene schema
   :shaders {s/Str Shader}
   (s/optional-key :skins) {s/Str s/Any}                    ;TODO create skin schema
   (s/optional-key :techniques) {s/Str Technique}
   (s/optional-key :textures) {s/Str s/Any}                 ;TODO create texture schema
   })

(defn explicit-schema-keys
  "Returns a set of all of the explicit keyword names in a map schema's keys.
  This includes keyword literals, required, and optional keywords."
  [schema]
  (into #{}
        (->> schema
             keys
             (filter s/specific-key?)
             (map s/explicit-schema-key)
             (filter keyword?)
             (map name))))

(defn json-with-keys-coercion-matcher
  [schema]
  (if (map? schema)
    (let [key-names (explicit-schema-keys schema)]
      (fn [item]
        (if (map? item)
          (map-keys #(if (and (string? %)
                              (key-names %))
                      (keyword %)
                      %)
                    item)
          item)))
    (coerce/json-coercion-matcher schema)))

(defn json->edn [json]
  (-> json
      js/JSON.parse
      js->clj
      ((coerce/coercer Root json-with-keys-coercion-matcher))))


