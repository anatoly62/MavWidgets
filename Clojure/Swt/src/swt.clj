(ns mav.swt
   (:import (org.eclipse.swt SWT)
   (org.eclipse.swt.custom ControlEditor TableCursor)
   (org.eclipse.swt.graphics Color)
   (org.eclipse.swt.widgets Display Shell Control  Widget Listener)
	 (org.eclipse.swt.widgets Label Button Text Combo DateTime Menu MenuItem MessageBox Table TableItem TableColumn)
	 (org.eclipse.swt.events VerifyListener SelectionAdapter MouseAdapter SelectionEvent MouseEvent ModifyListener  ModifyEvent
                                         TraverseListener  TraverseEvent ShellAdapter ShellEvent))
  (:use (mav func ))
  (:gen-class))

(defn dig2 [x]
  (if (< x  10)(str "0" x) (str x)))

(defn date  [^DateTime w]
  (let [m (dig2(inc (.getMonth w))) d (dig2 (.getDay w))]
    (str (.getYear w) "-" m "-" d)))

(defn date! [^DateTime w date ]
  (let [year (Integer/parseInt (.substring date 0 4))
        month (dec (Integer/parseInt (.substring date 5 7)))
        day   (Integer/parseInt  (.substring date 8 10))]
    (.setDate w year month day)))

(defn widget
  ([w prop]
  (case prop
    :column (.getColumn w)
    :columns (.getColumns w)
    :date (date w)
    :day (.getDay w)
    :enable (.getEnabled w)
    :index (.getSelectionIndex w)
    :items (.getItems w)
    :month (.getMonth w)
    :text (.getText w)
    :visible (.getVisible w)
    :width (.getWidth w)
    :year (.getYear w)))
  ([w n prop]
    (case prop
      :item (.getItem w n)
      :text (.getText w n)

      )))

(defn widget!
  ([w prop]
    (if (map? prop)(dorun (map #(widget! w  (key %) (val %)) prop))
      (case prop
        :focus (.setFocus w)
        :clear (.removeAll w))))
  ([w prop v]
    (if (vector? w)(dorun (map #(widget! % prop v) w))
      (case prop
        :date (date! w v)
        :day (.setDay w v)
        :editor (.setEditor w v)
        :enable (.setEnabled w v)
        :focus (.setFocus w)
        :index (.select w v)
        :month (.setMonth w v)
        :remove (.remove w v)
        :size (.setSize w (first v) (second v))
        :text (.setText w v)
        :visible (.setVisible w v)
        :width (.setWidth w v)
        :year (.setYear w v))))
  ([w n prop v]
    (case prop
      :text (.setText w n v))))



