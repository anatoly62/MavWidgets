package mav.core

open class Item(open var tag:String=""){
    open operator fun get(n: Int): Any = 0;
    open operator fun set(n: Int, v: Any)  {}

    open var id:Int=0
}