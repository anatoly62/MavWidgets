package com.example.tol.mavdroid
import kotlin.math.*

open class Item(var id:Int=0) {
    companion object {const val id=0}
    open val fields = arrayOf(0)
    open val sqlFields = ArrayList<String>()
    open val sqlTable: String = ""
    open val slaveUpdate:String=""
    open val slaveDelete:String=""
    open val selectCond:String=""
    open val parentSelect:String=""
    open operator fun get(n: Int): Any = 0;
    open operator fun set(n: Int, v: Any)  {}


fun timeToDate(ts: Long,stDay:Boolean=false):String {
    val leapyear:(Int)->Boolean = { year ->   year % 4 == 0 && (year % 100 != 0 || year % 400 == 0) }

    val _YTAB= arrayOf(
            arrayOf( 31, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30,31 ),
            arrayOf( 31, 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30,31)
    )
    var year = 1970
    var dayno = ts / 86400000;
    while(true) {
        val yearsize = if (leapyear(year)) 366  else  365
        if (dayno >= yearsize) {
            dayno -= yearsize
            year += 1;
        }
        else {
            break;
        }
    }
    var r=year.toString()+"-"
    var mon = 1
    while (dayno >= _YTAB[if (leapyear(year))  1  else  0 ][mon]) {
        dayno -= _YTAB[if (leapyear(year))  1  else  0 ][mon]
        mon += 1
    }
    if (mon<10) r+="0"
    r+=mon.toString()
    dayno+=1
    if (stDay) r+="-01"
    else {
        if (dayno<10) r+="-0" else r+="-"
        r+=dayno.toString()
    }
    return r
}

