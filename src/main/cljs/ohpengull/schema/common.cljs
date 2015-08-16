(ns ohpengull.schema.common
  (:require [schema.core :as s]
            [cljs-webgl.typed-arrays :as typed-arrays]))

(def Nil
  (s/eq nil))

(def PosInt
  (s/both s/Int (s/pred #(> 0))))

(def NonnegInt
  (s/both s/Int (s/pred #(>= 0))))

(def UnsignedByte
  (s/both s/Int (s/pred #(>= 0 %) #(<= 255 %))))

(def Uri
  s/Str)

(def TypedArray
  (s/pred typed-arrays/typed-array?))

(def Function
  (s/pred #(not (nil? %))))
