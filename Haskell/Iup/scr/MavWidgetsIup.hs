{-# LANGUAGE OverloadedStrings #-}
module MavWidgetsIup where


import Data.ByteString (ByteString,split)
import qualified Data.ByteString as B
import Data.IORef
import System.IO.Unsafe
import Foreign.C.Types
import Foreign.C.String
import Foreign.Ptr
import Foreign.Marshal.Array
import Control.Exception
import Control.Monad
import Data.Foldable(forM_)
import Data.List

import Data.List.Split
import Data.Char
import Data.Maybe (fromMaybe)
import Convert

iupMousePos= 65532
iupCurrent= 65531

data IupHandle
type IupPtr=Ptr IupHandle
type IupCallback = IupPtr->  Int -> Ptr Int-> CString ->IO CInt

type GuiPtr=IupPtr
type WinPtr=IupPtr

_menuItemsStore_ :: IORef [(IupPtr,IO())]
{-# NOINLINE _menuItemsStore_ #-}
_menuItemsStore_ = unsafePerformIO (newIORef [])

_checksStore_ :: IORef [(IupPtr,IO())]
{-# NOINLINE _checksStore_ #-}
_checksStore_ = unsafePerformIO (newIORef [])

_flatButtonsStore_ :: IORef [(IupPtr,IO())]
{-# NOINLINE _flatButtonsStore_ #-}
_flatButtonsStore_ = unsafePerformIO (newIORef [])

_buttonsStore_ :: IORef [(IupPtr,IO())]
{-# NOINLINE _buttonsStore_ #-}
_buttonsStore_ = unsafePerformIO (newIORef [])

_combosStore_ :: IORef [(IupPtr,IO())]
{-# NOINLINE _combosStore_ #-}
_combosStore_ = unsafePerformIO (newIORef [])

_listsStore_ :: IORef [(IupPtr,IO())]
{-# NOINLINE _listsStore_ #-}
_listsStore_ = unsafePerformIO (newIORef [])

_datesStore_ :: IORef [(IupPtr,IO())]
{-# NOINLINE _datesStore_ #-}
_datesStore_ = unsafePerformIO (newIORef [])

_textsStore_ :: IORef [(IupPtr,IO())]
{-# NOINLINE _textsStore_ #-}
_textsStore_ = unsafePerformIO (newIORef [])

_textsStoreK_ :: IORef [(IupPtr,Int->IO())]
{-# NOINLINE _textsStoreK_ #-}
_textsStoreK_ = unsafePerformIO (newIORef [])

_tablesStore_ :: IORef [(IupPtr,Int->IO())]
{-# NOINLINE _tablesStore_ #-}
_tablesStore_ = unsafePerformIO (newIORef [])

_tablesStore2_ :: IORef [(IupPtr,Int->IO())]
{-# NOINLINE _tablesStore2_ #-}
_tablesStore2_ = unsafePerformIO (newIORef [])

_tablesStoreM_ :: IORef [(IupPtr,Int->IO())]
{-# NOINLINE _tablesStoreM_ #-}
_tablesStoreM_ = unsafePerformIO (newIORef [])

_tablesStoreK_ :: IORef [(IupPtr,Int->IO())]
{-# NOINLINE _tablesStoreK_ #-}
_tablesStoreK_ = unsafePerformIO (newIORef [])

_buttonClick_::IupCallback
_buttonClick_ ptr _ _ _= do
  store<-readIORef _buttonsStore_
  fromMaybe (return ()) $ lookup ptr store
  return 0

_comboChange_::IupCallback
_comboChange_ ptr _ _ _= do
  store<-readIORef _combosStore_
  fromMaybe (return ()) $ lookup ptr store
  return 0

_dateChange_::IupCallback
_dateChange_ ptr _ _ _= do
  store<-readIORef _datesStore_
  fromMaybe (return ()) $ lookup ptr store
  return 0

_flatButtonClick_::IupCallback
_flatButtonClick_ ptr _ _ _= do
  store<-readIORef _flatButtonsStore_
  fromMaybe (return ()) $ lookup ptr store
  return 0

_listChange_::IupCallback
_listChange_ ptr _ _ _= do
  store<-readIORef _listsStore_
  fromMaybe (return ()) $ lookup ptr store
  return 0
  
_menuItemClick_::IupCallback
_menuItemClick_ ptr _ _ _= do
  store<-readIORef _menuItemsStore_
  fromMaybe (return ()) $ lookup ptr store
  return 0  

_tableKey_::IupCallback
_tableKey_ ptr k _ _= do
  store<-readIORef _tablesStoreK_
  case k of
    65360->do --Home
            tableSetIndex ptr 0
            tableScrollTo ptr 0
    65362->do --Up
            idx<-tableGetIndex ptr
            when (idx>0)  $ tableSetIndex ptr $ idx -1
    65364->do --Down
            max<-liftM toInt $ getAttrStr ptr "NUMLIN" ::IO Int
            idx<-tableGetIndex ptr
            when (idx<max-1)  $ tableSetIndex ptr $ idx +1
    65365->do --PgUp
              vis<-liftM toInt $ getAttrStr ptr "NUMLIN_VISIBLE" ::IO Int
              idx<-tableGetIndex ptr
              let line=if idx-vis >0 then  idx - vis else  0
              tableSetIndex ptr line
              tableScrollTo ptr line
    65366->do --PgDown
              vis<-liftM toInt $ getAttrStr ptr "NUMLIN_VISIBLE" ::IO Int
              max<-liftM toInt $ getAttrStr ptr "NUMLIN" ::IO Int
              idx<-tableGetIndex ptr
              let line=if idx+vis < max-1 then  idx + vis else   max-1
              tableSetIndex ptr line
              tableScrollTo ptr line
    65367->do --End
            max<-getAttrStr ptr "NUMLIN"
            tableSetIndex ptr $ toInt max -1
            tableScrollTo ptr $ toInt max -1
    _->   do
            let r=lookup ptr store
            case r of
                Nothing->return ()
                Just fun-> fun  k
  return (-1)

_tableClick_::IupCallback
_tableClick_ ptr l _ status= do
  when (l>0) $ tableSetIndex ptr $ l-1
  s<-peekCString status
  store<-  case () of _
                        | s!!5=='D' -> readIORef _tablesStore2_
                        | s!!2=='1' -> readIORef _tablesStore_
                        | s!!4=='3' -> readIORef _tablesStoreM_
                        | otherwise -> return []
  let r=lookup ptr store
  case r of
    Nothing->return ()
    Just fun->when (l > 0) $ fun $ l - 1
  return 0
  
_textChange_::IupCallback
_textChange_ ptr _ _ _= do
  store<-readIORef _textsStore_
  fromMaybe (return ()) $ lookup ptr store
  return 0

_textKey_::IupCallback
_textKey_ ptr k _ _= do
  store<-readIORef _textsStoreK_
  let r=lookup ptr store
  case r of
      Nothing->return ()
      Just fun-> fun  k
  return 0  

foreign import ccall "IupOpen" iupOpen::Ptr Int->Ptr Int->IO ()
foreign import ccall "IupControlsOpen" iupControlsOpen::IO ()
foreign import ccall "IupDestroy" iupDestroy::IupPtr->IO ()

foreign import ccall "IupShow" iupShow::IupPtr->IO ()
foreign import ccall "IupPopup" iupPopup::IupPtr->Int->Int->IO ()
foreign import ccall "IupMainLoop" iupMainLoop::IO()
foreign import ccall "IupClose" iupClose::IO()
foreign import ccall "IupSetFocus" iupSetFocus::IupPtr->IO IupPtr
foreign import ccall "IupMessage" iupMessage::CString->CString->IO ()

foreign import ccall "IupVboxv" iupVboxv::Ptr IupPtr->IO IupPtr
foreign import ccall "IupHboxv" iupHboxv::Ptr IupPtr->IO IupPtr
foreign import ccall "IupMenuv" iupMenuv::Ptr IupPtr->IO IupPtr
foreign import ccall "IupSetGlobal" iupSetGlobal::CString->CString->IO()
foreign import ccall "IupGetInt" iupGetInt::IupPtr->CString->IO Int
foreign import ccall "IupGetAttribute" iupGetAttribute::IupPtr->CString->IO CString
foreign import ccall "IupSetStrAttribute" iupSetStrAttribute::IupPtr->CString->CString->IO ()
foreign import ccall "IupSetAttributes" iupSetAttributes::IupPtr->CString->IO ()

foreign import ccall  "wrapper" wrap_callBack :: IupCallback -> IO (FunPtr IupCallback)
foreign import ccall  "IupSetCallback" setCallback :: IupPtr ->CString-> FunPtr IupCallback -> IO CInt

foreign import ccall "IupDialog" iupDialog::IupPtr->IO IupPtr
foreign import ccall "IupLabel" iupLabel::CString->IO IupPtr
foreign import ccall "IupFlatButton" iupFlatButton::CString->CString->IO IupPtr
foreign import ccall "IupItem" iupItem::CString->CString->IO IupPtr
foreign import ccall "IupToggle" iupToggle::CString->CString->IO IupPtr
foreign import ccall "IupButton" iupButton::CString->CString->IO IupPtr
foreign import ccall "IupList" iupList::CString->IO IupPtr
foreign import ccall "IupDatePick" iupDatePick::IO IupPtr
foreign import ccall "IupMatrix" iupMatrix::CString->IO IupPtr
foreign import ccall "IupSubmenu" iupSubmenu::CString->IupPtr->IO IupPtr
foreign import ccall "IupText" iupText::CString->IO IupPtr
foreign import ccall "IupLoadImage" iupLoadImage::CString->IO IupPtr
foreign import ccall "IupSetAttributeHandle" iupSetAttributeHandle::IupPtr->CString->IupPtr->IO IupPtr
foreign import ccall "IupFileDlg" iupFileDlg::IO IupPtr

message::String->IO()
message  mes=do
  title' <- newCStrUtf8 "Повдомлення"
  mes' <- newCStrUtf8 mes  
  iupMessage title' mes' 

someExcepion  :: SomeException -> IO () 
someExcepion e= message "Несподівана помилка"

checkError:: String->Either SomeException ()-> IO()
checkError  textError result=
 case result of
  Left e -> message $ textError ++ show e
  Right res -> return ()

tryE::String->IO()->IO()
tryE  err fun=(try  fun :: IO (Either SomeException ())) >>= checkError err

getAttrInt::IupPtr->String->IO Int
getAttrInt ptr val= newCString val >>= iupGetInt ptr

--getAttrStr'::IupPtr->String->IO String
--getAttrStr' ptr val= newCString val >>= iupGetAttribute ptr >>= peekUt8Str

getAttrStr::IupPtr->String->IO ByteString
getAttrStr ptr val= newCString val >>= iupGetAttribute ptr >>= B.packCString


setAttrStr:: IupPtr->String->ByteString->IO ()
setAttrStr ptr  name value =do
  name' <- newCString name
  B.useAsCString value $ \cs-> iupSetStrAttribute ptr name'  cs  

setAttrs:: IupPtr->String->IO()
setAttrs ptr  val=do
 _val <- newCString val
 iupSetAttributes ptr  _val

setAttrHandle:: IupPtr->String->IupPtr->IO IupPtr
setAttrHandle ptr name h=do
 _name <- newCString name
 iupSetAttributeHandle ptr _name h

widgetSetCallback:: IupPtr->String->IupCallback->IO ()
widgetSetCallback ptr name fun=do
  _name<-newCString name
  wrap_callBack  fun >>= setCallback ptr _name
  return ()  

doublePrefixOf:: ByteString->ByteString->Int->Bool
doublePrefixOf s elem  n  =
  let
  str= fromStr $ map toLower  $ toStr elem
  lst=B.split  0x20  s
  _list=B.split 0x20 str
  list=if n>0 then tail _list else _list
  in
  case (length lst,length list) of
    (1,_)-> s `B.isPrefixOf`  head list
    (2,1)-> head lst `B.isPrefixOf` str
    (2,2)->head lst `B.isPrefixOf` head list && (lst !! 1) `B.isPrefixOf` (list !! 1)
    (_,_)->False

initGui::IO()
initGui=do
  atr<-newCString "UTF8MODE"
  iupOpen nullPtr nullPtr >> newCString "YES" >>= iupSetGlobal  atr
  iupControlsOpen
  
loopGui::IO()
loopGui=iupMainLoop >>iupClose   

hBox::[IupPtr]->ByteString->ByteString->IO IupPtr
hBox list gap margin=do
  ptr<-newArray list >>= iupHboxv 
  setAttrStr ptr "GAP" gap >>  setAttrStr ptr "MARGIN" margin
  return ptr

vBox::[IupPtr]->ByteString->ByteString->IO IupPtr
vBox list gap margin=do
  ptr<-newArray list >>= iupVboxv 
  setAttrStr ptr "GAP" gap >>  setAttrStr ptr "MARGIN" margin
  return ptr

hBoxM::[IO IupPtr]->ByteString->ByteString->IO IupPtr
hBoxM list gap margin=do
  lst <- sequence list >>= newArray
  ptr<-iupHboxv lst
  setAttrStr ptr "GAP" gap
  setAttrStr ptr "MARGIN" margin
  return ptr

vBoxM::[IO IupPtr]->ByteString->ByteString->IO IupPtr
vBoxM list gap margin=do
  ptr <- sequence list >>= newArray >>=iupVboxv
  setAttrStr ptr "GAP" gap
  setAttrStr ptr "MARGIN" margin
  return ptr

window::IupPtr->ByteString->ByteString->IO IupPtr
window child title size=do
  ptr <- iupDialog child
  setAttrStr ptr "TITLE" title >>  setAttrStr ptr "SIZE" size
  return ptr  

windowShow::IupPtr->IO()
windowShow=iupShow

label::ByteString->ByteString->IO IupPtr
label title size =do
  ptr<-B.useAsCString  title $ \cs-> iupLabel cs
  setAttrStr ptr "SIZE" size
  return ptr

button::ByteString->IO IupPtr
button title =
  B.useAsCString title $ \title'-> do
    nullStr<-newCString ""
    ptr<- iupButton title' nullStr
    name<-newCString "ACTION"
    wrap_callBack  _buttonClick_ >>= setCallback ptr name
    return ptr

buttonOnClick:: IupPtr->IO ()->IO()
buttonOnClick ptr fun  =  modifyIORef _buttonsStore_ $ (:) (ptr,fun)

checkButton::ByteString->ByteString->IO IupPtr
checkButton title size=
  B.useAsCString title $ \title' -> do
    nullStr<-newCString ""
    ptr<- iupToggle title' nullStr
    name<-newCString "VALUECHANGED_CB"
    wrap_callBack  _textChange_ >>= setCallback ptr name
    setAttrStr ptr "SIZE" size
    return ptr

checkButtonGetText=windowGetText
checkButtonSetText =windowSetText

checkButtonIsChecked::IupPtr->IO Bool
checkButtonIsChecked ptr=do
  res<-getAttrStr ptr "VALUE"
  return $ res=="ON"

checkButtonSetOnChange:: IupPtr->IO ()->IO()
checkButtonSetOnChange ptr fun  =  modifyIORef _checksStore_ $ (:) (ptr,fun)

combo::ByteString->IO IupPtr
combo size=do
  nullStr<-newCString ""
  ptr<-iupList nullStr
  setAttrStr ptr "SIZE" size
  setAttrStr ptr "DROPDOWN" "YES"
  name<-newCString "VALUECHANGED_CB"
  wrap_callBack  _comboChange_ >>= setCallback ptr name
  return ptr

comboOnChange:: IupPtr->IO ()->IO()
comboOnChange ptr fun  =  modifyIORef _combosStore_ $ (:) (ptr,fun)

comboSetText::IupPtr->ByteString->IO ()
comboSetText ptr = setAttrStr ptr "VALUESTRING"

comboFill=listFill
comboGetText=listGetText
comboGetIndex=listGetIndex
comboSetIndex=listSetIndex

comboSearch::IupPtr->[ByteString]->ByteString->Int->IO()
comboSearch ptr lst str  n = forM_ (findIndex (\el->doublePrefixOf str el  n) lst) $ comboSetIndex ptr

datePick::ByteString->IO IupPtr
datePick size =do
  ptr<-iupDatePick
  setAttrStr ptr "SIZE" size
  setAttrStr ptr "ZEROPRECED" "YES"
  setAttrStr ptr "SEPARATOR" "-"
  name<-newCString "VALUECHANGED_CB"
  wrap_callBack  _dateChange_ >>= setCallback ptr name
  return ptr

datePickGetText::IupPtr->IO ByteString
datePickGetText ptr  = do
  r<-getAttrStr ptr "VALUE"
  let lst= map (\el->if toInt el < 10 then "0" <> el else el) $ B.split 0x2f r --"/"
  return $ head lst <> "-" <> (lst !! 1) <> "-" <> (lst !! 2)

datePickSetText::IupPtr->ByteString->IO ()
datePickSetText ptr s=  textSetText ptr $ B.map (\el-> if el==0x2d then 0x2f else el) s --"-" "/"

datePickOnChange:: IupPtr->IO ()->IO()
datePickOnChange ptr fun  =  modifyIORef _datesStore_ $ (:) (ptr,fun)

flatButton::ByteString->ByteString->IO IupPtr
flatButton title image =do
 nullStr<-newCString ""
 ptr<-iupFlatButton nullStr nullStr
 name<-newCString "FLAT_ACTION"
 wrap_callBack  _flatButtonClick_ >>= setCallback ptr name
 B.useAsCString image $ iupLoadImage >=> setAttrHandle ptr "IMAGE"
 return ptr

flatButtonOnClick:: IupPtr->IO ()->IO()
flatButtonOnClick ptr fun  =  modifyIORef _flatButtonsStore_ $ (:) (ptr,fun)

list::ByteString->IO IupPtr
list size=do
  nullStr<-newCString ""
  ptr<-iupList nullStr
  setAttrStr ptr "SIZE" size
  name<-newCString "VALUECHANGED_CB"
  wrap_callBack  _listChange_ >>= setCallback ptr name
  return ptr

listOnChange:: IupPtr->IO ()->IO()
listOnChange ptr fun  =  modifyIORef _listsStore_ $ (:) (ptr,fun)

listFill::IupPtr->[ByteString]->IO()
listFill ptr lst= do
 mapM_  (\(n,el)->setAttrStr ptr ( show n) el) $ zip [1..] lst
 setAttrStr ptr "VALUE" "1"

listFillBy::IupPtr->[a]->(a->ByteString)->IO()
listFillBy ptr lst fn= do
 mapM_  (\(n,el)->setAttrStr ptr ( show n) (fn el)) $ zip [1..] lst
 setAttrStr ptr "VALUE" "1"

listGetText::IupPtr->IO ByteString
listGetText ptr= getAttrStr ptr "VALUESTRING"

listSetText::IupPtr->ByteString->IO ()
listSetText ptr value =do
  r<- getAttrStr ptr "VALUE"
  setAttrStr ptr (toStr r) value
  setAttrStr ptr "VALUE" r

listGetIndex::IupPtr->IO Int
listGetIndex ptr=do
  r<- getAttrStr ptr "VALUE"
  return  $  toInt r - 1

listSetIndex::IupPtr->Int->IO ()
listSetIndex ptr val = setAttrStr ptr "VALUE" $ fromInt $ val+1

listAddItem::IupPtr->ByteString->IO ()
listAddItem ptr val=do
  setAttrStr ptr "APPENDITEM" val
  getAttrStr ptr "COUNT" >>=  setAttrStr ptr "VALUE"

listDeleteItem::IupPtr->IO ()
listDeleteItem ptr =do
  idx<-getAttrStr ptr "VALUE"
  let n=toInt idx
  when(n>0) $ do
    setAttrStr ptr "REMOVEITEM" idx
    listSetIndex ptr $ if n>1 then n-2 else 0

listExpandVertical::IupPtr->IO()
listExpandVertical ptr =setAttrStr ptr "EXPAND" "VERTICAL"

menu::[IupPtr]->IO IupPtr
menu list =newArray list >>= iupMenuv 

menuShow::IupPtr->IO()
menuShow ptr  =iupPopup ptr iupMousePos iupMousePos
  
menuItem::ByteString->IO IupPtr
menuItem title  = B.useAsCString title $ \title' -> do
 nullStr<-newCString ""
 ptr<-iupItem title' nullStr
 name<-newCString "ACTION"
 wrap_callBack  _menuItemClick_ >>= setCallback ptr name
 return ptr
 
menuItemSetText=windowSetText 

menuItemOnClick:: IupPtr->IO ()->IO()
menuItemOnClick ptr fun  =  modifyIORef _menuItemsStore_ $ (:) (ptr,fun)

subMenu::ByteString->IupPtr->IO IupPtr
subMenu title ptr=B.useAsCString title $ \str->iupSubmenu str ptr

text::ByteString->IO IupPtr
text size=do
  ptr<-newCString "" >>=iupText
  setAttrStr ptr "SIZE" size
  name<-newCString "VALUECHANGED_CB"
  wrap_callBack  _textChange_ >>= setCallback ptr name
  name<-newCString "K_ANY"
  wrap_callBack  _textKey_ >>= setCallback ptr name
  return ptr

textGetText::IupPtr->IO ByteString
textGetText ptr  =  getAttrStr ptr "VALUE"

textSetText::IupPtr->ByteString->IO ()
textSetText ptr = setAttrStr ptr "VALUE"

textSelectAll::IupPtr->IO()
textSelectAll ptr=setAttrStr ptr "SELECTION" "ALL"

textOnChange:: IupPtr->IO ()->IO()
textOnChange ptr fun  =  modifyIORef _textsStore_ $ (:) (ptr,fun)

textOnKey:: IupPtr->(Int->IO())->IO()
textOnKey ptr fun  =  modifyIORef _textsStoreK_ $ (:) (ptr,fun)

table::[ByteString]->IO IupPtr
table list = do
  ptr <- iupMatrix nullPtr
  mapM_ (\(el, idx)->setAttrStr ptr  ("0:" ++ show idx) el) $ zip list [1..]
  setAttrs ptr"NUMLIN_VISIBLE=0,NUMCOL_VISIBLE=0,RESIZEMATRIX=YES,HEIGHT0=8,READONLY=YES,EXPAND=YES"
  setAttrStr ptr "NUMCOL" $ fromInt $ length list
  setAttrStr ptr "INDEX" "-1"
  name<-newCString "CLICK_CB"
  wrap_callBack  _tableClick_ >>= setCallback ptr name
  name<-newCString "K_ANY"
  wrap_callBack  _tableKey_ >>= setCallback ptr name
  return ptr

tableGetIndex::IupPtr->IO Int
tableGetIndex ptr=  liftM  (pred.toInt) $ getAttrStr ptr "INDEX"

tableSetIndex::IupPtr->Int->IO ()
tableSetIndex ptr n=do
  let l=n+1
  idx <- getAttrStr ptr "INDEX"
  when (idx /= "-1") $ do
    setAttrStr ptr ("BGCOLOR" ++ (toStr idx) ++ ":*") "255 255 255"
    setAttrStr ptr "REDRAW" $ "L" <>  idx
  setAttrStr ptr ("BGCOLOR" <> show l <> ":*") "145 201 247"
  setAttrStr ptr "INDEX" $ fromInt l
  setAttrStr ptr "FOCUSCELL" $ fromInt l <> ":1"
  setAttrStr ptr "REDRAW"  $ "L" <>  fromInt l

tableSetColsWith ::IupPtr->[Int]->IO()
tableSetColsWith ptr list=do
  let lst= map(\(el,n)->"WIDTH" ++ show n ++"=" ++show el) $ zip list [1..]
  setAttrs ptr $ intercalate "," lst

tableSetHeaderText::IupPtr->Int->ByteString->IO()
tableSetHeaderText ptr n value=do
  setAttrStr ptr ("0:"++show (n+1)) value
  setAttrStr ptr "REDRAW" "L0"

tableScrollTo :: IupPtr->Int->IO()
tableScrollTo ptr line=do
  sVisible <- getAttrStr ptr "NUMLIN_VISIBLE"
  sOrigine <- getAttrStr ptr "ORIGIN"
  let nVisible=toInt sVisible ::Int
  let nOrigine=toInt $ head $ B.split 0x3a sOrigine --":"
  if line>=nVisible+nOrigine-1 then setAttrStr ptr "ORIGIN" $ fromInt ( line+2-nVisible) <>"*"
    else when (line < nOrigine) $ setAttrStr ptr "ORIGIN" $ fromInt (line + 1) <> "*"

tableRowFill ptr (el,l)  fns =  mapM_ (\(f,c)->setAttrStr ptr (show l++":"++show c) (f el) ) $ zip fns [1..]

tableFill::IupPtr->[a]->[a->ByteString]->IO()
tableFill ptr store fns = do
  setAttrStr ptr "NUMLIN" $ fromInt $ length store
  mapM_ (\el-> tableRowFill ptr el fns) $ zip  store [1..]

tableRowFillM::IupPtr->(a,Int)->[a->IO ByteString]->IO()  
tableRowFillM ptr (el,l)  fns =  mapM_ (\(f,c)->f el >>= setAttrStr ptr (show l <> ":" <> show c)  ) $ zip fns [1..]

tableFillM::IupPtr->[a]->[a->IO ByteString]->IO()
tableFillM ptr store fns =do
 setAttrStr ptr "NUMLIN" $ fromInt $ length store
 mapM_ (\el-> tableRowFillM ptr el fns) $ zip  store [1..]

tableAddRow::IupPtr->a->[a->ByteString]->IO()
tableAddRow ptr item fns=do
  l<-getAttrStr ptr "NUMLIN"
  setAttrStr ptr "ADDLIN"  l
  tableRowFill ptr (item,toInt l+1) fns
  tableSetIndex ptr $ toInt l
  tableScrollTo ptr $ toInt l

tableAddRowM::IupPtr->a->[a->IO ByteString]->IO()
tableAddRowM ptr item fns=do
  l<-getAttrStr ptr "NUMLIN"
  setAttrStr ptr "ADDLIN"  l
  tableRowFillM ptr (item,toInt l+1) fns
  tableSetIndex ptr $ toInt l
  tableScrollTo ptr $ toInt l

tableChangeRow::IupPtr->a->[a->ByteString]->IO()
tableChangeRow ptr item fns=do
  l<-getAttrStr ptr "INDEX"
  let n=toInt l
  tableRowFill ptr (item,n) fns
  setAttrStr ptr "REDRAW" $ "L" <>  fromInt n

tableChangeRowM::IupPtr->a->[a->IO ByteString]->IO()
tableChangeRowM ptr item fns=do
  l<-getAttrStr ptr "INDEX"
  let n=toInt l::Int
  tableRowFillM ptr (item,n) fns
  setAttrStr ptr "REDRAW" $ "L" <>  fromInt n

tableDeleteRow::IupPtr->IO()
tableDeleteRow ptr =do
  l <- getAttrStr ptr "INDEX"
  let n=toInt l
  when(n>0) $ do
    setAttrStr ptr "DELLIN" l
    if n>1 then tableSetIndex ptr (n-2) else tableSetIndex ptr 0

tableOnClick:: IupPtr->(Int->IO())->IO()
tableOnClick ptr fun  =  modifyIORef _tablesStore_ $ (:) (ptr,fun)

tableOnDblClick:: IupPtr->(Int->IO())->IO()
tableOnDblClick ptr fun  =  modifyIORef _tablesStore2_ $ (:) (ptr,fun)

tableOnMenu:: IupPtr->(Int->IO())->IO()
tableOnMenu ptr fun  =  modifyIORef _tablesStoreM_ $ (:) (ptr,fun)

tableOnKey:: IupPtr->(Int->IO())->IO()
tableOnKey ptr fun  =  modifyIORef _tablesStoreK_ $ (:) (ptr,fun)

tableSearch::IupPtr->[ByteString]->ByteString->Int->IO()
tableSearch ptr lst str  n = do
  let idx=findIndex (\el->doublePrefixOf str el  n) lst
  forM_ idx $ tableSetIndex ptr
  forM_ idx $ tableScrollTo ptr

directoryDialog::IO ByteString
directoryDialog=do
  ptr<-iupFileDlg
  setAttrStr ptr "DIALOGTYPE" "DIR"
  iupPopup ptr iupCurrent iupCurrent
  status<-getAttrInt ptr "STATUS"
  if status /= -1 then getAttrStr ptr "VALUE" else return ""


windowGetText::IupPtr->IO ByteString
windowGetText ptr = getAttrStr ptr "TITLE"

windowSetText::IupPtr->ByteString->IO ()
windowSetText ptr = setAttrStr ptr "TITLE"

windowMenu::IupPtr->IupPtr->IO IupPtr
windowMenu ptr = setAttrHandle ptr "MENU"

windowDestroy=iupDestroy

widgetSetSize:: IupPtr->ByteString->IO()
widgetSetSize ptr = setAttrStr ptr "SIZE"

widgetEnable:: IupPtr->IO()
widgetEnable ptr =setAttrStr ptr "ACTIVE" "YES"

widgetDisable:: IupPtr->IO()
widgetDisable ptr =setAttrStr ptr "ACTIVE" "NO"

widgetsExpandHorizontal::[IupPtr]->IO()
widgetsExpandHorizontal = mapM_  $ \el->setAttrStr el "EXPAND" "HORIZONTAL"

widgetSetFocus ptr=do
 iupSetFocus ptr
 return()
  
