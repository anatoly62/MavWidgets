module SciterApi where
import Graphics.Win32 hiding (messageBox, c_MessageBox,postQuitMessage) 
import System.Win32.Types
import System.Win32.DLL
import Foreign
import Foreign.C.String
import Foreign.C.Types
import Control.Monad
import Data.IORef
import System.IO.Unsafe
import Data.Maybe (fromMaybe)
import Data.ByteString (ByteString,useAsCString)
import qualified Data.ByteString as B(length)

import Convert

data Fake
data Value=Value{valueT::UINT,valueU::UINT,valueD::UINT64}
data CallbackData = CallbackData { callbackCode :: UINT, callbackHWND :: HWND}

instance Storable Value where
  sizeOf _ = 16
  alignment _ = 8
  poke ptr (Value a1 a2 a3)=pokeElemOff (castPtr ptr) 0 a1 >>pokeElemOff (castPtr ptr) 4 a2 >>pokeElemOff (castPtr ptr) 8 a3
  peek p=do
    a1<- peekByteOff p 0
    a2<- peekByteOff p 4
    a3<- peekByteOff p 8
    return $ Value a1 a2 a3

instance Storable CallbackData where
  sizeOf _ = 8
  alignment _ = 4
  poke ptr (CallbackData a1 a2 ) =  pokeElemOff (castPtr ptr) 0 a1 >> pokeElemOff (castPtr ptr) 4 a2
  peek p=do
    a1<- peekByteOff p 0
    a2<- peekByteOff p 4
    return $ CallbackData a1 a2

type Foo = HWND->LPCWSTR->IO Bool
type TRoot=HWND->Ptr Addr->IO Int
type FRootElement=Addr->Addr->IO Bool
type TRootElement=Addr->CWString->FunPtr FRootElement ->Addr->IO UINT
type TElement=Addr->Ptr Addr->IO UINT
type TNthElement=Addr->UINT->Ptr Addr->IO UINT
type TElemNew=CString->CWString->Ptr Addr->IO UINT
type TElemInsert=Addr->Addr->UINT->IO UINT
type TElemDelete=Addr->IO UINT
type TElemIndex=Addr->Ptr UINT->IO UINT
type TElemState=Addr->UINT->UINT->Bool->IO UINT
type TElemType=Addr->Ptr CString->IO UINT
type TElemValue=Addr->Ptr Value->IO UINT
type TElemScriptMethod=Addr->CString->Ptr Value->UINT->Ptr Value->IO UINT
type TElemScroll=Addr->UINT->IO UINT
type TValStr=Ptr Value->Ptr CWString->Ptr UINT->IO UINT
type TStrVal=Ptr Value->CWString->UINT->UINT->IO UINT
type TValInt=Ptr Value->Ptr Int->IO UINT
type TIntVal=Ptr Value->Int->UINT->UINT->IO UINT
type TValInt64=Ptr Value->Ptr Word64->IO UINT
type TInt64Val=Ptr Value->Word64->UINT->UINT->IO UINT
type TElemAttrSetter=Addr->CString->CWString->IO UINT
type FElemAttr=CWString->UINT->Addr->IO ()
type TElemAttrGetter=Addr->CString->FunPtr FElemAttr->Addr->IO UINT
type TElemStyleGetter=Addr->CString->FunPtr FElemAttr->Addr->IO UINT
type TElemTextGetter=Addr->FunPtr FElemAttr->Addr->IO UINT
type TElemTextSetter=Addr->CWString-> UINT->IO UINT
type TElemHtmlSetter=Addr->Ptr CChar-> UINT->UINT->IO UINT
type FElemEvent=Addr->Addr->UINT->Addr->IO Bool
type TElemEvent=Addr->FunPtr FElemEvent->Addr->IO UINT
type TWindowEvent=HWND->FunPtr FElemEvent->Addr->UINT->IO UINT
type FCallback=Ptr CallbackData->Addr->IO UINT
type TCallback=HWND->FunPtr FCallback->  Addr->IO UINT

foreign import stdcall "ole2.h  OleInitialize"  oleInitialize :: Ptr Fake ->IO Int
foreign import stdcall "wrapper" wrap_callBack :: FCallback -> IO (FunPtr FCallback)
foreign import stdcall "wrapper" wrap_element :: FRootElement -> IO (FunPtr FRootElement)
foreign import stdcall "wrapper" wrap_elem_attr :: FElemAttr -> IO (FunPtr FElemAttr)
foreign import stdcall "wrapper" wrap_elem_event :: FElemEvent -> IO (FunPtr FElemEvent)
foreign import stdcall "dynamic" myFun :: FunPtr (IO Addr) -> IO Addr
foreign import stdcall "dynamic" mkSciterClassName :: FunPtr (IO CWString) -> IO CWString
foreign import stdcall "dynamic" mkSciterLoadFile:: FunPtr Foo -> Foo
foreign import stdcall "dynamic" mkSciterGetRootElement:: FunPtr TRoot -> TRoot
foreign import stdcall "dynamic" mkSciterSelectElement:: FunPtr TRootElement -> TRootElement
foreign import stdcall "dynamic" mkSciterSetCallback:: FunPtr TCallback -> TCallback
foreign import stdcall "dynamic" mkSciterAttachEventHandler:: FunPtr TElemEvent -> TElemEvent
foreign import stdcall "dynamic" mkSciterAttachWindowEventHandler:: FunPtr TWindowEvent -> TWindowEvent
foreign import stdcall "dynamic" mkSciterSetAttribute:: FunPtr TElemAttrSetter -> TElemAttrSetter
foreign import stdcall "dynamic" mkSciterGetAttribute:: FunPtr TElemAttrGetter -> TElemAttrGetter
foreign import stdcall "dynamic" mkSciterCreateElem:: FunPtr TElemNew -> TElemNew
foreign import stdcall "dynamic" mkSciterInsertElem:: FunPtr TElemInsert -> TElemInsert
foreign import stdcall "dynamic" mkSciterDeleteElem:: FunPtr TElemDelete-> TElemDelete
foreign import stdcall "dynamic" mkSciterGetParent:: FunPtr TElement -> TElement
foreign import stdcall "dynamic" mkSciterGetNthChild:: FunPtr TNthElement -> TNthElement
foreign import stdcall "dynamic" mkSciterGetIndex:: FunPtr TElemIndex -> TElemIndex
foreign import stdcall "dynamic" mkSciterGetCount:: FunPtr TElemIndex -> TElemIndex
foreign import stdcall "dynamic" mkSciterGetType:: FunPtr TElemType -> TElemType
foreign import stdcall "dynamic" mkSciterScrollToView:: FunPtr TElemScroll -> TElemScroll
foreign import stdcall "dynamic" mkSciterGetValue:: FunPtr TElemValue -> TElemValue
foreign import stdcall "dynamic" mkSciterSetValue:: FunPtr TElemValue -> TElemValue
foreign import stdcall "dynamic" mkValueStringData:: FunPtr TValStr -> TValStr
foreign import stdcall "dynamic" mkValueIntData:: FunPtr TValInt -> TValInt
foreign import stdcall "dynamic" mkValueIntDataSet:: FunPtr TIntVal -> TIntVal
foreign import stdcall "dynamic" mkValueInt64Data:: FunPtr TValInt64 -> TValInt64
foreign import stdcall "dynamic" mkValueInt64DataSet:: FunPtr TInt64Val -> TInt64Val
foreign import stdcall "dynamic" mkValueFromString:: FunPtr TStrVal -> TStrVal
foreign import stdcall "dynamic" mkSciterGetStyle:: FunPtr TElemStyleGetter -> TElemStyleGetter
foreign import stdcall "dynamic" mkSciterGetText:: FunPtr TElemTextGetter -> TElemTextGetter
foreign import stdcall "dynamic" mkSciterSetText:: FunPtr TElemTextSetter -> TElemTextSetter
foreign import stdcall "dynamic" mkSciterSetHtml:: FunPtr TElemHtmlSetter -> TElemHtmlSetter
foreign import stdcall "dynamic" mkSciterSetState:: FunPtr TElemState -> TElemState
foreign import stdcall "dynamic" mkSciterScriptMethod:: FunPtr TElemScriptMethod -> TElemScriptMethod

apiStore :: IORef Addr
{-# NOINLINE apiStore #-}
apiStore = unsafePerformIO (newIORef nullPtr)

ptrLen=4

apiPtr::IO Addr
apiPtr =do
   oleInitialize nullPtr
   h<-loadLibrary "sciter.dll"
   f<-getProcAddress (castPtr h) "SciterAPI"
   api<-myFun $ castPtrToFunPtr f
   writeIORef apiStore api
   return api

----Sciter Api Function ---------------------------------------------------------------------------------
sciterClassName::IO CWString
sciterClassName = do
 addr<-readIORef apiStore
 peek  ( addr  `plusPtr` ptrLen) >>= mkSciterClassName
   
sciterLoadFile::HWND->String->IO Bool
sciterLoadFile  w path = do
  addr<-readIORef apiStore
  ptr <- peek  $ addr  `plusPtr` (7*ptrLen)
  newCWString path >>= mkSciterLoadFile ptr w

sciterSetCallback::HWND-> FunPtr FCallback->IO UINT
sciterSetCallback   w fn = do
  addr<-readIORef apiStore
  ptr <- peek  $ addr  `plusPtr` (9*ptrLen)
  mkSciterSetCallback ptr w fn  addr
  
sciterGetRoot::HWND->IO Addr
sciterGetRoot  w = do
  addr<-readIORef apiStore
  ptr <- peek  $ addr  `plusPtr` (33*ptrLen)
  alloca $ \mem->do
    res<-mkSciterGetRootElement ptr  w mem
    if res==0 then peek mem else error "sciterGetRoot"

sciterGetParent::Addr->IO Addr
sciterGetParent  elem = do
  addr<-readIORef apiStore
  ptr <- peek  $ addr  `plusPtr` (38*ptrLen)
  alloca $ \mem->do
    res<-mkSciterGetParent ptr  elem mem
    if res==0 then peek mem else error "sciterGetParent"

sciterGetNthChild::Addr->UINT->IO Addr
sciterGetNthChild  elem n = do
  addr<-readIORef apiStore
  ptr <- peek  $ addr  `plusPtr` (37*ptrLen)
  alloca $ \mem->do
    res<-mkSciterGetNthChild ptr  elem  n mem
    if res==0 then peek mem else error "sciterGetNthChild"

sciterGetIndex::Addr->IO UINT
sciterGetIndex  elem  = do
  addr<-readIORef apiStore
  ptr <- peek  $ addr  `plusPtr` (48*ptrLen)
  alloca $ \mem->do
    res<-mkSciterGetIndex ptr  elem   mem
    if res==0 then peek mem else error "sciterGetIndex"
    
sciterGetClildCount::Addr->IO UINT
sciterGetClildCount  elem  = do
  addr<-readIORef apiStore
  ptr <- peek  $ addr  `plusPtr` (36*ptrLen)
  alloca $ \mem->do
    res<-mkSciterGetCount ptr  elem   mem
    if res==0 then peek mem else error "sciterGetCount"

sciterGetType::Addr->IO String
sciterGetType  elem  = do
  addr<-readIORef apiStore
  ptr <- peek  $ addr  `plusPtr` (49*ptrLen)
  alloca $ \mem->do
    res<-mkSciterGetType ptr  elem   mem
    if res==0 then  peek mem >>= peekCString else error "sciterGetType"
    
sciterCreateElem::String->IO Addr
sciterCreateElem name=do
  addr<-readIORef apiStore
  ptr <- peek  $ addr  `plusPtr` (73*ptrLen)
  name'<-newCString name
  alloca $ \mem->do
      res<-mkSciterCreateElem ptr  name' nullPtr   mem
      when (res/=0) $ error "sciterCreateElem"
      peek mem 
      
sciterInsertElem::Addr->Addr->UINT->IO ()
sciterInsertElem elem parent index=do
  addr<-readIORef apiStore
  ptr <- peek  $ addr  `plusPtr` (75*ptrLen)
  res<-mkSciterInsertElem  ptr elem parent index
  when (res/=0) $ error "sciterInsertElem"

sciterShowPopUp::Addr->Addr->IO ()
sciterShowPopUp elem parent =do
  addr<-readIORef apiStore
  ptr <- peek  $ addr  `plusPtr` (68*ptrLen)
  res<-mkSciterInsertElem  ptr elem parent 8
  when (res/=0) $ error "sciterShowPopUp"
  
sciterDeleteElem::Addr->IO UINT
sciterDeleteElem elem =do
  addr<-readIORef apiStore
  ptr <- peek  $ addr  `plusPtr` (77*ptrLen)
  res<-mkSciterDeleteElem  ptr elem
  if res==0 then return 0 else error "sciterDeleteElem"
  
sciterScrollToView::Addr->IO UINT
sciterScrollToView elem=do  
  addr<-readIORef apiStore
  ptr <- peek  $ addr  `plusPtr` (54*ptrLen)
  res<-mkSciterScrollToView ptr elem 0
  if res==0 then return 0 else error "sciterScrollToView"
           
sciterGetStrValue::Addr->IO ByteString
sciterGetStrValue  elem  = do
  addr<-readIORef apiStore
  ptr <- peek  $ addr  `plusPtr` (102*ptrLen)
  ptr' <- peek  $ addr  `plusPtr` (133*ptrLen)
  alloca $ \mem->do
    res<-mkSciterGetValue ptr  elem   mem
    mr<-peek mem
    when(valueT mr/=5) $ error "not string value "
    when (res/=0) $ error  $ "mkSciterGetValue return " ++ show res
    alloca $ \mem' ->do
       res'<-mkValueStringData ptr'  mem mem' nullPtr
       when (res'/=0) $ error  $ "mkValueStringData return " ++ show res'
       peek mem' >>= peekCWStr

sciterGetIntValue::Addr->IO Int
sciterGetIntValue  elem  = do
  addr<-readIORef apiStore
  ptr <- peek  $ addr  `plusPtr` (102*ptrLen)
  ptr' <- peek  $ addr  `plusPtr` (135*ptrLen)
  alloca $ \mem->do
    res<-mkSciterGetValue ptr  elem   mem
    when (res/=0) $ error  $ "mkSciterGetValue return " ++ show res
    alloca $ \mem' ->do
       res'<-mkValueIntData ptr'  mem mem'
       when (res'/=0) $ error  $ "mkValueIntData return " ++ show res'
       peek mem'

sciterGetIn64Value::Addr->IO Word64
sciterGetIn64Value  elem  = do
  addr<-readIORef apiStore
  ptr <- peek  $ addr  `plusPtr` (102*ptrLen)
  ptr' <- peek  $ addr  `plusPtr` (137*ptrLen)
  alloca $ \mem->do
    res<-mkSciterGetValue ptr  elem   mem
    when (res/=0) $ error  $ "mkSciterGetValue return " ++ show res
    alloca $ \mem' ->do
       res'<-mkValueInt64Data ptr'  mem mem'
       when (res'/=0) $ error  $ "mkValueInt64Data return " ++ show res'
       peek mem'

sciterSetStrValue::Addr->ByteString->IO UINT
sciterSetStrValue el value=do
  addr<-readIORef apiStore
  ptr <- peek  $ addr  `plusPtr` (103*ptrLen)
  ptr' <- peek  $ addr  `plusPtr` (151*ptrLen)
  let value'=toStr value
  let l= length value'
  str<-newCWString value'
  alloca $ \mem ->do
    res<-mkValueFromString ptr' mem str  (fromIntegral l) 0
    when (res/=0) $ error "sciterSetStrValue0"
    r<-mkSciterSetValue ptr el mem
    when (r/=0) $ error "error SetStrValue1"
    return r

sciterSetInt64Value::Addr->Word64->UINT->IO UINT
sciterSetInt64Value el value tp=do
  addr<-readIORef apiStore
  ptr <- peek  $ addr  `plusPtr` (103*ptrLen)
  ptr' <- peek  $ addr  `plusPtr` (138*ptrLen)
  alloca $ \mem ->do
    res<-mkValueInt64DataSet ptr' mem value  tp 0
    when (res/=0) $ error "sciterSetInt64Value0"
    res<-mkSciterSetValue ptr el mem
    if res==0 then return 0 else error "sciterSetInt64Value1"

sciterSetIntValue::Addr->Int->UINT->IO UINT
sciterSetIntValue el value tp=do
  addr<-readIORef apiStore
  ptr <- peek  $ addr  `plusPtr` (103*ptrLen)
  ptr' <- peek  $ addr  `plusPtr` (136*ptrLen)
  alloca $ \mem ->do
    res<-mkValueIntDataSet ptr' mem value  tp 0
    when (res/=0) $ error "sciterSetIntValue0"
    res<-mkSciterSetValue ptr el mem
    if res==0 then return 0 else error "sciterSetIntValue1"

sciterGetText::Addr->IO String
sciterGetText elem  =do
  addr<-readIORef apiStore
  ptr <- peek  $ addr  `plusPtr` (40*ptrLen)
  store <- newIORef nullPtr
  let callbackFun attr len param =  writeIORef store attr
  cfn <- wrap_elem_attr callbackFun
  res<-mkSciterGetText ptr elem  cfn nullPtr
  when (res/=0) $ error "sciterGetText"
  readIORef store >>=  peekCWString
    
sciterSetText::Addr->String->IO UINT
sciterSetText el text=do
  addr<-readIORef apiStore
  ptr <- peek  $ addr  `plusPtr` (41*ptrLen)
  value<-newCWString text
  let l= length text
  res<-mkSciterSetText ptr el  value  (fromIntegral l)
  if res==0 then return 0 else error "sciterSetText"

sciterSetHtml::Addr->ByteString->IO UINT
sciterSetHtml el value=do
 addr<-readIORef apiStore
 ptr <- peek  $ addr  `plusPtr` (65*ptrLen)
 res<-useAsCString value $ \cs->  mkSciterSetHtml ptr el  cs  (fromIntegral (B.length value ))  0
 if res==0 then return 0 else error "sciterSetHtml"

sciterSelectElement::Addr->String->IO Addr
sciterSelectElement  root css  = do
  addr<-readIORef apiStore
  css'<-newCWString css
  store <- newIORef nullPtr
  let callbackFun el param=do
        writeIORef store el
        return True
  ptr <- peek $ addr `plusPtr` (62*ptrLen)
  cfn <- wrap_element callbackFun
  res<-mkSciterSelectElement ptr root css' cfn nullPtr
  if res==0 then readIORef store else error "sciterSelectElement"

sciterAttachEventHandler::Addr->FElemEvent->IO UINT
sciterAttachEventHandler  elem fn =do
  addr<-readIORef apiStore
  ptr <- peek  $ addr  `plusPtr` (80*ptrLen)
  cFn <- wrap_elem_event fn
  res<-mkSciterAttachEventHandler ptr elem cFn nullPtr
  if res==0 then return 0 else error "sciterAttachEventHandler"
  
sciterAttachWindowEventHandler::HWND->FElemEvent->IO UINT
sciterAttachWindowEventHandler  elem fn =do
  addr<-readIORef apiStore
  ptr <- peek  $ addr  `plusPtr` (81*ptrLen)
  cFn <- wrap_elem_event fn
  res<-mkSciterAttachWindowEventHandler ptr elem cFn nullPtr  0xFFFF
  if res==0 then return 0 else error $ "sciterAttachWindowEventHandler return " ++ show res 

sciterGetAttribute::Addr->String->IO String
sciterGetAttribute elem  name =do
  addr<-readIORef apiStore
  ptr <- peek  $ addr  `plusPtr` (45*ptrLen)
  name'<-newCString name
  store <- newIORef nullPtr
  let callbackFun attr len param = writeIORef store attr
  cfn <- wrap_elem_attr callbackFun
  res<-mkSciterGetAttribute ptr elem name' cfn nullPtr
  when (res/=0) $ error "sciterGetAttribute"
  readIORef store >>=  peekCWString

sciterSetAttribute::Addr->String->String->IO UINT
sciterSetAttribute elem  name value=do
  addr<-readIORef apiStore
  ptr <- peek  $ addr  `plusPtr` (46*ptrLen)
  name'<-newCString name
  value'<-newCWString value
  res<-mkSciterSetAttribute ptr elem name' value'
  if res==0 then return 0 else error "sciterSetAttribute"

sciterGetStyleAttribute::Addr->String->IO String
sciterGetStyleAttribute elem attr  =do
  name<-newCString attr
  addr<-readIORef apiStore
  ptr <- peek  $ addr  `plusPtr` (51*ptrLen)
  store <- newIORef nullPtr
  let callbackFun attr len param =  writeIORef store attr
  cfn <- wrap_elem_attr callbackFun
  res<-mkSciterGetStyle ptr elem  name cfn nullPtr
  when (res/=0) $ error "sciterGetStyle"
  readIORef store >>=  peekCWString

sciterSetStyleAttribute::Addr->String->String->IO UINT
sciterSetStyleAttribute elem  name value=do
  addr<-readIORef apiStore
  ptr <- peek  $ addr  `plusPtr` (52*ptrLen)
  name'<-newCString name
  value'<-newCWString value
  res<-mkSciterSetAttribute ptr elem name' value'
  if res==0 then return 0 else error "sciterSetStyleAttribute"

sciterSetState::Addr->UINT->UINT->Bool->IO UINT
sciterSetState elem setBits clearBits upDate=do
  addr<-readIORef apiStore
  ptr <- peek  $ addr  `plusPtr` (72*ptrLen)
  res<-mkSciterSetState ptr  elem setBits clearBits upDate
  if res==0 then return 0 else error "sciterSetState"

sciterScriptMethod::Addr->String-> IO UINT
sciterScriptMethod elem name=do
  addr<-readIORef apiStore
  ptr <- peek  $ addr  `plusPtr` (97*ptrLen)
  name'<-newCString name
  res<-mkSciterScriptMethod ptr elem name' nullPtr 0 nullPtr
  if res==0 then return 0 else error "sciterScriptMethod"

