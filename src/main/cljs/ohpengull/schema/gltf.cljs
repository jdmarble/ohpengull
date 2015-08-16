(ns ohpengull.schema.gltf
  (:require
    [schema.core :as s]
    [ohpengull.schema.common :as c]
    [ohpengull.schema.webgl :as webgl]))

(def TypeKeyword
  (s/enum :bool
          :bvec2
          :bvec3
          :bvec4
          :float
          :vec2
          :vec3
          :vec4
          :int
          :ivec2
          :ivec3
          :ivec4
          :mat2
          :mat3
          :mat4))

(def components-per-vertex
  {:scalar 1
   :vec2 2
   :vec3 3
   :vec4 4
   :mat2 4
   :mat3 9
   :mat4 16})

(def Accessor
  {:buffer-view s/Str
   :byte-offset c/NonnegInt
   (s/optional-key :byte-Stride) c/UnsignedByte
   :component-type webgl/DataType
   :count c/PosInt
   :type (s/enum :scalar :vec2 :vec3 :vec4 :mat2 :mat3 :mat4)
   (s/optional-key :max) s/Num
   (s/optional-key :min) s/Num})

(def Buffer
  {:uri c/Uri
   (s/optional-key :byte-length) c/NonnegInt
   :type (s/enum :arraybuffer :text)})

(def BufferView
  {:buffer s/Str
   :byteOffset c/NonnegInt
   (s/optional-key :byteLength) c/NonnegInt
   (s/optional-key :target) webgl/Target})

(def Material
  {:instance-technique {:technique s/Str
                        (s/optional-key :values) {s/Str s/Any}}})

(def Mesh
  {:primitives [{:attributes {s/Keyword s/Str}
                 :indices s/Str
                 :material s/Str
                 :primitive webgl/DrawMode}]})

(def Program
  {:attributes [s/Str]
   :fragment-shader s/Str
   :vertex-shader s/Str})

(def Technique
  {:parameters {s/Str {:semantic s/Keyword
                       :type TypeKeyword}}
   :pass s/Str
   :passes {s/Str {:instance-program {:attributes {s/Str s/Str}
                                      :program s/Str}}}})

(def Shader
  {:type webgl/ShaderType
   :uri c/Uri})

(def Root
  {:accessors {s/Str Accessor}
   :buffers {s/Str Buffer}
   :buffer-views {s/Str BufferView}
   :materials {s/Str Material}
   :meshes {s/Str Mesh}
   :programs {s/Str Program}
   :techniques {s/Str Technique}
   :shaders {s/Str Shader}})



