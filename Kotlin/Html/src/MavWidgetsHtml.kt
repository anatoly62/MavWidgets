import org.w3c.dom.*
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent
import kotlin.browser.document
import kotlin.browser.window
const val library="HTML"

fun createGui(s:String,shell:Int=0)=mutableMapOf<String,dynamic>()
fun initGui()=0
fun loopGui(shell:Int){}
fun showError(s:String,root: MutableMap<String,dynamic>){ window.alert(s) }
fun showError(s:String,root: Int){ window.alert(s) }

fun getKeyName(ev: Event):String {
    ev as KeyboardEvent
    return when (ev.code) {
        "BackSpace"->"BACK_SPACE"
        "Enter"->"ENTER"
        "Escape"->"ESCAPE"
        "Space"->"SPACE"
        "Delete"->"DELETE"
        "ArrowUp"->"UP"
        "ArrowDown"->"DOWN"
        "ArrowLeft"->"LEFT"
        "ArrowRight"->"RIGHT"
        "PageUp"->"PAGE_UP"
        "PageDown"->"PAGE_DOWN"
        "Home"->"HOME"
        "End"->"END"
        "Insert"->"INSERT"
        else -> ev.keyCode.toChar().toString().toUpperCase()
    }
}


fun searchForCombo(cb:MavCombo,s:String,n:Int){
    val ss=s.split(" ")
    val len=cb.widget.options.length
    for (i in 0 until len) {
        val item=(cb.widget.options[i] as HTMLOptionElement).text.toLowerCase()
        val names=item.split(" ")
        if (names.size==n+1) {
            if (names[n].startsWith(s)){
                cb.widget.selectedIndex=i
            }
        }
        else if (ss.size>1){
            if (names[n].startsWith(ss[0]) && names[n+1].startsWith(ss[1])) {
                cb.widget.selectedIndex=i
            }
        }
        else if (s.length>0 && ss.size==1) {
            if (s.startsWith(" ") && names[n+1].startsWith(ss[0])) {
                cb.widget.selectedIndex=i
            }
            else if (!s.startsWith(" ") && names[n].startsWith(ss[0])){
                cb.widget.selectedIndex=i
            }
        }
    }

}
fun searchForTable(table:MavTable,s:String,n:Int) {
    val ss = s.split(" ")
    val len = table.store?.size ?: 0
    var row = 0
    for (i in 0 until len) {
        val item = table.store!![i]
        val _names = if (n == 1) item[1].toString() + " " + item[2].toString() else item[1].toString()
        val _names_ = _names.toLowerCase()
        val names = _names_.split(" ")
        if (names.size == 1) {
            if (names[0].startsWith(s)) row = i
        } else if (ss.size > 1) {
            if (names[0].startsWith(ss[0]) && names[1].startsWith(ss[1])) row = i
        } else if (s.length > 0 && ss.size == 1) {
            if (s.startsWith(" ") && names[1].startsWith(ss[0])) row = i
            else if (!s.startsWith(" ") && names[0].startsWith(ss[0])) row = i
        }
    }
    table.setIndex(row)
    table.scrollTo(row)
    table.clickFun(row)
}

open class MavWidget(val name:String, val root:MutableMap<String,dynamic>){
    open val widget= document.getElementById(name)  as HTMLElement
    var visible:Boolean
        get()=if (widget.style.display=="none") false else true
        set(flg) { widget.style.display=if(flg) "display" else "none"}
    var enable:Boolean
        get()=if (widget.style.display=="none") false else true
        set(flg) { widget.style.display=if(flg) "display" else "none"}
    fun setFocus():MavWidget{
        widget.focus()
        return this
    }
}

open class MavWindow(val name:String, val root:MutableMap<String,dynamic>){
     val wName=name
    val widget= if(name=="dlg") null else document.getElementById(name)  as HTMLDivElement
    init{
        root["window"]=document.getElementById(name)
        root["name"]=name
        root["table"]=null
    }
    fun show():MavWindow{
        widget?.style?.display="block"
        if(widget==null) return this
        widget.style.minWidth= if(widget.dataset["minwidth"]!=undefined)  widget.dataset["minwidth"]!!  else "60px"
        widget.style.minHeight=if(widget.dataset["minheight"]!=undefined) widget.dataset["minheight"]!! else "40px"
        js("window.addEventListener('mousedown', window.onMouseDown)")
        js("window.addEventListener('touchstart', window.onTouchDown)")
        js("window.animate()")
        return this
    }
    fun setText(s:String):MavWindow{
        if(widget==null)
            document.title=s
        else {
            val h = document.getElementById("#" + wName) as HTMLElement
            h.innerHTML=s
        }
        return this
    }
    fun close(){
        widget?.style?.display="none"
    }
}

open class MavMenu(name:String, root:MutableMap<String,dynamic>):MavWidget(name,root){
    override val widget= document.getElementById(name)  as HTMLDivElement
    fun show(fake:Any,e:MouseEvent):MavMenu{
        widget.style.left = e.clientX.toString() + "px"
        widget.style.top = e.clientY.toString() + "px"
        widget.style.display="block"
        return this
    }
    fun hide():MavMenu{
        widget.style.display="none"
        return this
    }
}

open class MavItem(name:String, root:MutableMap<String,dynamic>):MavWidget(name,root){
    override val widget= document.getElementById(name)  as HTMLAnchorElement//HTMLLinkElement
    var text:String
        get()=widget.innerHTML
        set(s){widget.innerHTML=s}
    fun onClick(handler: (Event)->dynamic):MavItem {
        widget.onclick = handler
        return this
    }
}

open class MavButton(name:String, root:MutableMap<String,dynamic>):MavWidget(name,root){
    override val widget= document.getElementById(name) as HTMLInputElement
    var text:String
        get()=widget.value
        set(s){widget.value=s}
    fun onClick(handler: (Event)->dynamic):MavButton {
        widget.onclick = handler
        return this
    }
}

open class MavFlatButton(name:String, root:MutableMap<String,dynamic>):MavWidget(name,root){
    override val widget= document.getElementById(name) as HTMLImageElement
    var text:String
        get()=widget.innerHTML
        set(s){widget.innerHTML=s}

    fun onClick(handler: (Event)->dynamic):MavFlatButton {
        widget.onclick = handler
        return this
    }
}

open class MavDate(name:String,root:MutableMap<String,dynamic>):MavWidget(name,root){
    override val widget= document.getElementById(name) as HTMLInputElement
    var text:String
        get()=widget.value
        set(s){widget.value=s}
    fun setText(s:String):MavDate{
        widget.value=s
        return this
    }
    fun setFirstDay():MavDate{
        this.setText(timeToDate(currentTimeMillis(),true))
        return this
    }
    fun setCurrentDay():MavDate{
        this.setText(timeToDate(currentTimeMillis(),false))
        return this
    }
}

open class MavLabel( name:String, root:MutableMap<String,dynamic>):MavWidget(name,root) {
     var text: String
        get() = widget.innerHTML
        set(s) {
            widget.innerHTML = s
        }
    fun setText(s: String): MavLabel {
        widget.innerHTML = s
        return this
    }
}

open class MavText( name:String, root:MutableMap<String,dynamic>):MavWidget(name,root){
    override val widget= document.getElementById(name) as HTMLInputElement
    var text:String
        get()=widget.value
        set(s){widget.value=s}
    fun setText(s:String):MavText{
        widget.value=s
        return this
    }
    fun selectAll():MavText{
        widget.select()
        return this
    }
    fun onChange(handler: (Event)->dynamic):MavText {
        widget.onkeyup = handler
        return this
    }
    fun onKey(handler: (Event)->dynamic):MavText {
        widget.onkeyup = handler
        return this
    }
}

open class MavCombo (name:String,  root:MutableMap<String,dynamic>):MavWidget(name,root){
    override val widget= document.getElementById(name) as HTMLSelectElement
    var text:String
        get()=(widget.options[widget.selectedIndex] as HTMLOptionElement).text
        set(s){}
    var index:Int
        get() =  widget.selectedIndex
        set(n){widget.selectedIndex=n}
    fun setIndex(n:Int):MavCombo{
        widget.selectedIndex=n
        return this
    }
    fun fill(lst:List<String>):MavCombo{
        var html:String=""
        for(el in lst )  html+="<option>"+el+"</option>"
        widget.innerHTML=html
        return this
    }
    fun fill(lst:List<String>,ar:Array<String>):MavCombo{
        var html:String=""
        for(el in ar )  html+="<option>"+el+"</option>"
        for(el in lst )  html+="<option>"+el+"</option>"
        widget.innerHTML=html
        return this
    }
    fun onChange(handler: (Event)->dynamic):MavCombo {
        widget.onchange = handler
        return this
    }
}

open class MavTable(name:String, root:MutableMap<String,dynamic>,val cols:Int):MavWidget(name,root){
    override val widget= document.getElementById(name) as HTMLTableSectionElement
    var store:ArrayList<out Item>?=null
    var flds:Array<Int>?=null
    var fns:Array<out (Item, Int)->Any>?=null
    var rowIndex:Int=-1
    var clickFun:(Int)->Unit= {}
    var dblClickFun:(Int)->Unit= {}
    var index:Int
        get()=rowIndex
        set(n){if (rowIndex >= 0)
            (widget.rows[rowIndex] as HTMLTableRowElement) .style.background = "#ffffff"
            rowIndex=n
            (widget.rows[n] as HTMLTableRowElement).style.background="#00bbee"
            clickFun(n)
        }
    init{
        val window=root["window"]
        if (window!=null) {
            val stWindow = document.getElementById(root["name"]) as HTMLDivElement
            window["table"] = document.getElementById(name)
            window["div"] = window["table"].parentNode.parentNode
            window["table"]["delta"]=stWindow.dataset["table"]
            window["div"]["delta"]=stWindow.dataset["div"]
        }

        widget.onclick={
            if (rowIndex >= 0)
                (widget.rows[rowIndex] as HTMLTableRowElement) .style.background = "#ffffff"
            val n=((it.target as HTMLTableCellElement).parentNode as HTMLTableRowElement).rowIndex-1
            rowIndex=n
            (widget.rows[n] as HTMLTableRowElement).style.background="#00bbee"
            clickFun(n)
        }
    }
    fun <T:Item>getSelectedRow():T? = this.store!![rowIndex-1] as T
    fun setIndex(n:Int):MavTable{
       if (rowIndex >= 0)
            (widget.rows[rowIndex] as HTMLTableRowElement) .style.background = "#ffffff"
        rowIndex=n
        if(n>=0) {
            (widget.rows[n] as HTMLTableRowElement).style.background = "#00bbee"
            clickFun(n)
        }
        return this
    }
    fun scrollTo(n:Int):MavTable{
        val row=widget.rows[rowIndex]
        row?.scrollIntoView()
        return this
    }
    fun<T:Item> add(item:T):MavTable{
        var r=""
        for(i in 0 until cols){
            val value=if(fns==null) item[i] else  fns!![i](item,flds!![i])
            r += "<td>" + value.toString() + "</td>"
        }
        widget.innerHTML+=r
        setIndex(store!!.size-1)
        scrollTo(store!!.size-1)
        return this
    }
    fun<T:Item> change(item:T):MavTable{
        for(i in 0 until cols) {
            val value = if (fns == null) item[i] else fns!![i](item, flds!![i])
            (widget.rows[rowIndex] as HTMLTableRowElement).cells[i]!!.innerHTML=value.toString()
        }
        return this
    }
    fun<T:Item> delete():MavTable{
        widget.deleteRow(rowIndex)
        if(store!!.size>0) {
            rowIndex-=1
            (widget.rows[rowIndex] as HTMLTableRowElement).style.background="#00bbee"
            clickFun(rowIndex)
            return this
        }
        else rowIndex= -1

        return this
    }
    fun onClick(fn:(Int)->Unit ):MavTable {
        this.clickFun =fn
        return this
    }
    fun onDblClick(fn:(Int)->Unit ):MavTable {
        this.dblClickFun=fn
        widget.ondblclick={
            val n=((it.target as HTMLTableCellElement).parentNode as HTMLTableRowElement).rowIndex-1
            fn(n)
        }
        return this
    }
    fun onMenu(fn:(MouseEvent)->Unit ):MavTable {
        widget.onmousedown=fun(e:Event):Boolean{
            e as MouseEvent
            if(e.button.compareTo(2)==0){
                val cell=e.target as HTMLTableCellElement
                val row= cell.parentElement as HTMLTableRowElement
                val rows=widget.rows
                for(i in 0 until rows.length)
                    if (row == rows[i]){
                        setIndex(i)
                        break
                    }
                fn(e)
                return false
            }
            else return true
        }
        return this
    }
    fun<T:Item> fill(ar:ArrayList<T>):MavTable{
        store=ar
        rowIndex=-1
        var html = ""
        for (i in 0 until ar.size) {
            var r = ""
            for (j in 0 until cols)  r += "<td>" + ar[i][j] + "</td>"
            html += "<tr>" + r + "</tr>"
        }
        widget.innerHTML = html
        return this
    }
    fun<T:Item>fill(ar:ArrayList<T>, flds:Array<Int>, fns:Array< out (T, Int)->Any>):MavTable{
        store=ar
        rowIndex=-1
        var html = ""
        this.fns=fns as Array<out (Item, Int)->Any>
        this.flds=flds
        for (i in 0 until ar.size) {
            var r = ""
            val row=ar[i]
            for (j in 0 until fns.size)  r += "<td>" + (fns[j])(row,flds[j]) + "</td>"
            html += "<tr>" + r + "</tr>"
        }
        widget.innerHTML = html
        return this
    }
}

open class MavGrid(name:String, root:MutableMap<String,dynamic>,val cols:Int):MavWidget(name,root){
    override val widget= document.getElementById(name) as HTMLTableSectionElement
    var store:ArrayList<out Item>?=null
    lateinit  var editables:ArrayList<Boolean>
    var flds:Array<Int>?=null
    var fns:Array<out (Item, Int)->Any>?=null
    var rowIndex:Int=-1
    var collIndex:Int=-1
    init{
        editables=ArrayList()
        for (i in 0 until cols)  editables.add(false)
        val window=root["window"]
        if (window!=null) {
            val stWindow = document.getElementById(root["name"]) as HTMLDivElement
            window["table"] = document.getElementById(name)
            window["div"] = window["table"].parentNode.parentNode
            window["table"]["delta"]=stWindow.dataset["table"]
            window["div"]["delta"]=stWindow.dataset["div"]
        }
        widget.onclick={
            val cell=it.target as HTMLTableCellElement
            collIndex=cell.cellIndex
            rowIndex=(cell.parentNode as HTMLTableRowElement).rowIndex-1
            0
        }
    }

    fun<T:Item> add(item:T):MavGrid{
        var r=""
        for(i in 0 until cols){
            val value=if(fns==null) item[i] else  fns!![i](item,flds!![i])
            r += "<td>" + value.toString() + "</td>"
        }
        widget.innerHTML+=r
        return this
    }
    fun<T:Item> change(item:T):MavGrid{
        for(i in 0 until cols) {
            val value = if (fns == null) item[i] else fns!![i](item, flds!![i])
            (widget.rows[rowIndex] as HTMLTableRowElement).cells[i]!!.innerHTML=value.toString()
        }
        return this
    }
    fun<T:Item> delete():MavGrid{
        widget.deleteRow(rowIndex)
        return this
    }
    fun<T:Item> fill(ar:ArrayList<T>):MavGrid{
        store=ar
        rowIndex=-1
        var html = ""
        for (i in 0 until ar.size) {
            var r = ""
            for (j in 0 until cols)  r += "<td>" + ar[i][j] + "</td>"
            html += "<tr>" + r + "</tr>"
        }
        widget.innerHTML = html
        return this
    }
    fun<T:Item>fill(ar:ArrayList<T>, flds:Array<Int>, fns:Array< out (T, Int)->Any>):MavGrid{
        store=ar
        rowIndex=-1
        var html = ""
        this.fns=fns as Array<out (Item, Int)->Any>
        this.flds=flds
        for (i in 0 until ar.size) {
            var r = ""
            val row=ar[i]
            for (j in 0 until fns.size)  r += "<td>" + (fns[j])(row,flds[j]) + "</td>"
            html += "<tr>" + r + "</tr>"
        }
        widget.innerHTML = html
        return this
    }
    fun setEditables(ar:ArrayList<Boolean>):MavGrid{
        editables=ar
        val r=widget.rows
        for(i in 0 until r.length){
            val c=(r[i] as HTMLTableRowElement).cells
            for (j in 0 until c.length)
                if (ar[j])  c[j]?.setAttribute("contenteditable","true")
        }
        return this
    }
    fun onEdit(fn:(Item, Int, String)->Unit):MavGrid{
        widget.onkeydown=fun(e:Event){
            e as KeyboardEvent
            if (e.keyCode==13 && editables[collIndex]){
                val cell=e.target as HTMLTableCellElement
                fn(store!![rowIndex] ,collIndex,cell.innerHTML)
                cell.blur()
                e.preventDefault()
            }

        }

        return this
    }
}

//fun getHandler( n:Int,fn:(Int)->Unit):(Event)->dynamic { return fun(e:Event){ fn(n) } }