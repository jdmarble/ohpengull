(ns ohpengull.util)

(defn mapply [f & args]
  "Pass a map to a function as keyword arguments.
  From http://stackoverflow.com/a/19430023/336925"
  (apply f (apply concat (butlast args) (last args))))

(defn map-vals [m f]
  "Transform one map of values to another map with the same keys but with a function applied to the values.
  From http://stackoverflow.com/a/1677927/336925"
  (into {} (for [[k v] m] [k (f v)])))

(defn dissoc-nils [m]
  "Returns the given map with nil values removed.
  From http://stackoverflow.com/a/3938151"
  (apply dissoc m (for [[k v] m
                        :when (nil? v)]
                    k)))

(defn dissoc-in
  "Dissociates an entry from a nested associative structure returning a new nested structure.
  Any empty maps that result are kept in the new structure.
  From http://stackoverflow.com/a/14488955/336925"
  [m [k & ks]]
  (if-not ks
    (dissoc m k)
    (assoc m k (dissoc-in (m k) ks))))

(defn in?
  "true if `seq` contains `v`
  From http://stackoverflow.com/a/3249777/336925"
  [seq v]
  (some #(= v %) seq))
