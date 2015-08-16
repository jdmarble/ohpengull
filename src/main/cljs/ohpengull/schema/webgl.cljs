(ns ohpengull.schema.webgl
  (:require
    [schema.core :as s]
    [cljs-webgl.constants.data-type :as data-type]
    [cljs-webgl.constants.draw-mode :as draw-mode]
    [cljs-webgl.constants.shader :as shader]
    [cljs-webgl.constants.buffer-object :as buffer-object]))

(def DataType
  (s/enum data-type/byte
          data-type/unsigned-byte
          data-type/short
          data-type/unsigned-short
          data-type/int
          data-type/unsigned-int
          data-type/float))

(def DrawMode
  (s/enum draw-mode/points
          draw-mode/lines
          draw-mode/line-loop
          draw-mode/line-strip
          draw-mode/triangles
          draw-mode/triangle-strip
          draw-mode/triangle-fan))

(def ShaderType
  (s/enum shader/fragment-shader
          shader/vertex-shader))

(def Target
  (s/enum buffer-object/array-buffer
          buffer-object/element-array-buffer))

(def Buffer
  s/Any)

(def Program
  s/Any)
