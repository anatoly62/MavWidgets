(ns mav.swt )

;(defmacro defc [& body]
;  (let [res (map #(nop `(def ~% ~(str % shell))) body)]
;    (conj  res 'do)))

(defn show-info [s]
  (js/alert s))

(defn show-mes [s]
  (js/alert s))

(defn by-id [id]
  (.getElementById js/document id))

(defn get-text [obj]
  (let [tg-name (.-tagName obj)]
    (case tg-name
      "INPUT"   (.-value obj)
      "SPAN"    (.-innerHTML obj)
      "TBODY"   (.-innerHTML obj)
      "TD"      (.-innerHTML obj)
      "TH"      (.-innerHTML obj)
      "TR"      (.-innerHTML obj)
      "SELECT"  (let [sel-index (.-selectedIndex obj)
                      opt (aget (.-options obj) sel-index)]
                  (.-text opt)))))

(defn get-cell-text [w n]
  (let [columns (.-cells w)]
    (.-innerHTML (aget columns n ))))

(defn get-index [obj]
  (.-selectedIndex obj))

(defn get-table-items [obj]
  (let [rows (.-rows obj)
        cnt (.-length rows)]
    (map #(aget rows %) (range cnt))))

(defn get-combo-items [obj]
  (let [op  (.-options obj)
        cnt (.-length op)]
    (map #(.-text (aget op %)) (range cnt))))

(defn get-items [obj]
  (let [tg-name (.-tagName obj)]
    (case tg-name
      "SELECT" (get-combo-items obj)
      "TABLE"  (get-table-items obj)
      "TBODY"  (get-table-items obj))))

(defn widget
  ([w prop ]
    (case prop
      :checked (.-checked (by-id w))
      :date (get-text (by-id w))
      :text (get-text (by-id w))
      :index (get-index (by-id w))
      :items (get-items (by-id w))))
  ([w n prop]
    (case prop
      :text (get-cell-text w n)
      :index (get-index (by-id w)))))


(defn set-checked! [obj v]
  (set! (.-checked obj) v))

(defn set-combo-text! [obj v]
  (let [opt (.-options obj)
        cnt (.-length opt)]
    (loop [i 0]
      (if (= i cnt) nil
        (if (= (.-text (aget opt i)) v) (set! (.-selectedIndex obj) i)
          (recur (inc i)))))))

(defn set-text! [obj v]
  (let [tg-name (.-tagName obj)]
    (case tg-name
      "DIV"  (set! (.-innerHTML obj) v)
      "INPUT" (set! (.-value obj) v)
      "SELECT" (set-combo-text! obj v)
      "SPAN"  (set! (.-innerHTML obj) v)
      "TABLE" (set! (.-innerHTML obj) v)
      "TBODY" (set! (.-innerHTML obj) v)
      "TD"    (set! (.-innerHTML obj) v)
      "TH"    (set! (.-innerHTML obj) v))))

(defn set-index! [obj v]
  (set! (.-selectedIndex obj) v))

(defn set-focus! [w]
  (.focus w))

(defn set-selection! [tbl v]
  (let [rows (.-rows tbl)
        r (aget rows v)]
    (when-not (= js/undefined (.-selection tbl)) (set! (.-background (.-style (.-selection tbl))) "white"))
    (set! (.-selectedIndex tbl) v)
    (set! (.-selection tbl) r)
    (set! (.-background (.-style (.-selection tbl))) "#00bbee")
    (.scrollIntoView r false)))

(defn set-cursor-selection! [tbl prop v])

(defn cell-text! [w n v]
  (let [columns (.-cells w)
        l (.-length columns)]
    (when (< n l)  (set! (.-innerHTML (aget columns n )) v))))

(defn del-row! [w v]
  (.deleteRow w v))

(defn widget!
  ([w prop]
    (if (map? prop)(dorun (map #(widget! w  (key %) (val %)) prop))
      (if (vector? w)(dorun (map #(widget! % prop ) w))
        (case prop
          :focus (set-focus! (by-id w))
          :close (set! (.-display (.-style (by-id w))) "none")
          :clear (set! (.-innerHTML (by-id w)) "")
          :select-all (.select (by-id w))))))
  ([w prop v]
    (if (vector? w)(dorun (map #(widget! % prop v) w))
  (case prop
    :checked (set-checked! (by-id w) v)
    :date (set-text! (by-id w) v)
    :text (set-text! (by-id w) v)
    :index (set-index!  (by-id w) v)
    :remove (del-row! (by-id w) v)
    :select (set-selection! (by-id w) v))))
  ([w n prop v]
    (if (keyword? n)
      (case n
        :select (set-cursor-selection! w prop v))
      (case prop
        :text (cell-text! w n v)))))

