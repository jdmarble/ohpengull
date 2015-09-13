(ns ohpengull.util
  (:require
    [ajax.core :as ajax]
    [cljs.core.async :as async]))

(defn mapply [f & args]
  "Pass a map to a function as keyword arguments.
  From http://stackoverflow.com/a/19430023/336925"
  (apply f (apply concat (butlast args) (last args))))

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

(defn http-get
  "Returns a channel that outputs the response to an HTTP GET request."
  [url]
  (js/console.log " requesting " url)
  (let [c (async/chan)]
    (ajax/GET url
              {:handler
               (fn [response]
                 (async/put! c response)
                 (async/close! c))
               :error-handler
               (fn [{:keys [status-text]}]
                 (js/console.log "problem getting " url " : " status-text)
                 (async/put! c (js.Error. status-text))
                 (async/close! c))})
    c))

(defn http-get-arraybuffer
  "Returns a channel that outputs the response to an HTTP GET request."
  [uri]
  (js/console.log " requesting " uri)
  (let [ch (async/chan)]
    (ajax/ajax-request
      {:uri uri
       :method :get
       :headers nil
       :format nil
       :response-format {:description "get ArrayBuffer"
                         :read ajax/-body
                         :content-type "application/octet-stream"
                         :response-type :arraybuffer}
       :handler
       (fn [[okay result]]
         (js/console.log " okay -> " okay " result -> " (pr-str uri "=" result))
         (if okay
           (async/put! ch result)
           (async/put! ch (js/Error. result)))
         (async/close! ch))})
    ch))