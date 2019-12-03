{-# LANGUAGE OverloadedStrings #-}
module MavWidgetsSc where

import Graphics.Win32 hiding (messageBox, c_MessageBox,postQuitMessage,try) 
import System.Win32.Types hiding (try)
import Foreign
import Foreign.C.String
import Control.Monad
import Data.IORef
import System.IO.Unsafe
import Data.Maybe (fromMaybe)
import SciterApi
import Data.Foldable (foldl')
import Data.List(findIndex)

import Data.ByteString (ByteString,split,isPrefixOf)
import qualified Data.ByteString as B

import Data.Time.Clock.POSIX
import Convert
import Foreign.C.Types
import Data.Time.Clock (utctDay)
import Data.Time
import Data.Char

import Control.Exception

type GuiPtr=Addr
type WinPtr=HWND

---MavWidgets Callbacks
exitStore :: IORef UINT
{-# NOINLINE exitStore #-}
exitStore = unsafePerformIO (newIORef 0)

_buttonsStore_ :: IORef [(Addr,IO())]
{-# NOINLINE _buttonsStore_ #-}
_buttonsStore_ = unsafePerformIO (newIORef [])

_checksStore_ :: IORef [(Addr,IO())]
{-# NOINLINE _checksStore_ #-}
_checksStore_ = unsafePerformIO (newIORef [])

_combosStore_ :: IORef [(Addr,IO())]
{-# NOINLINE _combosStore_ #-}
_combosStore_ = unsafePerformIO (newIORef [])

_datesStore_ :: IORef [(Addr,IO())]
{-# NOINLINE _datesStore_ #-}
_datesStore_ = unsafePerformIO (newIORef [])

_flatButtonsStore_ :: IORef [(Addr,IO())]
{-# NOINLINE _flatButtonsStore_ #-}
_flatButtonsStore_ = unsafePerformIO (newIORef [])

_menuItemsStore_ :: IORef [(Addr,IO())]
{-# NOINLINE _menuItemsStore_ #-}
_menuItemsStore_ = unsafePerformIO (newIORef [])

_tablesStore_ :: IORef [(Addr,Int->IO())]
{-# NOINLINE _tablesStore_ #-}
_tablesStore_ = unsafePerformIO (newIORef [])

_tablesStoreDbl_ :: IORef [(Addr,Int->IO())]
{-# NOINLINE _tablesStoreDbl_ #-}
_tablesStoreDbl_ = unsafePerformIO (newIORef [])

_tablesStoreMenu_ :: IORef [(Addr,Int->IO())]
{-# NOINLINE _tablesStoreMenu_ #-}
_tablesStoreMenu_ = unsafePerformIO (newIORef [])

_textsStore_ :: IORef [(Addr,IO())]
{-# NOINLINE _textsStore_ #-}
_textsStore_ = unsafePerformIO (newIORef [])

_textsStore2_ :: IORef [(Addr,Int->IO())]
{-# NOINLINE _textsStore2_ #-}
_textsStore2_ = unsafePerformIO (newIORef [])

foreign import stdcall safe "windows.h MessageBoxW"
  c_MessageBox :: HWND -> LPCTSTR -> LPCTSTR -> MBStyle -> IO MBStatus

--WinApi
messageBox :: HWND -> String -> String -> MBStyle -> IO MBStatus
messageBox wnd text caption style =
  withTString text $ \ c_text ->
  withTString caption $ \ c_caption ->
  failIfZero "MessageBox" $ c_MessageBox wnd c_text c_caption style
  
message:: String->IO ()
message text= do
 messageBox nullPtr text "Повідомлення" 0
 return ()

--CallBasks

sciterMainCallBack::Ptr CallbackData->Addr->IO UINT
sciterMainCallBack res addr=do
  r<- peek res
  writeIORef exitStore $ callbackCode r
  return 0
  
sciterCallBack::Ptr CallbackData->Addr->IO UINT
sciterCallBack res addr=do
  r<- peek res
  return 0  

tableEvent::Addr->Addr->UINT->Addr->IO Bool
tableEvent tag elem evtg param=do
  when (evtg == 1) $ do
    cmd <- peekByteOff param 0 :: IO Word16
    trg <- peekByteOff param 4 :: IO (Ptr UINT)
    state <- peekByteOff param 24 :: IO Word16
    if cmd == 3 && state == 1 then do
      t<-sciterGetType $ castPtr trg
      n <- if t=="tr" then chCursor elem $ castPtr trg
           else if t=="td" then sciterGetParent ( castPtr trg) >>= chCursor elem else return (-1)
      when(n>=0) $ readIORef _tablesStore_ >>= callFun n
    else if cmd == 3 && state ==2 then do
      n<- sciterGetParent ( castPtr trg) >>= chCursor elem
      readIORef _tablesStoreMenu_ >>=  callFun n
    else when (cmd == 5) $ do
      strIdx <- sciterGetAttribute elem "index"
      readIORef _tablesStoreDbl_ >>= callFun ( read strIdx)
  return False
  where
  chCursor elem target = do
    strIdx <- sciterGetAttribute elem "index"
    let idx = read strIdx :: Int
    when (idx >= 0) $  do
      child <- sciterGetNthChild elem $ fromIntegral idx
      sciterSetStyleAttribute child "background"  "#ffffff"
      return ()
    n <- sciterGetIndex target
    sciterSetAttribute elem "index" $ show n
    sciterSetStyleAttribute target "background" "#00bbee"
    return $ fromIntegral n
  callFun  n store =
    when (n>=0) $ 
       case lookup elem store of
        Nothing -> return ()
        Just fun -> when (n >= 0) $ fun $ fromIntegral n

chEvent::Addr->Addr->UINT->Addr->IO Bool
chEvent tag elem evtg param=  tryEBool "Несподівана помилка : " $  do
  when (evtg==256) $ do
    cmd<-peekByteOff param 0 ::IO UINT
    src<-peekByteOff param 8 ::IO (Ptr UINT)
    res<-peekByteOff param 12 ::IO UINT
    when (res ==1 && cmd == 0) $ do
      store<-readIORef _checksStore_
      fromMaybe (return ()) $ lookup elem store
  return False

cbEvent::Addr->Addr->UINT->Addr->IO Bool
cbEvent tag elem evtg param=  tryEBool "Несподівана помилка : " $  do
  when (evtg==256) $ do
    cmd<-peekByteOff param 0 ::IO UINT
    src<-peekByteOff param 8 ::IO (Ptr UINT)
    res<-peekByteOff param 12 ::IO UINT
    when (res == 0 && cmd == 5) $ do
      idx<-sciterGetIndex (castPtr src)
      sciterSetAttribute elem "index" $ show idx
      store<-readIORef _combosStore_
      fromMaybe (return ()) $ lookup elem store
  return False
  
dateEvent::Addr->Addr->UINT->Addr->IO Bool
dateEvent tag elem evtg param=  tryEBool "Несподівана помилка : " $  do
  when (evtg==256) $ do
    cmd<-peekByteOff param 0 ::IO UINT
    src<-peekByteOff param 8 ::IO (Ptr UINT)
    res<-peekByteOff param 12 ::IO UINT
    when (res == 0 && cmd == 5) $ do
      store<-readIORef _datesStore_
      fromMaybe (return ()) $ lookup elem store
  return False  

btEvent::Addr->Addr->UINT->Addr->IO Bool
btEvent tag elem evtg param=do
  when (evtg==256) $ do
    cmd<-peekByteOff param 0 ::IO UINT
    res<-peekByteOff param 12 ::IO UINT
    when (res == 1 && cmd == 0) $ do
      store<-readIORef _buttonsStore_
      fromMaybe (return ()) $ lookup elem store
  return False

flatBtEvent::Addr->Addr->UINT->Addr->IO Bool
flatBtEvent tag elem evtg param=do
  when (evtg==256) $ do
    cmd<-peekByteOff param 0 ::IO UINT
    res<-peekByteOff param 12 ::IO UINT
    when (res == 1 && cmd == 0) $ do
      store<-readIORef _flatButtonsStore_
      fromMaybe (return ()) $ lookup elem store
  return False

textEvent::Addr->Addr->UINT->Addr->IO Bool
textEvent tag elem evtg param=do
  when (evtg==2) $ do
     cmd<-peekByteOff param 0 ::IO UINT
     code<-peekByteOff param 8 ::IO UINT
     when (cmd == 1) $ do
       store<-readIORef _textsStore_
       fromMaybe (return ()) $ lookup elem store
       store2<-readIORef _textsStore2_
       case lookup elem store2 of
         Nothing -> return ()
         Just fun -> fun $ fromIntegral code
  return False

menuEvent::Addr->Addr->UINT->Addr->IO Bool
menuEvent tag elem evtg param=do
  when (evtg==256) $ do
    cmd<-peekByteOff param 0 ::IO UINT
    res<-peekByteOff param 12 ::IO UINT
    when (res == 0 && cmd == 128) $ do
      store<-readIORef _menuItemsStore_
      fromMaybe (return ()) $ lookup elem store
  return False

---MavWidgets API

checkError:: String->Either SomeException ()-> IO()
checkError  textError result=
 case result of
  Left e -> message $ textError ++ show e
  Right res -> return ()
  
checkErrorBool:: String->Either SomeException Bool-> IO Bool
checkErrorBool  textError result=
 case result of
  Left e -> do
   message $ textError ++ show e
   return True
  Right res -> return res 

checkSciterError::String->UINT->IO ()
checkSciterError name res= if res==0 then return() else error $ name ++ " return "++ show res

tryE::String->IO()->IO()
tryE  err fun=(try  fun :: IO (Either SomeException ())) >>= checkError err

tryEBool::String->IO Bool->IO Bool
tryEBool  err fun=(try  fun :: IO (Either SomeException Bool)) >>= checkErrorBool err

initGui::String->IO Addr
initGui path=do
  apiPtr
  sciterClass<-sciterClassName
  windowTitle<-newCWString "Maint Sciter Window"
  hwnd<-c_CreateWindowEx 0 sciterClass windowTitle wS_OVERLAPPEDWINDOW 100 100 800 500 nullPtr nullPtr nullPtr nullPtr
  wrap_callBack sciterMainCallBack  >>= sciterSetCallback hwnd
  res<-sciterLoadFile  hwnd path
  unless res $ error "parse file error " 
  showWindow hwnd sW_SHOWNORMAL
  sciterGetRoot  hwnd
  
loadGui::String->Int->Int->IO (HWND, Addr)
loadGui path width height=do
  sciterClass<-sciterClassName
  windowTitle<-newCWString "Sciter Window"
  hwnd<-c_CreateWindowEx 0 sciterClass windowTitle wS_OVERLAPPEDWINDOW 100 100 width height nullPtr nullPtr nullPtr nullPtr
  wrap_callBack sciterCallBack  >>= sciterSetCallback hwnd
  sciterLoadFile  hwnd path
  showWindow hwnd sW_SHOWNORMAL
  root<-sciterGetRoot  hwnd  
  return (hwnd,root)

loopGui :: IO ()
loopGui  = allocaMessage $ \ msg ->
  let pump = do
        c_GetMessage msg nullPtr 0 0
        r<-readIORef exitStore
        when (r/= 5) $ do
          translateMessage msg
          dispatchMessage msg
          pump
  in pump

button::Addr->String->IO Addr
button root css =do
  ptr<-sciterSelectElement  root $ "#" ++ css
  sciterAttachEventHandler ptr btEvent 
  return ptr

buttonOnClick:: Addr->IO ()->IO()
buttonOnClick ptr fun  =  modifyIORef _buttonsStore_ $ (:) (ptr,fun)

checkButton::Addr->String->IO Addr
checkButton root css =do
  ptr<-sciterSelectElement  root $ "#" ++ css
  sciterAttachEventHandler ptr chEvent 
  return ptr

checkButtonIsChecked::Addr->IO Bool
checkButtonIsChecked ptr=do
  res<-sciterGetIntValue ptr
  return  $ res /= 0
  
checkButtonChecked::Addr->Bool->IO ()
checkButtonChecked ptr value=  sciterSetIntValue ptr (if value then 1 else 0) 2 >>=checkSciterError "checkButtonChecked"

checkButtonOnChange:: Addr->IO ()->IO()
checkButtonOnChange ptr fun  =  modifyIORef _checksStore_ $ (:) (ptr,fun)

combo::Addr->String->IO Addr
combo root css =do
  ptr<-sciterSelectElement  root $ "#" ++ css
  sciterSetAttribute ptr "index" "0"
  sciterAttachEventHandler ptr cbEvent
  return ptr

comboOnChange:: Addr->IO ()->IO()
comboOnChange ptr fun  =  modifyIORef _combosStore_ $ (:) (ptr,fun)

comboGetText::Addr->IO ByteString
comboGetText =sciterGetStrValue

comboSetText::Addr->ByteString->IO UINT
comboSetText =sciterSetStrValue

comboGetIndex::Addr->IO Int
comboGetIndex addr=do
  r<-sciterGetAttribute addr "index"
  return $ read r

comboSetIndex::Addr->Int->IO ()
comboSetIndex el n=do
  p<-sciterGetNthChild el 0
  cnt <-sciterGetClildCount p
  sciterSetAttribute el "index" $ show n
  if n<fromIntegral cnt then do
    row<-sciterGetNthChild p $ fromIntegral n
    val<-sciterGetStrValue row
    r<-sciterSetStrValue el  val
    when(r==0) $ sciterSetAttribute el "index" ( show n) >>= checkSciterError "end comboSetIndex"
  else error "comboSetIndexError"
  
comboFill::Addr->[ByteString]->IO ()
comboFill  ptr lst= do
  popup<-sciterGetNthChild ptr 0
  res<-sciterSetHtml  popup $ foldl' (\r el-> r <> "<option>" <> el <> "</option>") "" lst
  when(res/=0) $ error "comboFill"
  comboSetIndex ptr 0
  
comboGetItems::Addr->IO[ByteString]
comboGetItems ptr=do
  popUp<-sciterGetNthChild ptr 0
  cnt<-sciterGetClildCount popUp
  mapM (sciterGetNthChild popUp >=> sciterGetStrValue ) [0..cnt-1]

comboSearch::Addr->[ByteString]->ByteString->Int->IO()
comboSearch ptr lst str  n = forM_ (findIndex (\el->doublePrefixOf str el  n) lst) $ comboSetIndex ptr
  
datePick::Addr->String->IO Addr
datePick root css =do
  ptr<-sciterSelectElement  root $ "#" ++ css
  sciterAttachEventHandler ptr dateEvent
  return ptr

datePickGetText::Addr->IO ByteString
datePickGetText ptr  =do
  r<-sciterGetIn64Value ptr
  let delta=(r-116444628000000000)`div` 10000000
  return $ fromStr $ show $ utctDay  $ posixSecondsToUTCTime $ fromIntegral delta
  
datePickSetText::Addr->ByteString->IO ()
datePickSetText ptr val =do
--  print "start datePickSetText" 
  let ut=read $  toStr val ++" 00:00:00.000000 UTC"::UTCTime
  let sec= round $ utcTimeToPOSIXSeconds ut
  sciterSetInt64Value ptr ( 116444628000000000 + sec * 10000000+864000000000) 6 >>=checkSciterError "end datePickSetText"

datePickOnChange:: Addr->IO ()->IO()
datePickOnChange ptr fun  =  modifyIORef _datesStore_ $ (:) (ptr,fun)

flatButton::Addr->String->IO Addr
flatButton root css =do
  ptr<-sciterSelectElement  root $ "#" ++ css
  sciterAttachEventHandler ptr flatBtEvent
  return ptr

flatButtonOnClick:: Addr->IO ()->IO()
flatButtonOnClick ptr fun  =  modifyIORef _flatButtonsStore_ $ (:) (ptr,fun)

menuItem::Addr->String->IO Addr
menuItem root css =do
  ptr<-sciterSelectElement  root $ "#" ++ css
  sciterAttachEventHandler ptr menuEvent 
  return ptr

menuItemOnClick:: Addr->IO ()->IO()
menuItemOnClick ptr fun  =  modifyIORef _menuItemsStore_ $ (:) (ptr,fun)

menu::Addr->String->IO Addr
menu root css =sciterSelectElement  root $ "#" ++ css

menuShow::Addr->Addr->IO ()
menuShow menu parent=do
  sciterShowPopUp menu parent
  return()

table::Addr->String->IO Addr
table root css =do
  ptr<-sciterSelectElement  root $ "#" ++ css
  sciterSetAttribute ptr "index" "-1"
  sciterAttachEventHandler ptr tableEvent
  return ptr

tableOnClick:: Addr->(Int->IO())->IO()
tableOnClick ptr fun  =  modifyIORef _tablesStore_ $ (:) (ptr,fun)

tableOnDblClick:: Addr->(Int->IO())->IO()
tableOnDblClick ptr fun  =  modifyIORef _tablesStoreDbl_ $ (:) (ptr,fun)

tableOnMenu:: Addr->(Int->IO())->IO()
tableOnMenu ptr fun  =  modifyIORef _tablesStoreMenu_ $ (:) (ptr,fun)

tableGetIndex::Addr->IO Int
tableGetIndex tbl=do
  strIdx<-sciterGetAttribute tbl "index"
  return $ read strIdx::IO Int
  
tableSetIndex::Addr->Int->IO()
tableSetIndex tbl n=do
  strIdx<-sciterGetAttribute tbl "index"
  let idx=read strIdx::Int
  when (idx>=0) $ do
    oldChild<-sciterGetNthChild tbl $ fromIntegral idx
    sciterSetStyleAttribute oldChild "background" "#ffffff"
    return()
  if n>=0 then do
    cnt<-sciterGetClildCount tbl
    when (n<fromIntegral cnt) $ do
      newChild<-sciterGetNthChild tbl $ fromIntegral n
      sciterSetStyleAttribute newChild "background" "#00bbee"
      sciterSetAttribute tbl "index" ( show n) >>=checkSciterError "end tableSetIndex"
  else   sciterSetAttribute tbl "index" "-1"  >>=checkSciterError "end tableSetIndex"


tableScroll::Addr->IO()
tableScroll tbl=do
  strIdx<-sciterGetAttribute tbl "index"
  when(strIdx/="-1") $ do
    let idx=read strIdx::Word32
    row<-sciterGetNthChild tbl idx
    sciterScrollToView row >>=checkSciterError "end tableScroll"
    
tableScrollTo::Addr->Int->IO()
tableScrollTo tbl idx=
  when(idx>0) $ do
    row<-sciterGetNthChild tbl $ fromIntegral idx
    sciterScrollToView row >>=checkSciterError "end tableScroll"    

tableAddRow::Addr->c->[ c->  ByteString]->IO ()
tableAddRow ptr item fns=do
  let rowHtml=rowFill item fns
  cnt<-sciterGetClildCount ptr
  row<-sciterCreateElem "tr"
  r<-sciterInsertElem row ptr cnt
  sciterSetHtml row rowHtml
  tableSetIndex ptr $ fromIntegral cnt
  sciterScrollToView row >>=checkSciterError "tableAddRow"

tableAddRowM::Addr->c->[ c-> IO ByteString]->IO ()
tableAddRowM ptr item fns=do
  rowHtml<-rowFillM item fns
  cnt<-sciterGetClildCount ptr
  row<-sciterCreateElem "tr"
  r<-sciterInsertElem row ptr cnt
  sciterSetHtml row rowHtml
  tableSetIndex ptr $ fromIntegral cnt
  sciterScrollToView row >>=checkSciterError "tableAddRowM"

tableChangeRow::Addr->c->[ c-> ByteString]->IO ()
tableChangeRow ptr item fns=do
  let rowHtml=rowFill item fns
  strIdx<-sciterGetAttribute ptr "index"
  let idx=read strIdx::Int
  ptr'<-sciterGetNthChild ptr $ fromIntegral idx
  sciterSetHtml ptr' rowHtml >>=checkSciterError "tableChangeRow"

tableChangeRowM::Addr->c->[ c-> IO ByteString]->IO ()
tableChangeRowM ptr item fns=do
  rowHtml<-rowFillM item fns
  strIdx<-sciterGetAttribute ptr "index"
  let idx=read strIdx::Int
  ptr'<-sciterGetNthChild ptr $ fromIntegral idx
  sciterSetHtml ptr' rowHtml >>=checkSciterError "tableChangeRowM"

tableDeleteRow::Addr->IO ()
tableDeleteRow ptr=do
  strIdx<-sciterGetAttribute ptr "index"
  let n= read strIdx::Int
  sciterGetNthChild ptr (fromIntegral n) >>= sciterDeleteElem 
  sciterSetAttribute ptr "index" "-1"
  tableSetIndex ptr  $ if n>0 then  n-1 else  0 
  
rowFill:: c->[c->ByteString]->  ByteString
rowFill item = foldl' (\r f-> r <> "<td>" <>  f item <> "</td>") B.empty

tableFill::Addr->[c]->[ c-> ByteString]-> IO UINT
tableFill  ptr store fns=do
  tableSetIndex ptr (-1)
  sciterSetHtml  ptr $ foldl' (\r it-> r <> "<tr>" <> rowFill it fns <> "</tr>") B.empty store 

rowFillM:: c->[c->IO ByteString]-> IO ByteString
rowFillM item = foldM (\r f->do
                              s<-f item
                              return $! r <> "<td>" <>  s <> "</td>") B.empty 

tableFillM::Addr->[c]->[ c->IO ByteString]-> IO ()
tableFillM  ptr store fns=do
  tableSetIndex ptr (-1)
  str<-foldM (\r it->do
                       s<-rowFillM it fns
                       return $! r <> "<tr>" <> s <> "</tr>") B.empty store
  sciterSetHtml  ptr str >>=checkSciterError "end tableFillM"


tableGetRow::Addr->Word32->IO Addr
tableGetRow =sciterGetNthChild

tableSetHeaderText::Addr->Int->ByteString->IO()
tableSetHeaderText ptr n s=do
  row<-sciterGetNthChild ptr 0
  cell<-sciterGetNthChild row $ fromIntegral n
  sciterSetHtml cell s>>=checkSciterError "tableSetHeaderText"
  
tableSearch::Addr->[ByteString]->ByteString->Int->IO()
tableSearch ptr lst str  n = do
  let idx=findIndex (\el->doublePrefixOf str el  n) lst 
  forM_ idx $ tableSetIndex ptr
  forM_ idx $ tableScrollTo ptr
   
text::Addr->String->IO Addr
text root css =do
  ptr<-sciterSelectElement  root $ "#" ++ css
  sciterAttachEventHandler ptr textEvent
  return ptr  

textGetText::Addr->IO ByteString
textGetText =sciterGetStrValue
 
textSetText::Addr->ByteString->IO ()
textSetText el val = sciterSetStrValue el val >>=checkSciterError "textSetText"

textSelectAll::Addr->IO()
textSelectAll ptr=  sciterScriptMethod ptr "doSelectAll" >>=checkSciterError "textSelectAll"

textOnChange:: Addr->IO ()->IO()
textOnChange ptr fun  =  modifyIORef _textsStore_ $ (:) (ptr,fun)

textOnKey:: Addr->(Int->IO())->IO()
textOnKey ptr fun  =  modifyIORef _textsStore2_ $ (:) (ptr,fun)

windowDestroy::HWND->IO() 
windowDestroy =closeWindow

windowSetText::HWND->ByteString->IO() 
windowSetText ptr text =  setWindowText ptr $ toStr text

windowOnEvent =sciterAttachWindowEventHandler

doublePrefixOf:: ByteString->ByteString->Int->Bool
doublePrefixOf s elem  n  =
  let
  str= fromStr $ map toLower  $ toStr elem
  lst=split  0x20  s
  _list=split 0x20 str
  list=if n>0 then tail _list else _list
  in
  case (length lst,length list) of
    (1,_)-> s `isPrefixOf`  head list
    (2,1)-> head lst `isPrefixOf` str
    (2,2)->head lst `isPrefixOf` head list && (lst !! 1) `isPrefixOf` (list !! 1)
    (_,_)->False

widgetSetFocus::Addr->IO()
widgetSetFocus ptr=  sciterSetState ptr 0x00000008 0 False >>=checkSciterError "end widgetSetFocus"

widgetEnable::Addr->IO()
widgetEnable ptr= sciterSetState ptr 0 0x00000080 False >>=checkSciterError "widgetEnable"

widgetDisable::Addr->IO()
widgetDisable ptr= sciterSetState ptr 0x00000080 0 False >>=checkSciterError "widgetDisable"

widgetShow::Addr->IO()
widgetShow ptr= sciterSetStyleAttribute  ptr "display" "block" >>=checkSciterError "widgetShow"

widgetHide::Addr->IO()
widgetHide ptr=  sciterSetStyleAttribute  ptr "display" "none"  >>=checkSciterError "widgetHide"