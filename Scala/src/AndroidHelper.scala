import java.util.Date

import android.util.Log
import android.widget.{AdapterView, ArrayAdapter, BaseAdapter, Button, LinearLayout, ListView, TextView, Toast, EditText => Text, Spinner => Combo}
import android.content.Context
import android.graphics.Color
import android.text.{Editable, TextWatcher}
import android.view.{KeyEvent, LayoutInflater, View, ViewGroup}
import android.view.View.{OnClickListener, OnKeyListener}
import android.widget.AdapterView.OnItemClickListener
import AndroidHelper._

import scala.collection.mutable.ArrayBuffer
import scala.util.Try;
/**
  * Created by tol on 24.10.2016.
  */


object AndroidHelper {

  implicit class ContextExt(c: Context) {
    def showMessage(s: String) = Toast.makeText(c, s, Toast.LENGTH_LONG).show

    def showError: PartialFunction[Throwable, Unit] = {
      case e: Exception => {
        showMessage("" + e)
      }
    }
  }

  implicit class TextFieldExt(txt: Text) {
    def text = txt.getText.toString

    def text_=(a: Any) = txt.setText(a.toString)

    def onEnter(fn: Any => Unit) = {
      txt.setOnKeyListener(new OnKeyListener() {
        override def onKey(v: View, keyCode: Int, event: KeyEvent): Boolean = {
          if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction==KeyEvent.ACTION_UP) fn(txt)
          false
        }
      })
    }

    def onChange(fn: (Any, Int) => Unit) = {
      txt.setOnKeyListener(new OnKeyListener() {
        override def onKey(v: View, keyCode: Int, event: KeyEvent): Boolean = {
          fn(txt, keyCode)
          false
        }
      })
      txt.addTextChangedListener(new TextWatcher() {
        override def afterTextChanged(s: Editable) = fn(txt, 0)

        override def beforeTextChanged(ch: CharSequence, arg1: Int, arg2: Int, arg3: Int) = {}

        override def onTextChanged(ch: CharSequence, start: Int, before: Int, count: Int) = {}
      });
    }
  }

  implicit class ButtonExt(bt: Button) {
    def onSelect(fn: Any => Unit) = bt.setOnClickListener(new OnClickListener() {
      def onClick(view: View) = fn(view)
    })
  }


  implicit class ComboExt(combo: Combo) {
    def fill(ar: Array[String], context: Context): Unit = {
      val cbAdapter: ArrayAdapter[String] = new ArrayAdapter[String](context, R.layout.sp_item, ar)
      cbAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
      combo.setAdapter(cbAdapter)
      combo.setSelection(0)
    }
    def index_=(n: Int) = combo.setSelection(n)
    def index = combo.getSelectedItemPosition
    def text = combo.getSelectedItem.toString
    def text_=(s: String) = {
      val a = combo.getAdapter
      for (i <- 0 until a.getCount)
        if (s == a.getItem(i).asInstanceOf[String]) combo.setSelection(i)
    }

    def onSelect(fn: Any => Unit): Unit = {
      combo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        override def onItemSelected(adapterView: AdapterView[_], view: View, i: Int, l: Long) {
          fn(combo)
        }
        override def onNothingSelected(adapterView: AdapterView[_]): Unit = {
          return
        }
      })
    }
    
  }

}

 class ItemAdapter [T<:Item](context :Context,ar:ArrayBuffer[T],fld:Array[Int],fns:Array[(T,Int)=>Any],itT:Int)  extends BaseAdapter {
   var store = ar //.filter(it=>it(1)==6)
   val lInflater: LayoutInflater=context.getSystemService(Context.LAYOUT_INFLATER_SERVICE).asInstanceOf[LayoutInflater]
   var selectedIndex:Int= -1
   var selectedView:View=null
   var fn: (Any=>Unit)=null
   override def getCount :Int=store.size
   override def getItem(i: Int): AnyRef = if(i>=0) store(i) else null
   override def getItemId( pos:Int)=pos

   override  def getView(pos:Int,convertView:View,parent: ViewGroup):View={
     val view=if(convertView==null)lInflater.inflate(itT, parent, false)
              else convertView
     if(pos==selectedIndex){
       view.setBackgroundColor(Color.LTGRAY)
       selectedView=view
     }
     else
       view.setBackgroundColor(Color.WHITE)
     val it=getItem(pos).asInstanceOf[T]
     val layout=view.asInstanceOf[LinearLayout]
     for(i <- 0 until layout.getChildCount) {
       val v=if(fns==null) it(fld(i)).toString else fns(i)(it,fld(i)).toString
       layout.getChildAt(i).asInstanceOf[TextView].setText(v)
     }
     view
   }
 }
class Table[T<:Item](table: ListView,itT:Int) {
  var lastClickTime:Long = 0;
  var clickTime:Long=System.currentTimeMillis()
  var singleClick=true
  def changeData(arrayBuffer: ArrayBuffer[T])=table.getAdapter.asInstanceOf[ItemAdapter[T]].notifyDataSetChanged
  def index:Int=table.getAdapter.asInstanceOf[ItemAdapter[T]].selectedIndex
  def index_=(n:Int)={
    val adapter=table.getAdapter.asInstanceOf[ItemAdapter[T]]
    adapter.selectedIndex=n
    if((adapter.fn!=null) && (singleClick)) adapter.fn(adapter.getItem(adapter.selectedIndex))
  }
  def append(it:T)={
    val adapter=table.getAdapter.asInstanceOf[ItemAdapter[T]]
    adapter.notifyDataSetChanged
    adapter.selectedIndex=adapter.getCount-1
    table.setSelection(adapter.selectedIndex)
    if((adapter.fn!=null) && (singleClick)) adapter.fn(adapter.getItem(adapter.selectedIndex))
  }
  def replace(it:T)=table.getAdapter.asInstanceOf[ItemAdapter[T]].notifyDataSetChanged
  def delete(it:T)={
    val adapter=table.getAdapter.asInstanceOf[ItemAdapter[T]]
    adapter.notifyDataSetChanged
    if(adapter.getCount>0) {
      if (adapter.selectedIndex != 0) adapter.selectedIndex -= 1
      if((adapter.fn!=null) && (singleClick)) adapter.fn(adapter.getItem(adapter.selectedIndex))
    }
    else
      adapter.selectedIndex= -1
  }
  def selectedRow:Option[T]={
      val adapter = table.getAdapter.asInstanceOf[ItemAdapter[T]]
      if(adapter.selectedIndex>=0) Some(adapter.getItem(adapter.selectedIndex).asInstanceOf[T])
      else None
  }

  def fill(ar: ArrayBuffer[T],fld:Array[Int],context: Context) =
    table.setAdapter(new ItemAdapter[T](context,ar,fld,null,itT))

  def fill(ar: ArrayBuffer[T],fld:Array[Int],fn:Array[(T,Int)=>Any],context: Context) =
    table.setAdapter(new ItemAdapter[T](context,ar,fld,fn,itT))

  def onSelect(fn: Any=>Unit)={
    table.getAdapter.asInstanceOf[ItemAdapter[T]].fn=fn
    table.setOnItemClickListener(new OnItemClickListener(){
      override def onItemClick( parent: AdapterView[_], view :View, pos:Int, id:Long ): Unit ={
        val adapter=parent.getAdapter.asInstanceOf[ItemAdapter[T]]
        adapter.selectedIndex=pos
        if(adapter.selectedView!=null) adapter.selectedView.setBackgroundColor(Color.WHITE)
        adapter.selectedView=view
        view.setBackgroundColor(Color.LTGRAY)
        if (singleClick)  fn(adapter.getItem(pos))
        else {
          clickTime=System.currentTimeMillis()
          if (clickTime - lastClickTime < 300) fn(adapter.getItem(pos))
          lastClickTime = clickTime
        }
      }
    })
  }
  def onDoubleClick(fn: Any=>Unit)={
    singleClick=false
    onSelect(fn)
  }

  
}


