(ns ohpengull.util-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]
    [ohpengull.util :as util]
    [com.rpl.specter :as specter :refer [ALL]]))

(deftest inline-in-test
  (is (= [:c :d] (specter/select [:a ALL :b] {:a [{:b :c} {:b :d}]})))

  (is (= 1
         (:b (util/inline-in [:b] []
                             {:a 1, :b :a}))))
  (is (= 1
         (:b (util/inline-in [:b] [:a]
                             {:a {:c 1}, :b :c}))))
  
  (is (= {:a {:c 1, :d 2}, :b {:e 1, :f 2}}
         (util/inline-in [:b ALL] [:a]
                         {:a {:c 1, :d 2}, :b {:e :c, :f :d}})))

  (is (= [{:e 1} {:e 2}]
         (:b (util/inline-in [:b ALL :e] [:a]
                             {:a {:c 1, :d 2}, :b [{:e :c} {:e :d}]})))))