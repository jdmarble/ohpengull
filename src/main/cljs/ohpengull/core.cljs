(ns ^:figwheel-always ohpengull.core
  (:require [plumbing.core :refer-macros (defnk)]))

(def default-renderer
  "A graph specifying a computation that outputs a sequence of WebGL draw calls."
  {:accessors nil
   :buffers nil
   :buffer-views nil
   :materials nil
   :meshes nil
   :programs nil
   :techniques nil
   :shaders nil

   })

(defn render! [] nil)