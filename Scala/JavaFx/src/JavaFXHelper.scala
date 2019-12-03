import java.io.IOException
import java.time.LocalDate
import java.util.Date
import javafx.beans.property.{ReadOnlyObjectWrapper, SimpleObjectProperty}
import javafx.beans.value.ObservableValue
import javafx.collections.ListChangeListener.Change
import javafx.collections.{FXCollections, ListChangeListener, ObservableList}
import javafx.event.{ActionEvent, Event, EventHandler}
import javafx.fxml.FXMLLoader
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.TableColumn.CellDataFeatures
import javafx.scene.control.cell.TextFieldTableCell
import javafx.scene.{Parent, Scene}
import javafx.scene.control.{Alert, Button, DatePicker, MenuItem, Tab, TableColumn, TableView, ComboBox => Combo, TextField => Text}
import javafx.scene.input.{KeyEvent, MouseEvent}
import javafx.stage.Stage
import javafx.util.Callback

import scala.collection.mutable.ArrayBuffer
import JavaFXHelper._

import scala.util.Try
//Created by tol on 19.10.2016.

object JavaFXHelper {

  implicit class StageExt(stage: Stage) {
    def text=stage.getTitle
    def text_=(a: Any) = stage.setTitle(a.toString)

    def showMessage(s:String): Unit ={
      val alert: Alert = new Alert(AlertType.ERROR)
      alert.setTitle("Помилка")
      alert.setHeaderText("Iнформацiя про помилку")
      alert.setContentText(s)
      alert.showAndWait
    }
    def showError: PartialFunction[Throwable, Unit]= {case e: Exception=>{showMessage(""+e)}}

    def startGUI(s: String): Parent = {
      val resource = getClass.getResource("res/"+s)
      if (resource == null) {
        throw new IOException("Cannot load resource:" + s)
      }
      val root: Parent = FXMLLoader.load(resource)
      stage.setScene(new Scene(root, 935, 440))
      stage.show()
      root
    }
    def startGUI(s: String,h:Int,w:Int): Parent = {
      val resource = getClass.getResource("res/"+s)
      if (resource == null) {
        throw new IOException("Cannot load resource:" + s)
      }
      val root: Parent = FXMLLoader.load(resource)
      stage.setScene(new Scene(root, h, w))
      stage.show()
      root
    }

    def finish=stage.close
  }

  implicit class DatePickerExt(dt: DatePicker) {
    def text=dt.getValue.toString
    def text_=(s: String) = {
      val ar=s.split("-")
      dt.setValue(LocalDate.of(ar(0).toInt,ar(1).toInt,ar(2).toInt))
    }
    def onSelect(fn:(Any)=>Unit)={
      dt.setOnKeyReleased(new EventHandler[KeyEvent]() {
        override def handle(event: KeyEvent): Unit = {
          fn(dt)
        }
      })
    }

  }

  implicit class TextFieldExt(txt: Text) {
    def text=txt.getText
    def text_=(a: Any) = txt.setText(a.toString)

    def onChange(fn:(Any,String)=>Unit)={
      txt.setOnKeyReleased(new EventHandler[KeyEvent]() {
        override def handle(event: KeyEvent): Unit = {
          fn(txt,event.getCode.getName)
        }
      })
    }
    def onEnter(fn:(Any)=>Unit)={
      txt.setOnKeyReleased(new EventHandler[KeyEvent]() {
        override def handle(event: KeyEvent): Unit = {
          if (event.getCode.getName=="Enter") fn(txt)
        }
      })
    }

  }

  implicit class ButtonExt(bt: Button) {
    def onSelect(fn: Any=>Unit)= {
      bt.setOnAction(new EventHandler[ActionEvent]() {
        override def handle(event: ActionEvent): Unit = {
          fn(bt)
        }
      })
      bt.setOnKeyReleased(new EventHandler[KeyEvent]() {
        override def handle(event: KeyEvent): Unit = {
          if (event.getCode.getName=="Enter")  fn(bt)
        }
      })
    }
  }

  implicit class TabExt(tab: Tab) {
    def onSelect(fn: Any=>Unit)= {
      tab.setOnSelectionChanged(new EventHandler[Event]() {
        override def handle(event: Event): Unit = {
          fn(tab)
        }
      })

    }
  }

  implicit class MenuExt(mn: MenuItem) {
    def onSelect(fn: Any=>Unit)= {
      mn.setOnAction(new EventHandler[ActionEvent]() {
        override def handle(event: ActionEvent): Unit = {
          fn(mn)
        }
      })
    }
  }

  implicit class TableViewExt[T<:Item](table: TableView[T]) {

    def changeData(ar: ArrayBuffer[T]){
      var lst: ObservableList[T] = FXCollections.observableArrayList()
      for(r<-ar)  lst.add(r)
      table.setItems(lst)
    }

    def append(it:T)={
      val items=table.getItems
      items.add(it)
      table.getSelectionModel.select(items.size-1)
      table.scrollTo(items.size-1)
    }

    def replace(it:T)= {
      val n:Int=table.getSelectionModel.getSelectedIndex
      table.getItems.set(n, it)
      table.getSelectionModel.select(n)
    }

    def delete(it:T)= {
      val n:Int=table.getSelectionModel.getSelectedIndex
      val items=table.getItems
      items.remove(it)
      if(items.size>0) table.getSelectionModel.select(n-1)
    }

    def fillCol[R <: Item, V](i:Int,n: Int,fn:((R,Int)=>Any)): TableColumn[R, V] = {
      var columns = table.getColumns
      var column = columns.get(i).asInstanceOf[TableColumn[R, V]]

      column.setCellValueFactory(new Callback[CellDataFeatures[R, V], ObservableValue[V]] {
        override def call(p: CellDataFeatures[R, V]): ObservableValue[V] = {
          val el=p.getValue()
          val it= fn(el,n)
//          val r=new ReadOnlyObjectWrapper(it)
          val r=new SimpleObjectProperty(it)
         r.asInstanceOf[ObservableValue[V]]
        }
      })
      column
    }

    def fill(ar:ArrayBuffer[T],flds:Array[Int],fn:Array[(T,Int)=>Any],context:AnyRef){
      for(i<-0 until fn.size)  table.fillCol(i,flds(i),fn(i))
      var lst: ObservableList[T] = FXCollections.observableArrayList()
      for(r<-ar)  lst.add(r)
      table.setItems(lst)
    }

    def fillCol[R <: Item, V](n: Int): TableColumn[R, V] = {
      var columns = table.getColumns
      var column = columns.get(n).asInstanceOf[TableColumn[R, V]]
      column.setCellValueFactory(new Callback[CellDataFeatures[R, V], ObservableValue[V]] {
        override def call(p: CellDataFeatures[R, V]): ObservableValue[V] = {
          val it=p.getValue()(n)
          val r=new ReadOnlyObjectWrapper(it)
          r.asInstanceOf[ObservableValue[V]]
        }
      })
      column
    }

    def fill(ar:ArrayBuffer[T],flds:Array[Int],context:AnyRef){
      val cnt=table.getColumns.size
      for(i<- 0 until cnt)  table.fillCol(i)
      var lst: ObservableList[T] = FXCollections.observableArrayList()
      for(r<-ar)  lst.add(r)
      table.setItems(lst)
    }

    def setFilter(ar:ArrayBuffer[T]): Unit ={
      var lst: ObservableList[T] = FXCollections.observableArrayList()
      for(r<-ar)  lst.add(r)
      table.setItems(lst)
    }

    def onSelect(fn: T=>Unit){
      table.getSelectionModel.getSelectedItems.addListener(new ListChangeListener[T]() {
          def onChanged(change: Change[_<:T]): Unit={
            val lst=change.getList
            if(lst.size!=0) fn(lst.get(0))}})
    }
    def onDoubleClick(fn: T=>Unit): Unit ={
      table.setOnMouseClicked(new EventHandler[MouseEvent] {
        override def handle(t: MouseEvent): Unit = {
          val ar=table.getSelectionModel.getSelectedItems
          if(t.getClickCount==2) fn(ar.get(0))
        }
      })
    }

    def selectedRow= {
      val ar=table.getSelectionModel.getSelectedItems
      if(ar.isEmpty) None  else Some(ar.get(0))
    }
   
    def index = table.getSelectionModel.getSelectedIndex
    def index_=(n: Int) = {
      table.getSelectionModel.select(n)
      table.scrollTo(n)
    }
  }

  implicit class ComboExt[T](cb: Combo[T]) {
    def fill(ar:Array[T],context: Stage)={
      val items=cb.getItems
      for(el<-ar) items.add(el)
      cb.getSelectionModel.clearAndSelect(0)
    }
    def text=cb.getSelectionModel.getSelectedItem
    def text_=(s:T)=cb.getSelectionModel.select(s)
    def index=cb.getSelectionModel.getSelectedIndex
    def index_=(n:Int)=cb.getSelectionModel.clearAndSelect(n)
    def onSelect(fn: Int=>Unit)= {
      cb.setOnAction(new EventHandler[ActionEvent]() {
        override def handle(event: ActionEvent): Unit = {
          fn(cb.getSelectionModel.getSelectedIndex)
        }
      })
    }
  }

}




