package mav.widgets.swt

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ControlEditor
import org.eclipse.swt.custom.TableCursor
import org.eclipse.swt.events.KeyEvent
import org.eclipse.swt.events.KeyListener
import org.eclipse.swt.events.SelectionListener
import org.eclipse.swt.widgets.*
import org.eclipse.swt.events.SelectionAdapter

import mav.core.Item
import mav.core.currentTimeMillis
import mav.core.timeToDate

open class Application() {
    val shell=Shell(Display.getDefault())
    init{
        start( shell)
        loopGui(shell)
    }
    open fun start(shell:Shell=Display.getDefault().activeShell){ }
}

const val library="SWT"

fun loopGui(shell:Shell){
    while(!shell.isDisposed()){
        try{
            if (!shell.display.readAndDispatch())
                shell.display.sleep();
            }catch (e:Exception){
                showError(e.toString(),mutableMapOf<String,Widget>())
                e.printStackTrace()
            }
    }
}

fun tryE(fn:()->Unit) =
    try{ fn()  }
    catch (e:Exception){showError(e.toString()) }

fun showError(s:String,root:MutableMap<String,Widget> = mutableMapOf()){
    with(MessageBox(Shell(Display.getDefault()),SWT.ICON_ERROR)){
        setText ("Error")
        setMessage (s)
        open()
    }
}
fun showMessage(s:String,root:MutableMap<String,Widget> = mutableMapOf()){
    with(MessageBox(Shell(Display.getDefault()),SWT.ICON_INFORMATION)){
        setText ("Message")
        setMessage (s)
        open()
    }
}

fun runAsync(f:()->Unit) {
    Display.getDefault().asyncExec(object : Runnable {
        override fun run() { f() }
    })
}

fun getKeyName(ev:KeyEvent)=when(ev.keyCode){
    8->"BACK_SPACE"
    13,16777296->"ENTER"
    27->"ECSAPE"
    32->"SPACE"
    127->"DELETE"
    16777217->"UP"
    16777218->"DOWN"
    16777219->"LEFT"
    16777220->"RIGHT"
    16777221->"PAGE_UP"
    16777222->"PAGE_DOWN"
    16777223->"HOME"
    16777224->"END"
    16777225->"INSERT"
    else->ev.keyCode.toChar().toString().toUpperCase()
}
fun searchForCombo(cb:MavCombo,s:String,n:Int){
    val combo=cb.widget
    val ss=s.split(" ")
    val len=combo.items.size
    for (i in 0 until len) {
        val item=combo.items[i].toLowerCase()
        val names=item.split(" ")
        if (names.size==n+1) {
            if (names[n].startsWith(s)){
                cb.setIndex(i)
            }
        }
        else if (ss.size>1){
            if (names[n].startsWith(ss[0]) && names[n+1].startsWith(ss[1])) {
                cb.setIndex(i)
            }
        }
        else if (s.length>0 && ss.size==1) {
            if (s.startsWith(" ") && names[n+1].startsWith(ss[0])) {
                cb.setIndex(i)
            }
            else if (!s.startsWith(" ") && names[n].startsWith(ss[0])){
                cb.setIndex(i)
            }
        }
    }
}
fun searchForTable(table:MavTable,s:String,n:Int){
    val ss=s.split(" ")
    val len=table.store?.size ?:0
    var row=0
    for (i in 0 until len) {
        val item = table.store!![i]
        val _names = if( n == 1) item[1].toString() + " " + item[2].toString() else item[1].toString()
        val _names_ = _names.toLowerCase()
        val names  = _names_.split(" ")
        if (names.size == 1) {
            if (names[0].startsWith(s)) row = i
        }
        else if (ss.size > 1) {
            if (names[0].startsWith(ss[0]) && names[1].startsWith(ss[1]))  row = i
        }
        else if (s.length>0 &&  ss.size==1) {
            if (s.startsWith(" ") && names[1].startsWith(ss[0])) row = i
            else if (!s.startsWith(" ") && names[0].startsWith(ss[0])) row = i
        }
    }
    table.setIndex(row)
    table.clickFun(row)
}

open class MavWidget{
    open val widget:Control
    var root:MutableMap<String,Widget> =mutableMapOf()
    constructor(name:String,_root:MutableMap<String,Widget>){
        root=_root
        widget=root[name] as Control
    }
    constructor( _widget:Control){  widget=_widget }
    var visible:Boolean
        get()=widget.visible
        set(flg) {widget.visible=flg }
    var enable:Boolean
        get()=widget.enabled
        set(flg){ widget.visible=flg }
    fun setFocus(){ widget.forceFocus() }
}

open class MavMenu {
    val widget: Menu
    constructor(name:String,root:MutableMap<String,Widget>){  widget=root[name] as Menu  }
    constructor(_widget:Menu){ widget=_widget }
    fun show(w:Widget,c:Int):MavMenu= this
    fun hide():MavMenu= this
}

open class MavItem {
    val widget: MenuItem
    constructor(name:String,root:MutableMap<String,Widget>){  widget=root[name] as MenuItem  }
    constructor(_widget:MenuItem){ widget=_widget }
    var text:String
        get()=widget.text
        set(s) { widget.text=s }
    var enable:Boolean
        get()=widget.enabled
        set(flg) { widget.enabled =flg}
    fun setText(s:String):MavItem{
        widget.text=s
        return this
    }
    fun onClick(fn:()->Unit): MavItem {
        widget.addSelectionListener(SelectionListener.widgetSelectedAdapter{fn()})
        return this
    }
}

open class MavWindow:MavWidget {
    override val widget: Shell
    constructor(name:String,root:MutableMap<String,Widget>):super(name,root){  widget=root[name] as Shell  }
    constructor(_widget:Shell):super(_widget){ widget=_widget }
    var text:String
        get()=widget.text
        set(s){ widget.text=s }
    fun show():MavWindow{
        widget.open()
        widget.layout()
        return this
    }
    fun close():MavWindow{
        widget.close()
        return this
    }
    fun setText(s:String):MavWindow{
        widget.text=s
        return this
    }
}

open class MavDate:MavWidget {
    override val widget:DateTime
    constructor(name:String,root:MutableMap<String,Widget>):super(name,root){  widget=root[name] as DateTime  }
    constructor(_widget:DateTime):super(_widget){ widget=_widget }
    private fun dig2(v:Int):String=if(v>9) v.toString() else "0"+v.toString()
    var text:String
        get()=widget.year.toString()+"-"+dig2(widget.month+1)+"-"+dig2(widget.day)
        set(s){
            val year=s.substring(0,4).toInt()
            val month=s.substring(5,7).toInt()-1
            val day=s.substring(8,10).toInt()
            widget.setDate(year, month, day)
        }
    fun setText(s:String):MavDate{
        val year=s.substring(0,4).toInt()
        val month=s.substring(5,7).toInt()-1
        val day=s.substring(8,10).toInt()
        widget.setDate(year, month, day)
        return this
    }
    fun setFirstDay():MavDate{
        this.setText(timeToDate(currentTimeMillis(), true))
        return this
    }
    fun setCurrentDay():MavDate{
        this.setText(timeToDate(currentTimeMillis(), false))
        return this
    }
    fun onChange(fn:()->Unit):MavDate{
        widget.addSelectionListener(SelectionListener.widgetSelectedAdapter{fn()})
        return this
    }
}

open class MavLabel:MavWidget {
    override val widget: Label
    constructor(name:String,root:MutableMap<String,Widget>):super(name,root){  widget=root[name] as Label  }
    constructor(_widget:Label):super(_widget){ widget=_widget }
    var text:String
        get()=widget.text
        set(s) { widget.text=s }
    fun setText(s:String):MavLabel{
        widget.text=s
        return this
    }
}

open class MavText:MavWidget {
    override val widget: Text
    constructor(name:String,root:MutableMap<String,Widget>):super(name,root){  widget=root[name] as Text  }
    constructor(_widget:Text):super(_widget){ widget=_widget }
    var text:String
        get()=widget.text
        set(s) { widget.text=s }
    fun setText(s:String):MavText{
        widget.text=s
        return this
    }
    fun selectAll():MavText{
        widget.selectAll()
        return this
    }
    fun onChange(fn:()->Unit):MavText{
        widget.addModifyListener { fn()  }
        return this
    }
    fun onKey(fn:(KeyEvent)->Unit):MavText{
        widget.addKeyListener(KeyListener.keyReleasedAdapter { fn(it) })
        return this
    }
}

open class MavCombo:MavWidget{
    override val widget:Combo
    constructor(name:String,root:MutableMap<String,Widget>):super(name,root){  widget=root[name] as Combo  }
    constructor(_widget:Combo):super(_widget){ widget=_widget }
    var text:String
        get()=widget.text
        set(s) { widget.text=s }
    var index:Int
        get()=widget.selectionIndex
        set(n) { widget.select(n) }
    fun setIndex(n:Int):MavCombo{
        widget.select(n)
        return this
    }
    fun setText(s:String):MavCombo{
        widget.text=s
        return this
    }
    fun fill(lst:List<String>):MavCombo{
        widget.removeAll()
        for(el in lst)    widget.add(el)
        widget.select(0)
        return this
    }
    fun fill(lst:List<String>,ar:Array<String>):MavCombo{
        widget.removeAll()
        for(el in ar)  widget.add(el)
        for(el in lst) widget.add(el)
        widget.select(0)
        return this
    }
    fun onChange(fn:()->Unit):MavCombo{
        widget.addModifyListener { fn()  }
        return this
    }
}

open class MavCheck:MavWidget {
    override val widget:Button
    constructor(name:String,root:MutableMap<String,Widget>):super(name,root){  widget=root[name] as Button  }
    constructor(_widget:Button):super(_widget){ widget=_widget }
    var checked:Boolean
        get()=widget.selection
        set(flg) { widget.selection=flg }
    var text:String
        get()=widget.text
        set(s) { widget.text=s }
    fun setText(s: String): MavCheck {
        widget.text=s
        return this
    }
    fun onChange(fn:()->Unit): MavCheck {
        widget.addSelectionListener(SelectionListener.widgetSelectedAdapter{fn()})
        return this
    }
}

open class MavRadio:MavWidget {
    override val widget:Button
    constructor(name:String,root:MutableMap<String,Widget>):super(name,root){  widget=root[name] as Button  }
    constructor(_widget:Button):super(_widget){ widget=_widget }
    var checked:Boolean
        get()=widget.selection
        set(flg) { widget.selection=flg }
    var text:String
        get()=widget.text
        set(s) { widget.text=s }
    fun setText(s: String): MavRadio {
        widget.text=s
        return this
    }
    fun onChange(fn:()->Unit): MavRadio {
        widget.addSelectionListener(SelectionListener.widgetSelectedAdapter{fn()})
        return this
    }
}

open class MavButton:MavWidget{
    override val widget:Button
    constructor(name:String,root:MutableMap<String,Widget>):super(name,root){  widget=root[name] as Button  }
    constructor(_widget:Button):super(_widget){ widget=_widget }
    var text:String
        get()=widget.text
        set(s) { widget.text=s }
    fun setText(s: String): MavButton {
        widget.text=s
        return this
    }
    fun onClick(fn:()->Unit): MavButton {
        widget.addSelectionListener(SelectionListener.widgetSelectedAdapter{fn()})
        return this
    }
}

open class MavFlatButton {
    var widget:ToolItem
    constructor(name:String,root:MutableMap<String,Widget>){  widget=root[name] as ToolItem  }
    constructor(_widget:ToolItem){ widget=_widget }

    var text:String
        get()=widget.text
        set(s) { widget.text=s }
    fun setText(s: String): MavFlatButton {
        widget.text=s
        return this
    }
    fun onClick(fn:()->Unit): MavFlatButton {
        widget.addSelectionListener(SelectionListener.widgetSelectedAdapter{fn()})
        return this
    }
}

open class MavTable:MavWidget{
    override val widget:Table
    val cols:Int
    constructor(name:String,root:MutableMap<String,Widget>,_cols:Int):super(name,root){
        widget=root[name] as Table
        cols=_cols
        init()
    }
    constructor(_widget:Table,_cols:Int):super(_widget){
        widget=_widget
        cols=_cols
        init()
    }
    var store:ArrayList<out Item>?=null
    private var flds:Array<Int>?=null
    private var fns:Array<out (Item, Int)->Any>?=null
    var clickFun:(Int)->Unit= {}
    var dblClickFun:(Int)->Unit= {}
    var index:Int
        get()=widget.selectionIndex
        set(n) { widget.setSelection(n) }
    fun init(){
        widget.headerBackground=Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND)
        widget.addListener(SWT.MeasureItem, { it.height=25  })
    }
    fun setHeaderText(n:Int,s:String):MavTable{
        widget.columns[n].text=s
        return this
    }
    fun<T: Item> getSelectedRow():T?=store!![widget.selectionIndex] as T
    fun setIndex(n:Int):MavTable{
        widget.setSelection(n)
        return this
    }
    fun scrollTo(n:Int):MavTable=this
    fun<T: Item> add(item:T):MavTable{
        var row=TableItem(widget,SWT.NONE)
        for (j in 0 until cols) {
            val value=if(fns==null) item[j] else  fns!![j](item,flds!![j])
            row.setText(j,value.toString())
        }
        return this
    }
    fun<T: Item> change(item:T):MavTable{
        val row=widget.selection[0]
        for (j in 0 until cols) {
            val value=if(fns==null) item[j] else  fns!![j](item,flds!![j])
            row.setText(j,value.toString())
        }
        return this
    }
    fun<T: Item> delete():MavTable{
        val l=this.index
        widget.remove(l)
        if(l>0) this.setIndex(l-1)
        return this
    }
   fun<T: Item>fill(ar:ArrayList<T>):MavTable{
        this.store=ar// as ArrayList<out Item>
        widget.removeAll()
        for(el in ar) add(el)
        return this
    }
    fun<T: Item>fill(ar:ArrayList<T>, flds:Array<Int>, fns:Array< (T, Int)->Any>):MavTable{
        this.store=ar// as ArrayList<out Item>
        this.flds=flds
        this.fns=fns as Array<out (Item, Int)->Any>
        widget.removeAll()
        for(el in ar) add(el)
        return this
    }
    fun onClick(fn:(Int)->Unit):MavTable{
        this.clickFun=fn
        widget.addSelectionListener(SelectionListener.widgetSelectedAdapter{ fn(widget.selectionIndex) })
            return this
        }
    fun onDblClick(fn:(Int)->Unit):MavTable{
        this.dblClickFun=fn
        widget.addSelectionListener(SelectionListener.widgetDefaultSelectedAdapter{ fn(widget.selectionIndex) })
        return this
    }
    fun onMenu(fn:(Int)->Unit){
        widget.addMenuDetectListener { fn(0) }
    }
}

open class MavGrid:MavWidget {
    override val widget:Table
    val cols:Int
    private val cursor:TableCursor
    private val editor:ControlEditor
    private val text:Text
    constructor(name:String,root:MutableMap<String,Widget>,_cols:Int):super(name,root){
        widget=root[name] as Table
        cols=_cols
        cursor= TableCursor(widget, SWT.NONE)
        editor = ControlEditor(cursor)
        text = Text(cursor, SWT.NONE)
        init()
    }
    constructor(_widget:Table,_cols:Int):super(_widget){
        widget=_widget
        cols=_cols
        cursor= TableCursor(widget, SWT.NONE)
        editor = ControlEditor(cursor)
        text = Text(cursor, SWT.NONE)
        init()
    }
    var store: ArrayList<out Item>? = null
    lateinit  var editables:ArrayList<Boolean>
    private var flds: Array<Int>? = null
    private var fns: Array<out (Item, Int) -> Any>? = null
    var index:Int
        get()=widget.selectionIndex
        set(n) { widget.setSelection(n) }
    fun init() {
        editables=ArrayList()
        for (i in 0 until cols)  editables.add(false)
        widget.headerBackground = Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND)
        widget.addListener(SWT.MeasureItem, { it.height = 25 })
        widget.addListener(SWT.EraseItem) { event ->
            if (event.detail and SWT.SELECTED !== 0) {
                event.detail = event.detail and SWT.SELECTED.inv()
            }
        }
        editor.grabHorizontal = true
        editor.grabVertical = true
    }
    fun setEditables(ar:ArrayList<Boolean>):MavGrid{
        editables=ar
        return this
    }
    fun <T : Item> add(item: T): MavGrid {
        var row = TableItem(widget, SWT.NONE)
        for (j in 0 until cols) {
            val value = if (fns == null) item[j] else fns!![j](item, flds!![j])
            row.setText(j, value.toString())
        }
        return this
    }
    fun <T : Item> change(item: T): MavGrid {
        val row = widget.selection[0]
        for (j in 0 until cols) {
            val value = if (fns == null) item[j] else fns!![j](item, flds!![j])
            row.setText(j, value.toString())
        }
        return this
    }
    fun <T : Item> fill(ar: ArrayList<T>): MavGrid {
        this.store = ar// as ArrayList<out Item>
        widget.removeAll()
        for (el in ar) add(el)
        return this
    }
    fun <T : Item> fill(ar: ArrayList<T>, flds: Array<Int>, fns: Array<(T, Int) -> Any>): MavGrid {
        this.store = ar// as ArrayList<out Item>
        this.flds = flds
        this.fns = fns as Array<out (Item, Int) -> Any>
        widget.removeAll()
        for (el in ar) add(el)
        return this
    }
    fun onEdit(fn: (Item, Int, String) -> Unit) {
        cursor.addSelectionListener(SelectionListener.widgetSelectedAdapter {
            text.setVisible(false)
        })
        cursor.addSelectionListener(SelectionListener.widgetDefaultSelectedAdapter {
            if(editables[cursor.column]) {
                text.setVisible(true)
                editor.setEditor(text)
                text.setText(cursor.getRow().getText(cursor.getColumn()));
                text.setFocus()
            }
        })
        text.addTraverseListener {
            if (it.detail == SWT.TRAVERSE_RETURN) {
                cursor.row.setText(cursor.getColumn(), text.getText())
                lateinit var row: Item
                for(i in 0 until widget.itemCount)
                    if (cursor.row==widget.getItem(i)) row=store!![i]
                fn(row,cursor.getColumn(),text.text)
                if (cursor.getColumn() < 2) {
                    cursor.setSelection(cursor.getRow(), cursor.getColumn() + 1);
                    text.setText(cursor.getRow().getText(cursor.getColumn()));
                }
             else
                text.setVisible(false);
            }
        }
    }
}

open class MavTab {
    var widget:TabFolder
    constructor(name:String,root:MutableMap<String,Widget>){  widget=root[name] as TabFolder  }
    constructor(_widget:TabFolder){ widget=_widget }
    fun onChange(fn:(String)->Unit):MavTab{
        widget.addSelectionListener(object : SelectionAdapter() {
            override fun widgetSelected(event: org.eclipse.swt.events.SelectionEvent?) {
                fn(widget.getSelection()[0].getText())
            }
        })
        return this
    }
}