package mav.core

import kotlin.math.roundToInt

fun round2(v:Double):Double{
    val num=(v*100).toDouble()
    val r=num.roundToInt().toDouble()
    return (r/100)
}

fun round2Str(v:Double):String{
    val num=(v*100).toDouble()
    val r=num.roundToInt().toDouble()
    return (r/100).toString()
}

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

fun eval(expr:String):Double {
    var xxx:String=""
    for (i  in 0 until expr.length)
        if (expr[i] != ' ') xxx += expr[i]
    var tok = ""
    var i=0
    while (i<xxx.length){
        if (xxx[i] == '('){
            var iter = 1
            var token=""
            i+=1
            while (true){
                if (xxx[i] == '('){
                    iter=iter+1
                }
                else if (xxx[i] == ')'){
                    iter=iter-1
                    if (iter == 0){
                        i+=1
                        break
                    }
                }
                token += xxx[i]
                i+=1
            }
            tok += eval(token).toString()
        }
        tok += xxx[i]
        i+=1
    }
    for (i in 0 until tok.length) {
        if (tok[i] == '+'){
            return eval(tok.substring(0, i)) + eval(tok.substring(i + 1, tok.length))
        }
        else if (tok[i] == '-'){
            return eval(tok.substring(0, i)) - eval(tok.substring(i + 1, tok.length));
        }
    }
    for (i in 0 until tok.length){
        if (tok[i] == '*'){
            return eval(tok.substring(0, i)) * eval(tok.substring(i + 1, tok.length));
        }
        else if (tok[i] == '/') {
            return eval(tok.substring(0, i)) / eval(tok.substring(i + 1, tok.length));
        }
    }
    return tok.toDouble()
}

fun <T>ArrayList<T>.indexOf(predicate:(T)->Boolean):Int{
    for (i in 0 until size)
        if(predicate(this[i])) return i
    return -1
}

fun <T : Item> ArrayList<T>.numById(id: Int): Int {
    for (i in 0 until this.size)
        if (this[i].id == id) return i
    return -1
}
fun <T : Item> ArrayList<T>.idByNum(n:Int): Int? = if(n<this.size) this[n].id  else null

