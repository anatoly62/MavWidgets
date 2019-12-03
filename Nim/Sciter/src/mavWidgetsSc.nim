import sciterApi, winim/inc/[windef,winuser],winim/winstr,strutils,tables,sugar,times

type
  GuiPtr* =HELEMENT
  MavWidget= ref object of RootObj
    widget*: HELEMENT 
    text:string 

  MavButton* = ref object of MavWidget
  MavCheckButton* = ref object of MavWidget
    check:bool
  MavFlatButton* = ref object of MavWidget
  MavItem* = ref object of MavWidget
  MavMenu* = ref object of MavWidget

  MavCombo* = ref object of MavWidget
    index:int

  MavLabel* = ref object of MavWidget
  MavText* = ref object of MavWidget
  MavDate* = ref object of MavWidget
  
  MavWindow* = ref object of RootObj
    widget*:HWND
 
  MavTable* = ref object of MavWidget
    index:int 

var exitCode:uint=0
var msg: MSG

proc myCallBack*(pns: LPSCITER_CALLBACK_NOTIFICATION; callbackParam: pointer): uint32 {.stdcall.}= 
  if  pns.code==5:
    exitCode= pns.code
  return 0

proc MessageBoxW*(hWnd: HWND, lpText: LPCWSTR, lpCaption: LPCWSTR, uType: UINT): int32 {.winapi, stdcall, dynlib: "user32", importc.}
proc OleInitialize*(p:LPVOID ):int32   {.winapi, stdcall, dynlib: "ole32", importc.}

proc Window*(wnd:HWND):MavWindow=
  result=MavWindow()
  result.widget=wnd

proc show*(w:MavWindow):MavWindow{.discardable.}=
  ShowWindow(w.widget,SW_SHOWNORMAL)
  result=w  

proc close*(w:MavWindow)=CloseWindow(w.widget)

proc setText*(w:MavWindow,title:string):MavWindow{.discardable}=
  SetWindowTextW(w.widget,newWideCString(title))
  result=w   

proc initGui*(fname:string,width,height:int32):(MavWindow,HELEMENT)=
  var he:HELEMENT
  OleInitialize(nil)
  SAPI()
  let sciterClass= api.SciterClassName()
  let wnd=CreateWindowEx(0, sciterClass, "Sciter", WS_OVERLAPPEDWINDOW, 100, 100, width,height, 0,0,0,nil)
  api.SciterSetCallback(wnd,myCallBack,nil)
  if api.SciterLoadFile(wnd,newWString (fname))==false: raise newException(SciterError, "SciterLoadFile error")
  if api.SciterGetRootElement(wnd,addr(he)) != 0: raise newException(SciterError, "SciterGetRootElement error")
  result=(Window(wnd),he)

proc loadGui*(fname:string,width,height:int32):(MavWindow,HELEMENT)=
  var he:HELEMENT
  let sciterClass= api.SciterClassName()
  let wnd=CreateWindowEx(0, sciterClass, "Sciter", WS_OVERLAPPEDWINDOW, 100, 100, width,height, 0,0,0,nil)
  if api.SciterLoadFile(wnd,newWString (fname))==false: raise newException(SciterError, "SciterLoadFile error")
  if api.SciterGetRootElement(wnd,addr(he)) != 0: raise newException(SciterError, "SciterGetRootElement error")
  result=(Window(wnd),he)

proc loopGui*()=
  while GetMessage(addr( msg), 0, 0, 0) != 0:
    if exitCode!=5:
      TranslateMessage(addr( msg))
      DispatchMessage(addr( msg))
    else:  break  

## -------------------------------Widget---------------------------------------- 

method show(w:MavWidget){.base.} =  w.widget.setStyle("display","block")
method hide(w:MavWidget){.base.} =  w.widget.setStyle("display","none")  
method enable(w:MavWidget){.base.} =  w.widget.setState(0, 0x00000080,false)  
method disable(w:MavWidget){.base.} =  w.widget.setState(0x00000080,0,false)  

## -------------------------------Button----------------------------------------
var buttonsStore=newTable[HELEMENT,proc ()]()
proc btEvent (tag: pointer; he: HELEMENT; evtg: uint32; prms: pointer): bool {.stdcall.}=
  if evtg==0x100:
    let data = cast[ptr BEHAVIOR_EVENT_PARAMS](prms)[]
    if data.reason==1 and data.cmd==0:
      if buttonsStore[he]!=nil:  buttonsStore[he]()
  result=false

proc Button*(root:HELEMENT,name:string):MavButton=
  result=MavButton()
  result.widget=selectElement(root,"#" & name)
  buttonsStore[result.widget]=nil
  result.widget.setEvent(btEvent)

proc onClick*(w:MavButton,fn:proc ()):MavButton{.discardable.} =
  buttonsStore[w.widget]=fn
  result=w    

## -------------------------------CheckButton-------------------------------------
var checkButtonsStore=newTable[HELEMENT,proc ()]()
proc checkBtEvent (tag: pointer; he: HELEMENT; evtg: uint32; prms: pointer): bool {.stdcall.}=
  if evtg==0x100:
    let data = cast[ptr BEHAVIOR_EVENT_PARAMS](prms)[]
    if data.reason==1 and data.cmd==0:
      if checkButtonsStore[he]!=nil:  checkButtonsStore[he]()
  result=false

proc CheckButton*(root:HELEMENT,name:string):MavCheckButton=
  result=MavCheckButton()
  result.widget=selectElement(root,"#" & name)
  checkButtonsStore[result.widget]=nil
  result.widget.setEvent(checkBtEvent)

proc check*(w:MavCheckButton):bool=
  let res=w.widget.getIntValue
  result = res != 0  

proc onClick*(w:MavCheckButton,fn:proc ()):MavCheckButton{.discardable.} =
  checkButtonsStore[w.widget]=fn
  result=w      

## -------------------------------FlatButton-------------------------------------
var flatButtonsStore=newTable[HELEMENT,proc ()]()
proc flatBtEvent (tag: pointer; he: HELEMENT; evtg: uint32; prms: pointer): bool {.stdcall.}=
  if evtg==0x100:
    let data = cast[ptr BEHAVIOR_EVENT_PARAMS](prms)[]
    if data.reason==1 and data.cmd==0:
      if flatButtonsStore[he]!=nil:  flatButtonsStore[he]()
  result=false

proc FlatButton*(root:HELEMENT,name:string):MavFlatButton=
  result=MavFlatButton()
  result.widget=selectElement(root,"#" & name)
  flatButtonsStore[result.widget]=nil
  result.widget.setEvent(flatBtEvent)

proc onClick*(w:MavFlatButton,fn:proc ()):MavFlatButton{.discardable.} =
  flatButtonsStore[w.widget]=fn
  result=w    

## -------------------------------Menu-----------------------------------------
proc Menu*(root:HELEMENT,name:string):MavMenu=
  result=MavMenu()
  result.widget=selectElement(root,"#" & name)

proc show*(w:MavMenu,parent:MavWidget):MavMenu{.discardable.}=
  w.widget.showPopUp(parent.widget,8)
  result=w

## -------------------------------MenuItem-------------------------------------
var menusStore=newTable[HELEMENT,proc ()]()
proc menuEvent (tag: pointer; he: HELEMENT; evtg: uint32; prms: pointer): bool {.stdcall.}=
  if evtg==0x100:
    let data = cast[ptr BEHAVIOR_EVENT_PARAMS](prms)[]
    if data.reason==0 and data.cmd==128:
      if menusStore[he]!=nil:  menusStore[he]()
  result=false

proc MenuItem*(root:HELEMENT,name:string):MavItem=
  result=MavItem()
  result.widget=selectElement(root,"#" & name)
  menusStore[result.widget]=nil
  result.widget.setEvent(menuEvent)

proc onClick*(w:MavItem,fn:proc ()):MavItem{.discardable.} =
  menusStore[w.widget]=fn
  result=w    

## -------------------------------Label----------------------------------------
proc Label*(root:HELEMENT,name:string):MavLabel=
  result=MavLabel()
  result.widget=selectElement(root,"#" & name)   

## -------------------------------Text----------------------------------------
var textsStore=newTable[HELEMENT,proc ()]()
var textsStoreKey=newTable[HELEMENT,proc (key:uint32)]()

proc textEvent(tag: pointer; text: HELEMENT; evtg: uint32; prms: pointer): bool {.stdcall.}=
  if evtg==handleKey:
    let data = cast[ptr KeyData](prms)[]
    if data.cmd == 1:
      if textsStore[text]!=nil:  textsStore[text]()
      if textsStoreKey[text]!=nil:  textsStoreKey[text](data.code)
  result=false

proc Text*(root:HELEMENT,name:string):MavText=
  result=MavText()
  result.widget=selectElement(root,"#" & name) 
  textsStore[result.widget]=nil
  textsStoreKey[result.widget]=nil
  result.widget.setEvent(textEvent)

proc Text*(widget:HELEMENT):MavText=
  result=MavText()
  result.widget=widget
  
proc text*(w: MavText|MavButton): string=getStrValue(w.widget)  

proc setText*(w:MavText|MavButton,value:string): MavText|MavButton{.discardable,cdecl.} =
    setStrValue(w.widget,value)
    result=w 
  
proc `text=`*(w: MavText|MavButton, value: string)=setText(w,value) 

proc setFocus*(w:MavText|MavDate|MavFlatButton):MavText|MavButton|MavFlatButton{.discardable,cdecl.} =
  result=w

proc selectAll*(w: MavText):MavText=
  w.widget.scriptMethod("doSelectAll")
  result=w

proc onChange*(w:MavText,fn:proc ()):MavText{.discardable.} =
  textsStore[w.widget]=fn
  result=w  

proc onKey*(w:MavText,fn:proc (code:uint32)):MavText{.discardable.} =
  textsStoreKey[w.widget]=fn
  result=w    

## -------------------------------Date----------------------------------------
var datesStore=newTable[HELEMENT,proc ()]()
proc dateEvent(tag: pointer; date: HELEMENT; evtg: uint32; prms: pointer): bool {.stdcall.}=
  if evtg==0x100:
    let data = cast[ptr BEHAVIOR_EVENT_PARAMS](prms)[]
    if data.reason==0 and data.cmd==5:
      echo "changed"
      if datesStore[date]!=nil:  datesStore[date]()
  result=false

proc DatePick*(root:HELEMENT,name:string):MavDate=
  result=MavDate()
  result.widget=selectElement(root,"#" & name) 
  datesStore[result.widget]=nil
  result.widget.setEvent(dateEvent)

proc text*(w: MavDate): string=
  let tm:int64=(getInt64Value(w.widget)-116444628000000000)  div 10000000
  let dt= $fromUnix(tm).utc
  result = dt[0..9] 

proc setText*(w:MavDate,value:string):MavDate{.discardable,cdecl.}=
  let tm:Time=parse(value,"yyyy-MM-dd").toTime
  let i64val:int64=116444628000000000 + (tm.toUnix * 10000000)+864000000000
  w.widget.setInt64Value(i64val,6)
  result=w 

proc `text=`*(w: MavDate, value: string)=setText(w,value)      

proc onChange*(w:MavDate,fn:proc ()):MavDate{.discardable.} =
  datesStore[w.widget]=fn
  result=w   

## -------------------------------Combo----------------------------------------
var combosStore=newTable[HELEMENT,proc ()]()
proc comboEvent (tag: pointer; he: HELEMENT; evtg: uint32; prms: pointer): bool {.stdcall.}=
  if evtg==0x100:
    let data = cast[ptr BEHAVIOR_EVENT_PARAMS](prms)[]
    if data.reason==0 and data.cmd==5:
      let idx=data.src.getIndex
      setAttribute(he, "INDEX", $idx)
      if combosStore[he]!=nil:  combosStore[he]()
  result=false  

proc Combo*(root:HELEMENT,name:string):MavCombo=
  result=MavCombo()
  result.widget=selectElement(root,"#" & name)
  setAttribute(result.widget,"INDEX","0")
  combosStore[result.widget]=nil
  result.widget.setEvent(comboEvent)

proc Combo*(widget:HELEMENT):MavCombo=
  result=MavCombo()
  result.widget=widget

proc onChange*(w:MavCombo,fn:proc ()):MavCombo{.discardable.} =
  combosStore[w.widget]=fn
  result=w   

proc setText*(w:MavCombo,value:string):MavCombo{.discardable.}=
  let op=getNthChild(w.widget,0)
  let cnt= op.getChildCount
  var idx=0
  var ch:HELEMENT
  for n in 0..<cnt:
    ch=op.getNthChild(n.int)
    let str=ch.getStrValue
    if str==value: 
      idx=n.int
      break
  w.widget.setValue(ch.getValue)
  w.widget.setAttribute("INDEX",$idx)
  result= w

proc `text=`*(w: MavCombo, value: string)=setText(w,value)

proc  index*(w: MavCombo): int= parseInt(getAttribute(w.widget,"INDEX"))

proc setIndex*(w:MavCombo,value:int):MavCombo{.discardable,cdecl.}=
  let op=getNthChild(w.widget,0)
  if op.getChildCount<=value.uint32:  raise newException(SciterError, "getChildCount out of index error")
  let row=getNthChild(op,value)
  let selVal=getValue(row)
  setValue(w.widget,selVal)
  w.widget.update(true)
  w.widget.setAttribute("INDEX",$value)
  result=w

proc `index=`*(w: MavCombo, value: int)=setIndex(w,value)  

proc fill*(w:MavCombo,lst:openArray[string]):MavCombo{.discardable.} =
  let op=getNthChild(w.widget,0)
  var s=""
  for el in lst: s &= "<option>" & el & "</option>"
  setHtml(op,s)
  setIndex(w,0)
  result=w

## -------------------------------Table----------------------------------------
var tablesStore=newTable[HELEMENT,proc (n:int)]()
var tablesStore2=newTable[HELEMENT,proc (n:int)]()
var tablesStoreMenu=newTable[HELEMENT,proc (n:int)]()

proc cursor(table:HELEMENT,row:HELEMENT):int32{.cdecl}=
  let idx=parseInt(table.getAttribute("INDEX"))
  if idx >= 0:
    table.getNthChild(idx).setStyle("background",  "#ffffff")
  let n = row.getIndex 
  table.setAttribute("INDEX",$n) 
  row.setStyle("background","#00bbee")
  result= n.int32

proc tableEvent (tag: pointer; table: HELEMENT; evtg: uint32; prms: pointer): bool {.stdcall.}=
  if evtg==handleMouse:
    let data = cast[ptr EventData](prms)[]
    if data.cmd == mouseDblClick:
      let n= parseInt(table.getAttribute("index"))
      if tablesStore2[table]!=nil:  tablesStore2[table](n)
    elif data.cmd == mouseUp:
      if data.state==mainMouseButton:
        let t=data.trg.getType
        let n =
          if   t=="tr": cursor(table,data.trg)
          elif t=="td":  cursor(table,data.trg.getParent)
          else:0    
        if tablesStore[table]!=nil:  tablesStore[table](n)
      elif  data.state==propMouseButton:
        let n=cursor(table,data.trg.getParent)
        if tablesStoreMenu[table]!=nil:  tablesStoreMenu[table](n)
  result=false

proc Table*(root:HELEMENT,name:string):MavTable=
  result=MavTable()
  result.widget=selectElement(root,"#" & name)
  setAttribute(result.widget,"INDEX","-1")
  tablesStore[result.widget]=nil
  tablesStore2[result.widget]=nil
  tablesStoreMenu[result.widget]=nil
  result.widget.setEvent(tableEvent)

proc  index*(w: MavTable): int= parseInt(getAttribute(w.widget,"INDEX"))

proc setIndex*(tbl:MavTable,n:int):MavTable{.discardable}=
  let ch=tbl.widget.getNthChild(n)
  discard cursor(tbl.widget,ch)
  result=tbl

proc `index=`*(w: MavTable, value: int)=setIndex(w,value)    

proc onClick*(w:MavTable,fn:proc (l:int)):MavTable=
  tablesStore[w.widget]=fn
  result=w

proc onDblClick*(w:MavTable,fn:proc (l:int)):MavTable=
  tablesStore2[w.widget]=fn
  result=w 

proc onMenu*(w:MavTable,fn:proc (l:int)):MavTable=
  tablesStoreMenu[w.widget]=fn
  result=w   

proc fill*[T](tbl:MavTable,store:seq[T], fns:openArray[(T)->string]):MavTable=
  var str=""
  for i,it in store:
    var s=""
    for j in 0..<fns.len:
      s &= "<td>" & fns[j](it) & "</td>"
    str &= "<tr>" & s & "</tr>"  
  setHtml(tbl.widget,str)
  result=tbl 

proc add*[T](tbl:MavTable,it:T,fns:openArray[(T)->string]):MavTable{.discardable,cdecl.}=
  let idx=tbl.widget.getChildCount
  let row=create("tr")
  row.insert(tbl.widget,idx)
  var s=""
  for j in 0..<fns.len:
    s &= "<td>" & fns[j](it) & "</td>"
  row.setHtml(s)
  discard tbl.widget.cursor(row)
  row.scroll
  result=tbl   

proc change*[T](tbl:MavTable,it:T,fns:openArray[(T)->string]):MavTable{.discardable,cdecl.}=
  var s=""
  for j in 0..<fns.len:
    s &= "<td>" & fns[j](it) & "</td>"
  let idx=parseInt(tbl.widget.getAttribute("INDEX"))
  let row=tbl.widget.getNthChild(idx)
  setHtml(row,s) 
  result=tbl

proc delete*(tbl:MavTable):MavTable{.discardable,cdecl.}=
  let idx=parseInt(tbl.widget.getAttribute("INDEX"))
  tbl.widget.getNthChild(idx).delete
  tbl.widget.setAttribute("INDEX","-1")
  tbl.setIndex(if idx>0: idx-1 else: 0) 
  result=tbl

proc setHeader*(tbl:MavTable,n:int,title:string):MavTable{.discardable,cdecl.}=
  let row=tbl.widget.getNthChild(0)
  let header=row.getNthChild(n)
  header.setHtml(title)
  result=tbl  