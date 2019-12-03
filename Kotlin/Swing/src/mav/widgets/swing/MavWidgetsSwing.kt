package mav.widgets.swing

import org.jdatepicker.impl.JDatePickerImpl
import javax.swing.table.AbstractTableModel
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar;
import java.awt.*
import javax.swing.*
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JTable
import java.awt.event.MouseAdapter
import java.awt.event.KeyListener
import java.awt.event.MouseEvent
import java.awt.event.KeyEvent

import mav.core.Item
import mav.core.currentTimeMillis
import mav.core.timeToDate

open class Application(){ companion object { open fun start(fn:()->Unit,args: Array<String>){ javax.swing.SwingUtilities.invokeLater{  fn() } } } }

fun searchForCombo(cb:MavCombo,s:String,n:Int){
    val combo=cb.widget
    val ss=s.split(" ")
    val len=combo.model.size
    for (i in 0 until len) {
        val item=combo.model.getElementAt(i).toLowerCase()
        val names=item.split(" ")
        if (names.size==n+1) {
            if (names[n].startsWith(s)){
                combo.selectedIndex=i
            }
        }
        else if (ss.size>1){
            if (names[n].startsWith(ss[0]) && names[n+1].startsWith(ss[1])) {
                combo.selectedIndex=i
            }
        }
        else if (s.length>0 && ss.size==1) {
            if (s.startsWith(" ") && names[n+1].startsWith(ss[0])) {
                combo.selectedIndex=i
            }
            else if (!s.startsWith(" ") && names[n].startsWith(ss[0])){
                combo.selectedIndex=i
            }
        }
    }
}

fun searchForTable(table:MavTable,s:String,n:Int){
    val ss=s.split(" ")
    val len=table.store.size
    var row=0
    for (i in 0 until len) {
        val item = table.store[i]
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
    table.scrollTo(row)
    table.clickFun(row)
}

fun getKeyName(ev:KeyEvent)= when(ev.keyCode){
        8->"BACK_SPACE"
        10->"ENTER"
        27->"ECSAPE"
        32->"SPACE"
        33->"PAGE_UP"
        34->"PAGE_DOWN"
        35->"END"
        36->"HOME"
        37->"LEFT"
        38->"UP"
        39->"RIGHT"
        40->"DOWN"
        127->"DELETE"
        155->"INSERT"
        else->ev.keyCode.toChar().toString().toUpperCase()
}

fun tryE(fn:()->Unit) =
    try{ fn()  }
    catch (e:Exception){showError(e.toString()) }

fun showError(s:String,root:MutableMap<String,Component> = mutableMapOf()){
    JOptionPane.showMessageDialog(null,s, "Error",JOptionPane.ERROR_MESSAGE)
}
fun showMessage(s:String,root:MutableMap<String,Component> = mutableMapOf()){ JOptionPane.showMessageDialog(null,s)}

open class MavWidget{
    open val widget:Component
    var root:MutableMap<String,Component> =mutableMapOf()
    constructor(name:String,_root:MutableMap<String,Component>){
        root=_root
        widget=root[name]!!
    }
    constructor( _widget:Component){  widget=_widget }
    var visible:Boolean
        get()=widget.isVisible
        set(flg) {widget.setVisible(flg)}
    var enable:Boolean
        get()=widget.isEnabled
        set(flg) {widget.setEnabled(flg)}
    fun setFocus(){widget.requestFocus()}
}

open class MavItem:MavWidget {
    override val widget:JMenuItem
    constructor(name:String,root:MutableMap<String,Component>):super(name,root){  widget=root[name] as JMenuItem  }
    constructor(_widget:JMenuItem):super(_widget){ widget=_widget }
    var text:String
        get()=widget.text
        set(s){ widget.text=s }
    fun setText(s:String):MavItem{
        widget.text=s
        return this
    }
    fun onClick(fn:()->Unit):MavItem{
        widget.addActionListener { fn() }
        return this
    }
}

open class MavMenu:MavWidget {
    override val widget:JPopupMenu
    constructor(name:String,root:MutableMap<String,Component>):super(name,root){  widget=root[name] as JPopupMenu  }
    constructor(_widget:JPopupMenu):super(_widget){ widget=_widget }

    fun show(w:Component,e:MouseEvent):MavMenu{
        widget.show(w,e.x,e.y)
        return this
    }
    fun hide():MavMenu{
        widget.hide()
        return this
    }
}

open class MavWindow:MavWidget {
    override val widget: JFrame
    constructor(name:String,root:MutableMap<String,Component>):super(name,root){  widget=root[name] as JFrame  }
    constructor(_widget:JFrame):super(_widget){ widget=_widget }
    var text:String
        get()=widget.title
        set(s:String){ widget.title=s }
    fun show():MavWindow{
        widget.isVisible=true
        return this
    }
    fun close():MavWindow{
        widget.dispose()
        return this
    }
    fun setText(s:String):MavWindow{
        widget.title=s
        return this
    }
}

open class MavCheck:MavWidget{
    override val widget:JCheckBox
    constructor(name:String,root:MutableMap<String,Component>):super(name,root){  widget=root[name] as JCheckBox  }
    constructor(_widget:JCheckBox):super(_widget){ widget=_widget }
    var checked:Boolean
        get()=widget.isSelected
        set(flg) { widget.isSelected=flg }
    var text:String
        get()=widget.text
        set(s:String){ widget.text=s }
    fun setText(s: String): MavCheck {
        widget.text=s
        return this
    }
    fun onChange(fn:()->Unit): MavCheck {
        widget.addActionListener { fn() }
        return this
    }
}

open class MavRadio:MavWidget {
    override val widget:JRadioButton
    constructor(name:String,root:MutableMap<String,Component>):super(name,root){  widget=root[name] as JRadioButton  }
    constructor(_widget:JRadioButton):super(_widget){ widget=_widget }
    var checked:Boolean
        get()=widget.isSelected
        set(flg) { widget.isSelected=flg }
    var text:String
        get()=widget.text
        set(s:String){ widget.text=s }
    fun setText(s: String): MavRadio {
        widget.text=s
        return this
    }
    fun onChange(fn:()->Unit): MavRadio {
        widget.addActionListener { fn() }
        return this
    }
}

open class MavLabel:MavWidget {
    override val widget:JLabel
    constructor(name:String,root:MutableMap<String,Component>):super(name,root){  widget=root[name] as JLabel  }
    constructor(_widget:JLabel):super(_widget){ widget=_widget }
    var text:String
        get()=widget.text
        set(s:String){ widget.text=s }
    fun setText(s: String): MavLabel {
        widget.text=s
        return this
    }
}

open class MavButton:MavWidget {
    override val widget:JButton
    constructor(name:String,root:MutableMap<String,Component>):super(name,root){  widget=root[name] as JButton  }
    constructor(_widget:JButton):super(_widget){ widget=_widget }
    var text:String
        get()=widget.text
        set(s:String){ widget.text=s }
    fun setText(s: String): MavButton {
        widget.text=s
        return this
    }
    fun onClick(fn:()->Unit): MavButton {
        widget.addActionListener { fn() }
        widget.addKeyListener( object : KeyListener {
            override fun keyPressed(keyEvent: KeyEvent) { }
            override fun keyReleased(keyEvent: KeyEvent) {if(keyEvent.keyCode==10) fn() }
            override fun keyTyped(keyEvent: KeyEvent) { }
        })
        return this
    }
}

open class MavFlatButton:MavWidget {
    override val widget:JButton
    constructor(name:String,root:MutableMap<String,Component>):super(name,root){  widget=root[name] as JButton  }
    constructor(_widget:JButton):super(_widget){ widget=_widget }
    var text:String
        get()=widget.text
        set(s:String){ widget.text=s }
    fun setText(s: String): MavFlatButton {
        widget.text=s
        return this
    }
    fun onClick(fn:()->Unit): MavFlatButton {
        widget.addActionListener { fn() }
        return this
    }
}

open class MavText:MavWidget {
    override val widget:JTextField
    constructor(name:String,root:MutableMap<String,Component>):super(name,root){  widget=root[name] as JTextField  }
    constructor(_widget:JTextField):super(_widget){ widget=_widget }
    var text:String
        get()=widget.text
        set(s:String){ widget.text=s }
    fun setText(s:String):MavText{
        widget.text=s
        return this
    }
    fun selectAll():MavText{
        widget.selectAll()
        return this
    }
    fun onChange(fn:()->Unit):MavText{
        widget.addKeyListener( object : KeyListener {
            override fun keyPressed(keyEvent: KeyEvent) { }
            override fun keyReleased(keyEvent: KeyEvent) {fn() }
            override fun keyTyped(keyEvent: KeyEvent) { }
        })
        return this
    }
    fun onKey(fn:(key:KeyEvent)->Unit):MavText{
        widget.addKeyListener( object : KeyListener {
            override fun keyPressed(keyEvent: KeyEvent) { }
            override fun keyReleased(keyEvent: KeyEvent) {fn(keyEvent) }
            override fun keyTyped(keyEvent: KeyEvent) { }
        })
        return this
    }
}

open class MavDate:MavWidget {
    override val widget:JDatePickerImpl
    constructor(name:String,root:MutableMap<String,Component>):super(name,root){  widget=root[name] as JDatePickerImpl  }
    constructor(_widget:JDatePickerImpl):super(_widget){ widget=_widget }
    var text:String
        get(){
            val ar=widget.jFormattedTextField.text.split("-")
            return ar[2]+"-"+ar[1]+"-"+ar[0]
        }
        set(s:String){
            val ar=s.split("-")
            widget.jFormattedTextField.text=ar[2]+"-"+ar[1]+"-"+ar[0]
        }
    fun setText(s:String):MavDate{
        val ar=s.split("-")
        widget.jFormattedTextField.text=ar[2]+"-"+ar[1]+"-"+ar[0]
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
        widget.addActionListener { fn() }
        return this
    }
}

open class MavCombo:MavWidget {
    override val widget:JComboBox<String>
    constructor(name:String,root:MutableMap<String,Component>):super(name,root){  widget=root[name] as JComboBox<String>  }
    constructor(_widget:JComboBox<String>):super(_widget){ widget=_widget }
    var text:String
        get()=widget.selectedItem.toString()
        set(s){
            val model=widget.model as DefaultComboBoxModel
            for (i in 0 until widget.itemCount)
                if(model.getElementAt(i)==s){
                    widget.selectedIndex=i
                    break
                }
        }
    var index:Int
        get()=widget.selectedIndex
        set(n){ widget.selectedIndex=n }
    fun setIndex(n:Int):MavCombo{
        widget.selectedIndex=n
        return this
    }
    fun setText(s:String):MavCombo{
        val model=widget.model as DefaultComboBoxModel
        for (i in 0 until widget.itemCount)
            if(model.getElementAt(i)==s){
                widget.selectedIndex=i
                break
            }
        return this
    }
    fun onChange(fn:()->Unit):MavCombo{
        widget.addActionListener { fn() }
        return this
    }
    fun fill(lst:List<String>):MavCombo{
        widget.setModel(DefaultComboBoxModel(lst.toTypedArray()))
        return this
    }
    fun fill(lst:List<String>,ar:Array<String>):MavCombo{
        val items=ArrayList<String>()
        for (el in ar) items.add(el)
        for (el in lst) items.add(el)
        widget.setModel(DefaultComboBoxModel(items.toTypedArray()))
        return this
    }
}

open class MavTable:MavWidget {
    override val widget:JTable
    val cols:Int
    private val model=MyModel()
    constructor(name:String,root:MutableMap<String,Component>,_cols:Int):super(name,root){
        widget=root[name] as JTable
        cols=_cols
        init()
    }
    constructor(_widget:JTable,_cols:Int):super(_widget){
        widget=_widget
        cols=_cols
        init()
    }
    var clickFun:(Int)->Unit={}
    var dblClickFun:(Int)->Unit={}

    var index:Int
        get()=widget.selectedRow
        set(n) { widget.setRowSelectionInterval(n, n) }
    var store:ArrayList<out Item>
        get()=model.store!!
        set(ar){model.store=ar}
    fun init(){ model.columns=cols }
    fun<T: Item> getSelectedRow():T?=model?.store?.get(widget.selectedRow) as T
    fun setIndex(n:Int):MavTable{
        widget.setRowSelectionInterval(n, n);
        return this
    }
    fun scrollTo(n:Int):MavTable{
        widget.scrollRectToVisible( Rectangle(widget.getCellRect(n, 0, true)))
        return this
    }
    fun<T: Item> add(item:T):MavTable{
        val row=model.store!!.size-1
        model.fireTableRowsInserted(row,row)
        setIndex(row)
        scrollTo(row)
        return this
    }
    fun<T: Item> change(item:T):MavTable{
        model.fireTableRowsUpdated(widget.selectedRow,widget.selectedRow)
        return this
    }
    fun<T: Item> delete():MavTable{
        val row=widget.selectedRow
        model.fireTableRowsDeleted(row,row)
        if(row>0) setIndex(row-1)
        return this
    }
    fun onClick(fn:(Int)->Unit):MavTable{
        this.clickFun=fn
        widget.addKeyListener( object : KeyListener {
            override fun keyPressed(keyEvent: KeyEvent) { }
            override fun keyReleased(keyEvent: KeyEvent) {fn(widget.selectedRow) }
            override fun keyTyped(keyEvent: KeyEvent) { }
        })
        widget.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(mouseEvent: MouseEvent) {
                val table = mouseEvent.getSource() as JTable
                val point = mouseEvent.getPoint()
                val row = table.rowAtPoint(point)
                if (mouseEvent.getClickCount() === 1 && table.selectedRow != -1) {
                    fn(table.selectedRow)
                }
            }
        })
        return this
    }
    fun onDblClick(fn:(Int)->Unit):MavTable{
        this.dblClickFun=fn
        widget.addKeyListener( object : KeyListener {
            override fun keyPressed(keyEvent: KeyEvent) {if(keyEvent.keyCode==10){
                fn(widget.selectedRow)
                keyEvent.consume()
                }
            }
            override fun keyReleased(keyEvent: KeyEvent) { }
            override fun keyTyped(keyEvent: KeyEvent) { }
        })
        widget.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(mouseEvent: MouseEvent) {
                val table = mouseEvent.getSource() as JTable
                val point = mouseEvent.getPoint()
                val row = table.rowAtPoint(point)
                if (mouseEvent.getClickCount() === 2 && table.selectedRow != -1) {
                    fn(table.selectedRow)
                }
            }
        })
        return this
    }
    fun onMenu(fn:(e: MouseEvent)->Unit):MavTable{
        widget.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent?) {}
            override fun mouseReleased(e: MouseEvent?) {
                if (e!!.isPopupTrigger) {
                    val source = e.source as JTable
                    val row = source.rowAtPoint(e.point)
                    val column = source.columnAtPoint(e.point)
                    if (!source.isRowSelected(row))
                        source.changeSelection(row, column, false, false)
                    fn(e)
                }
            }
        })
        return this
    }
    fun<T: Item>fill(ar:ArrayList<T>):MavTable{
        val columnModel = widget.getTableHeader().getColumnModel()
        for (i in 0 until cols)
            model.headers.add(columnModel.getColumn(i).headerValue as String)
        model.store=ar
        widget.model=model
        model.fireTableDataChanged()
        return this
    }
    fun<T: Item>fill(ar:ArrayList<T>, flds:Array<Int>, fns:Array< (T, Int)->Any>):MavTable{
        val columnModel = widget.getTableHeader().getColumnModel()
        for (i in 0 until cols)
            model.headers.add(columnModel.getColumn(i).headerValue as String)
        model.store=ar
        model.flds=flds
        model.fns=fns as Array<out (Item, Int)->Any>
        widget.model=model
        model.fireTableDataChanged()
        return this
    }
    fun setHeaderText(n:Int,s:String):MavTable{
        val columnModel = widget.tableHeader.columnModel
        columnModel.getColumn(n).headerValue = s
        widget.tableHeader.repaint()
        return this
    }
}

open class MavGrid:MavWidget {
    override val widget:JTable
    val cols:Int
    constructor(name:String,root:MutableMap<String,Component>,_cols:Int):super(name,root){
        widget=root[name] as JTable
        cols=_cols
        init()
    }
    constructor(_widget:JTable,_cols:Int):super(_widget){
        widget=_widget
        cols=_cols
        init()
    }
    private val model=MyEditableModel()
    var editables:ArrayList<Boolean>
        get()=model.editables
        set(ar){model.editables=ar}
    var store:ArrayList<out Item>
        get()=model.store!!
        set(ar){model.store=ar}
    fun init(){
        model.columns=cols
        widget.selectionBackground=Color.WHITE
    }
    fun setEditables(ar:ArrayList<Boolean>):MavGrid{
        model.editables=ar
        return this
    }
    fun<T: Item> add(item:T):MavGrid{
        val row=model.store!!.size-1
        model.fireTableRowsInserted(row,row)
        return this
    }
    fun<T: Item> change(item:T):MavGrid{
        model.fireTableRowsUpdated(widget.selectedRow,widget.selectedRow)
        return this
    }
    fun<T: Item> delete():MavGrid{
        val row=widget.selectedRow
        model.fireTableRowsDeleted(row,row)
        return this
    }
    fun<T: Item>fill(ar:ArrayList<T>):MavGrid{
        val columnModel = widget.getTableHeader().getColumnModel()
        for (i in 0 until cols)
            model.headers.add(columnModel.getColumn(i).headerValue as String)
        model.store=ar
        widget.model=model
        model.fireTableDataChanged()
        return this
    }
    fun<T: Item>fill(ar:ArrayList<T>, flds:Array<Int>, fns:Array< (T, Int)->Any>):MavGrid{
        val columnModel = widget.getTableHeader().getColumnModel()
        for (i in 0 until cols)
            model.headers.add(columnModel.getColumn(i).headerValue as String)
        model.store=ar
        model.flds=flds
        model.fns=fns as Array<out (Item, Int)->Any>
        widget.model=model
        model.fireTableDataChanged()
        return this
    }
    fun getHeaderText(n:Int):String=widget.tableHeader.columnModel.getColumn(n).headerValue.toString()
    fun setHeaderText(n:Int,s:String):MavGrid{
        val columnModel = widget.tableHeader.columnModel
        columnModel.getColumn(n).headerValue = s
        widget.tableHeader.repaint()
        return this
    }
    fun onEdit(fn:(Item, Int, String)->Unit):MavGrid{
        model.editFun=fn
        return this
    }
}

class MyModel : AbstractTableModel() {
    val headers=ArrayList<String>()
    var columns=0
    var store:ArrayList<out Item>?=null
    var flds:Array<Int>?=null
    var fns:Array<out (Item, Int)->Any>?=null
    override fun getColumnCount()=columns
    override fun getRowCount() = store?.size ?: 0
    override fun getColumnName(col: Int)= headers[col]
    override fun getValueAt(row: Int, col: Int): Any {
        val item=store!![row]
        return if(fns==null) item[col] else fns!![col](item,flds!![col])
    }
}

class MyEditableModel : AbstractTableModel() {
    val headers=ArrayList<String>()
    var columns=0
    var store:ArrayList<out Item>?=null
    var flds:Array<Int>?=null
    var fns:Array<out (Item, Int)->Any>?=null
    lateinit  var editables:ArrayList<Boolean>
    var editFun:(Item, Int, String)->Unit= { o, r, c->{}}
    init{ repeat (columns)  { editables.add(false) } }
    override fun getColumnCount()=columns
    override fun getRowCount() = store?.size ?: 0
    override fun getColumnName(col: Int)= headers[col]
    override fun getValueAt(row: Int, col: Int): Any {
        val item=store!![row]
        return if(fns==null) item[col] else fns!![col](item,flds!![col])
    }
    override fun isCellEditable(row:Int,col:Int)= editables[col]
    override fun setValueAt(value:Any,row:Int,col:Int){  editFun( store!![row],col,value.toString()) }
}

class DateLabelFormatter : AbstractFormatter() {
    private val datePattern = "dd-MM-yyyy"
    private val dateFormatter = SimpleDateFormat(datePattern)
    @Throws(ParseException::class)
    override fun stringToValue(text: String): Any {
        return dateFormatter.parseObject(text)
    }
    @Throws(ParseException::class)
    override fun valueToString(value: Any?): String {
        if (value != null) {
            val cal = value as Calendar?
            return dateFormatter.format(cal!!.time)
        }
        return ""
    }
}

open class MavTab:MavWidget {
    override val widget:JTabbedPane
    constructor(name:String,root:MutableMap<String,Component>):super(name,root){  widget=root[name] as JTabbedPane  }
    constructor(_widget:JTabbedPane):super(_widget){ widget=_widget }
    fun onChange(fn:(String)->Unit):MavTab{
        widget.addChangeListener { fn(widget.getTitleAt(widget.selectedIndex)) }
        return this
    }
}

