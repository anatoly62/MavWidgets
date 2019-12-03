package mav.core

import kotlinx.cinterop.*
import post.*
import platform.posix.*
const val platform="NATIVE"
fun await(fn:()->Unit)=fn()
fun send(tbl:String="no", sql:String):String{
	val sqlEnc=sql.replace("+","%2b")
	return post("tbl=$tbl&sql=$sqlEnc")!!.toKString()
}
fun currentTimeMillis(): Long = memScoped {
		val timeVal = alloc<timeval>()
		mingw_gettimeofday(timeVal.ptr, null) 
		val sec = timeVal.tv_sec
		val usec = timeVal.tv_usec
		(sec * 1_000L) + (usec / 1_000L)+7200000
}

