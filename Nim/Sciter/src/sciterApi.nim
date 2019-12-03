import winim/inc/[windef,winuser]
import winim/winstr
import dynlib,os
import xtiscript
import xgraphics

const SCITER_DLL_NAME="sciter00.dll"
const handleMouse* = 0x0001
const handleKey* = 0x0002
const mouseUp* = 3
const mouseDblClick* = 5
const mainMouseButton* = 1
const propMouseButton* = 2

when defined(osx):
  type
    SciterWindowDelegate* = pointer
elif defined(windows):
  type
    SciterWindowDelegate* = proc (hwnd: HWND; msg: uint32; wParam: WPARAM; lParam: LPARAM; pParam: pointer; handled: ptr bool): LRESULT {.cdecl.}
elif defined(posix):
  type
    SciterWindowDelegate* = pointer

type
  HWINDOW=HWND
  SciterError* = object of Exception
  SCITER_CALLBACK_NOTIFICATION* = object
    code*: uint32             
    hwnd*: HWND           
  LPSCITER_CALLBACK_NOTIFICATION* = ptr SCITER_CALLBACK_NOTIFICATION
  SciterHostCallback* = proc (pns: LPSCITER_CALLBACK_NOTIFICATION; callbackParam: pointer): uint32 {.stdcall.}
  SciterElementCallback* = proc (he: HELEMENT; param: pointer): bool  {.stdcall.}
  LPSciterHostCallback* = ptr SciterHostCallback
  KeyValueCallback* = proc (param: pointer; pkey: ptr VALUE; pval: ptr VALUE): bool {.cdecl.}
  ElementEventProc* = proc (tag: pointer; he: HELEMENT; evtg: uint32; prms: pointer): bool {.stdcall.}
  LPCWSTR_RECEIVER* = proc (str: LPCWSTR; str_length: cuint; param: pointer) {.cdecl.}
  LPCSTR_RECEIVER* = proc (str: cstring; str_length: cuint; param: pointer) {.cdecl.}
  LPCBYTE_RECEIVER* = proc (str: ptr byte; num_bytes: cuint; param: pointer) {.cdecl.}
  DEBUG_OUTPUT_PROC* = proc (param: pointer; subsystem: uint32; severity: uint32; text: WideCString; text_length: uint32) {.cdecl.}
  HELEMENT* = pointer
  HNODE* = pointer
  HSARCHIVE* = pointer
  ELEMENT_COMPARATOR* = proc (he1: HELEMENT; he2: HELEMENT; param: pointer): int32 {. stdcall.}

 
  VALUE* = object
    t*: uint32
    u*: uint32
    d*: uint64
  
  NATIVE_FUNCTOR_INVOKE* = proc (tag: pointer; argc: uint32; argv: ptr VALUE; retval: ptr VALUE) {.cdecl.}
  NATIVE_FUNCTOR_RELEASE* = proc (tag: pointer) {.cdecl.}

  EventData* = object
    cmd*:uint32
    trg*:HELEMENT
    src*:HELEMENT
    reason*:uint32
    p1:uint32
    p2:uint32
    state*:uint16

  BEHAVIOR_EVENT_PARAMS* = object
    cmd*: uint32               ## # BEHAVIOR_EVENTS
    trg*: HELEMENT ## # target element handler, in MENU_ITEM_CLICK this is owner element that caused this menu - e.g. context menu owner
    src*: HELEMENT              ## # source element e.g. in SELECTION_CHANGED it is new selected <option>, in MENU_ITEM_CLICK it is menu item (LI) element
    reason*: uint32 ## # EVENT_REASON or EDIT_CHANGED_REASON - UI action causing change. In case of custom event notifications this may be anyapplication specific value
    data*: Value               ## # auxiliary data accompanied with the event. E.g. FORM_SUBMIT event is using this field to pass collection of values.
  
  KeyData* = object
    cmd*:uint32
    p1*:uint32
    code*:uint32
    reason*:uint32
    
  METHOD_PARAMS* = object
    methodID*: uint32
  REQUEST_PARAM* = object
    name*: WideCString
    value*: WideCString
   
  ISciterAPI* = object
    version*: uint32           ## # is zero for now
    SciterClassName*: proc (): LPCWSTR {.cdecl.}
    SciterVersion*: proc (major: bool): uint32 {.cdecl.}
    SciterDataReady*: proc (hwnd: HWND; uri: LPCWSTR; data: pointer;  dataLength: uint32): bool {.cdecl.}
    SciterDataReadyAsync*: proc (hwnd: HWND; uri: LPCWSTR; data: pointer; dataLength: uint32; requestId: pointer): bool {.cdecl.}
    SciterProc*: proc (hwnd: HWND; msg: uint32; wParam: WPARAM; lParam: LPARAM): LRESULT  {.stdcall.}
    SciterProcND*: proc (hwnd: HWND; msg: uint32; wParam: WPARAM; lParam: LPARAM; pbHandled: ptr bool): LRESULT  {.stdcall.}
    SciterLoadFile*: proc (hWndSciter: HWND; filename: WideCString): bool {.cdecl.}
    SciterLoadHtml*: proc (hWndSciter: HWND; html: pointer; htmlSize: uint32; baseUrl: LPCWSTR): bool {.cdecl.}
    SciterSetCallback*: proc (hWndSciter: HWND; cb: SciterHostCallback; cbParam: pointer) {.cdecl.}
    SciterSetMasterCSS*: proc (utf8: pointer; numBytes: uint32): bool {.cdecl.}
    SciterAppendMasterCSS*: proc (utf8: pointer; numBytes: uint32): bool {.cdecl.}
    SciterSetCSS*: proc (hWndSciter: HWND; utf8: pointer; numBytes: uint32; baseUrl: WideCString; mediaType: WideCString): bool {.cdecl.}
    SciterSetMediaType*: proc (hWndSciter: HWND; mediaType: WideCString): bool {.cdecl.}
    SciterSetMediaVars*: proc (hWndSciter: HWND; mediaVars: ptr Value): bool {.cdecl.}
    SciterGetMinWidth*: proc (hWndSciter: HWND): uint32 {.cdecl.}
    SciterGetMinHeight*: proc (hWndSciter: HWND; width: uint32): uint32 {.cdecl.}
    SciterCall*: proc (hWnd: HWND; functionName: cstring; argc: uint32;argv: ptr Value; retval: ptr Value): bool {.cdecl.}
    SciterEval*: proc (hwnd: HWND; script: WideCString; scriptLength: uint32; pretval: ptr Value): bool {.cdecl.}
    SciterUpdateWindow*: proc (hwnd: HWND) {.cdecl.}
    SciterTranslateMessage*: proc (lpMsg: ptr MSG): bool  {.stdcall.}
    SciterSetOption*: proc (hWnd: HWND; option: uint32; value: uint32): bool {.cdecl.}
    SciterGetPPI*: proc (hWndSciter: HWND; px: ptr uint32; py: ptr uint32) {.cdecl.}
    SciterGetViewExpando*: proc (hwnd: HWND; pval: ptr VALUE): bool {.cdecl.}
    SciterRenderD2D*: proc (hWndSciter:HWND, tgt:pointer): bool {.stdcall.}
    SciterD2DFactory*: proc (ppf: pointer): bool {.stdcall.}
    SciterDWFactory*: proc (ppf: pointer): bool {.stdcall.}
    SciterGraphicsCaps*: proc (pcaps: ptr uint32): bool {.cdecl.}
    SciterSetHomeURL*: proc (hWndSciter: HWND; baseUrl: WideCString): bool {.cdecl.}
    SciterCreateWindow*: proc (creationFlags: uint32; frame: ptr Rect; delegate: SciterWindowDelegate; delegateParam: pointer; parent: HWND): HWND {. cdecl.}
    SciterSetupDebugOutput*: proc (hwndOrNull: HWND; param: pointer; pfOutput: DEBUG_OUTPUT_PROC) {.cdecl.}  
    Sciter_UseElement*: proc (he: HELEMENT): int32 {.cdecl.}
    Sciter_UnuseElement*: proc (he: HELEMENT): int32 {.cdecl.}
    SciterGetRootElement*: proc (hwnd: HWND; phe: ptr HELEMENT): int32 {.cdecl.}
    SciterGetFocusElement*: proc (hwnd: HWND; phe: ptr HELEMENT): int32 {.cdecl.}
    SciterFindElement*: proc (hwnd: HWND; pt: Point; phe: ptr HELEMENT): int32 {.cdecl.}
    SciterGetChildrenCount*: proc (he: HELEMENT; count: ptr uint32): int32 {.cdecl.}
    SciterGetNthChild*: proc (he: HELEMENT; n: uint32; phe: ptr HELEMENT): int32 {.cdecl.}
    SciterGetParentElement*: proc (he: HELEMENT; p_parent_he: ptr HELEMENT): int32 {.cdecl.}
    SciterGetElementHtmlCB*: proc (he: HELEMENT; outer: bool;  rcv: LPCBYTE_RECEIVER; rcv_param: pointer): int32 {.cdecl.}
    SciterGetElementTextCB*: proc (he: HELEMENT; rcv: LPCWSTR_RECEIVER; rcv_param: pointer): int32 {.cdecl.}
    SciterSetElementText*: proc (he: HELEMENT; utf16: WideCString; length: uint32): int32 {. cdecl.}
    SciterGetAttributeCount*: proc (he: HELEMENT; p_count: ptr uint32): int32 {.cdecl.}
    SciterGetNthAttributeNameCB*: proc (he: HELEMENT; n: uint32; rcv: LPCSTR_RECEIVER; rcv_param: pointer): int32 {.cdecl.}
    SciterGetNthAttributeValueCB*: proc (he: HELEMENT; n: uint32; rcv: LPCWSTR_RECEIVER; rcv_param: pointer): int32 {.cdecl.}
    SciterGetAttributeByNameCB*: proc (he: HELEMENT; name: cstring; rcv: LPCWSTR_RECEIVER; rcv_param: pointer): int32 {.cdecl.}
    SciterSetAttributeByName*: proc (he: HELEMENT; name: cstring; value: WideCString): int32 {.cdecl.}
    SciterClearAttributes*: proc (he: HELEMENT): int32 {.cdecl.}
    SciterGetElementIndex*: proc (he: HELEMENT; p_index: ptr uint32): int32 {.cdecl.}
    SciterGetElementType*: proc (he: HELEMENT; p_type: ptr cstring): int32 {.cdecl.}
    SciterGetElementTypeCB*: proc (he: HELEMENT; rcv: LPCSTR_RECEIVER; rcv_param: pointer): int32 {.cdecl.}
    SciterGetStyleAttributeCB*: proc (he: HELEMENT; name: cstring; rcv: LPCWSTR_RECEIVER; rcv_param: pointer): int32 {.cdecl.}
    SciterSetStyleAttribute*: proc (he: HELEMENT; name: cstring; value: WideCString): int32 {.cdecl.}
    SciterGetElementLocation*: proc (he: HELEMENT; p_location: ptr Rect; areas: uint32): int32 {.cdecl.}       
    SciterScrollToView*: proc (he: HELEMENT; SciterScrollFlags: uint32): int32 {.cdecl.}
    SciterUpdateElement*: proc (he: HELEMENT; andForceRender: bool): int32 {.cdecl.}
    SciterRefreshElementArea*: proc (he: HELEMENT; rc: Rect): int32 {.cdecl.}
    SciterSetCapture*: proc (he: HELEMENT): int32 {.cdecl.}
    SciterReleaseCapture*: proc (he: HELEMENT): int32 {.cdecl.}
    SciterGetElementHwnd*: proc (he: HELEMENT; p_hwnd: ptr HWND; rootWindow: bool): int32 {.cdecl.}
    SciterCombineURL*: proc (he: HELEMENT; szUrlBuffer: WideCString; UrlBufferSize: uint32): int32 {.cdecl.}
    SciterSelectElements*: proc (he: HELEMENT; CSS_selectors: cstring; callback: SciterElementCallback; param: pointer): int32 {.cdecl.}
    SciterSelectElementsW*: proc (he: HELEMENT; CSS_selectors: WideCString; callback: SciterElementCallback; param: pointer): int32 {.cdecl.}
    SciterSelectParent*: proc (he: HELEMENT; selector: cstring; depth: uint32; heFound: ptr HELEMENT): int32 {.cdecl.}
    SciterSelectParentW*: proc (he: HELEMENT; selector: WideCString; depth: uint32; heFound: ptr HELEMENT): int32 {.cdecl.}
    SciterSetElementHtml*: proc (he: HELEMENT; html: ptr byte; htmlLength: uint32; where: uint32): int32 {.cdecl.}
    SciterGetElementUID*: proc (he: HELEMENT; puid: ptr uint32): int32 {.cdecl.}
    SciterGetElementByUID*: proc (hwnd: HWND; uid: uint32; phe: ptr HELEMENT): int32 {.cdecl.}
    SciterShowPopup*: proc (hePopup: HELEMENT; heAnchor: HELEMENT; placement: uint32): int32 {. cdecl.}
    SciterShowPopupAt*: proc (hePopup: HELEMENT; pos: Point; animate: bool): int32 {.cdecl.}
    SciterHidePopup*: proc (he: HELEMENT): int32 {.cdecl.}
    SciterGetElementState*: proc (he: HELEMENT; pstateBits: ptr uint32): int32 {.cdecl.}
    SciterSetElementState*: proc (he: HELEMENT; stateBitsToSet: uint32; stateBitsToClear: uint32; updateView: bool): int32 {.cdecl.}
    SciterCreateElement*: proc (tagname: cstring; textOrNull: WideCString; phe: ptr HELEMENT): int32 {.cdecl.} ## #out
    SciterCloneElement*: proc (he: HELEMENT; phe: ptr HELEMENT): int32 {.cdecl.} ## #out
    SciterInsertElement*: proc (he: HELEMENT; hparent: HELEMENT; index: uint32): int32 {.cdecl.}
    SciterDetachElement*: proc (he: HELEMENT): int32 {.cdecl.}
    SciterDeleteElement*: proc (he: HELEMENT): int32 {.cdecl.}
    SciterSetTimer*: proc (he: HELEMENT; milliseconds: uint32; timer_id: uint32): int32 {.cdecl.}
    SciterDetachEventHandler*: proc (he: HELEMENT; pep: ElementEventProc; tag: pointer): int32 {.cdecl.}
    SciterAttachEventHandler*: proc (he: HELEMENT; pep: ElementEventProc; tag: pointer): int32 {.cdecl.}
    SciterWindowAttachEventHandler*: proc (hwndLayout: HWND; pep: ElementEventProc; tag: pointer; subscription: uint32): int32 {.cdecl.}
    SciterWindowDetachEventHandler*: proc (hwndLayout: HWND; pep: ElementEventProc; tag: pointer): int32 {.cdecl.}
    SciterSendEvent*: proc (he: HELEMENT; appEventCode: uint32; heSource: HELEMENT; reason: uint32; handled: ptr bool): int32 {.cdecl.} ## #out
    SciterPostEvent*: proc (he: HELEMENT; appEventCode: uint32; heSource: HELEMENT; reason: uint32): int32 {.cdecl.}
    SciterCallBehaviorMethod*: proc (he: HELEMENT; params: ptr METHOD_PARAMS): int32 {.cdecl.}
    SciterRequestElementData*: proc (he: HELEMENT; url: WideCString; dataType: uint32; initiator: HELEMENT): int32 {.cdecl.}
    SciterHttpRequest*: proc (he:HELEMENT;url:WideCString;dataType:uint32;requestType:uint32;requestParams:ptr REQUEST_PARAM;nParams:uint32):int32 {.cdecl.}               ## # element to deliver data
    SciterGetScrollInfo*: proc (he: HELEMENT; scrollPos: ptr Point; viewRect: ptr Rect; contentSize: ptr Size): int32 {.cdecl.}
    SciterSetScrollPos*: proc (he: HELEMENT; scrollPos: Point; smooth: bool): int32 {.cdecl.}
    SciterGetElementIntrinsicWidths*: proc (he: HELEMENT; pMinWidth: ptr int32;pMaxWidth: ptr int32): int32 {.cdecl.}
    SciterGetElementIntrinsicHeight*: proc (he: HELEMENT; forWidth: int32; pHeight: ptr int32): int32 {.cdecl.}
    SciterIsElementVisible*: proc (he: HELEMENT; pVisible: ptr bool): int32 {.cdecl.}
    SciterIsElementEnabled*: proc (he: HELEMENT; pEnabled: ptr bool): int32 {.cdecl.}
    SciterSortElements*: proc (he: HELEMENT; firstIndex: uint32; lastIndex: uint32;cmpFunc: ptr ELEMENT_COMPARATOR; cmpFuncParam: pointer): int32 {.cdecl.}
    SciterSwapElements*: proc (he1: HELEMENT; he2: HELEMENT): int32 {.cdecl.}
    SciterTraverseUIEvent*: proc (evt: uint32; eventCtlStruct: pointer; bOutProcessed: ptr bool): int32 {.cdecl.}
    SciterCallScriptingMethod*: proc (he: HELEMENT; name: cstring; argv: ptr VALUE; argc: uint32; retval: ptr VALUE): int32 {.cdecl.}
    SciterCallScriptingFunction*: proc (he: HELEMENT; name: cstring; argv: ptr VALUE; argc: uint32; retval: ptr VALUE): int32 {.cdecl.}
    SciterEvalElementScript*: proc (he: HELEMENT; script: WideCString; scriptLength: uint32; retval: ptr VALUE): int32 {. cdecl.}
    SciterAttachHwndToElement*: proc (he: HELEMENT; hwnd: HWND): int32 {.cdecl.}
    SciterControlGetType*: proc (he: HELEMENT; pType: ptr uint32): int32 {.cdecl.} ## #CTL_TYPE
    SciterGetValue*: proc (he: HELEMENT; pval: ptr VALUE): int32 {.cdecl.}
    SciterSetValue*: proc (he: HELEMENT; pval: ptr VALUE): int32  {.cdecl.}
    SciterGetExpando*: proc (he: HELEMENT; pval: ptr VALUE; forceCreation: bool): int32 {.cdecl.}
    SciterGetObject*: proc (he: HELEMENT; pval: ptr tiscript_value; forceCreation: bool): int32 {. cdecl.}
    SciterGetElementNamespace*: proc (he: HELEMENT; pval: ptr tiscript_value): int32 {.cdecl.}
    SciterGetHighlightedElement*: proc (hwnd: HWND; phe: ptr HELEMENT): int32 {.cdecl.}
    SciterSetHighlightedElement*: proc (hwnd: HWND; he: HELEMENT): int32 {.cdecl.} 
    SciterNodeAddRef*: proc (hn: HNODE): int32 {.cdecl.}
    SciterNodeRelease*: proc (hn: HNODE): int32 {.cdecl.}
    SciterNodeCastFromElement*: proc (he: HELEMENT; phn: ptr HNODE): int32 {.cdecl.}
    SciterNodeCastToElement*: proc (hn: HNODE; he: ptr HELEMENT): int32 {.cdecl.}
    SciterNodeFirstChild*: proc (hn: HNODE; phn: ptr HNODE): int32 {.cdecl.}
    SciterNodeLastChild*: proc (hn: HNODE; phn: ptr HNODE): int32 {.cdecl.}
    SciterNodeNextSibling*: proc (hn: HNODE; phn: ptr HNODE): int32 {.cdecl.}
    SciterNodePrevSibling*: proc (hn: HNODE; phn: ptr HNODE): int32 {.cdecl.}
    SciterNodeParent*: proc (hnode: HNODE; pheParent: ptr HELEMENT): int32 {.cdecl.}
    SciterNodeNthChild*: proc (hnode: HNODE; n: uint32; phn: ptr HNODE): int32 {.cdecl.}
    SciterNodeChildrenCount*: proc (hnode: HNODE; pn: ptr uint32): int32 {.cdecl.}
    SciterNodeType*: proc (hnode: HNODE; pNodeType: ptr uint32): int32 {.cdecl.} ## #NODE_TYPE
    SciterNodeGetText*: proc (hnode: HNODE; rcv: LPCWSTR_RECEIVER; rcv_param: pointer): int32 {.cdecl.}
    SciterNodeSetText*: proc (hnode: HNODE; text: WideCString; textLength: uint32): int32 {.cdecl.}
    SciterNodeInsert*: proc (hnode: HNODE; where: uint32;  what: HNODE): int32 {.cdecl.}
    SciterNodeRemove*: proc (hnode: HNODE; finalize: bool): int32 {.cdecl.}
    SciterCreateTextNode*: proc (text: WideCString; textLength: uint32; phnode: ptr HNODE): int32 {.cdecl.}
    SciterCreateCommentNode*: proc (text: WideCString; textLength: uint32; phnode: ptr HNODE): int32 {.cdecl.} 
    ValueInit*: proc (pval: ptr VALUE): uint32 {.cdecl.}
    ValueClear*: proc (pval: ptr VALUE): uint32 {.cdecl.}
    ValueCompare*: proc (pval1: ptr VALUE; pval2: ptr VALUE): uint32 {.cdecl.}
    ValueCopy*: proc (pdst: ptr VALUE; psrc: ptr VALUE): uint32 {.cdecl.}
    ValueIsolate*: proc (pdst: ptr VALUE): uint32 {.cdecl.}
    ValueType*: proc (pval: ptr VALUE; pType: ptr uint32; pUnits: ptr uint32): uint32 {.cdecl.}
    ValueStringData*: proc (pval: ptr VALUE; pChars: ptr LPCWSTR; pNumChars: ptr uint32): uint32 {.cdecl.}
    ValueStringDataSet*: proc (pval: ptr VALUE; chars: LPCWSTR; numChars: uint32;units: uint32): uint32 {.cdecl.}
    ValueIntData*: proc (pval: ptr VALUE; pData: ptr int32): uint32 {.cdecl.}
    ValueIntDataSet*: proc (pval: ptr VALUE; data: int32; `type`: uint32; units: uint32): uint32 {.cdecl.}
    ValueInt64Data*: proc (pval: ptr VALUE; pData: ptr int64): uint32 {.cdecl.}
    ValueInt64DataSet*: proc (pval: ptr VALUE; data: int64; `type`: uint32; units: uint32): uint32 {.cdecl.}
    ValueFloatData*: proc (pval: ptr VALUE; pData: ptr float64): uint32 {.cdecl.}
    ValueFloatDataSet*: proc (pval: ptr VALUE; data: float64; `type`: uint32; units: uint32): uint32 {.cdecl.}
    ValueBinaryData*: proc (pval: ptr VALUE; pBytes: ptr pointer; pnBytes: ptr uint32): uint32 {.cdecl.}
    ValueBinaryDataSet*: proc (pval: ptr VALUE; pBytes: pointer; nBytes: uint32;`type`: uint32; units: uint32): uint32 {.cdecl.}
    ValueElementsCount*: proc (pval: ptr VALUE; pn: ptr int32): uint32 {.cdecl.}
    ValueNthElementValue*: proc (pval: ptr VALUE; n: int32; pretval: ptr VALUE): uint32 {.cdecl.}
    ValueNthElementValueSet*: proc (pval: ptr VALUE; n: int32; pval_to_set: ptr VALUE): uint32 {.cdecl.}
    ValueNthElementKey*: proc (pval: ptr VALUE; n: int32; pretval: ptr VALUE): uint32 {.cdecl.}
    ValueEnumElements*: proc (pval: ptr VALUE; penum: KeyValueCallback; param: pointer): uint32 {.cdecl.}
    ValueSetValueToKey*: proc (pval: ptr VALUE; pkey: ptr VALUE; pval_to_set: ptr VALUE): uint32 {.cdecl.}
    ValueGetValueOfKey*: proc (pval: ptr VALUE; pkey: ptr VALUE; pretval: ptr VALUE): uint32 {.cdecl.}
    ValueToString*: proc (pval: ptr VALUE; how: uint32): uint32 {.cdecl.} 
    ValueFromString*: proc (pval: ptr VALUE; str: LPCWSTR; strLength: uint32; how: uint32): uint32  {.cdecl.} 
    ValueInvoke*: proc (pval: ptr VALUE; pthis: ptr VALUE; argc: uint32; argv: ptr VALUE; pretval: ptr VALUE; url: WideCString): uint32 {.cdecl.}
    ValueNativeFunctorSet*: proc (pval: ptr VALUE; pinvoke: NATIVE_FUNCTOR_INVOKE; prelease: NATIVE_FUNCTOR_RELEASE; tag: pointer): uint32 {.cdecl.}
    ValueIsNativeFunctor*: proc (pval: ptr VALUE): bool {.cdecl.} 
    TIScriptAPI*: proc (): ptr tiscript_native_interface {.cdecl.}
    SciterGetVM*: proc (hwnd: HWINDOW): HVM {.cdecl.}
    Sciter_tv2V*: proc (vm: HVM; script_value: tiscript_value; value: ptr VALUE; isolate: bool): bool {.cdecl.}
    Sciter_V2tv*: proc (vm: HVM; valuev: ptr VALUE; script_value: ptr tiscript_value): bool {.cdecl.}
    SciterOpenArchive*: proc (archiveData: pointer; archiveDataLength: uint32): HSARCHIVE {. cdecl.}
    SciterGetArchiveItem*: proc (harc: HSARCHIVE; path: WideCString; pdata: ptr pointer; pdataLength: ptr uint32): bool {. cdecl.}
    SciterCloseArchive*: proc (harc: HSARCHIVE): bool {.cdecl.}
    SciterFireEvent*: proc (evt: ptr BEHAVIOR_EVENT_PARAMS; post: bool; handled: ptr bool): int32 {.cdecl.}
    SciterGetCallbackParam*: proc (hwnd: HWINDOW): pointer {.cdecl.}
    SciterPostCallback*: proc (hwnd: HWINDOW; wparam: uint32; lparam: uint32; timeoutms: uint32): uint32 {.cdecl.}
    GetSciterGraphicsAPI*: proc (): LPSciterGraphicsAPI {.cdecl.}
    GetSciterGraphicsAPI1*: proc (): LPSciterGraphicsAPI {.cdecl.}
    when defined(windows):
      SciterCreateOnDirectXWindow*: proc (hwnd:HWINDOW, pSwapChain:pointer): bool {.cdecl.}
      SciterRenderOnDirectXWindow*: proc (hwnd:HWINDOW, elementToRenderOrNull:HELEMENT, frontLayer:bool): bool {.cdecl.}
      SciterRenderOnDirectXTexture*: proc (hwnd:HWINDOW, elementToRenderOrNull:HELEMENT, surface:pointer): bool {.cdecl.}
    
  SciterAPI_ptr* = proc (): ptr ISciterAPI {.cdecl.}

var api*:ptr ISciterAPI = nil
var elemStore:HELEMENT=nil
var cResult: LPCWSTR

proc SAPI*():ptr ISciterAPI {.inline, discardable,cdecl.} =
  var libhandle = loadLib(SCITER_DLL_NAME)
  if libhandle == nil:
    libhandle = loadLib(getCurrentDir()&"/"&SCITER_DLL_NAME)
  if libhandle == nil:
    quit "sciter runtime library not found: "&SCITER_DLL_NAME
  var procPtr = symAddr(libhandle, "SciterAPI")
  let p = cast[SciterAPI_ptr](procPtr)
  api = p()
  return api

proc selectElement*(root:HELEMENT,name:string):HELEMENT{.inline.}=
  elemStore=nil
  proc selectCallback(he: HELEMENT; param: pointer): bool{.stdcall.} =
    elemStore=he
    return  true
  if api.SciterSelectElementsW(root,newWString(name),selectCallback,nil) != 0: raise newException(SciterError, "SciterSelectElements error")
  if elemStore != nil: result=elemStore else: raise newException(SciterError, "Element not found")

var value:VALUE
proc getValue*(elem:HELEMENT):ptr VALUE=
  if api.SciterGetValue(elem,addr(value)) == 0:  result=addr(value) else: raise newException(SciterError, "SciterGetValue error")

proc setValue*(elem:HELEMENT,value:ptr VALUE)=
  if api.SciterSetValue(elem,value) != 0: raise newException(SciterError, "SciterSetValue error")  

proc getStrValue*(elem:HELEMENT):string=
  var value:VALUE
  if api.SciterGetValue(elem,addr(value)) != 0: raise newException(SciterError, "SciterGetValue error")
  var str:LPCWSTR
  if api.ValueStringData(addr(value),addr(str),nil) == 0:  result= $str else: raise newException(SciterError, "ValueStringData error")
 
proc setStrValue*(elem:HELEMENT,s:string)=
  var strValue:VALUE
  if api.ValueFromString(addr(strValue),newWideCString(s),s.len.uint32,0'u32) != 0: raise newException(SciterError, "ValueFromString error")
  if api.SciterSetValue(elem,addr(strValue)) != 0: raise newException(SciterError, "SciterSetValue error")

proc getIntValue*(elem:HELEMENT):int32=
  var value:VALUE
  if api.SciterGetValue(elem,addr(value)) != 0: raise newException(SciterError, "SciterGetValue error")
  var res:int32
  if api.ValueIntData(addr(value),addr(res)) == 0:  result= res else: raise newException(SciterError, "ValueIntData error")

proc setIntValue*(elem:HELEMENT,n:int32,tp:uint32)=
  var intValue:VALUE
  if api.ValueIntDataSet(addr(intValue),n,tp,0'u32) != 0: raise newException(SciterError, "ValueIntDataSet error")
  if api.SciterSetValue(elem,addr(intValue)) != 0: raise newException(SciterError, "SciterSetValue error")  

proc getInt64Value*(elem:HELEMENT):int64=
  var value:VALUE
  if api.SciterGetValue(elem,addr(value)) != 0: raise newException(SciterError, "SciterGetValue error")
  var res:int64
  if api.ValueInt64Data(addr(value),addr(res)) == 0:  result= res else: raise newException(SciterError, "ValueInt64Data error")

proc setInt64Value*(elem:HELEMENT,n:int64,tp:uint32)=
  var int64Value:VALUE
  if api.ValueInt64DataSet(addr(int64Value),n,tp,0'u32) != 0: raise newException(SciterError, "ValueInt64DataSet error")
  if api.SciterSetValue(elem,addr(int64Value)) != 0: raise newException(SciterError, "SciterSetValue error")
  
proc setHtml*(elem:HELEMENT,s:string)=
  if api.SciterSetElementHtml(elem,cast[ptr byte](s.cstring),s.len.uint32,0'u32)!=0: raise newException(SciterError, "SciterSetElementHtml error")

proc getNthChild*(elem:HELEMENT,n:int):HELEMENT = 
  var op:HELEMENT
  if api.SciterGetNthChild(elem,n.cuint,addr(op)) == 0: result=op  else: raise newException(SciterError, "SciterGetNthChild error")
   
proc getChildCount*(elem:HELEMENT):uint32=
  var cnt:uint32
  if api.SciterGetChildrenCount(elem,addr(cnt)) == 0: result=cnt else: raise newException(SciterError, "SciterGetChildrenCount error")

proc getIndex*(elem:HELEMENT):uint32=
  var idx:uint32=0
  if api.SciterGetElementIndex(elem,addr(idx)) == 0:  result=idx  else: raise newException(SciterError, "SciterGetElementIndex error") 

proc getParent*(elem:HELEMENT):HELEMENT=
  var parent:HELEMENT
  if api.SciterGetParentElement(elem,addr(parent)) == 0:  result=parent  else: raise newException(SciterError, "SciterGetParentElement error") 
  
proc setAttribute*(elem:HELEMENT,name:string,value:string)=
  if api.SciterSetAttributeByName(elem,name,newWideCString(value))!=0: raise newException(SciterError, "SciterSetAttributeByName error")

proc setStyle*(elem:HELEMENT,name:string,value:string)=
  if api.SciterSetStyleAttribute(elem,name,newWideCString(value))!=0: raise newException(SciterError, "SciterSetStyleAttribute error")

proc getAttribute*(elem:HELEMENT,name:string):string{.cdecl}=
  proc attrCallback (str:LPCWSTR; str_length: cuint; param: pointer) {.cdecl.}= cResult= str 
  if api.SciterGetAttributeByNameCB(elem,name,attrCallback,nil) != 0: raise newException(SciterError, "SciterGetAttributeByNameCB error")
  if cResult != nil:  result = $cResult else: raise newException(SciterError, "SciterGetAttributeByNameCB nilPointer error")

proc getType*(elem:HELEMENT):string=
  var etype:cstring
  if api.SciterGetElementType(elem,addr(etype))==0: result = $etype else:  raise newException(SciterError, "SciterGetElementType  error")

proc create*(name:string):HELEMENT=
  var elem:HELEMENT
  if api.SciterCreateElement(name,nil,addr(elem)) == 0: result= elem else: raise newException(SciterError, "SciterCreateElement  error")
  
proc insert*(elem:HELEMENT,parent:HELEMENT,index:uint32)=
  if api.SciterInsertElement(elem,parent,index) != 0: raise newException(SciterError, "SciterInsertElement  error")

proc update*(elem:HELEMENT,forse:bool)=
  if api.SciterUpdateElement(elem,forse) != 0: raise newException(SciterError, "SciterUpdateElement error")

proc delete*(elem:HELEMENT)=
  if api.SciterDeleteElement(elem) != 0: raise newException(SciterError, "SciterDeleteElement error") 

proc scroll*(elem:HELEMENT)=
  if api.SciterScrollToView(elem,0) != 0: raise newException(SciterError, "SciterScrollToView error") 
    
proc setEvent*(elem:HELEMENT,pep:ElementEventProc)=
  if api.SciterAttachEventHandler(elem,pep,nil) != 0: raise newException(SciterError, "SciterAttachEventHandler error")

proc setState*(elem:HELEMENT,setBits:uint32,clearBits:uint32,upDate:bool)=
  if api.SciterSetElementState(elem,setBits,clearBits,upDate) != 0: raise newException(SciterError, "SciterSetElementState error") 

proc showPopUp*(elem:HELEMENT,parent:HELEMENT,where:uint32)=
  if api.SciterShowPopup(elem,parent,where) != 0: raise newException(SciterError, "SciterShowPopup error")     

proc scriptMethod*(elem:HELEMENT,name:string)=
  if api.SciterCallScriptingMethod(elem,name,nil,0,nil) != 0: raise newException(SciterError, "SciterCallScriptingMethod error")    
 