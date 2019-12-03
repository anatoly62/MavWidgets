package mav.widgets.iup

import mav.core.*
import kotlinx.cinterop.*
import libiupex.*

const val library="IUP"
const val CURSOR_COLOR="145 201 247"

val nil:CPointer<Ihandle>?=null

open class Application() {
	val shell=0
	init{
		IupOpen(null,null)
		IupSetGlobal("UTF8MODE","YES")
		IupControlsOpen()
		start(0)
		loopGui(shell)
	}
	open fun start(shell:Int){ }
}

fun tryE(fn:()->Unit){
	try{fn() }
	catch (e:Exception){
		showError(e.toString())
	}
}

fun loopGui(shell:Int=0){
	IupMainLoop()
	IupClose()
}

fun getKeyName(k:Int)=when(k){
	8->"BACK_SPACE"
	13->"ENTER"
	27->"ECSAPE"
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

fun searchForCombo(cb: MavCombo, s:String, n:Int){
	val handle=cb.widget
	val ss=s.split(" ")
	val len=IupGetInt(handle,"COUNT");
	for (i in 1..len) {
		val item=IupGetAttribute(handle,i.toString())?.toKString()?.toLowerCase() ?:""
		val names=item.split(" ")
		if (names.size==n+1) {
			if (names[n].startsWith(s)){
				IupSetAttribute(handle,"VALUE",i.toString())
			}
		}
		else if (ss.size>1){
			if (names[n].startsWith(ss[0]) && names[n+1].startsWith(ss[1])) {
				IupSetAttribute(handle,"VALUE",i.toString())
			}
		}
		else if (s.length>0 && ss.size==1) {
			if (s.startsWith(" ") && names[n+1].startsWith(ss[0])) {
				IupSetAttribute(handle,"VALUE",i.toString())
			}
			else if (!s.startsWith(" ") && names[n].startsWith(ss[0])){
				IupSetAttribute(handle,"VALUE",i.toString());
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
fun showError(s:String,root:MutableMap<String,CPointer<Ihandle>?> =  mutableMapOf()){ IupMessage("Error",s) }
fun showMessage(s:String,root:MutableMap<String,CPointer<Ihandle>?> =  mutableMapOf()){ IupMessage("Message",s) }
private var dates = mutableMapOf<CPointer<Ihandle>?, MavDate>()
private var texts = mutableMapOf<CPointer<Ihandle>?, MavText>()
private var buttons = mutableMapOf<CPointer<Ihandle>?, MavButton>()
private var combos = mutableMapOf<CPointer<Ihandle>?, MavCombo>()
private var flatButtons = mutableMapOf<CPointer<Ihandle>?, MavFlatButton>()
private var tables = mutableMapOf<CPointer<Ihandle>?, MavTable>()
private var tabs = mutableMapOf<CPointer<Ihandle>?, MavTab>()
private var grids = mutableMapOf<CPointer<Ihandle>?, MavGrid>()
private var menus = mutableMapOf<CPointer<Ihandle>?, MavItem>()
private var checks = mutableMapOf<CPointer<Ihandle>?, MavCheck>()
private var radios = mutableMapOf<CPointer<Ihandle>?, MavRadio>()
private var trees = mutableMapOf<CPointer<Ihandle>?, MavTree>()

@Suppress("UNUSED_PARAMETER")
fun text_k_any(ptr:CPointer<Ihandle>?,l:Int,c:Int,fake: CPointer<ByteVar>?):Int{
	texts[ptr]?.onKey(l)
	return IUP_DEFAULT
}
@Suppress("UNUSED_PARAMETER")
fun table_k_any(ptr:CPointer<Ihandle>?,l:Int,c:Int,fake: CPointer<ByteVar>?):Int{
	tables[ptr].let {
		val idx=it?.index ?:-1
		when(l){
            13->it?.dblClickFun(it?.index)
            65364->{//Down
                val maxIdx=it?.store?.size ?:-1
			    if (idx<maxIdx-1) {
					it?.setIndex(idx+1)
					it?.scrollTo(idx+1)
					it?.clickFun(idx+1)
				} else {}
				return IUP_IGNORE
            }
            65362->{//Up
                 if (idx>0){
					it?.setIndex(idx-1)
					it?.scrollTo(idx-1)
					it?.clickFun(idx-1)
				} else {}
				return IUP_IGNORE
            }
			65360,536936272->{ //Home
				if (idx>0){
					it?.setIndex(0)
					it?.scrollTo(0)
					it?.clickFun(0)
				} else {}
				return IUP_IGNORE
			}
			65367,536936279->{ //End
				val maxIdx=it?.store?.size ?:-1
				if (idx<maxIdx-1) {
					it?.setIndex(maxIdx-1)
					it?.scrollTo(maxIdx-1)
					it?.clickFun(maxIdx-1)
				} else {}
				return IUP_IGNORE
			}
			65365->{ //PgUp
				val lineVisible = IupGetAttribute(it?.widget, "NUMLIN_VISIBLE")!!.toKString().toInt() ?: 0
				val line=if(idx-lineVisible>0) idx-lineVisible else 0
				it?.setIndex(line)
				it?.scrollTo(line)
				it?.clickFun(line)
				return IUP_IGNORE
			}
			65366->{ //PgDown
				val lineVisible = IupGetAttribute(it?.widget, "NUMLIN_VISIBLE")!!.toKString().toInt() ?: 0
				val endLine=it?.store?.size ?:0
				val line=if(idx+lineVisible<endLine-1) idx+lineVisible else endLine-1
				it?.setIndex(line)
				it?.scrollTo(line)
				it?.clickFun(line)
				return IUP_IGNORE
			}
            else->{}
        }
	}
	return IUP_DEFAULT
}

@Suppress("UNUSED_PARAMETER")
fun text_valuechanged_cb(ptr:CPointer<Ihandle>?,l:Int,c:Int,fake: CPointer<ByteVar>?):Int{
	texts[ptr]?.onChange()
	texts[ptr]?.onKey(0)
	return IUP_DEFAULT
}
@Suppress("UNUSED_PARAMETER")
fun combo_valuechanged_cb(ptr:CPointer<Ihandle>?,l:Int,c:Int,fake: CPointer<ByteVar>?):Int{
	combos[ptr]?.onChange()
	return IUP_DEFAULT
}
@Suppress("UNUSED_PARAMETER")
fun date_valuechanged_cb(ptr:CPointer<Ihandle>?,l:Int,c:Int,fake: CPointer<ByteVar>?):Int{
	dates[ptr]?.onChange()
	return IUP_DEFAULT
}
@Suppress("UNUSED_PARAMETER")
fun check_valuechanged_cb(ptr:CPointer<Ihandle>?,l:Int,c:Int,fake: CPointer<ByteVar>?):Int{
	checks[ptr]?.onChange()
	return IUP_DEFAULT
}
@Suppress("UNUSED_PARAMETER")
fun radio_valuechanged_cb(ptr:CPointer<Ihandle>?,l:Int,c:Int,fake: CPointer<ByteVar>?):Int{
	radios[ptr]?.onChange()
	return IUP_DEFAULT
}
@Suppress("UNUSED_PARAMETER")
fun tree_selection_cb(ptr:CPointer<Ihandle>?,l:Int,c:Int,fake: CPointer<ByteVar>?):Int{
	if(l>0 && c==1) {
		val title= IupGetAttribute(ptr,"TITLE"+l)?.toKString() ?: ""
		trees[ptr]?.onClick(l, title)
	}
    return IUP_DEFAULT
}

@Suppress("UNUSED_PARAMETER")
fun menu_action_cb(ptr:CPointer<Ihandle>?,l:Int,c:Int,fake: CPointer<ByteVar>?):Int{
	menus[ptr]?.onClick()
	return IUP_DEFAULT
}
@Suppress("UNUSED_PARAMETER")
fun button_action_cb(ptr:CPointer<Ihandle>?,l:Int,c:Int,fake: CPointer<ByteVar>?):Int{
	buttons[ptr]?.onClick()
	return IUP_DEFAULT
}
@Suppress("UNUSED_PARAMETER")
fun flatButton_action_cb(ptr:CPointer<Ihandle>?,l:Int,c:Int,fake: CPointer<ByteVar>?):Int{
	flatButtons[ptr]?.onClick()
	return IUP_DEFAULT
}

@Suppress("UNUSED_PARAMETER")
fun grid_value_edit_cb(ptr:CPointer<Ihandle>?,l:Int,c:Int,valPtr: CPointer<ByteVar>?): Int{
	val s=valPtr!!.toKString()
	grids[ptr]?.onEdit(l-1,c-1,s)
	return IUP_DEFAULT
}

@Suppress("UNUSED_PARAMETER")
fun grid_edition_cb(ptr:CPointer<Ihandle>?,l:Int,c:Int,valPtr: CPointer<ByteVar>?): Int{
	val flg= grids[ptr]?.editables?.get(c-1) ?:false
		if(flg) return IUP_DEFAULT
	return IUP_IGNORE
}

@Suppress("UNUSED_PARAMETER")
fun tab_change_cb(ptr:CPointer<Ihandle>?,l:Int,c:Int,fake: CPointer<ByteVar>?): Int{
	tabs[ptr]?.onChange(l,c)
	return IUP_DEFAULT
}

@Suppress("UNUSED_PARAMETER")
fun table_value_cb(ptr:CPointer<Ihandle>?,l:Int,c:Int): CPointer<ByteVar>?{
	val str= tables[ptr]?.onValue(l,c)
	memScoped {
		return interpretCPointer<ByteVar>(str?.cstr?.getPointer(memScope).rawValue)
	}
}	
fun table_enteritem_cb(ptr:CPointer<Ihandle>?,l:Int,c:Int,fake:CPointer<ByteVar>?):Int{
	tables[ptr]?.clickFun(l-1)
	return IUP_DEFAULT
}
fun table_click_cb(ptr:CPointer<Ihandle>?,l:Int,c:Int,status:CPointer<ByteVar>?):Int{
	val table= tables[ptr]!!
	if (table.cursorIndex>=0) cursor(table.widget, table.cursorIndex + 1, "255 255 255")
	table.cursorIndex=l-1
	cursor(table.widget, l, CURSOR_COLOR);
	IupSetStrAttribute(table.widget,"FOCUSCELL",l.toString()+":1")
	if(status!!.toKString()[5]=='D')
		table.dblClickFun(l-1)
	else if((status!!.toKString()[4]=='3'))
		table.onMenu(l-1)
	else
		table.clickFun(l-1)
	return IUP_DEFAULT
}

open class MavWidget{
	val root:MutableMap<String,CPointer<Ihandle>?>
	open val widget: CPointer<Ihandle>
	constructor(_widget:CPointer<Ihandle>){
		widget=_widget
		root=mutableMapOf()
	}
	constructor(name:String,_root:MutableMap<String,CPointer<Ihandle>?>) {
		root=_root
		widget= root[name]!!
	}

	var visible:Boolean
		get() {
			val atr=IupGetAttribute(this.widget, "VISIBLE")?.toKString() ?:"NO"
			return if(atr=="NO") false else true
		}
		set(flg){IupSetStrAttribute( this.widget, "VISIBLE",if(flg)"YES" else "NO")	}
	var enable:Boolean
		get() {
			val atr=IupGetAttribute(this.widget, "ACTIVE")?.toKString() ?:"NO"
			return if(atr=="NO") false else true
		}
		set(flg){IupSetStrAttribute( this.widget, "ACTIVE",if(flg)"YES" else "NO")	}
	fun setFocus(): MavWidget {
		IupSetFocus(this.widget)
		return this
	}
}

open class MavWindow: MavWidget{
	constructor(name:String,root:MutableMap<String,CPointer<Ihandle>?>):super(name,root)
	constructor(_widget:CPointer<Ihandle>):super(_widget)
	var text:String
		get()=IupGetAttribute(this.widget,"TITLE")?.toKString() ?:"err"
		set(s){IupSetStrAttribute( this.widget, "TITLE", s)}
	fun show(): MavWindow {
		IupShow(widget)
		return this
	}
	fun close(){
		IupDestroy(widget)
	}
	fun setText(s:String): MavWindow {
		IupSetStrAttribute( this.widget, "TITLE",s)
		return this
	}
}

open class MavMenu: MavWidget{
	constructor(name:String,root:MutableMap<String,CPointer<Ihandle>?>):super(name,root)
	constructor(_widget:CPointer<Ihandle>):super(_widget)
	fun show(fake:CPointer<Ihandle>?,fake2:Any){	IupPopup(widget,IUP_MOUSEPOS,IUP_MOUSEPOS) }
}

fun mavItem(title:String="",active:String="yes"):CPointer<Ihandle>?=IupSetAttributes( IupItem(title,""), "ACTIVE=$active")
open class MavItem: MavWidget{
	constructor(name:String,root:MutableMap<String,CPointer<Ihandle>?>):super(name,root)
	constructor(_widget:CPointer<Ihandle>):super(_widget)
	var text:String
		get()=IupGetAttribute(this.widget,"TITLE")?.toKString() ?:"err"
		set(s){IupSetStrAttribute( this.widget, "TITLE", s)}
	init{
		menus[widget] = this
		IupSetCallback( this.widget, "ACTION", staticCFunction(::menu_action_cb))
	}
	fun setText(s:String): MavItem {
		IupSetStrAttribute( this.widget, "TITLE",s)
		return this
	}
	fun onClick(handler: MavItem.() -> Unit): MavItem {
		onClick = handler
		return this
	}
    internal var onClick: (MavItem.() -> Unit)? = null
    internal fun onClick() = onClick?.invoke(this)			
}

fun mavLabel(title:String="",size:String="xx"):CPointer<Ihandle>?=IupSetAttributes( IupLabel(title), "SIZE=$size")
open class MavLabel: MavWidget {
	constructor(name:String,root:MutableMap<String,CPointer<Ihandle>?>):super(name,root)
	constructor(_widget:CPointer<Ihandle>):super(_widget)
	var text:String
		get()=IupGetAttribute(this.widget,"TITLE")?.toKString() ?:"err"
		set(s){IupSetStrAttribute( this.widget, "TITLE", s)}
	fun setText(s:String): MavLabel {
		IupSetStrAttribute( this.widget, "TITLE", s)
		return this
	}
}

fun mavDate(text:String="",size:String="xx"):CPointer<Ihandle>?=IupSetAttributes( IupDatePick(), "SEPARATOR=-,ZEROPRECED=yes,SIZE=$size")
open class MavDate: MavWidget{
	constructor(name:String,root:MutableMap<String,CPointer<Ihandle>?>):super(name,root)
	constructor(_widget:CPointer<Ihandle>):super(_widget)
	var text:String
		get(){
			val str= IupGetAttribute(this.widget,"VALUE")?.toKString() ?:"3000-01-01"
			val ar=str.split("/")
			return ar[0]+"-"+(if(ar[1].length>1) ar[1] else "0"+ar[1])+"-"+(if(ar[2].length>1) ar[2] else "0"+ar[2])
		}
		set(s){	IupSetStrAttribute( this.widget, "VALUE", s.replace("-","/"))}
	init{
		dates[widget]=this
		IupSetCallback( this.widget, "VALUECHANGED_CB", staticCFunction(::date_valuechanged_cb))
	}
	fun setText(s:String): MavDate {
		IupSetStrAttribute( this.widget, "VALUE", s.replace("-","/"))
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
	fun onChange(handler: MavDate.() -> Unit): MavDate {
		onChange = handler
		return this
	}
    internal var onChange: (MavDate.() -> Unit)? = null
    internal fun onChange() = onChange?.invoke(this)
}

fun mavCheck(title:String="",size:String="xx")=IupSetAttributes( IupToggle(title,""), "SIZE=$size")
open class MavCheck: MavWidget{
	constructor(name:String,root:MutableMap<String,CPointer<Ihandle>?>):super(name,root)
	constructor(_widget:CPointer<Ihandle>):super(_widget)
	var checked:Boolean
		get(){
			val atr=IupGetAttribute(this.widget, "VALUE")?.toKString() ?:"OFF"
			return if(atr=="ON") true else false
		}
		set(flg) {IupSetStrAttribute( this.widget, "VALUE",if(flg)"ON" else "OFF")	}
	var text:String
		get()=IupGetAttribute(this.widget,"TITLE")?.toKString() ?:"err"
		set(s){IupSetStrAttribute( this.widget, "TITLE", s)}
	init{
		checks[widget]=this
		IupSetCallback( this.widget, "VALUECHANGED_CB", staticCFunction(::check_valuechanged_cb))
	}
	fun onChange(handler: MavCheck.() -> Unit): MavCheck {
		onChange = handler
		return this
	}
	internal var onChange: (MavCheck.() -> Unit)? = null
	internal fun onChange() = onChange?.invoke(this)
}

fun mavRadio(title:String="",size:String="xx")=IupSetAttributes( IupToggle(title,""), "SIZE=$size")
open class MavRadio: MavWidget{
	constructor(name:String,root:MutableMap<String,CPointer<Ihandle>?>):super(name,root)
	constructor(_widget:CPointer<Ihandle>):super(_widget)
	var checked:Boolean
		get(){
			val atr=IupGetAttribute(this.widget, "VALUE")?.toKString() ?:"OFF"
			return if(atr=="ON") true else false
		}
		set(flg) {IupSetStrAttribute( this.widget, "VALUE",if(flg)"ON" else "OFF")	}
	var text:String
		get()=IupGetAttribute(this.widget,"TITLE")?.toKString() ?:"err"
		set(s){IupSetStrAttribute( this.widget, "TITLE", s)}
	init{
		radios[widget]=this
		IupSetCallback( this.widget, "VALUECHANGED_CB", staticCFunction(::radio_valuechanged_cb))
	}
	fun onChange(handler: MavRadio.() -> Unit): MavRadio {
		onChange = handler
		return this
	}
	internal var onChange: (MavRadio.() -> Unit)? = null
	internal fun onChange() = onChange?.invoke(this)
}

fun mavCombo(value:String="1", visibleitems:String="5", size:String="xx", expand:String="no")=
	IupSetAttributes( IupList(""), "VALUE=$value,VISIBLEITEMS=$visibleitems,SIZE=$size,EXPAND=$expand,DROPDOWN=yes")
open class MavCombo: MavWidget{
	constructor(name:String,root:MutableMap<String,CPointer<Ihandle>?>):super(name,root)
	constructor(_widget:CPointer<Ihandle>):super(_widget)
	var text:String
		get()=IupGetAttribute(this.widget,"VALUESTRING")?.toKString() ?:"err"
		set(s){IupSetStrAttribute( this.widget, "VALUESTRING", s)}
	var index:Int
		get()=IupGetAttribute(this.widget,"VALUE")!!.toKString().toInt()-1
		set(n){IupSetStrAttribute( this.widget, "VALUE", (n+1).toString())}
	init{
		combos[widget] = this
		IupSetCallback( this.widget, "VALUECHANGED_CB", staticCFunction(::combo_valuechanged_cb))
	}
	fun getIndex():Int=IupGetAttribute(this.widget,"VALUE")!!.toKString().toInt()-1
	fun setIndex(n:Int): MavCombo {
		IupSetStrAttribute(this.widget,"VALUE",(n+1).toString())
		return this
	}
	fun setText(s:String): MavCombo {
		IupSetStrAttribute( this.widget, "VALUESTRING", s)
		return this
	}
	//fun getText():String=IupGetAttribute(this.widget,"VALUESTRING")?.toKString() ?:"err"
	fun fill(lst:List<String>): MavCombo {
		var i=1;
		for(el in lst){
			IupSetStrAttribute(this.widget,i.toString(),el)
			i+=1	
		}
		IupSetStrAttribute( this.widget, "VALUE", "1")
		return this
	}
	fun fill(lst:List<String>,ar:Array<String>): MavCombo {
		var i=1;
		for(el in ar){
			IupSetStrAttribute(this.widget,i.toString(),el)
			i+=1	
		}
		for(el in lst){
			IupSetStrAttribute(this.widget,i.toString(),el)
			i+=1	
		}
		IupSetStrAttribute( this.widget, "VALUE", "1")
		return this
	}
	fun onChange(handler: MavCombo.() -> Unit): MavCombo {
		onChange = handler
		return this
	}
    internal var onChange: (MavCombo.() -> Unit)? = null
    internal fun onChange() = onChange?.invoke(this)	
}

fun mavText(text:String="",size:String="xx",expand:String="no",active:String="yes",visible:String="yes",multiline:String="no",psw:String="no")=
	IupSetAttributes( IupText(null),
		"VALUE=$text,SIZE=$size,EXPAND=$expand,ACTIVE=$active,VISIBLE=$visible,MULTILINE=$multiline,PASSWORD=$psw")
open class MavText: MavWidget{
	constructor(name:String,root:MutableMap<String,CPointer<Ihandle>?>):super(name,root)
	constructor(_widget:CPointer<Ihandle>):super(_widget)
	var text:String
		get()=IupGetAttribute(this.widget,"VALUE")?.toKString() ?:"err"
		set(s){IupSetStrAttribute( this.widget, "VALUE", s)}
	init{
		texts[widget] = this
		IupSetCallback( this.widget, "VALUECHANGED_CB", staticCFunction(::text_valuechanged_cb))
		IupSetCallback( this.widget, "K_ANY", staticCFunction(::text_k_any))
	}
	fun setText(s:String): MavText {
		IupSetStrAttribute( this.widget, "VALUE", s)
		return this
	}
	//fun getText():String=IupGetAttribute(this.widget,"VALUE")?.toKString() ?:"err"
	
	fun selectAll(): MavText {
		IupSetStrAttribute(this.widget,"SELECTION","ALL")
		return this
	}
	fun onChange(handler: MavText.() -> Unit): MavText {
		onChange = handler
		return this
	}
    internal var onChange: (MavText.() -> Unit)? = null
    internal fun onChange() = onChange?.invoke(this)
	fun onKey(handler: MavText.(c:Int) -> Unit): MavText {
		onKey = handler
		return this
	}
    internal var onKey: (MavText.(c:Int) -> Unit)? = null
    internal fun onKey(c:Int) = onKey?.invoke(this,c)			
}

fun mavButton(title:String="",size:String="xx",active:String="yes"):CPointer<Ihandle>?=
	IupSetAttributes(IupButton(title,""),"SIZE=$size,ACTIVE=$active")
open class MavButton: MavWidget{
	constructor(name:String,root:MutableMap<String,CPointer<Ihandle>?>):super(name,root)
	constructor(_widget:CPointer<Ihandle>):super(_widget)
	var text:String
		get()=IupGetAttribute(this.widget,"TITLE")?.toKString() ?:"err"
		set(s){IupSetStrAttribute( this.widget, "TITLE", s)}
	init{
		buttons[widget] = this
		IupSetCallback( this.widget, "ACTION", staticCFunction(::button_action_cb))
	}
	fun setText(s:String): MavButton {
		IupSetAttribute( this.widget, "TITLE", s )
		return this
	}
	fun onClick(handler: MavButton.() -> Unit): MavButton {
		onClick = handler
		return this
	}

	fun onClickE(handler: MavButton.() -> Unit): MavButton {
		onClick = { tryE { handler() } }
		return this
	}

    internal var onClick: (MavButton.() -> Unit)? = null
    internal fun onClick() = onClick?.invoke(this)	
}

fun mavFlatButton(title:String="",size:String="xx",image:String,tip:String=""):CPointer<Ihandle>?{
	val widget=IupFlatButton(title)
	IupSetAttributes(widget, "SIZE=$size")
	IupSetStrAttribute(widget,"TIP",tip)
	IupSetAttributeHandle(widget, "IMAGE", IupLoadImage(image))
	return widget
}
open class MavFlatButton: MavWidget{
	constructor(name:String,root:MutableMap<String,CPointer<Ihandle>?>):super(name,root)
	constructor(_widget:CPointer<Ihandle>):super(_widget)
	var text:String
		get()=IupGetAttribute(this.widget,"TITLE")?.toKString() ?:"err"
		set(s){IupSetStrAttribute( this.widget, "TITLE", s)}
	init{
		flatButtons[widget] = this
		IupSetCallback( this.widget, "FLAT_ACTION", staticCFunction(::flatButton_action_cb))
	}
	fun setText(s:String): MavFlatButton {
		IupSetAttribute( this.widget, "TITLE", s )
		return this
	}
	fun onClick(handler: MavFlatButton.() -> Unit): MavFlatButton {
		onClick = handler
		return this
	}
    internal var onClick: (MavFlatButton.() -> Unit)? = null
    internal fun onClick() = onClick?.invoke(this)	
}
fun mavTable(labels:Array<String>):CPointer<Ihandle>?{
	val widget=IupMatrix(null)
	val lcol=labels.size.toString()
	IupSetAttributes(widget,"NUMLIN_VISIBLE=0,NUMCOL_VISIBLE=0,RESIZEMATRIX=YES,HEIGHT0=8,READONLY=YES,EXPAND=YES")
	IupSetStrAttribute(widget,"NUMCOL",lcol)
	IupSetCallback(widget, "CLICK_CB", staticCFunction(::table_click_cb))
	IupSetCallback( widget, "K_ANY", staticCFunction(::table_k_any))
	for (n in 1..labels.size)
		IupSetStrAttribute(widget,"0:"+n.toString(),labels[n-1])
	return widget
}

fun cursor(table:CPointer<Ihandle>?, line:Int,value:String){
	val r = line.toString() + ":*"
	IupSetStrAttribute(table, "BGCOLOR" + r, value)
	IupSetStrAttribute(table, "REDRAW", "L" + line.toString())
}

open class MavTable:MavWidget{
	val cols:Int
	constructor(name:String,root:MutableMap<String,CPointer<Ihandle>?>,_cols:Int):super(name,root){  cols=_cols  }
	constructor(_widget:CPointer<Ihandle>,_cols:Int):super(_widget){ cols=_cols }
	var store:ArrayList<out Item>?=null
	fun clickFun(l:Int) = onClick?.invoke(l)
	fun dblClickFun(l:Int) = onDblClick?.invoke(l)
	private var flds:Array<Int>?=null
	private var fns:Array<out (Item, Int)->Any>?=null
	var cursorIndex:Int=-1
	var index:Int
		get(){
			val (l,c)=IupGetAttribute(this.widget,"FOCUSCELL")!!.toKString().split(":")
			return l.toInt()-1
		}
		set(line){
			if(line>=0){
				if (cursorIndex>=0) cursor(this.widget, cursorIndex + 1, "255 255 255")
				cursorIndex=line
				cursor(this.widget, line + 1, CURSOR_COLOR);
				IupSetStrAttribute(this.widget,"FOCUSCELL",(line+1).toString()+":1")
			}
		}
	init { tables[widget] = this  }
	fun setIndex(line:Int): MavTable {
		if(line>=0){
			if (cursorIndex>=0) cursor(this.widget, cursorIndex + 1, "255 255 255")
			cursorIndex=line
			cursor(this.widget, line + 1, CURSOR_COLOR);
			IupSetStrAttribute(this.widget,"FOCUSCELL",(line+1).toString()+":1")
		}
        return this
	}
	fun scrollTo(line:Int): MavTable {
		if (line>=0) {
			val lineVisible = IupGetAttribute(this.widget, "NUMLIN_VISIBLE")!!.toKString().toInt()
			val (origin, _) = IupGetAttribute(this.widget, "ORIGIN")!!.toKString().split(":")
			if(line>=lineVisible+origin.toInt()-1   )
				IupSetStrAttribute(this.widget, "ORIGIN", (line + 2-lineVisible).toString() + "*")
			else if(line<origin.toInt())
				IupSetStrAttribute(this.widget, "ORIGIN", (line+1).toString() + "*")
		}
		return this
	}
	fun <T: Item>getSelectedRow():T?{
		val (l,c)=IupGetAttribute(this.widget,"FOCUSCELL")!!.toKString().split(":")
		return this.store!![l.toInt()-1] as T
	}

	fun<T: Item> add(item:T): MavTable {
		val l=IupGetAttribute(this.widget,"NUMLIN")!!.toKString().toInt()
		IupSetStrAttribute(this.widget,"ADDLIN",l.toString())
		for (j in 1..cols) {
			val value=if(fns==null) item[j-1] else  fns!![j-1](item,flds!![j-1])
			IupSetStrAttribute(this.widget,(l+1).toString()+":"+j.toString(),value.toString())
		}
		this.setIndex(l)
		this.scrollTo(l)
		return this
	}
	fun<T: Item> change(item:T): MavTable {
		val (l,c)=IupGetAttribute(this.widget,"FOCUSCELL")!!.toKString().split(":")
		for (j in 1..cols) {
			val value=if(fns==null) item[j-1] else  fns!![j-1](item,flds!![j-1])
			IupSetStrAttribute(this.widget,l.toString()+":"+j.toString(),value.toString())
		}
		IupSetStrAttribute(this.widget,"REDRAW","L"+l.toString())
		return this
	}
	fun<T: Item> delete(): MavTable {
		val (l,c)=IupGetAttribute(this.widget,"FOCUSCELL")!!.toKString().split(":")
		IupSetStrAttribute(this.widget,"DELLIN",l.toString())
		val n=l.toInt()
		if(n>1) this.setIndex(n-2)
		else if(store!!.size>0) this.setIndex(0)
		return this	
	}
	fun<T: Item> fill(ar:ArrayList<T>): MavTable {
		this.store=ar as ArrayList<out Item>
		IupSetStrAttribute(this.widget,"NUMLIN",ar.size.toString())
		for((i,el) in ar.withIndex())
			for (j in 1..cols)
				IupSetStrAttribute(this.widget,(i+1).toString()+":"+j.toString(),el[j-1].toString())
		return this
	}
	fun<T: Item>fill(ar:ArrayList<T>, flds:Array<Int>, fns:Array< out (T, Int)->Any>): MavTable {
		this.store=ar as ArrayList<out Item>
		this.flds=flds
		this.fns=fns as Array<out (Item, Int)->Any>
		IupSetStrAttribute(this.widget,"NUMLIN",ar.size.toString())
		for((i,el) in ar.withIndex())
			for (j in 1..cols) IupSetStrAttribute(this.widget,(i+1).toString()+":"+j.toString(),(fns[j-1](el,flds[j-1])).toString())
		return this
	}
	fun setHeaderText(n:Int,s:String){
		IupSetStrAttribute(this.widget,"0:"+(n+1).toString(),s)
		IupSetStrAttribute(this.widget,"REDRAW","L0")
	}

	fun onClick(handler:(Int) -> Unit): MavTable {
		onClick = handler
		return this
	}
	fun onDblClick(handler: (Int) -> Unit): MavTable {
		onDblClick = handler
		return this
	}
	fun onMenu(handler: MavTable.(Int) -> Unit): MavTable {
		onMenu = handler
		return this
	}
	private var onClick: ((Int) -> Unit)? = null

	private var onDblClick: ((Int) -> Unit)? = null
	internal var onMenu: (MavTable.(Int) -> Unit)? = null
    internal fun onMenu(l:Int) { setIndex(l); onMenu?.invoke(this,l)}
	internal var onValue: (MavTable.(Int, Int) -> String)? = null
    internal fun onValue(l:Int,c:Int) = onValue?.invoke(this,l,c) ?: ""		
}

fun mavGrid(labels:Array<String>):CPointer<Ihandle>?{
	val widget=IupMatrix(null)
	val lcol=labels.size.toString()
	IupSetAttributes(widget,"NUMLIN_VISIBLE=0,NUMCOL_VISIBLE=0,RESIZEMATRIX=YES,HEIGHT0=8")
	IupSetStrAttribute(widget,"NUMCOL",lcol)
	IupSetCallback(widget, "VALUE_EDIT_CB", staticCFunction(::grid_value_edit_cb))
	IupSetCallback( widget, "EDITION_CB", staticCFunction(::grid_edition_cb))
	for (n in 1..labels.size)
		IupSetStrAttribute(widget,"0:"+n.toString(),labels[n-1])
	return widget
}
open class MavGrid:MavWidget{
	val cols:Int
	constructor(name:String,root:MutableMap<String,CPointer<Ihandle>?>,_cols:Int):super(name,root){  cols=_cols;init()  }
	constructor(_widget:CPointer<Ihandle>,_cols:Int):super(_widget){ cols=_cols;init() }
	var store: ArrayList<out Item>? = null
	lateinit  var editables:ArrayList<Boolean>
	private var flds: Array<Int>? = null
	private var fns: Array<out (Item, Int) -> Any>? = null
	fun init() {
		grids[widget] = this
		editables=ArrayList()
		for (i in 0 until cols)  editables.add(false)
	}
	fun setEditables(ar:ArrayList<Boolean>): MavGrid {
		editables=ar
		return this
	}
	fun<T: Item> add(item:T): MavGrid {
		val l=IupGetAttribute(this.widget,"NUMLIN")!!.toKString().toInt()
		IupSetStrAttribute(this.widget,"ADDLIN",l.toString())
		for (j in 1..cols) {
			val value=if(fns==null) item[j-1] else  fns!![j-1](item,flds!![j-1])
			IupSetStrAttribute(this.widget,(l+1).toString()+":"+j.toString(),value.toString())
		}
		return this
	}
	fun<T: Item> change(item:T): MavGrid {
		val (l,c)=IupGetAttribute(this.widget,"FOCUSCELL")!!.toKString().split(":")
		for (j in 1..cols) {
			val value=if(fns==null) item[j-1] else  fns!![j-1](item,flds!![j-1])
			IupSetStrAttribute(this.widget,l.toString()+":"+j.toString(),value.toString())
		}
		IupSetStrAttribute(this.widget,"REDRAW","L"+l.toString())
		return this
	}
	fun<T: Item> delete(): MavGrid {
		val (l,c)=IupGetAttribute(this.widget,"FOCUSCELL")!!.toKString().split(":")
		IupSetStrAttribute(this.widget,"DELLIN",l.toString())
		return this
	}
	fun<T: Item> fill(ar:ArrayList<T>): MavGrid {
		this.store=ar
		IupSetStrAttribute(this.widget,"NUMLIN",ar.size.toString())
		for((i,el) in ar.withIndex())
			for (j in 1..cols)
				IupSetStrAttribute(this.widget,(i+1).toString()+":"+j.toString(),el[j-1].toString())
		return this
	}
	fun<T: Item>fill(ar:ArrayList<T>, flds:Array<Int>, fns:Array< out (T, Int)->Any>): MavGrid {
		this.store=ar
		this.flds=flds
		this.fns=fns as Array<out (Item, Int)->Any>
		IupSetStrAttribute(this.widget,"NUMLIN",ar.size.toString())
		for((i,el) in ar.withIndex())
			for (j in 1..cols) IupSetStrAttribute(this.widget,(i+1).toString()+":"+j.toString(),(fns[j-1](el,flds[j-1])).toString())
		return this
	}
	fun onEdit(handler: MavGrid.(Int, Int, String) -> Unit): MavGrid {
		onEdit = handler
		return this
	}
	internal var onEdit: (MavGrid.(Int, Int, String) -> Unit)? = null
	internal fun onEdit(l:Int,c:Int,s:String) = onEdit?.invoke(this,l,c,s)
}

open class MavTab: MavWidget {
	constructor(name:String,root:MutableMap<String,CPointer<Ihandle>?>):super(name,root)
	constructor(_widget:CPointer<Ihandle>):super(_widget)
	init {
		tabs[widget] = this
		IupSetCallback( widget, "TABCHANGEPOS_CB", staticCFunction(::tab_change_cb))
	}
	fun onChange(handler: MavTab.(Int, Int) -> Unit): MavTab {
		onChange = handler
		return this
	}
	internal var onChange: (MavTab.(Int, Int) -> Unit)? = null
	internal fun onChange(l:Int,c:Int) = onChange?.invoke(this,l,c)
}

open class MavTree(name:String, root:MutableMap<String,CPointer<Ihandle>?>,title:String): MavWidget(name,root) {
    var store: ArrayList<out Item>? = null
    init {
        IupSetStrAttribute(widget, "TITLE",title)
		trees[widget] = this
        IupSetCallback( this.widget, "SELECTION_CB", staticCFunction(::tree_selection_cb))
    }

    fun setTitle(title:String){
        IupSetStrAttribute(widget, "TITLE",title)
    }

    fun<T: Item> fill(ar:ArrayList<T>): MavTree where T:Treeable{
        this.store=ar
        ar.forEach {
                val strId=if(it.getLevel()==0) "" else it.getLevel().toString()
                IupSetAttribute(widget, "ADDBRANCH"+strId,it[1].toString())
            }
        return this
    }
    var count:String
        get() = IupGetAttribute(this.widget, "COUNT")?.toKString() ?: "err"
        set(value){}

    var text:String
        get() {
            val id=IupGetAttribute(this.widget, "VALUE")?.toKString() ?: "1"
            return IupGetAttribute(this.widget,"TITLE"+id)?.toKString() ?: ""
        }
        set(value){}

	fun delete(){
		val id=IupGetAttribute(this.widget, "VALUE")?.toKString() ?: "-1"
		IupSetAttribute(this.widget,"DELNODE"+id,"SELECTED")
	}
    fun clear(){
        IupSetAttribute(this.widget,"DELNODE0","CHILDREN")
    }

    fun onClick(handler: MavTree.(l:Int, c:String) -> Unit): MavTree {
        onClick = handler

        return this
    }
    internal var onClick: (MavTree.(l:Int, c:String) -> Unit)? = null
    internal fun onClick(l:Int,c:String) = onClick?.invoke(this,l,c)

}

open class MavFileDialog: MavWidget {
	constructor(name:String,root:MutableMap<String,CPointer<Ihandle>?>):super(name,root)
	constructor(_widget:CPointer<Ihandle>):super(_widget)
	var store: ArrayList<out Item>? = null

	fun open(filter:String=""):String{
		IupSetAttribute(widget, "DIALOGTYPE", "OPEN")
		if(filter.isNotEmpty()) IupSetAttributes(widget,filter)
		IupPopup(widget, IUP_CURRENT, IUP_CURRENT)
		return if (IupGetInt(widget, "STATUS") != -1)
			IupGetAttribute(widget, "VALUE")!!.toKString()
		else ""
	}
	fun save(filter:String=""):String{
		IupSetAttribute(widget, "DIALOGTYPE", "SAVE")
		if(filter.isNotEmpty()) IupSetAttributes(widget,filter)
		IupPopup(widget, IUP_CURRENT, IUP_CURRENT)
		return if (IupGetInt(widget, "STATUS") != -1)
			IupGetAttribute(widget, "VALUE")!!.toKString()
		else ""
	}
}