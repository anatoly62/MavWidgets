package com.example.tol.mavdroid

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*

const val lib="ANDROID"
fun createGui(name:Int,root:Activity):Activity{
    root.setContentView(name)
    return root
}
fun getKeyCode(key:Int):Int = key
fun runActivity(className:Class<*>, root:Activity){
    val intent = Intent(root, className)
    root.startActivity(intent)
}
fun runActivity(className:Class<*>, root:Activity, id:Int, name:Int, title:String){
    val intent = Intent(root, className)
    intent.putExtra("id",id)
    intent.putExtra("name",name)
    intent.putExtra("title",title)
    root.startActivity(intent)
}
fun runActivity(className:Class<*>, root:Activity,  name:Int, title:String){
    val intent = Intent(root, className)
    intent.putExtra("name",name)
    intent.putExtra("title",title)
    root.startActivity(intent)
}
fun showError(s:String,context:Activity)= Toast.makeText(context, s, Toast.LENGTH_LONG).show()
fun searchForCombo(cb:MavCombo,s:String,n:Int){
    val combo=cb.combo
    val ss=s.split(" ")
    for(i in 0 until combo.count){
        val names=(combo.getItemAtPosition(i) as String).toLowerCase().split(" ")
        if(names.size==n+1){
            if(names[n].startsWith(s)) cb.setIndex(i)
        }
        else if(ss.size>1){
            if( names[n].startsWith(ss[0]) && names[n+1].startsWith(ss[1])) cb.setIndex(i)
        }
        else if(s.length>0 && ss.size==1)  {
            if ( s[0] == ' ' && names[n+1].startsWith(ss[0])) cb.setIndex(i)
            else if (s[0] != ' ' && names[n].startsWith(ss[0])) cb.setIndex(i)
        }
    }
}
fun searchForTable(table:MavTable,s:String,n:Int){
    val ss=s.split(" ")
    val store = (table.table.adapter as ItemAdapter<Item>).store
    for(i in 0 until store.size){
        val names= (if(n==1) store[i][1].toString()+" "+store[i][2].toString() else store[i][1].toString()+"").toLowerCase().split(" ")
        if(names.size==1){
            if(names[0].startsWith(s)) table.setIndex(i).scrollTo(i)
        }
        else if(ss.size>1){
            if( names[0].startsWith(ss[0]) && names[1].startsWith(ss[1])) table.setIndex(i).scrollTo(i)
        }
        else if(s.length>0 && ss.size==1)  {
            if ( s[0] == ' ' && names[1].startsWith(ss[0])) table.setIndex(i).scrollTo(i)
            else if (s[0] != ' ' && names[0].startsWith(ss[0])) table.setIndex(i).scrollTo(i)
        }
    }
}

open class MavWindow(val name:Int,val root:Activity){
    fun show():MavWindow=this
    fun setText(s:String):MavWindow{
        root.title=s
        return this
    }
    fun close():MavWindow{
        root.finish()
        return this
    }
}

open class MavMenu{
    constructor(name:Int,menu:ContextMenu,root:Activity) { root.getMenuInflater().inflate(name, menu) }
    constructor(name:Int,menu:Menu,root:Activity) { root.getMenuInflater().inflate(name, menu) }
    fun show()=this
}

open class MavItem{
    var item:MenuItem?=null
    constructor(name:Int,root:ContextMenu){
        item=root.findItem(name)
    }
    constructor(name:Int,root:Menu){
        item=root.findItem(name)
    }
    fun setActive(flg:Boolean):MavItem{
        item?.setEnabled(flg)
        return this
    }
    fun onClick(fn:()->Unit):MavItem{
        item?.setOnMenuItemClickListener {  fn();true }
        return this
    }
}

open class MavCheck(val name:Int,val root:Activity){
    val check=root.findViewById<CheckBox>(name)
    fun isChecked():Boolean=check.isChecked()
}

open class MavButton(val name:Int,val root:Activity) {
    val button = root.findViewById<Button>(name)
    fun setFocus():MavButton{
        button.requestFocus()
        return this
    }
    fun getText()=button.text
    fun setText(s:String):MavButton{
        button.text=s
        return this
    }
    fun onClick(fn:()->Unit):MavButton{
        button.setOnClickListener { fn() }
        return this
    }
}

open class MavFlatButton(val name:Int,val root:Activity) {
    val button = root.findViewById<Button>(name)
    fun setFocus():MavFlatButton{
        button.requestFocus()
        return this
    }
    fun setText(s:String):MavFlatButton{
        button.text=s
        return this
    }
    fun getText()=button.text
    fun onClick(fn:()->Unit):MavFlatButton{
        button.setOnClickListener { fn() }
        return this
    }
}

open class MavText(val name:Int,val root:Activity){
    val text=root.findViewById<EditText>(name)
    fun setFocus():MavText{
        text.requestFocus()
        return this
    }
    fun selectAll():MavText=this
    fun getText():String=text.text.toString()
    fun setText(s:String):MavText{
        text.setText(s)
        return this
    }
    fun onChange(fn:()->Unit):MavText{
        text.addTextChangedListener( object :TextWatcher{
            override fun afterTextChanged(s: Editable?) { fn() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        return this
    }
    fun onKey(fn:(Int)->Unit):MavText{
        text.addTextChangedListener( object :TextWatcher{
            override fun afterTextChanged(s: Editable?) { fn(0) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        text.setOnKeyListener { v, keyCode, event ->
            fn(keyCode)
            false
            }
        return this
    }
}

open class MavDate(val name:Int,val root:Activity){
    val date=root.findViewById<EditText>(name)
    fun setFocus():MavDate{
        date.requestFocus()
        return this
    }
    fun getText():String=date.text.toString()
    fun setText(s:String):MavDate{
        date.setText(s)
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
    fun onChange(fn:()->Unit):MavDate{
        date.addTextChangedListener( object :TextWatcher{
            override fun afterTextChanged(s: Editable?) { fn() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        return this
    }
}

open class MavCombo(val name:Int,val root:Activity){
    val combo=root.findViewById<Spinner>(name)
    fun setFocus():MavCombo=this
    fun getIndex():Int =combo.getSelectedItemPosition()
    fun setIndex(n:Int):MavCombo{
        combo.setSelection(n)
        return this
    }
    fun getText()=combo.selectedItem.toString()
    fun setText(s:String):MavCombo{
        val adapter=combo.adapter
        for (i in 0 until adapter.count)
        if (s == adapter.getItem(i) as String) combo.setSelection(i)
        return this
    }
    fun onChange(fn:()->Unit):MavCombo{
        combo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) { fn() }
            override fun onNothingSelected(parent: AdapterView<out Adapter>?) { }
        }
        return this
    }
    fun fill(lst: List<String>):MavCombo {
        val cbAdapter: ArrayAdapter<String> = ArrayAdapter<String>(root, R.layout.sp_item, lst)
        cbAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        combo.setAdapter(cbAdapter)
        combo.setSelection(0)
        return this
    }
    fun fill(lst: List<String>, ar:Array<String>):MavCombo {
        var myArr=arrayListOf<String>()
        for (el in ar) myArr.add(el)
        for (el in lst) myArr.add(el)
        val cbAdapter: ArrayAdapter<String> = ArrayAdapter<String>(root, R.layout.sp_item, myArr)
        cbAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        combo.setAdapter(cbAdapter)
        combo.setSelection(0)
        return this
    }
}

class ItemAdapter <T:Item>(context :Context,ar:ArrayList<T>,val fld:Array<Int>,val fns:Array<(T,Int)->Any>,val itT:Int) : BaseAdapter() {
    var store = ar
    val lInflater: LayoutInflater=context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    var selectedIndex:Int= -1
    var selectedView:View?=null
    var fn: (Int)->Unit=fun(a:Int){}
    override fun getCount() :Int=store.size
    override fun getItem(i: Int): Any? = if(i>=0) store[i] else null
    override fun getItemId( pos:Int)=pos.toLong()
    override  fun getView(pos:Int, convertView: View?, parent: ViewGroup):View{
        val view=if(convertView==null) lInflater.inflate(itT, parent, false)
        else convertView
        if(pos==selectedIndex){
            view.setBackgroundColor(Color.LTGRAY)
            selectedView=view
        }
        else
            view.setBackgroundColor(Color.WHITE)
        val item=getItem(pos) as T
        val layout=view as LinearLayout
        for(i in 0 until layout.getChildCount()) {
            val v=if(fns.isEmpty()) item[i].toString() else fns[i](item,fld[i]).toString()
                (layout.getChildAt(i) as TextView) .setText(v)
        }
        return view
    }
}

open class MavTable(val name:Int,val root:Activity,val itT:Int){
    var lastClickTime:Long = 0;
    var clickTime:Long=System.currentTimeMillis()
    val table=root.findViewById<ListView>(name)
    var singleClick=true
    fun getIndex():Int=(table.adapter as ItemAdapter<Item>).selectedIndex
    fun<T:Item> getSelectedRow():T?{
        val adapter = table.adapter as ItemAdapter<T>
        if(adapter.selectedIndex>=0) return adapter.getItem(adapter.selectedIndex) as T
        else return null
    }
    fun setIndex(n:Int):MavTable{
        if(n>=0){
            val adapter=table.adapter as ItemAdapter<Item>
            adapter.notifyDataSetChanged()
            adapter.selectedIndex=n
            if((adapter.fn!=null) && (singleClick)) adapter.fn(n)
        }
        return this
    }
    fun scrollTo(n:Int):MavTable{
        if(n>=0)  table.setSelection(n)
        return this
    }
    fun <T:Item>add(it:T):MavTable{
        val adapter=table.adapter as ItemAdapter<T>
        adapter.notifyDataSetChanged()
        adapter.selectedIndex=adapter.getCount()-1
        table.setSelection(adapter.selectedIndex)
        if((adapter.fn!=null) && (singleClick)) adapter.fn(adapter.selectedIndex)
        return this
    }
    fun <T:Item>change(it:T):MavTable{
        (table.adapter as ItemAdapter<T>).notifyDataSetChanged()
        return this
    }
    fun <T:Item>delete():MavTable{
        val adapter=table.adapter as ItemAdapter<T>
        adapter.notifyDataSetChanged()
        if(adapter.getCount()>0) {
            if (adapter.selectedIndex != 0) adapter.selectedIndex -= 1
            if((adapter.fn!=null) && (singleClick)) adapter.fn(adapter.selectedIndex)
        }
        else
            adapter.selectedIndex= -1
        return this
    }
    fun <T:Item>fill(ar: ArrayList<T> ):MavTable {
        table.setAdapter(ItemAdapter <T>(root, ar, arrayOf(), arrayOf(), itT))
        return this
    }
    fun <T:Item>fill(ar: ArrayList<T>,fld:Array<Int>,fn:Array<(T,Int)->Any>):MavTable{
        table.setAdapter(ItemAdapter<T>(root,ar,fld,fn,itT))
        return this
    }
    fun onClick(fn: (Int)->Unit):MavTable{
        (table.adapter as ItemAdapter<Item>).fn=fn
        table.setOnItemClickListener {
            parent, view, pos, id ->
                val adapter = parent.adapter as ItemAdapter<Item>
                adapter.selectedIndex = pos
                if (adapter.selectedView != null) adapter.selectedView?.setBackgroundColor(Color.WHITE)
                adapter.selectedView=view
                view.setBackgroundColor(Color.LTGRAY)
                if (singleClick) fn(pos)
                else {
                    clickTime = System.currentTimeMillis()
                    if (clickTime - lastClickTime < 300) fn(pos)
                    lastClickTime = clickTime
                }
            }
        return this
    }
    fun onDblClick(fn: (Int)->Unit):MavTable{
        singleClick=false
        return onClick(fn)
    }
    fun onMenu(fn:()->Unit):MavTable{
        root.registerForContextMenu(root.findViewById(name) as ListView)
        return this
    }
}