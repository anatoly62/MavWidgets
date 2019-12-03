(ns mav.gui-utils
  (:import (org.eclipse.swt SWT)
           (org.eclipse.swt.custom ControlEditor TableCursor)
           (org.eclipse.swt.graphics Color)
           (org.eclipse.swt.widgets Display Shell Control  Widget Listener)
           (org.eclipse.swt.widgets Label Button Text Combo DateTime Menu MenuItem MessageBox Table TableItem TableColumn)
           (org.eclipse.swt.events  VerifyListener SelectionAdapter MouseAdapter SelectionEvent MouseEvent ModifyListener  ModifyEvent
                                            TraverseListener  TraverseEvent ShellAdapter ShellEvent))
  (:use (mav func swt))
  (:gen-class))

(def display (new Display))
(def bgc (new Color (Display/getCurrent) 230, 230, 230))
(def yes SWT/YES)

(defn card-fill! [card  it]
  (dorun (map #(let [val (widget it (:col %) :text)
                     lfun (:from-table %)
                     val (if lfun (lfun val) val )]
                 (widget! (:id %) (:prop %) val)) card)))

(defn data-fill [card store]
  (let [vec-keys (drop 2 store)
        vec-vals (rest (map #(let [val (widget (:id %) (:prop %))
                                   fun (:to-data %)]
                               (if fun (fun val) val )) card))]
    (zipmap vec-keys vec-vals)))

(defn combo-fill! [^Combo cb data]
  (.setItems cb (into-array data))
  (.select cb 0))

(defn table-newColumn [^Table tbl txt width ]
  (let [ col (new TableColumn tbl SWT/NONE)]
    (.setText col txt)
    (.setWidth col width)))

(defn tableCursor-text [^TableCursor cursor pos]
  (.. cursor (getRow) (getText pos)))

(defn tableCursor-wiget! [^TableCursor cursor w]
  (.. cursor (getRow) (setText (widget cursor :column)  (widget  w :text))))

(defn tableItem-data [^TableItem it fld fns]
  (if (keyword? (first fld))
    (let [idx (range (count fld))]
      (zipmap fld (map #((fns %) (.getText it %)) idx )))
    (vec (map #((fns %) (.getText it %)) fld ))))

(defn tableItem-change-row! [^TableItem tbl-it row fld fns n]
  (let [cnt (count fld) ]
    (if (keyword? (first fld))
      (dotimes [i cnt]
        (.setText tbl-it (+ i n) ((fns i)((fld i)row))))
      (doseq [i fld]
        (.setText tbl-it (+ i n) ((fns i)(row i)))))))

(defn table-newItem [^Table tbl & n]
  (if n (new TableItem tbl SWT/NONE (first n))
    (new TableItem tbl SWT/NONE)))

(defn table-sel-item [^Table tbl]
  (aget (.getSelection tbl) 0))

(defn table-sel-item! [^Table tbl n]
  (.setSelection tbl n))

(defn table-add-row!  [^Table tbl row fld fns]
  (let [tbl-it (new TableItem tbl SWT/NONE) cnt (count fld) ]
    (if (keyword? (first fld))
      (dotimes [i cnt ]
        (.setText tbl-it i ((fns i)((fld i)row))))
      (doseq [i fld ]
        (.setText tbl-it i ((fns i)(row i)))))))

(defn table-fill! [^Table tbl fld fns data]
  (doseq [row data]
    (table-add-row! tbl row fld fns )))

(defn table-change! [^Table tbl fld fns data n]
  (let [items (.getItems tbl)]
    (dorun (map #(tableItem-change-row! % %2 fld fns n) items data))))

(defn table-data [^Table tbl fld fns & {nd :drop nt :take :or {nd 0 nt (.getItemCount tbl)}}]
  (let [items (take nt (drop nd (.getItems tbl)))]
    (vec(map #(tableItem-data % fld fns) items))))

(defn table-background! [^Table tbl  & {nd :drop nt :take :or {nd 0 nt (.getItemCount tbl)}}]
  (let [items (take nt (drop nd (.getItems tbl)))]
    (dorun (map #(.setBackground % bgc) items))))

(defn table-col-sum [^Table tbl n]
  (let [items (.getItems tbl)]
    (round (apply +$ (map #(.getText % n) items)))))

(defn table-col-average [^Table tbl n]
  (let [items (.getItems tbl)]
    (round (/ (apply +$ (map #(.getText % n) items)) (float (count items))))))

(defn table-coumn-operation! [^Table tbl op col n]
  (let [items (.getItems tbl)]
    (dorun (map (fn[it] (.setText it n
                     (str (apply op (map #(.getText it %) col))))) items))))

(defn table-column-rest! [^Table ftbl fcol ^Table stbl scol rcol]
  (let [fit (.getItems ftbl)
        sit (.getItems stbl)]
    (dorun (map #(.setText %2 rcol
            (str (-$ (.getText % fcol) (.getText %2 scol)))) fit sit))))

(defn traverse-listener [^Widget widget func]
  (doto widget
    (. addTraverseListener
      (proxy [TraverseListener] []
        (keyTraversed [evt]
          (when (= (.detail evt) SWT/TRAVERSE_RETURN)  (func ) ))))))

(defn traverse-esc-listener [^Widget widget func & param]
  (doto widget
    (. addTraverseListener
      (proxy [TraverseListener] []
        (keyTraversed [evt]
          (cond
            (= (.detail evt) SWT/TRAVERSE_RETURN) (func 1 param)
            (= (.detail evt) SWT/TRAVERSE_ESCAPE) (func 0 param)))))))

(defn traverse-tab-listener [^Widget widget func]
  (doto widget
    (. addTraverseListener
      (proxy [TraverseListener] []
        (keyTraversed [evt]
          (when (= (.detail evt) SWT/TRAVERSE_RETURN)
            (func widget)
            (set! (.doit evt) true)
            (set! (.detail evt) SWT/TRAVERSE_TAB_NEXT )
            (.traverse widget SWT/TRAVERSE_TAB_NEXT evt)))))))

(defn modify-listener [^Widget widget func]
  (doto widget
    (. addModifyListener
      (proxy [ModifyListener] []
        (modifyText [evt]
          (func (.getText (.widget evt))))))))

(defn mouse-down-listener [^Widget widget func & param]
  (doto widget
    (. addMouseListener
      (proxy [MouseAdapter] []
        (mouseDown [evt]
          (if param (func param) (func)))))))

 (defn selection-listener [^Widget widget func & param]
  (doto widget
    (. addSelectionListener
      (proxy [SelectionAdapter] []
        (widgetSelected [evt]
          (if param (func param) (func)))))))

(defn default-selection-listener [^Widget widget func & param]
  (doto widget
    (. addSelectionListener
      (proxy [SelectionAdapter] []
        (widgetDefaultSelected [evt]
          (if param (func param) (func)))))))

(defn show-confirm [^Shell sh s]
  (when-not (.isVisible sh) (.open sh ))
  (let [mbox ( MessageBox. sh (bit-or SWT/ICON_QUESTION  SWT/YES  SWT/NO) )]
    (doto mbox
      (.setText "Підтверження")
      (.setMessage s))
      (.open mbox)))

(defn show-error [^Shell sh s]
  (when-not (.isVisible sh) (.open sh ))
    (let [mbox ( MessageBox. sh SWT/ICON_ERROR )]
      (doto mbox
        (.setText "Помилка")
        (.setMessage s)
        (.open))))

(defn show-info [^Shell sh s]
  (when-not (.isVisible sh) (.open sh ))
  (let [mbox ( MessageBox. sh SWT/ICON_INFORMATION )]
    (doto mbox
      (.setText "Повідомленн")
      (.setMessage s)
      (.open))))

(defn- show-exception [sh s e]
  (let [l-msg (.getLocalizedMessage e)
        stack (.getStackTrace e)
        msg (str (mapv #(str ( aget stack %)) (range (count stack))))]
    (.printStackTrace e)
    (if (nil? l-msg) (show-error sh (str s msg)) (show-error sh (str s l-msg)))))

(defn swt-app! [^Shell sh]
  (. sh (open))
  (loop [] (when-not (. sh (isDisposed))
               (try
                 (if (not (. display (readAndDispatch)))
                       (. display (sleep)))
                 (catch NumberFormatException e
                   (show-exception  sh "NumberFormatException: " e))
                 (catch ArithmeticException e
                   (show-exception  sh "ArithmeticException: " e))
                 (catch IndexOutOfBoundsException e
                   (show-exception  sh "IndexOutOfBoundsException: " e))
                 (catch NullPointerException e
                   (show-exception  sh "NullPointerException: " e))
                 (catch Exception e
                   (show-exception  sh "Несподівана помилка: " e)))
               (recur))))