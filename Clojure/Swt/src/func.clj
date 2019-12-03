(ns mav.func
  (:gen-class)
  (:import  (java.text SimpleDateFormat))
  (:import (java.util Calendar GregorianCalendar)))

(defn abs "(abs n) is the absolute value of n" [n]
  (cond
    (nil? n) 0
    (not (number? n)) (throw (IllegalArgumentException.
                               "abs requires a number"))
    (neg? n) (- n)
    :else n))

(defn formula [s]
  (.evaluate (new mav.MathEval) s))

(defn nop [x] x)

(defn to-time [s]
  (let [sdf  (new SimpleDateFormat "yyyy-MM-dd")
        dt (.parse sdf s)]
    (.getTime dt)))

(defn to-float [s]
  (if-not (empty? s) (Float/parseFloat s) 0 ))

(defn to-int [s]
  (if (number? s) s
    (if-not (empty? s) (Integer/parseInt s) 0 )))

(defn dec-to-int [s]
  (if-not (empty? s) ( dec (Integer/parseInt s)) 0))

(defn inc-to-int [s]
  (if-not (empty? s) ( inc (Integer/parseInt s)) 0))

(defn round [n]
  (.setScale (bigdec n) 2 java.math.RoundingMode/HALF_EVEN))

(defn to-round [s]
  (round (to-float s)))

(defn div2 [a b]
  (try ( / a b)
    (catch ArithmeticException e
      (.divide (bigdec a) (bigdec b) (java.math.MathContext. 8)))))

(defn div2-round [a b]
  (round (div2 a b)))



(defn day-of-week [date]
  (let [gc (GregorianCalendar.)
        year (to-int (subs date 0 4))
        month (dec-to-int (subs date 5 7))
        day (to-int (subs  date 8 10))]
    (.set gc year month day 0 0 0)
    (.setTime gc (.getTime gc))
    (.get gc Calendar/DAY_OF_WEEK)))

(defn day-max [date]
  (let [gc (GregorianCalendar.)
        year (to-int (subs date 0 4))
        month (dec-to-int (subs date 5 7))
        day (to-int (subs  date 8 10))]
    (.set gc year month day 0 0 0)
    (.setTime gc (.getTime gc))
    (.getActualMaximum  gc Calendar/DAY_OF_MONTH)))

(defn str-round [n]
  (str (.setScale (bigdec n) 2 java.math.RoundingMode/HALF_EVEN)))

(defn starts-with [st s]
  (if (and st s) (.startsWith (.toLowerCase st) s) false))

(defn trim [s]
  (.trim s))

(defn transpose [v]
  (vec (apply map vector v)))

(defn vvec-cons [seq1 seq2]
  (mapv #(vec (concat % %2)) seq1 seq2))

(defn vseq-sum [k-vec]
  (vec (apply map + k-vec)))

(defn inc-search [s col]
  (first (filter #(starts-with % s) col)))

(defn nth$ [text n]
  (let [lst (clojure.string/split text #"\s+") ]
    (if (>= n (count lst)) nil (nth lst n ))))

(defn dual-search [s col n]
  (let [lst (clojure.string/split s #"\s+")
        ff (filter #(starts-with (nth$ % n) (first lst)) col)]
    (if (second lst) (first (filter #(starts-with (nth$ % (inc n)) (second lst)) ff))
      (first ff))))

(defn get-first-part [text]
  (Integer/parseInt (first(clojure.string/split text #"\s+"))))

(defn <t [& op]
  (apply (< (map #(to-time %) op))))

(defn <=t [& op]
  (apply <= (map #(to-time %) op)))

(defn >t [& op]
  (apply > (map #(to-time %) op)))

(defn >=t [& op]
  (apply >= (map #(to-time %) op)))

(defn =t [& op]
  (apply = (map #(to-time %) op)))

(defn *$ [& op]
  (round (apply * (map #(to-float %) op ))))

(defn +$ [& op]
  (round (apply + (map #(to-float %) op ))))

(defn -$ [& op]
  (round (apply - (map #(to-float %) op ))))

(defn div$ [& op]
  (round (apply / (map #(to-float %) op ))))

(defn index-of [seq el]
  (.indexOf seq el))

(defn row-by-id [seq id]
  (first (filter #(= (:_id %) id) seq)))

(defn name-by-id [seq id]
  (when-first [r (filter #(= (:_id %) id) seq)] (:name r)))

(defn id-by-name [seq name]
  (when-first [r (filter #(= (:name %) name) seq)] (:_id r)))

(defn row-by-name [seq name]
  (first (filter #(= (:name %) name) seq)))

(defn num-by-id [seq id]
  (let [cnt (count seq)]
    (loop [i 0]
      (if (= i cnt) nil
        (if (= (:_id (seq i)) id) i
          (recur (inc i)))))))

(defn sort-by-other-map [col o-col v-keys]
  (if (vector? (first col))  (sort-by #(vec (map (row-by-id o-col (first %)) v-keys)) col)
    (sort-by #(vec (map (row-by-id o-col (:_id %)) v-keys)) col)))

;  (sort-by #((juxt :num :posId) (row-by-id @sql-workers (:_id %))) col)