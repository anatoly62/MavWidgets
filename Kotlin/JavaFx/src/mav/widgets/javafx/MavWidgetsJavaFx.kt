package mav.widgets.javafx

import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.geometry.Side
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.stage.Stage
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.cell.TextFieldTableCell
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.scene.text.TextAlignment
import javafx.util.Callback
import java.time.LocalDate
import mav.core.Item
import mav.core.currentTimeMillis
import mav.core.timeToDate
import java.io.FileInputStream



const val library="JAVAFX"
fun getKeyName(cod:KeyCode)=cod.name

fun tryE(fn:()->Unit) =
    try{ fn()  }
    catch (e:Exception){showError(e.toString()) }

fun showError(s:String,stage:Stage=Stage()){
    with(Alert(AlertType.ERROR)) {
        title = "Error!"
        headerText = "Error Info:"
        contentText = s
        showAndWait()
    }
}
fun showError(s:String,root:Parent?){
    with(Alert(AlertType.ERROR)) {
        title = "Error!"
        headerText = "Error Info:"
        contentText = s
        showAndWait()
    }
}
fun showError(e:Exception):Exception{
    with(Alert(AlertType.ERROR)) {
        title = "Error!"
        headerText = "Error Info:"
        contentText = e.toString()
        showAndWait()
    }
    return e
}

fun showMessage(s:String,stage:Stage){
    with(Alert(AlertType.INFORMATION)) {
        title = "Message"
        headerText = "Message Info:"
        contentText = s
        showAndWait()
    }
}
fun showMessage(s:String,root:Parent){
    with(Alert(AlertType.INFORMATION)) {
        title = "Message"
        headerText = "Message Info:"
        contentText = s
        showAndWait()
    }
}
fun searchForCombo(cb: MavCombo, s:String, n:Int){
    val combo=cb.widget
    val ss=s.split(" ")
    val len=combo.items.size
    for (i in 0 until len) {
        val item=combo.items[i].toLowerCase()
        val names=item.split(" ")
        if (names.size==n+1) {
            if (names[n].startsWith(s)){
                combo.selectionModel.select(i)
            }
        }
        else if (ss.size>1){
            if (names[n].startsWith(ss[0]) && names[n+1].startsWith(ss[1])) {
                combo.selectionModel.select(i)
            }
        }
        else if (s.length>0 && ss.size==1) {
            if (s.startsWith(" ") && names[n+1].startsWith(ss[0])) {
                combo.selectionModel.select(i)
            }
            else if (!s.startsWith(" ") && names[n].startsWith(ss[0])){
                combo.selectionModel.select(i)
            }
        }
    }
}
fun searchForTable(table: MavTable, s:String, n:Int){
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
    table.scrollTo(row)
    table.clickFun(row)
}

//builders function

fun mavVBox(ar:Array<Node>,spacing:Int=0,padding:Int=0):VBox{
    val box = VBox()
    if(spacing!=0) box.spacing=spacing.toDouble()
    ar.forEach { box.children.add(it) }
    return box
}
fun mavVFiller(height:Int):HBox{
    val box = HBox()
    box.prefHeight=height.toDouble()
    return box
}
fun mavHBox(ar:Array<Node>,spacing:Int=0,padding:Int=0): HBox {
    val box = HBox()
    if(spacing!=0) box.spacing=spacing.toDouble()
    ar.forEach { box.children.add(it) }
    return box
}
fun mavWindow(parent:Parent,width: Int=0,height:Int=0):Stage{
    val scene = Scene(parent)
    val stage= Stage()
    stage.scene=scene
    if(width!=0) stage.width=width.toDouble()
    if(height!=0) stage.height=height.toDouble()
    return stage
}

fun mavItem(text:String): MenuItem= MenuItem(text)

fun mavMenu(text:String,ar:Array<MenuItem>):Menu{
    val menu=Menu(text)
    ar.forEach { menu.items.add(it) }
    return menu

}

fun mavMenuBar(ar:Array<Menu> =arrayOf()):MenuBar{
    val menuBar=MenuBar()
    ar.forEach { menuBar.menus.add(it) }
    return menuBar
}

fun mavToolBar(ar:Array<Node> =arrayOf()):ToolBar{
    val toolBar=ToolBar()
    ar.forEach { toolBar.items.add(it) }
    return toolBar
}

fun mavText(text:String,width:Int=0,readOnly:Boolean=false):TextField{
    val text=TextField(text)
    if(width!=0) text.prefWidth=width.toDouble()
    if(readOnly) {
        text.isDisable = true
        text.font=Font("System Bold",12.0)
    }
    return text
}
fun mavDate(width:Int=0):DatePicker{
    val date=DatePicker()
    if(width!=0) date.prefWidth=width.toDouble()
    return date
}
fun mavCombo(width:Int=0):ComboBox<String>{
    val combo=ComboBox<String>()
    if(width!=0) combo.prefWidth=width.toDouble()
    return combo
}
fun mavLabel(text:String,width:Int=0):Label{
    val label=Label(text)
    if(width!=0) label.prefWidth=width.toDouble()
    label.prefHeight=24.0
    return label
}
fun mavButton(text:String,width:Int=0):Button{
    val button=Button(text)
    if(width!=0) button.prefWidth=width.toDouble()
    return button
}
fun mavFlatButton(imagePath:String,toolTip:String="",width:Int=0):Button{

    val input = FileInputStream(imagePath)
    val image = Image(input)
    val imageView = ImageView(image)
    val button=Button("",imageView)

    if(width!=0) button.prefWidth=width.toDouble()
    return button
}
fun mavTable(headers:Array<String>):TableView<Any>{
    val table = TableView<Any>()
    headers.forEach { table.columns.add(TableColumn<Any,Any>(it)) }
    return table
}
fun tableColsWidth(table:TableView<Any>,ar:Array<Int>){
    table.prefWidth=ar.sum().toDouble()
    for((n,el) in ar.withIndex()) { table.columns[n].prefWidth=el.toDouble()  }
}



//Classes
open class MavWidget{
    val root:Parent?
    var stage:Stage?
    open val widget:Node
    constructor(name:String,_stage:Stage){
        stage=_stage
        root=stage?.scene?.root
        widget=if(name=="dlg") _stage.scene.root else root!!.lookup("#" + name)
    }
    constructor(_widget:Node){
        stage=null
        root=stage?.scene?.root
        widget=_widget
    }

    var visible:Boolean
        get()=widget.isVisible
        set(flg) { widget.setVisible(flg)}
    var enable:Boolean
        get()=!widget.isDisable
        set(flg) { widget.setDisable(!flg)}
    fun setFocus(){
        widget.requestFocus()
    }
}

open class MavWindow: MavWidget{
    constructor(name:String,_stage:Stage):super(name,_stage){
        stage=_stage
    }
    constructor(_stage:Stage):super("dlg",_stage){
        stage=_stage
    }
    var text:String
        get()=stage!!.title
        set(s){stage!!.title=s}
    fun show(): MavWindow {
        stage?.show()
        return this
    }
    fun close() { stage!!.close() }
    fun setText(s:String): MavWindow {
        stage!!.title=s
        return this
    }
}

open class MavMenuButton: MavWidget {
    override val widget:MenuButton
    constructor(name:String,stage:Stage):super(name,stage){ widget = root!!.lookup("#" + name) as MenuButton }
    constructor(_widget:MenuButton):super(_widget) {widget=_widget}

    fun setItemHandler(fn: (Int) -> Unit) {
        for ((i, el) in widget.items.withIndex()) {
            el.setOnAction { fn(i) }
        }
    }
}

open class MavMenu(vararg menu: MavItem) {
    val widget = ContextMenu()
    init {
        for (el in menu)
            widget.items.add(el.widget as MenuItem)
    }
    fun show(node: Node, ev:MouseEvent): MavMenu {
        widget.show(node, Side.TOP,ev.x,ev.y)
        return this
    }
    fun hide(): MavMenu {
        widget.hide()
        return this
    }
}

open class MavItem {
    val widget:MenuItem
    constructor(name:String,stage:Stage) {
         widget = if (name == "") SeparatorMenuItem() else MenuItem(name)
    }
    constructor(_widget:MenuItem){widget=_widget}
    var text:String
        get()=widget.text
        set(s){widget.text=s}
    var enable:Boolean
        get()=!widget.isDisable
        set(flg) { widget.setDisable(!flg)}
    fun setText(s:String): MavItem {
        widget.text=s
        return this
    }
    fun onClick(fn: () -> Unit): MavItem {
        widget.setOnAction { fn() }
        return this
    }
}

open class MavCheck: MavWidget {
    override val widget: CheckBox
    constructor(name:String,stage:Stage):super(name,stage){ widget = root!!.lookup("#" + name) as CheckBox }
    constructor(_widget:CheckBox):super(_widget) {widget=_widget}

    var checked:Boolean
        get()=widget.isSelected
        set(flg) {widget.setSelected(flg) }
    var text:String
        get()=widget.text
        set(s){widget.text=s}
    fun onChange(fn:()->Unit): MavCheck {
        widget.setOnAction { fn() }
        return this
    }
}

open class MavRadio: MavWidget {
    override val widget: RadioButton
    constructor(name:String,stage:Stage):super(name,stage){ widget = root!!.lookup("#" + name) as RadioButton }
    constructor(_widget:RadioButton):super(_widget) {widget=_widget}
    var checked:Boolean
        get()=widget.isSelected
        set(flg) {widget.setSelected(flg) }
    var text:String
        get()=widget.text
        set(s){widget.text=s}
    fun onChange(fn:()->Unit): MavRadio {
        widget.setOnAction { fn() }
        return this
    }
}

open class MavDate: MavWidget {
    override val widget:DatePicker
    constructor(name:String,stage:Stage):super(name,stage){ widget = root!!.lookup("#" + name) as DatePicker }
    constructor(_widget:DatePicker):super(_widget) {widget=_widget}
    var text:String
        get()=widget.value.toString()
        set(s){
            val ar=s.split("-")
            widget.value= LocalDate.of(ar[0].toInt(),ar[1].toInt(),ar[2].toInt())
        }
    fun setText(s:String): MavDate {
        val ar=s.split("-")
        widget.value= LocalDate.of(ar[0].toInt(),ar[1].toInt(),ar[2].toInt())
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
    fun onChange(fn: () -> Unit): MavDate {
        widget.setOnAction { fn() }
        return this
    }
}

open class MavFlatButton: MavWidget {
    override val widget:Button
    constructor(name:String,stage:Stage):super(name,stage){ widget = root!!.lookup("#" + name) as Button }
    constructor(_widget:Button):super(_widget) {widget=_widget}
    var text:String
        get()=widget.text
        set(s){widget.text=s}
    fun setText(s:String): MavFlatButton {
        widget.text=s
        return this
    }
    fun onClick(fn: () -> Unit): MavFlatButton {
        widget.setOnAction { fn() }
        return this
    }
}

open class MavButton: MavWidget {
    override val widget:Button
    constructor(name:String,stage:Stage):super(name,stage){ widget = root!!.lookup("#" + name) as Button }
    constructor(_widget:Button):super(_widget) {widget=_widget}

    var text:String
        get()=widget.text
        set(s){widget.text=s}
    fun setText(s:String): MavButton {
        widget.text=s
        return this
    }
    fun onClick(fn: () -> Unit): MavButton {
        widget.setOnAction { fn() }
        widget.setOnKeyReleased { if(it.code.name=="ENTER") fn() }
        return this
    }
}

open class MavLabel: MavWidget {
    override val widget:Label
    constructor(name:String,stage:Stage):super(name,stage){ widget = root!!.lookup("#" + name) as Label }
    constructor(_widget:Label):super(_widget) {widget=_widget}
    var text:String
        get()=widget.text
        set(s){widget.text=s}
    fun setText(s:String): MavLabel {
        widget.text=s
        return this
    }
}

open class MavText: MavWidget {
    override val widget: TextField
    constructor(name:String,stage:Stage):super(name,stage){ widget = root!!.lookup("#" + name) as TextField }
    constructor(_widget:TextField):super(_widget) {widget=_widget}
    var text:String
        get()=widget.text
        set(s){widget.text=s}
    fun setText(s:String): MavText {
        widget.text=s
        return this
    }
    fun selectAll(): MavText {
        widget.selectAll()
        return this
    }
    fun onChange(fn: () -> Unit): MavText {
        widget.setOnKeyReleased  { fn() }
        return this
    }
    fun onKey(fn: (KeyCode) -> Unit): MavText {
        widget.setOnKeyReleased {  fn(it.code) }
        return this
    }
}

open class MavCombo: MavWidget {
    override val widget: ComboBox<String>
    constructor(name:String,stage:Stage):super(name,stage){ widget = root!!.lookup("#" + name) as ComboBox<String>}
    constructor(_widget:ComboBox<String>):super(_widget) {widget=_widget}
    var text:String
        get()=widget.selectionModel.selectedItem
        set(s){widget.selectionModel.select(s)}
    var index:Int
        get()=widget.selectionModel.selectedIndex
        set(n)=widget.selectionModel.select(n)
    fun setText(s: String): MavCombo {
        widget.selectionModel.select(s)
        return this
    }
    fun setIndex(n: Int): MavCombo {
        widget.selectionModel.select(n)
        return this
    }
    fun fill(ar: List<String>): MavCombo {
        val items = widget.getItems()
        for (el in ar) items.add(el)
        widget.getSelectionModel().clearAndSelect(0)
        return this
    }
    fun fill(lst: List<String>, ar: Array<String>): MavCombo {
        val items = widget.getItems()
        for (el in ar) items.add(el)
        for (el in lst) items.add(el)
        widget.getSelectionModel().clearAndSelect(0)
        return this
    }
    fun onChange(fn: () -> Unit): MavCombo {
        widget.setOnAction { fn() }
        return this
    }
}

open class MavTable: MavWidget{
    override val widget:TableView<Any>
    val cols:Int
    constructor(name:String,stage:Stage,_cols:Int):super(name,stage){
        widget = root!!.lookup("#" + name) as TableView<Any>
        cols=_cols
        init()
    }
    constructor(_widget:TableView<Any>,_cols:Int):super(_widget) {
        widget=_widget
        cols=_cols
        init()
    }
    var store:ArrayList<out Item>?=null
    private var flds:Array<Int>?=null
    private var fns:Array<out (Item, Int)->Any>?=null
    var clickFun:(Int)->Unit=fun(n:Int){}
    var dblClickFun:(Int)->Unit=fun(n:Int){}
    private var menuFun:(MouseEvent)->Unit=fun(n:MouseEvent){}
    var index:Int
        get()=widget.getSelectionModel().selectedIndex
        set(n)=  widget.getSelectionModel().select(store!![n])
    fun init(){
        widget.setOnKeyReleased {
            if(it.code.name=="ENTER")
                dblClickFun(widget.getSelectionModel().selectedIndex)
            else
                clickFun(widget.getSelectionModel().selectedIndex)
        }
        widget.setOnMouseClicked {
            val n=widget.getSelectionModel().selectedIndex
            if(it.clickCount==2) dblClickFun(n)
            else if(it.button.name=="SECONDARY") menuFun(it)
            else clickFun(n)
        }
    }
    fun setIndex(line:Int): MavTable {
        widget.getSelectionModel().select(store!![line])
        return this
    }
    @Suppress("UNCHECKED_CAST")
    fun<R: Item>add(item:R){
        widget as TableView<R>
        val items=widget.getItems()
        items.add(item)
        (widget as TableView<R>).getSelectionModel().select(items.size-1)
        widget.scrollTo(item)
    }
    @Suppress("UNCHECKED_CAST")
    fun<R: Item>change(item:R){
        widget as TableView<R>
        val n:Int=widget.getSelectionModel().getSelectedIndex()
        widget.getItems().set(n, item)
        (widget as TableView<R>).getSelectionModel().select(n )
    }
    @Suppress("UNCHECKED_CAST")
    fun<R: Item> delete() {
        widget as TableView<R>
        val n:Int=widget.getSelectionModel().getSelectedIndex()
        val items=widget.getItems()
        items.remove(n,n+1)
        if(items.size>0) (widget as TableView<R>).getSelectionModel().select(n-1)
    }
    fun <R: Item> getSelectedRow():R?{
        val ar=widget.getSelectionModel().getSelectedItems()
        return if(ar.isEmpty()) null  else ar[0] as R
    }
    fun scrollTo(line:Int): MavTable {
        widget.scrollTo(store!![line])
        return this
    }
    @Suppress("UNCHECKED_CAST")
    fun <R: Item, V> fillCol(i:Int, n: Int, fn:((R, Int)->Any))  {
        var column = widget.columns[i] as TableColumn<R, V>
        column.setCellValueFactory(Callback<TableColumn.CellDataFeatures<R, V>, ObservableValue<V>> {
                p -> ReadOnlyObjectWrapper(fn(p.getValue(),n)) as ObservableValue<V> })
    }
    @Suppress("UNCHECKED_CAST")
    fun <R: Item, String> fillCol(n: Int){
        var column = widget.columns[n] as TableColumn<R, String>
        column.setCellValueFactory(Callback<TableColumn.CellDataFeatures<R, String>, ObservableValue<String>> {
                p -> ReadOnlyObjectWrapper(p.getValue()[n]) as? ObservableValue<String> })
    }
    @Suppress("UNCHECKED_CAST")
    fun<R: Item> fill(ar:ArrayList<R>){
        this.store=ar as ArrayList<out Item>
        widget as TableView<R>
        val cnt=widget.columns.size
        for(i in 0 until cnt)  fillCol<R,String>(i)
        var lst: ObservableList<R> = FXCollections.observableArrayList()
        for(r in ar)  lst.add(r)
        (widget as TableView<R>).setItems(lst)
    }
    @Suppress("UNCHECKED_CAST")
    fun<R: Item>fill(ar:ArrayList<R>, flds:Array<Int>, fns:Array<  (R, Int)->Any>): MavTable {
        this.store=ar as ArrayList<out Item>
        this.flds=flds
        this.fns=fns as Array<out (Item, Int)->Any>
        widget as TableView<R>
        val cnt=if(fns.size<widget.columns.size) fns.size else widget.columns.size
        for(i in 0 until cnt)  fillCol<R,String>(i,flds[i],fns[i])
        var lst: ObservableList<R> = FXCollections.observableArrayList()
        for(r in ar)  lst.add(r)
        (widget as TableView<R>).setItems(lst)
        return this
    }

    fun setHeaderText(n:Int,s:String){ widget.columns[n].text=s }
    fun onClick(fn: (Int)->Unit): MavTable {
        clickFun=fn
        return this
    }
    fun onDblClick(fn: (Int)->Unit): MavTable {
        dblClickFun=fn
        return this
    }
    fun onMenu(fn:(MouseEvent)->Unit): MavTable {
        menuFun=fn
        return this
    }
}

open class MavGrid: MavWidget{
    override val widget:TableView<Any>
    val cols:Int
    constructor(name:String,stage:Stage,_cols:Int):super(name,stage){
        widget = root!!.lookup("#" + name) as TableView<Any>
        cols=_cols
        init()
    }
    constructor(_widget:TableView<Any>,_cols:Int):super(_widget) {
        widget=_widget
        cols=_cols
        init()
    }
    var store:ArrayList<out Item>?=null
    lateinit  var editables:ArrayList<Boolean>
    private var flds:Array<Int>?=null
    private var fns:Array<out (Item, Int)->Any>?=null
    private var edFun:(Item, Int, String)->Unit={ item, n, s->{}}
    fun init(){
        editables=ArrayList()
        for (i in 0 until cols)  editables.add(false)
    }
    fun setEditables(ar:ArrayList<Boolean>): MavGrid {
        editables=ar
        return this
    }
    @Suppress("UNCHECKED_CAST")
    fun<R: Item>change(item:R){
        widget as TableView<R>
        val n:Int=widget.getSelectionModel().getSelectedIndex()
        widget.getItems().set(n, item)
        (widget as TableView<R>).getSelectionModel().select(n)
    }
    @Suppress("UNCHECKED_CAST")
    fun <R: Item> fillCol(i:Int, n: Int, fn:((R, Int)->Any))  {
        var column = widget.columns[i] as TableColumn<R, String>
        if(editables[i]) {
            column.setCellFactory(TextFieldTableCell.forTableColumn<R>())
            column.setOnEditCommit { edFun(it.rowValue, i, it.newValue) }
        }
        column.setCellValueFactory(Callback<TableColumn.CellDataFeatures<R, String>, ObservableValue<String>>
        { p -> ReadOnlyObjectWrapper(fn(p.getValue(),n).toString()) as ObservableValue<String> })
    }
    @Suppress("UNCHECKED_CAST")
    fun<R: Item>fill(ar:ArrayList<R>, flds:Array<Int>, fns:Array<  (R, Int)->Any>): MavGrid {
        widget.setEditable(true)
        this.store=ar as ArrayList<out Item>
        this.flds=flds
        this.fns=fns as Array<out (Item, Int)->Any>
        widget as TableView<R>
        val cnt=if(fns.size<widget.columns.size) fns.size else widget.columns.size
        for(i in 0 until cnt)  fillCol<R>(i,flds[i],fns[i])
        var lst: ObservableList<R> = FXCollections.observableArrayList()
        for(r in ar)  lst.add(r)
        (widget as TableView<R>).setItems(lst)
        return this
    }
    fun onEdit(fn:(Item, Int, String)->Unit): MavGrid {
        edFun=fn
        return this
    }
}

open class MavTab: MavWidget{
    override val widget: TabPane
    constructor(name:String,stage:Stage):super(name,stage){ widget = root!!.lookup("#" + name) as TabPane }
    constructor(_widget:TabPane):super(_widget) {widget=_widget}
    fun onChange(fn:(String)->Unit): MavTab {
        widget.getSelectionModel().selectedItemProperty().addListener({ ov, oldTab, newTab ->fn(newTab.text)})
        return this
    }
}

