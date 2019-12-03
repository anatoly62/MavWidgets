
import scala.scalajs.js.Dynamic
import Dynamic.{global => g}
import org.scalajs.dom
import dom.html.{Button, Div, Image, Table, TableCell, TableRow, TableHeaderCell,  Input => Text, Select => Combo,Element}
import java.util.Date
import org.scalajs.dom.raw.HTMLLinkElement
import HtmlHelper._
import scala.collection.mutable.ArrayBuffer
import scala.util.Try

/**
  * Created by tol on 30.10.2016.
  */
object HtmlHelper {

  implicit class DivExt(global: Div) {
    def showMessage(s:String)= g.alert(s)
    def showError: PartialFunction[Throwable, Unit]= {case e: Exception=>{showMessage(""+e)}}
    def showGui={
      global.style.display="block"
      g.pane=global
      g.addEventListener("mousedown", g.onMouseDown)
      g.addEventListener("touchstart", g.onTouchDown)
      g.animate()
    }
    def finish=global.style.display="none"
  }

  implicit class TextExt(txt: Text) {
    def text=txt.value
    def text_=(a: Any) = txt.value=a.toString
    def onChange(fn:(Any,Int)=>Unit)= txt.onkeyup = (e: dom.KeyboardEvent) =>fn(txt,e.keyCode)
    def onEnter(fn:Any=>Unit)= txt.onkeyup = (e: dom.KeyboardEvent) =>if(e.keyCode==13) fn(txt)
  }

  implicit class THeaderExt(txt:TableHeaderCell) {
    def text=txt.innerHTML
    def text_=(a: Any) = txt.innerHTML=a.toString
  }
  implicit class TableCellExt(txt:TableCell) {
    def text=txt.innerHTML
    def text_=(a: Any) = txt.innerHTML=a.toString
  }

  implicit class ButtonExt(bt: Button) {
    def onSelect(fn: Any=>Unit):Unit= bt.onclick=fn
  }
  implicit class ImageExt(im: Image) {
    def onSelect(fn: Any=>Unit):Unit= im.onclick=fn
  }
  implicit class LinlExt(ln: HTMLLinkElement) {
    def onSelect(fn: Any=>Unit):Unit= ln.onclick=fn
  }

  implicit class ComboExt[T](cb: Combo) {
    def fill(ar:Array[T],context: AnyRef)={
      var html:String=""
      for(el<-ar )  html+="<option>"+el+"</option>"
      cb.innerHTML=html
    }
    def index=cb.selectedIndex
    def index_=(n:Int)=cb.selectedIndex=n
    def text=cb.options(cb.selectedIndex).text
    def text_=(s:String)={
      for(i<-0 until cb.options.length)
        if(s==cb.options(i).text)
          cb.options(i).selected=true
    }
    def onSelect(fn:Any=>Unit)=cb.onchange=fn
   

  }
}

class  TableView[T<:Item](table:Table) {
  var data:ArrayBuffer[Item]=new ArrayBuffer[Item]
  var selectedIndex:Int= -1
  var click:Int=0
  var func:(T)=>Unit=null
  var fns: Array[(T,Int)=>Any]=null
  var flds: Array[Int]=null

  def index=this.selectedIndex
  def index_=(n:Int)={
    if(selectedIndex>=0) table.rows(selectedIndex).asInstanceOf[TableRow].style.background="#ffffff"
    selectedIndex=n
    val row=table.rows(selectedIndex).asInstanceOf[TableRow]
    row.style.background="#00bbee"
    row.scrollIntoView(false)
    if(click==0)func(data(selectedIndex).asInstanceOf[T])
  }
  def selectRow(row:TableRow)={
    val rows=table.rows
    for(i<- 0 until rows.length)
      if(row==rows(i)) {
        if(selectedIndex>=0) table.rows(selectedIndex).asInstanceOf[TableRow].style.background="#ffffff"
        selectedIndex=i
        row.style.background="#00bbee"
    }
  }

  def append(it:T)={
    var r=""
    val cntCols=if(fns==null) it.fields.size else fns.size
    for(i <- 0 until cntCols){
      val s=if(fns==null) it(i) else (fns(i))(it,flds(i))
      r += "<td>" + s.toString + "</td>"
    }
    table.innerHTML+=r
    if(selectedIndex>=0)table.rows(selectedIndex).asInstanceOf[TableRow].style.background="#ffffff"
    selectedIndex=data.size-1
    table.rows(selectedIndex).asInstanceOf[TableRow].style.background="#00bbee"
    if(click==0) {
      onSelect(func)
      func(data(selectedIndex).asInstanceOf[T])
    }
    else onDoubleClick(func)
  }

  def replace(it:T)= {
    data(selectedIndex)=it
    val cntCols=if(fns==null) it.fields.length-1 else fns.length
    for(i <- 0 until cntCols){
      val s=if(fns==null) it(i) else (fns(i))(it,flds(i))
      table.rows(selectedIndex).asInstanceOf[TableRow].cells(i).asInstanceOf[TableCell].innerHTML=s.toString
    }
  }

  def delete(it:T)= {
    val n:Int=selectedIndex
    table.deleteRow(n)
    if(data.size>0) {
      selectedIndex=n-1
      table.rows(selectedIndex).asInstanceOf[TableRow].style.background="#00bbee"
      if(click==0) {
        onSelect(func)
        func(data(selectedIndex).asInstanceOf[T])
      }
      else onDoubleClick(func)
    }
    else selectedIndex= -1
  }

  def fill(ar: ArrayBuffer[T], flds: Array[Int],context:Div)  = {
    data=ar.asInstanceOf[ArrayBuffer[Item]]
    var html = ""
    for (i <- 0 until ar.size) {
      var r = ""
      for (j <- 0 until flds.size-1)  r += "<td>" + ar(i)(j) + "</td>"
      html += "<tr>" + r + "</tr>"
    }
    table.innerHTML = html
  }
  def fill(ar: ArrayBuffer[T], flds: Array[Int],fn: Array[(T,Int)=>Any],context:Div)  = {
    data=ar.asInstanceOf[ArrayBuffer[Item]]
    var html = ""
    fns=fn
    this.flds=flds
    for (i <- 0 until ar.size) {
      var r = ""
      val row=ar(i)
      for (j <- 0 until fn.size)  r += "<td>" + (fn(j))(row,flds(j)) + "</td>"
      html += "<tr>" + r + "</tr>"
    }
    table.innerHTML = html
  }


  def onSelect(fn:(T)=>Unit)= {
    for (i <- 0 until table.rows.length) {
      var row = table.rows(i).asInstanceOf[TableRow]
      row.onclick= (e: dom.MouseEvent) => getHandler[T](i,fn)
    }
    func=fn
    click=0
  }
  def onDoubleClick(fn:(T)=>Unit)= {
    for (i <- 0 until table.rows.length) {
      var row = table.rows(i).asInstanceOf[TableRow]
      row.onclick= (e: dom.MouseEvent) => getHandler[T](i,println)
      row.ondblclick= (e: dom.MouseEvent) => getHandler[T](i,fn)
    }
    func=fn
    click=1
  }

  def selectedRow: Option[T] = if(selectedIndex>=0) Some(data(selectedIndex).asInstanceOf[T]) else None

  def getHandler[T<:Item]( n:Int,fn:(T)=>Unit):Unit = {
      if (selectedIndex >= 0)
         table.rows(selectedIndex).asInstanceOf[TableRow].style.background = "#ffffff"
      selectedIndex=n
      table.rows(n).asInstanceOf[TableRow].style.background="#00bbee"
      fn(data(n).asInstanceOf[T])
    }

  
  def colEditable(ar:Array[Int])={
    val r=table.rows
    for(i<-0 until r.length){
      val c=r(i).asInstanceOf[TableRow].cells
      for (j<-0 until c.length){
        for(k<-0 until ar.length){
          if (j==ar(k)) c(j).setAttribute("contenteditable","true")
        }
      }
    }
  }
  def onEnter(fn:(Any,Option[T])=>Unit)= table.onkeydown = (e: dom.KeyboardEvent) =>{
    if(e.keyCode==13) {
      val cell=e.target.asInstanceOf[TableCell]
      cell.blur()
      val r=cell.parentNode.asInstanceOf[TableRow].cells
      var ar=new Array[String](r.length)
      for (i<-0 until r.length) ar(i)=r(i).innerHTML
      fn(ar,this.selectedRow)
      if(cell.nextSibling!=null) cell.nextSibling.asInstanceOf[TableCell].focus()
      e.preventDefault()
    }
  }

}

