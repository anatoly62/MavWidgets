package mav.widgets.gtk3

import kotlinx.cinterop.*
import gtk3.*
import mav.core.*

val nil:CPointer<GtkWidget>?=null

private var mainFun:(CPointer<GObject>)->Unit={}

//events storages

private var buttons = mutableMapOf<CPointer<GObject>?, MavButton>()
private var flat_buttons = mutableMapOf<CPointer<GObject>?, MavFlatButton>()
private var menus = mutableMapOf<CPointer<GObject>?, MavItem>()
private var combos = mutableMapOf<CPointer<GObject>?, MavCombo>()
private var texts = mutableMapOf<CPointer<GObject>?, MavText>()
private var dates = mutableMapOf<CPointer<GObject>?, MavDate>()
private var dates_text = mutableMapOf<CPointer<GObject>?, String>()
private var tables = mutableMapOf<CPointer<GObject>?, MavTable>()
private var tables_buttons = mutableMapOf<CPointer<GObject>?, Int>()

//events functions
fun button_click(ptr:CPointer<GObject>?, data: gpointer?=null) {  buttons[ptr]?.onClick() }
fun flat_button_click(ptr:CPointer<GObject>?, data: gpointer?=null) {  flat_buttons[ptr]?.onClick() }

fun menu_click(ptr:CPointer<GObject>?, data: gpointer?=null) {  menus[ptr]?.onClick() }

fun combo_change(ptr:CPointer<GObject>?, data: gpointer?=null) {  combos[ptr]?.onChange() }

fun text_key_event(ptr:CPointer<GObject>?, event:CPointer<GdkEventKey>,data: gpointer?=null):Int {
   texts[ptr]?.onKey(event.pointed.keyval.toInt())
   return 0
}
fun text_change_event(ptr:CPointer<GObject>?, data: gpointer?=null):Int {
    texts[ptr]?.onChange()
    return 0
}
fun date_key_event(ptr:CPointer<GObject>?, event:CPointer<GdkEventKey>,data: gpointer?=null):Int {
    fun canDel():Int{
        val pos=gtk_editable_get_position(dates[ptr]?.widget?.reinterpret())
        val ch= dates[ptr]?.text?.get(pos) ?:"*"
        return if(ch=='-')   1 else 0
    }
    fun canBsp():Int{
        val pos=gtk_editable_get_position(dates[ptr]?.widget?.reinterpret())
        if(pos==0) return 1
        val ch= dates[ptr]?.text?.get(pos-1) ?:"*"
        return if(ch=='-')   1 else 0
    }

    fun isDate(text:String):Boolean {
        if (text.length < 8) return false
        val r= text.all {
            when (it.toByte()) {
                45.toByte() -> true
                in 48..57 -> true
                else -> false
            }
        }
        if (!r) return false
        val ar = text.split("-")
        if(ar.size!=3)  return false
        if(ar[0].isNullOrEmpty() || ar[1].isNullOrEmpty() || ar[2].isNullOrEmpty()) return false
        if(ar[0].length>4 || ar[1].length>2 || ar[2].length>2) return false
        if (ar[0].toInt() < 1970 || ar[0].toInt() > 9999 ) return false
        if  (ar[1].toInt() == 0 || ar[1].toInt() > 12 ) return false
        if ( ar[2].toInt() == 0 || ar[2].toInt() > 31 ) return false
        return true
    }

    fun up(up:Boolean):Int{
        var text = dates[ptr]?.text ?: ""
        if(isDate(text)) {
            val pos = gtk_editable_get_position(dates[ptr]?.widget?.reinterpret())
            var n = 0
            for (i in 0 until pos) if (text[i] == '-') n += 1
            var ar = text.split("-")
            val max = when (n) {
                0 -> 9999
                1 -> 12
                2 -> 31
                else -> 0
            }
            var r =
                if (up)
                    if (ar[n].toInt() < max)
                        (ar[n].toInt() + 1).toString()
                    else
                        ar[n]
                else
                    if (ar[n].toInt() > 1)
                        (ar[n].toInt() - 1).toString()
                    else
                        ar[n]
            if (r.length == 1) r = "0" + r
            dates[ptr]?.text = when (n) {
                0 -> r + "-" + ar[1] + "-" + ar[2]
                1 -> ar[0] + "-" + r + "-" + ar[2]
                2 -> ar[0] + "-" + ar[1] + "-" + r
                else -> "Error"
            }
            gtk_editable_set_position(dates[ptr]?.widget?.reinterpret(), pos)

            dates_text[ptr] = text
            dates[ptr]?.onChange()
        }
        else dates[ptr]?.text= dates_text[ptr]!!
        return 1
    }

    fun enter():Int {
        try {
            var text = dates[ptr]?.text ?: ""
            if (!isDate(text)) dates[ptr]?.text = dates_text[ptr]!!
            else {
                var ar = text.split("-")
                text = ar[0] + "-" + (if (ar[1].length == 1) "0" + ar[1] else ar[1]) + "-" + if (ar[2].length == 1) "0" + ar[2] else ar[2]
                dates[ptr]?.text = text
                dates_text[ptr] = text
                dates[ptr]?.onChange()
            }
            return 1
        } catch (e: Exception) {
            dates[ptr]?.text = dates_text[ptr]!!
            return 1
        }
    }


    when(event.pointed.keyval.toInt()){
        65293       -> return enter()
        in 0..47    -> return 1
        in 58..65000-> return 1
        65455       -> return 1
        65450       -> return 1
        65453       -> return 1
        65451       -> return 1
        65535       -> return canDel()
        65288       -> return canBsp()
        65362       ->return up(true)
        65364       ->return up(false)
    }
    return 0
}

fun table_double_click(ptr:CPointer<GObject>?, data: gpointer?=null) {
    val idx= getTreeViewIndex(ptr!!.reinterpret())
    if(idx>=0)  tables[ptr]?.dblClickFun(idx)
}

fun table_button_event(ptr:CPointer<GObject>?, event:CPointer<GdkEventButton>,data: gpointer?=null):Int {
    if(event.pointed.button==3u)   tables_buttons[ptr]=3
    return 0
}

fun table_menu(ptr:CPointer<GObject>?, data: gpointer?=null) {
    val idx= getTreeViewIndex(ptr!!.reinterpret())
    if(idx>=0)  tables[ptr]?.onMenu(idx)
}

fun table_click(ptr:CPointer<GObject>?, data: gpointer?=null) {
    val idx= getTreeViewIndex(ptr!!.reinterpret())
    if(idx>=0)
        if (tables_buttons[ptr] == 3) {
            tables_buttons[ptr] = 0
            tables[ptr]?.onMenu(idx)
       }
       else
            tables[ptr]?.clickFun(idx)
}

//private functions
private fun getTreeViewIndex(view:CPointer<GtkTreeView>):Int {
    var model = gtk_tree_view_get_model(view.reinterpret())
    val selection = gtk_tree_view_get_selection(view.reinterpret())
    var i = 0
    memScoped {
        var iter = alloc<GtkTreeIter>()
        var valid = gtk_tree_model_get_iter_first(model, iter.ptr)
        while (valid == 1) {
            if (gtk_tree_selection_iter_is_selected(selection, iter.ptr) == 1) break
            valid = gtk_tree_model_iter_next(model, iter.ptr)
            i++
        }
    }
    return i
}

private fun clearModel(model:CPointer<GtkTreeModel>) {
    var i = 0
    memScoped {
        var iter = alloc<GtkTreeIter>()
        var valid = gtk_tree_model_get_iter_first(model, iter.ptr)
        while (valid == 1) {
            gtk_list_store_remove(model.reinterpret(),iter.ptr)
            valid = gtk_tree_model_get_iter_first(model, iter.ptr)
            i++
        }
    }
}

//app init
fun <F : CFunction<*>> g_signal_connect(obj: CPointer<*>, actionName: String, action: CPointer<F>, data: gpointer? = null, connect_flags: UInt = 0u) {
    g_signal_connect_data(obj.reinterpret(), actionName, action.reinterpret(), data = data, destroy_data = null, connect_flags = connect_flags)
}

fun activate(app: CPointer<GtkApplication>?, user_data: gpointer?) {
    val window = gtk_application_window_new(app)!!
    mainFun(window.reinterpret())
}
fun initGui(fn:(CPointer<GObject>)->Unit,args: Array<String>): Int {
    mainFun =fn
    val app = gtk_application_new("org.gtk.example", G_APPLICATION_FLAGS_NONE)!!
    g_signal_connect(app, "activate", staticCFunction(::activate))
    val status = memScoped { g_application_run(app.reinterpret(), args.size, args.map { it.cstr.getPointer(memScope) }.toCValues()) }
    g_object_unref(app)
    return status
}


//Utils fuctions
fun getKeyName(k:Int)=when(k){
    65288->"BACK_SPACE"
    65293->"ENTER"
    65307->"ECSAPE"
    32->"SPACE"
    65535->"DELETE"
    65362->"UP"
    65364->"DOWN"
    65361->"LEFT"
    65363->"RIGHT"
    65365->"PAGE_UP"
    65366->"PAGE_DOWN"
    65360->"HOME"
    65367->"END"
    65379->"INSERT"
    else->k.toChar().toString().toUpperCase()
}

fun searchForCombo(cb:MavCombo,s:String,n:Int){
    val ss=s.split(" ")
    val len=cb.store.size
    for (i in 0 until len) {
        val item=cb.store[i].toLowerCase()
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
    table.scrollTo(row)
    table.clickFun(row)
}

fun showError(text:String,root:MutableMap<String,CPointer<GObject>> = mutableMapOf()){
    val dlg=gtk_message_dialog_new_with_markup(null, GTK_DIALOG_MODAL, GtkMessageType.GTK_MESSAGE_ERROR, GtkButtonsType.GTK_BUTTONS_OK,text)
    gtk_dialog_run(dlg?.reinterpret())
    gtk_widget_destroy(dlg)
}
fun showMessage(text:String,root:MutableMap<String,CPointer<GObject>> = mutableMapOf()){
    val dlg=gtk_message_dialog_new_with_markup(null, GTK_DIALOG_MODAL, GtkMessageType.GTK_MESSAGE_INFO, GtkButtonsType.GTK_BUTTONS_OK,text)
    gtk_dialog_run(dlg?.reinterpret())
    gtk_widget_destroy(dlg)
}

//Builders functions
fun mavWindow(box:CPointer<GObject>,width: Int,height:Int):CPointer<GObject>{
    val window=gtk_window_new(GtkWindowType.GTK_WINDOW_TOPLEVEL)!!
    gtk_container_add(window.reinterpret(), box.reinterpret())
    gtk_widget_set_size_request(window.reinterpret(),width,height)
    return window.reinterpret()
}

fun mavWindowSize(window:CPointer<GObject>,width:Int,height:Int){  gtk_window_set_default_size(window.reinterpret(),width,height) }
fun mavWindowLayout(window:CPointer<GObject>,box:CPointer<GObject>) {  gtk_container_add(window.reinterpret(), box.reinterpret()) }

fun mavHBox(ar:Array<CPointer<GObject>>, spacing:Int=5, padding:Int=0, expand:Int=0):CPointer<GObject>{
    val w=gtk_box_new(GtkOrientation.GTK_ORIENTATION_HORIZONTAL,spacing)!!
    ar.forEach { gtk_box_pack_start(w.reinterpret(),it.reinterpret(),expand,expand,padding.toUInt()) }
    gtk_widget_show(w)
    return w.reinterpret()
}
fun mavVBox(ar:Array<CPointer<GObject>>, spacing:Int=10, padding:Int=0, expand:Int=0):CPointer<GObject>{
    val w=gtk_box_new(GtkOrientation.GTK_ORIENTATION_VERTICAL,spacing)!!
    ar.forEach { gtk_box_pack_start(w.reinterpret(),it.reinterpret(),expand,expand,padding.toUInt()) }
    gtk_widget_show(w)
    return w.reinterpret()
}
fun mavBoxAdd(box:CPointer<GObject>,widget:CPointer<GObject>,padding:Int=0,expand:Int=0){
    gtk_box_pack_start(box.reinterpret(),widget.reinterpret(),expand,expand,padding.toUInt())
}

fun mavMenuBar():CPointer<GObject>{
    val w=gtk_menu_bar_new()!!
    gtk_widget_show(w)
    return w.reinterpret()
}
fun mavMenu():CPointer<GObject> =gtk_menu_new()!!.reinterpret()
fun mavMenu(menubar:CPointer<GObject>,title:String,ar:Array<CPointer<GObject>>){
    val menu = gtk_menu_new()!!
    val menuIt = gtk_menu_item_new_with_label(title)!!
    gtk_menu_item_set_submenu(menuIt.reinterpret(), menu)
    ar.forEach {gtk_menu_shell_append(menu.reinterpret(), it.reinterpret()) }
    gtk_menu_shell_append(menubar.reinterpret(), menuIt)
    gtk_widget_show(menuIt)
    gtk_widget_show(menu)
}
fun mavMenuAdd(menu:CPointer<GObject>,widget:CPointer<GObject>){gtk_menu_shell_append(menu.reinterpret(), widget.reinterpret())}

fun mavItem(title:String):CPointer<GObject> {
    val w=gtk_menu_item_new_with_label(title)!!
    gtk_widget_show(w)
    return w.reinterpret()
}

fun mavLabel(text:String,width:Int=0):CPointer<GObject>{
    val w= gtk_label_new(text)!!
    gtk_label_set_xalign(w.reinterpret(),0.0f)
    if(width!=0)  gtk_widget_set_size_request(w.reinterpret(),width,20)
    gtk_widget_show(w)
    return w.reinterpret()
}

fun mavButton(text:String,width:Int=0):CPointer<GObject>{
    val w= gtk_button_new_with_label(text)!!
    if(width!=0)  gtk_widget_set_size_request(w.reinterpret(),width,20)
    gtk_widget_show(w)
    return w.reinterpret()
}

fun mavCheck(text:String,width:Int=0):CPointer<GObject>{
    val w= gtk_check_button_new_with_label(text)!!
    if(width!=0)  gtk_widget_set_size_request(w.reinterpret(),width,20)
    gtk_widget_show(w)
    return w.reinterpret()
}
fun mavFlatButton(fileName:String, label:String=""):CPointer<GObject> {
    val icon=gtk_image_new_from_file(fileName)
    gtk_widget_show(icon?.reinterpret())
    val w=gtk_tool_button_new(icon,label)!!
    gtk_widget_show(w.reinterpret())
    return w.reinterpret()
}

fun mavSeparator():CPointer<GObject>{
    val w=gtk_separator_new(GtkOrientation.GTK_ORIENTATION_VERTICAL)!!
    gtk_widget_show(w)
    return w.reinterpret()
}

fun mavText(text:String,width:Int=0,readOnly:Boolean=false,visible:Boolean=true):CPointer<GObject>{
    val w=gtk_entry_new()!!
    if(width>0 && width<50)  gtk_entry_set_width_chars(w.reinterpret(),width)
    else if(width>150) gtk_widget_set_size_request(w.reinterpret(),width,20)
    if(readOnly) gtk_widget_set_can_focus(w,0)
    if(text.isNotEmpty()) gtk_entry_set_text(w.reinterpret(),text)
    if(visible) gtk_widget_show(w)
    return w.reinterpret()
}

fun mavDate(width:Int=0):CPointer<GObject>{
    val w=gtk_entry_new()!!
    if(width>0 && width<50)  gtk_entry_set_width_chars(w.reinterpret(),width)
    else if(width>150) gtk_widget_set_size_request(w.reinterpret(),width,20)
    gtk_widget_show(w)
    return w.reinterpret()
}

fun mavCombo(width:Int=0):CPointer<GObject>{
    val w=gtk_combo_box_text_new()!!
    if(width!=0)  gtk_widget_set_size_request(w.reinterpret(),width,20)
    gtk_widget_show(w)
    return w.reinterpret()
}

fun mavTable(header:Array<String>):CPointer<GObject> {
    memScoped {
        val table_types =  allocArray<GTypeVar>(header.size)
        for (i in 0 until header.size) table_types[i] = G_TYPE_STRING
        val store = gtk_list_store_newv(header.size, table_types)!!
        val widget = gtk_tree_view_new()!!
        var render = gtk_cell_renderer_text_new()
        for ((n, el) in header.withIndex()) {
            gtk_tree_view_insert_column_with_attributes(widget.reinterpret(), -1, header[n], render, "text", n, nil)
            render = gtk_cell_renderer_text_new()
        }
        gtk_tree_view_set_model(widget.reinterpret(), store.reinterpret())
        gtk_widget_show(widget)
        return widget.reinterpret()
    }
}
fun tableColsWidth(widget:CPointer<GObject>,ar:Array<Int>){
    for((n,el) in ar.withIndex()) gtk_tree_view_column_set_fixed_width(gtk_tree_view_get_column(widget.reinterpret(),n), el)
}
fun tablePack(table:CPointer<GObject>,width:Int,height:Int):CPointer<GObject>{
    val sw = gtk_scrolled_window_new(null, null)!!
    gtk_widget_set_size_request(sw, width, height);
    gtk_container_add(sw.reinterpret(),table.reinterpret())
    gtk_widget_show(sw)
    return sw.reinterpret()
}

// Classes
open class MavWidget{
    val root:MutableMap<String,CPointer<GObject>>
    open val  widget:CPointer<GObject>
    constructor(_widget:CPointer<GObject>){
        widget=_widget
        root=mutableMapOf()
    }
    constructor(name:String,_root:MutableMap<String,CPointer<GObject>>) {
        root=_root
        widget= root[name]!!
    }
    var enable:Boolean
        get()=if(gtk_widget_get_sensitive(widget.reinterpret())==1) true else false
        set(value){ gtk_widget_set_sensitive (widget.reinterpret(), if(value) 1 else 0) }
    var visible:Boolean
        get()=if(gtk_widget_get_visible(widget.reinterpret()) ==1) true else false
        set(value){ gtk_widget_set_visible (widget.reinterpret(), if(value) 1 else 0) }
    fun setFocus(){ gtk_widget_grab_focus(widget.reinterpret())}
}

open class MavMenu:MavWidget{
    constructor(name:String,root:MutableMap<String,CPointer<GObject>>):super(name,root)
    constructor(_widget:CPointer<GObject>):super(_widget)
    fun show(w:CPointer<GObject>,n:Int){
        gtk_widget_show_all(widget.reinterpret())
        gtk_menu_popup_at_pointer(widget.reinterpret(),null)
    }
}

open class MavItem: MavWidget{
    constructor(name:String,root:MutableMap<String,CPointer<GObject>>):super(name,root)
    constructor(_widget:CPointer<GObject>):super(_widget)
    init{
        menus[widget] = this
        g_signal_connect(widget, "activate", staticCFunction(::menu_click))
    }
    var text:String
        get()=gtk_menu_item_get_label(widget.reinterpret())?.toKString() ?:"ERROR"
        set(value) {gtk_menu_item_set_label(widget.reinterpret(),value)}
    fun setText(title:String):MavItem{
        gtk_menu_item_set_label(widget.reinterpret(),title)
        return this
    }
    fun onClick(handler: MavItem.() -> Unit): MavItem {
        onClick = handler
        return this
    }
    internal var onClick: (MavItem.() -> Unit)? = null
    internal fun onClick() = onClick?.invoke(this)
}

open class MavWindow: MavWidget{
    constructor(name:String,root:MutableMap<String,CPointer<GObject>>):super(name,root)
    constructor(_widget:CPointer<GObject>):super(_widget)
    var text:String
       get()=  gtk_window_get_title(widget.reinterpret())?.toKString() ?:"err"
       set(value){ gtk_window_set_title(widget.reinterpret(),value)}
    fun show(): MavWindow {
        gtk_widget_show(widget.reinterpret())
        return this
    }
    fun close(){  gtk_window_close(widget.reinterpret()) }
    fun setText(text:String): MavWindow {
        gtk_window_set_title(widget.reinterpret(),text)
        return this
    }
}

open class MavLabel: MavWidget{
    constructor(name:String,root:MutableMap<String,CPointer<GObject>>):super(name,root)
    constructor(_widget:CPointer<GObject>):super(_widget)
    var text:String
        get()= gtk_label_get_text(widget.reinterpret())?.toKString() ?:"ERROR"
        set(value) {gtk_menu_item_set_label(widget.reinterpret(),value)}
    fun setText(title:String):MavLabel{
        gtk_label_set_text(widget.reinterpret(),title)
        return this
    }
}

open class MavCheck: MavWidget{
    constructor(name:String,root:MutableMap<String,CPointer<GObject>>):super(name,root)
    constructor(_widget:CPointer<GObject>):super(_widget)
    var checked:Boolean
        get()= if(gtk_toggle_button_get_active(widget.reinterpret())==1) true else false
        set(flg) {gtk_toggle_button_set_active(widget.reinterpret(),if(flg) 1 else 0)  }
}

open class MavButton: MavWidget{
    constructor(name:String,root:MutableMap<String,CPointer<GObject>>):super(name,root)
    constructor(_widget:CPointer<GObject>):super(_widget)
    init{
        buttons[widget] = this
        g_signal_connect(widget, "clicked", staticCFunction(::button_click))
    }
    var text:String
        get()= gtk_button_get_label(widget.reinterpret())?.toKString() ?:"Error"
        set(value){gtk_button_set_label(widget.reinterpret(),value)}
    fun onClick(handler: MavButton.() -> Unit): MavButton {
        onClick = handler
        return this
    }
    internal var onClick: (MavButton.() -> Unit)? = null
    internal fun onClick() = onClick?.invoke(this)
}

open class MavFlatButton: MavWidget{
    constructor(name:String,root:MutableMap<String,CPointer<GObject>>):super(name,root)
    constructor(_widget:CPointer<GObject>):super(_widget)
    init{
        flat_buttons[widget] = this
        g_signal_connect(widget, "clicked", staticCFunction(::flat_button_click))
    }
    var text:String
        get()= gtk_button_get_label(widget.reinterpret())?.toKString() ?:"Error"
        set(value){gtk_button_set_label(widget.reinterpret(),value)}
    fun onClick(handler: MavFlatButton.() -> Unit): MavFlatButton {
        onClick = handler
        return this
    }
    internal var onClick: (MavFlatButton.() -> Unit)? = null
    internal fun onClick() = onClick?.invoke(this)
}

open class MavDate: MavWidget{
    constructor(name:String,root:MutableMap<String,CPointer<GObject>>):super(name,root)
    constructor(_widget:CPointer<GObject>):super(_widget)
    init{
        dates[widget]=this
        g_signal_connect(widget, "key-press-event", staticCFunction(::date_key_event))
    }
    var text:String
        get()= gtk_entry_get_text(widget.reinterpret())?.toKString() ?:"ERROR"
        set(value){ gtk_entry_set_text(widget.reinterpret(),value)}
    fun setText(text:String): MavDate {
        gtk_entry_set_text(widget.reinterpret(),text)
        dates_text[widget]=text
        return this
    }
    fun setCurrentDay(): MavDate {
        this.setText(timeToDate(currentTimeMillis(), false))
        return this
    }
    fun setFirstDay(): MavDate {
        this.setText(timeToDate(currentTimeMillis(), true))
        return this
    }
    fun onChange(handler: MavDate.() -> Unit): MavDate {
        onChange = handler
        return this
    }
    internal var onChange: (MavDate.() -> Unit)? = null
    internal fun onChange() = onChange?.invoke(this)
}

open class MavText: MavWidget{
    constructor(name:String,root:MutableMap<String,CPointer<GObject>>):super(name,root)
    constructor(_widget:CPointer<GObject>):super(_widget)
    init{
        texts[widget]=this
        g_signal_connect(widget, "key-release-event", staticCFunction(::text_key_event))
        g_signal_connect(widget, "changed", staticCFunction(::text_change_event))
    }
    var text:String
        get()= gtk_entry_get_text(widget.reinterpret())?.toKString() ?:"ERROR"
        set(value){ gtk_entry_set_text(widget.reinterpret(),value)}
    fun setText(text:String): MavText {
        gtk_entry_set_text(widget.reinterpret(),text)
        return this
    }
    fun selectAll(): MavText{
        gtk_editable_select_region(widget.reinterpret(),0,-1)
        return this
    }
    fun onKey(handler: MavText.(Int) -> Unit): MavText {
        onKey= handler
        return this
    }
    internal var onKey: (MavText.(Int) -> Unit)? = null
    fun onKey(l:Int) = onKey?.invoke(this,l)

    fun onChange(handler: MavText.() -> Unit): MavText {
        onChange = handler
        return this
    }
    internal var onChange: (MavText.() -> Unit)? = null
    internal fun onChange() = onChange?.invoke(this)
}

open class MavCombo: MavWidget{
    constructor(name:String,root:MutableMap<String,CPointer<GObject>>):super(name,root)
    constructor(_widget:CPointer<GObject>):super(_widget)
    init{
        combos[widget]=this
        g_signal_connect(widget, "changed", staticCFunction(::combo_change))
    }
    var store:ArrayList<String> =ArrayList()
    var index:Int
        get()=gtk_combo_box_get_active(widget.reinterpret())
        set(value){ gtk_combo_box_set_active(widget.reinterpret(),value)}

    fun setIndex(index:Int): MavCombo {
        gtk_combo_box_set_active(widget.reinterpret(),index)
        return this
    }
    var text:String
        get()= gtk_combo_box_text_get_active_text(widget.reinterpret())?.toKString() ?:"ERROR"
        set(value){gtk_combo_box_set_active(widget.reinterpret(),store.indexOf(value))}

    fun setText(text:String): MavCombo {
        gtk_combo_box_set_active(widget.reinterpret(),store.indexOf(text))
        return this
    }

    fun fill(lst:List<String>): MavCombo {
        lst.forEach {
            gtk_combo_box_text_append_text(widget.reinterpret(),it)
            store.add(it)
        }
        gtk_combo_box_set_active(widget.reinterpret(),0)
        return this
    }
    fun fill(lst:List<String>,ar:Array<String>): MavCombo {
        ar.forEach {
            gtk_combo_box_text_append_text(widget.reinterpret(),it)
            store.add(it)
        }
        lst.forEach {
            gtk_combo_box_text_append_text(widget.reinterpret(),it)
            store.add(it)
        }
        gtk_combo_box_set_active(widget.reinterpret(),0)
        return this
    }
    fun onChange(handler: MavCombo.() -> Unit): MavCombo {
        onChange = handler
        return this
    }
    internal var onChange: (MavCombo.() -> Unit)? = null
    internal fun onChange() = onChange?.invoke(this)
}

open class MavTable: MavWidget {
    val cols:Int
    constructor(name:String,root:MutableMap<String,CPointer<GObject>>,_cols:Int):super(name,root){  cols=_cols  }
    constructor(_widget:CPointer<GObject>,_cols:Int):super(_widget){ cols=_cols }
    init{
        tables[widget]=this
        g_signal_connect(widget, "row-activated", staticCFunction(::table_double_click))
        g_signal_connect(widget, "cursor-changed", staticCFunction(::table_click))
        g_signal_connect(widget, "button-press-event", staticCFunction(::table_button_event))
        g_signal_connect(widget, "popup-menu", staticCFunction(::table_menu))
        gtk_tree_view_set_grid_lines(widget.reinterpret(),GtkTreeViewGridLines.GTK_TREE_VIEW_GRID_LINES_BOTH)
    }
    var store:ArrayList<out Item>?=null
    private var flds:Array<Int>?=null
    private var fns:Array<out (Item, Int)->Any>?=null
    fun clickFun(l:Int) = onClick?.invoke(this,l)
    fun dblClickFun(l:Int) = onDblClick?.invoke(this,l)
    var index:Int
        get()= getTreeViewIndex(widget.reinterpret())
        set(value){
           val path = gtk_tree_path_new_from_indices(value, -1);
            gtk_tree_selection_select_path(gtk_tree_view_get_selection(widget.reinterpret()), path);
            gtk_tree_path_free(path)
        }
    fun setIndex(n:Int): MavTable {
        val path = gtk_tree_path_new_from_indices(n, -1);
        gtk_tree_selection_select_path(gtk_tree_view_get_selection(widget.reinterpret()), path);
        gtk_tree_path_free(path)
        return this
    }
    fun scrollTo(n:Int): MavTable {
        val path = gtk_tree_path_new_from_indices(n, -1);
        gtk_tree_view_scroll_to_cell(widget.reinterpret(),path,null,0,0.0f,0.0f)
        return this
    }
    fun <T: Item>getSelectedRow():T{
        val n= getTreeViewIndex(widget.reinterpret())
        return store!![n] as T
    }
    fun setHeaderText(n:Int,s:String){
        val column=gtk_tree_view_get_column(widget.reinterpret(),n)
        gtk_tree_view_column_set_title(column,s)
    }
    fun<T: Item> delete(): MavTable {
        val n= getTreeViewIndex(widget.reinterpret())
        val path = gtk_tree_path_new_from_indices(n, -1)
        val model = gtk_tree_view_get_model(widget.reinterpret())!!
        memScoped {
            val iter = alloc<GtkTreeIter>()
            gtk_tree_model_get_iter(model, iter.ptr, path)
            gtk_list_store_remove(model.reinterpret(),iter.ptr)
        }
        if(n>0){
            index=n-1
            scrollTo(n-1)
        }
        return this
    }
    fun<T:Item>add(item:T): MavTable {
        val model =gtk_tree_view_get_model(widget.reinterpret())!!
        memScoped {
            val iter = alloc<GtkTreeIter>()
            gtk_list_store_append(model.reinterpret(), iter.ptr)
            for(i in 0 until cols ) {
                val value=if(fns==null) item[i] else  fns!![i](item,flds!![i])
                gtk_list_store_set(model.reinterpret(), iter.ptr, i, value.toString(), -1)
            }
        }
        setIndex(store!!.size-1)
        scrollTo(store!!.size-1)
        return this
    }
    fun<T:Item>change(item:T): MavTable {
        val n=store!!.indexOf { item.id==it.id }
        val model =gtk_tree_view_get_model(widget.reinterpret())!!
        memScoped {
            val iter = alloc<GtkTreeIter>()
            gtk_tree_model_get_iter_from_string(model,iter.ptr,n.toString())
            for(i in 0 until cols ) {
                val value=if(fns==null) item[i] else  fns!![i](item,flds!![i])
                gtk_list_store_set(model.reinterpret(), iter.ptr, i, value.toString(), -1)
            }
        }
        return this
    }
    fun<T:Item> fill(store:ArrayList<T>): MavTable {
        this.store=store
        val model =gtk_tree_view_get_model(widget.reinterpret())!!
        clearModel(model)
        memScoped {
            val iter = alloc<GtkTreeIter>()
            for(el in store){
                gtk_list_store_append(model.reinterpret(), iter.ptr)
                for(i in 0 until cols ) {
                    val value=el[i]
                    gtk_list_store_set(model.reinterpret(), iter.ptr, i, value.toString(), -1)
                }
            }
        }
        gtk_tree_view_set_model(widget.reinterpret(),model.reinterpret())
        return this
    }
    fun<T: Item>fill(store:ArrayList<T>, flds:Array<Int>, fns:Array< (T, Int)->Any>): MavTable {
        this.store=store
        this.flds=flds
        this.fns=fns as Array<out (Item, Int)->Any>
        val model =gtk_tree_view_get_model(widget.reinterpret())!!
        clearModel(model)
        memScoped {
            val iter = alloc<GtkTreeIter>()
            for(el in store){
                gtk_list_store_append(model.reinterpret(), iter.ptr)
                for(i in 0 until cols ) {
                    val value= fns[i](el, flds[i])
                    gtk_list_store_set(model.reinterpret(), iter.ptr, i, value.toString(), -1)
                }
            }
        }
        gtk_tree_view_set_model(widget.reinterpret(),model.reinterpret())
        return this
    }
    fun onClick(handler: MavTable.(Int) -> Unit): MavTable {
        onClick = handler
        return this
    }
    fun onDblClick(handler: MavTable.(Int) -> Unit): MavTable {
        onDblClick = handler
        return this
    }
    fun onMenu(handler: MavTable.(Int) -> Unit): MavTable {
        onMenu = handler
        return this
    }
    internal var onClick: (MavTable.(Int) -> Unit)? = null
    internal var onDblClick: (MavTable.(Int) -> Unit)? = null
    internal var onMenu: (MavTable.(Int) -> Unit)? = null
    internal fun onMenu(l:Int) { setIndex(l); onMenu?.invoke(this,l)}
}