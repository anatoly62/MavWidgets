package mav.widgets.qt

import mav.core.*
import com.trolltech.qt.gui.*
import com.trolltech.qt.core.*

private var itemsClick = mutableMapOf<MavItem,()->Unit>()
private var buttonsClick = mutableMapOf<MavButton,()->Unit>()
private var buttonsPress = mutableMapOf<MavButton,()->Unit>()
private var flatButtonsClick = mutableMapOf<MavFlatButton,()->Unit>()
private var tablesClick = mutableMapOf<MavTable,(Int)->Unit>()
private var tablesDoubleClick = mutableMapOf<MavTable,(Int)->Unit>()
private var combosChange = mutableMapOf<MavCombo,()->Unit>()
private var checksChange = mutableMapOf<MavCheck,()->Unit>()
private var datesChange = mutableMapOf<MavDate,()->Unit>()
private var textsChange = mutableMapOf<MavText,()->Unit>()

class GridDelegate(view:QObject) : QStyledItemDelegate(view) {
    override fun paint( painter:QPainter,   option: QStyleOptionViewItem,  index: QModelIndex  ) {
        painter.save()
        painter.setPen(QColor(0xd0d0d0));
        painter.drawRect(option.rect())
        painter.restore()
        super.paint(painter, option, index)
    }
}
class MavLineEdit(widget:QWidget,s:String):QLineEdit(widget){
    init{  this.setText(s) }
    var fn:(QKeyEvent)->Unit={}
    override fun keyReleaseEvent(ev: QKeyEvent?) {
        fn(ev!!)
        super.keyReleaseEvent(ev)
    }
}
class MavTreeView():QTreeView() {
    var fn: (Int) -> Unit = {}
    var pos=QPoint(0,0)
    override fun contextMenuEvent(ev: QContextMenuEvent?) {
        pos=ev?.globalPos()!!
        fn(0)
        super.contextMenuEvent(ev)
    }
}
open class Application(): QMainWindow(){
    companion object {
        open fun start(fn:()->Unit,args: Array<String>){
            QApplication.initialize(args)
            fn()
            QApplication.execStatic()
            QApplication.quit()
        }
    }
}
//Utils functions
fun getKeyName(ev:QKeyEvent)= when(ev.key()){
    16777219->"BACK_SPACE"
    16777220->"ENTER"
    16777221->"ENTER"
    27->"ECSAPE"
    32->"SPACE"
    16777238->"PAGE_UP"
    16777239->"PAGE_DOWN"
    16777233->"END"
    16777232->"HOME"
    16777234->"LEFT"
    16777235->"UP"
    16777236->"RIGHT"
    16777237->"DOWN"
    16777223->"DELETE"
    16777222->"INSERT"
    else->ev.key().toChar().toString().toUpperCase()
}
fun showError(s:String,root: MutableMap<String, QObject> =mutableMapOf()){
    QMessageBox.critical(QWidget(),"Помилка",s)
}
fun tryE(fn:()->Unit) =  try{ fn()  }   catch (e:Exception){showError(e.toString()) }
fun searchForCombo(cb:MavCombo,s:String,n:Int){
    val combo=cb.widget
    val ss=s.split(" ")
    val len=combo.count()
    for (i in 0 until len) {
        val item=combo.itemText(i).toLowerCase()
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

//Builders functions

fun mavVBoxW(ar:Array<QObject>):QVBoxLayout{
    val l=QVBoxLayout()
    ar.forEach { l.addWidget(it as QWidget) }
    return l
}
fun mavVBoxL(ar:Array<QLayout>):QVBoxLayout{
    val l=QVBoxLayout()
    ar.forEach { l.addLayout(it) }
    return l
}
fun mavHBoxW(ar:Array<QObject>):QHBoxLayout{
    val l=QHBoxLayout()
    ar.forEach { l.addWidget(it as QWidget ) }
    return l
}
fun mavHBoxL(ar:Array<QLayout>):QHBoxLayout{
    val l=QHBoxLayout()
    ar.forEach { l.addLayout(it) }
    return l
}
fun mavMdiWidget(shell:QMainWindow):QWidget{
    val mdi=QMdiArea()
    shell.setCentralWidget(mdi)
    val widget=QWidget(mdi)
    mdi.addSubWindow(widget)
    widget.showMaximized()
    return widget
}
fun mavWidget()=QWidget()
fun mavWindow()=QMainWindow()
fun mavWindowSize(shell:QWidget,width:Int,height:Int=0){
    when (shell){
        is QMainWindow ->shell.resize(width,height)
        else ->{
            shell.setFixedWidth(width)
            if(height!=0) shell.setFixedHeight(height)
        }
    }
}
fun mavWindowLayout(shell:QWidget,layout:QLayout){  shell.setLayout(layout) }

fun mavToolBar(shell:QMainWindow,title:String,ar:Array<QObject>){
    val toolBar=shell.addToolBar(title)
    ar.forEach { toolBar.addWidget(it as QWidget) }
}

fun mavMenu(ar:Array<QObject> =arrayOf()):QMenu{
    val menu=QMenu()
    ar.forEach { menu.addAction(it as QAction)  }
    return menu
}

fun mavMenu(shell:QMainWindow,title:String,ar:Array<QObject> =arrayOf()):QMenu{
    val mBar=shell.menuBar().addMenu(title)
    ar.forEach { mBar.addAction(it as QAction)  }
    return mBar
}
fun mavItem(text:String):QAction{
    val w=QAction(text,null)
    return w
}
fun mavLabel(text:String,width:Int=0,height: Int=0):QLabel{
    val w=QLabel(text)
    if(width!=0) w.setFixedWidth(width)
    if(height!=0) w.setFixedHeight(height)
    return w
}
fun mavCheck(s:String,width:Int=0):QCheckBox{
    val w=QCheckBox(s)
    if(width!=0) w.setFixedWidth(width)
    return w
}
fun mavButton(text:String,width:Int=0):QPushButton{
    val w=QPushButton(text)
    if(width!=0) w.setFixedWidth(width)
    return w
}
fun mavFlatButton(icon:String,toolTip:String="",width:Int=0):QToolButton{
    val w=QToolButton()
    w.setIcon(QIcon(icon))
    if(toolTip.isNotEmpty()) w.setToolTip(toolTip)
    if(width!=0) w.setFixedWidth(width)
    return w
}
fun mavText(widget:QWidget,text:String,width:Int=0,readOnly:Boolean=false,visible:Boolean=true):MavLineEdit{
    val w=MavLineEdit(widget,text)
    if(width!=0) w.setFixedWidth(width)
    w.isReadOnly=readOnly
    w.isVisible=visible
    return w
}
fun mavDate(width:Int=0):QDateEdit{
    val w=QDateEdit()
    if(width!=0) w.setFixedWidth(width)
    return w
}
fun mavCombo(width:Int=0):QComboBox{
    val w=QComboBox()
    if(width!=0) w.setFixedWidth(width)
    return w
}
fun mavTable(w:QWidget,headers:Array<String>,fixed:Boolean=false):MavTreeView{
    val view=MavTreeView()
    view.setRootIsDecorated(false)
    view.setAlternatingRowColors(false)
    view.setEditTriggers()
    view.setStyleSheet("QTreeView::item { height: 24px;}\nQHeaderView::section {background-color: #eee;}")
    val model=QStandardItemModel(0,headers.size,w)
    for (i in 0 until headers.size )
        model.setHeaderData(i,Qt.Orientation.Horizontal,headers[i])
    view.setModel(model)
    view.setItemDelegate(GridDelegate(view))
    if(fixed) view.setFixedHeight(24)
    return view
}

//Classes
open class MavItem {
    val widget: QAction
    constructor(name:String,root:MutableMap<String,QObject>) { widget = root[name] as QAction }
    constructor(_widget:QAction){ widget=_widget }
    var text: String
        get() = widget.text()
        set(s: String) {
            widget.setText(s)
        }
    var enable:Boolean
        get()=widget.isEnabled
        set(flg) {widget.setEnabled(flg)}

    fun setText(s: String): MavItem {
        widget.setText(s)
        return this
    }
    fun onClick(fn:()->Unit):MavItem{
        itemsClick[this]=fn
        widget.triggered.connect(this,"click()")
        return this
    }
    private fun click(){ itemsClick[this]!!()}
}

open class MavWidget{
    open val widget: QWidget
    var root:MutableMap<String,QObject> =mutableMapOf()
    constructor( name:String, _root:MutableMap<String,QObject>){
        root=_root
        widget=root[name] as QWidget
    }
    constructor( _widget:QWidget){  widget=_widget }
    var visible:Boolean
        get()=widget.isVisible
        set(flg) {widget.setVisible(flg)}
    var enable:Boolean
        get()=widget.isEnabled
        set(flg) {widget.setEnabled(flg)}
    fun setFocus(){widget.setFocus()}
}

open class MavWindow:MavWidget{
    override val widget:QWidget
    constructor(name:String,root:MutableMap<String,QObject>):super(name,root) { widget = root[name] as QWidget }
    constructor(_widget:QWidget):super(_widget){ widget=_widget }
    fun show(max:Boolean=false):MavWindow{
        widget.show()
        return this
    }
    fun setText(s:String):MavWindow{
        widget.setWindowTitle(s)
        return this
    }
    fun close()= widget.close()
}

open class MavMenu:MavWidget {
    override val widget:QMenu
    constructor(name:String,root:MutableMap<String,QObject>):super(name,root) { widget = root[name] as QMenu }
    constructor(_widget:QMenu):super(_widget){ widget=_widget }
    fun show(w:QWidget,n:Int):MavMenu{
        if(w is MavTreeView)
            widget.exec(w.pos)
        return this
    }
    fun hide():MavMenu{
        widget.hide()
        return this
    }
}

open class MavLabel:MavWidget {
    override val widget: QLabel
    constructor(name:String,root:MutableMap<String,QObject>):super(name,root) { widget = root[name] as QLabel }
    constructor(_widget:QLabel):super(_widget){ widget=_widget }
    var text: String
        get() = widget.text()
        set(s: String) {
            widget.setText(s)
        }
    fun setText(s: String): MavLabel {
        widget.setText(s)
        return this
    }
}

open class MavCheck:MavWidget {
    override val widget:QCheckBox
    constructor(name:String,root:MutableMap<String,QObject>):super(name,root) { widget = root[name] as QCheckBox }
    constructor(_widget:QCheckBox):super(_widget){ widget=_widget }
    var checked:Boolean
        get()=widget.isChecked
        set(flg) { widget.isCheckable=flg }
    var text:String
        get()=widget.text()
        set(s) { widget.setText(s) }
    fun setText(s: String): MavCheck {
        widget.setText(s)
        return this
    }
    fun onChange(fn:()->Unit): MavCheck {
        checksChange[this]=fn
        widget.stateChanged.connect(this,"change(int)")
        return this
    }
    private fun change(n:Int){ checksChange[this]!!()}
}

open class MavButton:MavWidget {
    override val widget:QPushButton
    constructor(name:String,root:MutableMap<String,QObject>):super(name,root) { widget = root[name] as QPushButton }
    constructor(_widget:QPushButton):super(_widget){ widget=_widget }
    var text: String
        get() = widget.text()
        set(s: String) { widget.setText(s) }
    fun setText(s: String): MavButton {
        widget.setText(s)
        return this
    }
    fun onClick(fn:()->Unit):MavButton{
        buttonsClick[this]=fn
        widget.clicked.connect(this,"click()")
        widget.setAutoDefault(true)
        return this
    }
    fun onClickE(fn:()->Unit):MavButton{
        buttonsClick[this]=fn
        widget.clicked.connect(this,"clicke()")
        return this
    }
    private fun click(){ buttonsClick[this]!!()}
    private fun clicke(){ tryE{  buttonsClick[this]!!() } }
}

open class MavFlatButton:MavWidget {
    override val widget:QToolButton
    constructor(name:String,root:MutableMap<String,QObject>):super(name,root) { widget = root[name] as QToolButton }
    constructor(_widget:QToolButton):super(_widget){ widget=_widget }
    var text: String
        get() = widget.text()
        set(s: String) {  widget.setText(s) }
    fun setText(s: String): MavFlatButton {
        widget.setText(s)
        return this
    }
    fun onClick(fn:()->Unit):MavFlatButton{
        flatButtonsClick[this]=fn
        widget.clicked.connect(this,"click()")
        return this
    }
    fun onClickE(fn:()->Unit):MavFlatButton{
        flatButtonsClick[this]=fn
        widget.clicked.connect(this,"clicke()")
        return this
    }
    private fun click(){ flatButtonsClick[this]!!()}
    private fun clicke(){  tryE{  flatButtonsClick[this]!!() } }
}

open class MavText:MavWidget {
    override val widget: MavLineEdit
    constructor(name:String,root:MutableMap<String,QObject>):super(name,root) { widget = root[name] as MavLineEdit }
    constructor(_widget:MavLineEdit):super(_widget){ widget=_widget }
    var text: String
        get() = widget.text()
        set(s: String) { widget.setText(s) }
    fun setText(s: String): MavText {
        widget.setText(s)
        return this
    }
    fun selectAll():MavText{
        widget.selectAll()
        return this
    }
    fun onChange(fn:()->Unit):MavText{
        textsChange[this]=fn
        widget.textChanged.connect(this,"change()")
        return this
    }
    fun onKey(fn:(QKeyEvent)->Unit):MavText {
        widget.fn=fn
        return this
    }
    private fun change(){ textsChange[this]!!()}
}

open class MavDate:MavWidget {
    override val widget:QDateEdit
    constructor(name:String,root:MutableMap<String,QObject>):super(name,root) { widget = root[name] as QDateEdit }
    constructor(_widget:QDateEdit):super(_widget){ widget=_widget }
    var text: String
        get()  {
            val ar=widget.text().split(".")
            return ar[2]+"-"+ar[1]+"-"+ar[0]
        }
        set(s: String) {
            widget.setDate(QDate.fromString(s,"yyyy-MM-dd"))
            widget.setWindowTitle(s)
        }
    fun setText(s:String):MavDate{
        widget.setDate(QDate.fromString(s,"yyyy-MM-dd"))
        widget.setWindowTitle(s)
        return this
    }
    fun setFirstDay(): MavDate {
        this.setText(timeToDate(currentTimeMillis(), true))
        return this
    }
    fun setCurrentDay(): MavDate {
        this.setText(timeToDate(currentTimeMillis(), false))
        return this
    }
    fun onChange(fn:()->Unit):MavDate{
        datesChange[this]=fn
        widget.dateChanged.connect(this,"change()")
        return this
    }
    private fun change(){ datesChange[this]!!()}
}

open class MavCombo:MavWidget {
    override val widget:QComboBox
    constructor(name:String,root:MutableMap<String,QObject>):super(name,root) { widget = root[name] as QComboBox }
    constructor(_widget:QComboBox):super(_widget){ widget=_widget }
    var index: Int
        get() = widget.currentIndex()
        set(value: Int) { widget.setCurrentIndex(value) }
    fun setIndex(n: Int): MavCombo {
        widget.setCurrentIndex(n)
        return this
    }
    var text: String
        get() = widget.itemText(widget.currentIndex())
        set(s: String) {
            widget.setItemText(widget.currentIndex(),s)
        }
    fun setText(s: String): MavCombo {
        widget.setItemText(widget.currentIndex(),s)
        return this
    }
    fun fill(lst:List<String>):MavCombo{
        lst.forEach{ widget.addItem(it) }
        return this
    }
    fun fill(lst:List<String>,ar:Array<String>):MavCombo{
        ar.forEach{ widget.addItem(it) }
        lst.forEach{ widget.addItem(it) }
        return this
    }
    fun onChange(fn:()->Unit):MavCombo{
        combosChange[this]=fn
        widget.activatedIndex.connect(this,"change(int)")
        return this
    }
    private fun change(n:Int){ combosChange[this]!!()}
}

open class MavTable:MavWidget{
    override val widget: MavTreeView
    val cols:Int
    constructor(name:String,root:MutableMap<String,QObject>, _cols:Int):super(name,root) {
        widget = root[name] as MavTreeView
        cols=_cols
    }
    constructor(_widget:MavTreeView,_cols:Int):super(_widget){
        widget=_widget
        cols=_cols
    }
    var store:ArrayList<out Item> =ArrayList()
    var clickFun:(Int)->Unit={}
    private var flds:Array<Int>?=null
    private var fns:Array<out (Item, Int)->Any>?=null

    fun<T:Item> fill(store:ArrayList<T>):MavTable{
        val model=widget.model() as QStandardItemModel
        this.store=store
        model.removeRows(0,model.rowCount())
        for(i in 0 until store.size ){
            model.insertRow(model.rowCount())
            for(j in 0 until cols)  model.setData(model.index(model.rowCount()-1, j),store[i] [j])
        }
        widget.setModel(model)
        return this
    }
    fun<T: Item>fill(store:ArrayList<T>, flds:Array<Int>, fns:Array< (T, Int)->Any>):MavTable{
        val model=widget.model() as QStandardItemModel
        this.store=store
        this.flds=flds
        this.fns=fns as Array<out (Item, Int)->Any>
        model.removeRows(0, model.rowCount())
        for(i in 0 until store.size ){
            model.insertRow(model.rowCount())
            for(j in 0 until cols)  model.setData(model.index(model.rowCount()-1, j),fns[j](store[i],flds[j]))
        }
        widget.setModel(model)
        return this
    }
    var index: Int
        get() = widget.currentIndex().row()
        set(value) {widget.setCurrentIndex(widget.model().index(value, 0)) }
    fun setIndex(n:Int):MavTable{
        widget.setCurrentIndex(widget.model().index(n, 0))
        return this
    }
    fun scrollTo(n:Int):MavTable=this
    fun <T:Item>getSelectedRow():T =  store[widget.currentIndex().row()] as T
    fun<T: Item> add(item:T):MavTable{
        val model=widget.model()
        model.insertRow(model.rowCount())
        for(j in 0 until cols)
            model.setData(model.index(model.rowCount()-1, j),if(fns==null) item[j] else  fns!![j](item,flds!![j]))
        return this
    }
    fun<T: Item> change(item:T):MavTable{
        val row=widget.currentIndex().row()
        val model=widget.model()
        for (j in 0 until cols)
            model.setData(model.index(row,j),if(fns==null) item[j] else  fns!![j](item,flds!![j]))
        return this
    }
    fun<T: Item> delete():MavTable{
        widget.model().removeRow(widget.currentIndex().row())
        return this
    }
    fun setHeaderText(n:Int,s:String){
        val model=widget.model()
        model.setHeaderData(n,Qt.Orientation.Horizontal,s)
    }
    fun onClick(fn:(n:Int)->Unit):MavTable{
        tablesClick[this]=fn
        clickFun=fn
        widget.clicked.connect(this,"click()")
        return this
    }
    fun onDblClick(fn:(n:Int)->Unit):MavTable{
        tablesDoubleClick[this]=fn
        widget.doubleClicked.connect(this,"doubleclick()")
        return this
    }
    fun onMenu(fn:(Int)->Unit) { widget.fn=fn  }
    private fun click(){ tablesClick[this]!!(widget.currentIndex().row()) }
    private fun doubleclick() {  tablesDoubleClick[this]!!(widget.currentIndex().row()) }
}