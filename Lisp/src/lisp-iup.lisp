(ql:quickload :cffi)
(defpackage :lisp-iup
    (:use :common-lisp :cffi)
	(:export
		:IupOpen
		:IupClose
		:IupImageLibOpen

		:IupMainLoop
		:IupLoopStep
		:IupLoopStepWait
		:IupMainLoopLevel
		:IupFlush
		:IupExitLoop

		:IupRecordInput
		:IupPlayInput

		:IupUpdate
		:IupUpdateChildren
		:IupRedraw
		:IupRefresh
		:IupRefreshChildren

		:IupHelp
		:IupLoad
		:IupLoadBuffer

		:IupVersion
		:IupVersionDate
		:IupVersionNumber
		:IupSetLanguage
		:IupGetLanguage

		:IupDestroy
		:IupDetach
		:IupAppend
		:IupInsert
		:IupGetChild
		:IupGetChildPos
		:IupGetChildCount
		:IupGetNextChild
		:IupGetBrother
		:IupGetParent
		:IupGetDialog
		:IupGetDialogChild
		:IupReparent

		:IupPopup
		:IupShow
		:IupShowXY
		:IupHide
		:IupMap
		:IupUnmap

		:IupSetStrAttribute
		:IupSetAttribute
		:IupStoreAttribute
		:IupSetAttributes
		:IupGetAttribute
		:IupGetAttributes
		:IupGetInt
		:IupGetInt2
		:IupGetIntInt
		:IupGetFloat
		:IupSetfAttribute
		:IupResetAttribute
		:IupGetAllAttributes
		:IupSetAtt

		:IupSetAttributeId
		:IupStoreAttributeId
		:IupGetAttributeId
		:IupGetFloatId
		:IupGetIntId
		:IupSetfAttributeId

		:IupSetAttributeId2
		:IupStoreAttributeId2
		:IupGetAttributeId2
		:IupGetIntId2
		:IupGetFloatId2
		:IupSetfAttributeId2

		:IupSetGlobal
		:IupStoreGlobal
		:IupGetGlobal

		:IupSetFocus
		:IupGetFocus
		:IupPreviousField
		:IupNextField

		:IupGetCallback
		:IupSetCallback
		:IupSetCallbacks

		:IupGetFunction
		:IupSetFunction
		:IupGetActionName

		:IupGetHandle
		:IupSetHandle
		:IupGetAllNames
		:IupGetAllDialogs
		:IupGetName

		:IupSetAttributeHandle
		:IupGetAttributeHandle

		:IupGetClassName
		:IupGetClassType
		:IupGetAllClasses
		:IupGetClassAttributes
		:IupGetClassCallbacks
		:IupSaveClassAttributes
		:IupCopyClassAttributes
		:IupSetClassDefaultAttribute
		:IupClassMatch

		:IupCreate
		:IupCreatev
		:IupCreatep

		;;---------------------------------------------
		;; Elements                                     
		;;---------------------------------------------
		:IupFill
		:IupRadio
		:IupVbox
		:IupVboxv
		:IupZbox
		:IupZboxv
		:IupHbox
		:IupHboxv

		:IupNormalizer
		:IupNormalizerv

		:IupCbox
		:IupCboxv
		:IupSbox
		:IupSplit
		:IupScrollBox
		:IupGridBox
		:IupGridBoxv
		:IupExpander

		:IupFrame

		:IupImage
		:IupImageRGB
		:IupImageRGBA

		:IupItem
		:IupSubmenu
		:IupSeparator
		:IupMenu
		:IupMenuv

		:IupButton
		:IupCanvas
		:IupDialog
		:IupUser
		:IupLabel
		:IupList
		:IupText
		:IupMultiLine
		:IupToggle
		:IupTimer
		:IupClipboard
		:IupProgressBar
		:IupVal
		:IupTabs
		:IupTabsv
		:IupTree
		:IupLink
		;; Deprecated controls, use SPIN attribute of IupText
		:IupSpin
		:IupSpinbox
		;; IupImage utility 
		:IupSaveImageAsText
		;; IupText and IupScintilla utilities 
		:IupTextConvertLinColToPos
		:IupTextConvertPosToLinCol
		;; IupText, IupList, IupTree, IupMatrix and IupScintilla utility 
		:IupConvertXYToPos


		;;---------------------------------------------
		;; Pre-definided dialogs                          
		;;---------------------------------------------
		:IupFileDlg
		:IupMessageDlg
		:IupColorDlg
		:IupFontDlg

		:IupGetFile
		:IupMessage
		:IupMessagef
		:IupAlarm
		:IupScanf
		:IupListDialog
		:IupGetText
		:IupGetColor

		:IupGetParam
		:IupGetParamv

		:IupLayoutDialog
		:IupElementPropertiesDialog

		;;---------------------------------------------
		;; CONSTANTS
		;;---------------------------------------------
		;; Common Return Values                              
		:IUP_ERROR
		:IUP_NOERROR
		:IUP_OPENED
		:IUP_INVALID
		;;---------------------------------------------
		;; Callback Return Values                            
		:IUP_IGNORE
		:IUP_DEFAULT
		:IUP_CLOSE
		:IUP_CONTINUE
		;;---------------------------------------------
		;; IupPopup and IupShowXY Parameter Values                   
		:IUP_CENTER
		:IUP_LEFT
		:IUP_RIGHT
		:IUP_MOUSEPOS
		:IUP_CURRENT
		:IUP_CENTERPARENT
		:IUP_TOP
		:IUP_BOTTOM
		;;---------------------------------------------
		;; Mouse Button Values and Macros                        
		:IUP_BUTTON1
		:IUP_BUTTON2
		:IUP_BUTTON3
		:IUP_BUTTON4
		:IUP_BUTTON5
		;;---------------------------------------------
		;; Pre-Defined Masks                              
		:IUP_MASK_FLOAT
		:IUP_MASK_UFLOAT
		:IUP_MASK_EFLOAT
		:IUP_MASK_INT
		:IUP_MASK_UINT
		;;---------------------------------------------
		;; IupGetParam Callback situations                   
		:IUP_GETPARAM_OK
		:IUP_GETPARAM_INIT
		:IUP_GETPARAM_CANCEL
		:IUP_GETPARAM_HELP

		;;---------------------------------------------
		;; Enums
		;;---------------------------------------------
		;; SHOW_CB Callback Values                               
		:IUP_SHOW :IUP_RESTORE :IUP_MINIMIZE :IUP_MAXIMIZE :IUP_HIDE
		;;---------------------------------------------
		;; SCROLL_CB Callback Values                             
		:IUP_SBUP :IUP_SBDN :IUP_SBPGUP :IUP_SBPGDN :IUP_SBPOSV :IUP_SBDRAGV
		:IUP_SBLEFT :IUP_SBRIGHT :IUP_SBPGLEFT :IUP_SBPGRIGHT :IUP_SBPOSH :IUP_SBDRAGH
		;;---------------------------------------------
		;; Record Input Modes                                
		:IUP_RECBINARY :IUP_RECTEXT

		:iup-open
		:with-iup
		:iup-defcallback
		:iup-defcallback-default

		:iup-register-event
		:iup-defevent
		:iup-defevent-default
		:iup-set-all-events

		:iup-lambda-callback
		:iup-hbox
		:iup-vbox
		:iup-grid-box
		:iup-tabs
		
		:start-gui
		))

	
(in-package :lisp-iup)	


(define-foreign-library iup
    (:unix "libiup.so")
    (T (:default "iup")))

(use-foreign-library iup)

;;--------------------------------------------------------------------------------------
;;======================================================================================
;; Main API
;;======================================================================================
;;--------------------------------------------------------------------------------------
(defcfun ("IupOpen" IupOpen) :int
  (argc :pointer)
  (argv :pointer))

(defcfun ("IupClose" IupClose) :void)

;;--------------------------------------------------------------------------------------
(defcfun ("IupMainLoop" IupMainLoop) :int)

(defcfun ("IupLoopStep" IupLoopStep) :int)

(defcfun ("IupLoopStepWait" IupLoopStepWait) :int)

(defcfun ("IupMainLoopLevel" IupMainLoopLevel) :int)

(defcfun ("IupFlush" IupFlush) :void)

(defcfun ("IupExitLoop" IupExitLoop) :void)
;;--------------------------------------------------------------------------------------
(defcfun ("IupRecordInput" IupRecordInput) :int
  (filename :string)
  (mode :int))

(defcfun ("IupPlayInput" IupPlayInput) :int
  (filename :string))
;;--------------------------------------------------------------------------------------
(defcfun ("IupUpdate" IupUpdate) :void
  (ih :pointer))

(defcfun ("IupUpdateChildren" IupUpdateChildren) :void
  (ih :pointer))

(defcfun ("IupRedraw" IupRedraw) :void
  (ih :pointer)
  (children :int))

(defcfun ("IupRefresh" IupRefresh) :void
  (ih :pointer))

(defcfun ("IupRefreshChildren" IupRefreshChildren) :void
  (ih :pointer))
;;--------------------------------------------------------------------------------------

(defcfun ("IupHelp" IupHelp) :int
  (url :string))

(defcfun ("IupLoad" IupLoad) :string
  (filename :string))

(defcfun ("IupLoadBuffer" IupLoadBuffer) :string
  (buffer :string))
;;--------------------------------------------------------------------------------------
(defcfun ("IupVersion" IupVersion) :string)

(defcfun ("IupVersionDate" IupVersionDate) :string)

(defcfun ("IupVersionNumber" IupVersionNumber) :int)

(defcfun ("IupSetLanguage" IupSetLanguage) :void
  (lng :string))

(defcfun ("IupGetLanguage" IupGetLanguage) :string)
;;--------------------------------------------------------------------------------------
(defcfun ("IupDestroy" IupDestroy) :void
  (ih :pointer))

(defcfun ("IupDetach" IupDetach) :void
  (child :pointer))

(defcfun ("IupAppend" IupAppend) :pointer
  (ih :pointer)
  (child :pointer))

(defcfun ("IupInsert" IupInsert) :pointer
  (ih :pointer)
  (ref_child :pointer)
  (child :pointer))

(defcfun ("IupGetChild" IupGetChild) :pointer
  (ih :pointer)
  (pos :int))

(defcfun ("IupGetChildPos" IupGetChildPos) :int
  (ih :pointer)
  (child :pointer))

(defcfun ("IupGetChildCount" IupGetChildCount) :int
  (ih :pointer))

(defcfun ("IupGetNextChild" IupGetNextChild) :pointer
  (ih :pointer)
  (child :pointer))

(defcfun ("IupGetBrother" IupGetBrother) :pointer
  (ih :pointer))

(defcfun ("IupGetParent" IupGetParent) :pointer
  (ih :pointer))

(defcfun ("IupGetDialog" IupGetDialog) :pointer
  (ih :pointer))

(defcfun ("IupGetDialogChild" IupGetDialogChild) :pointer
  (ih :pointer)
  (name :string))

(defcfun ("IupReparent" IupReparent) :int
  (ih :pointer)
  (new_parent :pointer)
  (ref_child :pointer))
;;--------------------------------------------------------------------------------------
(defcfun ("IupPopup" IupPopup) :int
  (ih :pointer)
  (x :int)
  (y :int))

(defcfun ("IupShow" IupShow) :int
  (ih :pointer))

(defcfun ("IupShowXY" IupShowXY) :int
  (ih :pointer)
  (x :int)
  (y :int))

(defcfun ("IupHide" IupHide) :int
  (ih :pointer))

(defcfun ("IupMap" IupMap) :int
  (ih :pointer))

(defcfun ("IupUnmap" IupUnmap) :void
  (ih :pointer))
;;--------------------------------------------------------------------------------------
(defcfun ("IupSetStrAttribute" IupSetStrAttribute) :void
  (ih :pointer)
  (name :string)
  (value :string))
  
(defcfun ("IupSetAttribute" IupSetAttribute) :void
  (ih :pointer)
  (name :string)
  (value :string))

(defcfun ("IupStoreAttribute" IupStoreAttribute) :void
  (ih :pointer)
  (name :string)
  (value :string))

(defcfun ("IupSetAttributes" IupSetAttributes) :pointer
  (ih :pointer)
  (str :string))

(defcfun ("IupGetAttribute" IupGetAttribute) :string
  (ih :pointer)
  (name :string))

(defcfun ("IupGetAttributes" IupGetAttributes) :string
  (ih :pointer))

(defcfun ("IupGetInt" IupGetInt) :int
  (ih :pointer)
  (name :string))

(defcfun ("IupGetInt2" IupGetInt2) :int
  (ih :pointer)
  (name :string))

(defcfun ("IupGetIntInt" IupGetIntInt) :int
  (ih :pointer)
  (name :string)
  (i1 :pointer)
  (i2 :pointer))

(defcfun ("IupGetFloat" IupGetFloat) :float
  (ih :pointer)
  (name :string))

(defcfun ("IupSetfAttribute" IupSetfAttribute) :void
  (ih :pointer)
  (name :string)
  (format :string)
  &rest)

(defcfun ("IupResetAttribute" IupResetAttribute) :void
  (ih :pointer)
  (name :string))

(defcfun ("IupGetAllAttributes" IupGetAllAttributes) :int
  (ih :pointer)
  (names :pointer)
  (n :int))

(defcfun ("IupSetAtt" IupSetAtt) :pointer
  (handle_name :string)
  (ih :pointer)
  (name :string)
  &rest)
;;--------------------------------------------------------------------------------------
(defcfun ("IupSetAttributeId" IupSetAttributeId) :void
  (ih :pointer)
  (name :string)
  (id :int)
  (value :string))

(defcfun ("IupStoreAttributeId" IupStoreAttributeId) :void
  (ih :pointer)
  (name :string)
  (id :int)
  (value :string))

(defcfun ("IupGetAttributeId" IupGetAttributeId) :string
  (ih :pointer)
  (name :string)
  (id :int))

(defcfun ("IupGetFloatId" IupGetFloatId) :float
  (ih :pointer)
  (name :string)
  (id :int))

(defcfun ("IupGetIntId" IupGetIntId) :int
  (ih :pointer)
  (name :string)
  (id :int))

(defcfun ("IupSetfAttributeId" IupSetfAttributeId) :void
  (ih :pointer)
  (name :string)
  (id :int)
  (format :string)
  &rest)
;;--------------------------------------------------------------------------------------
(defcfun ("IupSetAttributeId2" IupSetAttributeId2) :void
  (ih :pointer)
  (name :string)
  (lin :int)
  (col :int)
  (value :string))

(defcfun ("IupStoreAttributeId2" IupStoreAttributeId2) :void
  (ih :pointer)
  (name :string)
  (lin :int)
  (col :int)
  (value :string))

(defcfun ("IupGetAttributeId2" IupGetAttributeId2) :string
  (ih :pointer)
  (name :string)
  (lin :int)
  (col :int))

(defcfun ("IupGetIntId2" IupGetIntId2) :int
  (ih :pointer)
  (name :string)
  (lin :int)
  (col :int))

(defcfun ("IupGetFloatId2" IupGetFloatId2) :float
  (ih :pointer)
  (name :string)
  (lin :int)
  (col :int))

(defcfun ("IupSetfAttributeId2" IupSetfAttributeId2) :void
  (ih :pointer)
  (name :string)
  (lin :int)
  (col :int)
  (format :string)
  &rest)
;;--------------------------------------------------------------------------------------
(defcfun ("IupSetGlobal" IupSetGlobal) :void
  (name :string)
  (value :string))

(defcfun ("IupStoreGlobal" IupStoreGlobal) :void
  (name :string)
  (value :string))

(defcfun ("IupGetGlobal" IupGetGlobal) :string
  (name :string))
;;--------------------------------------------------------------------------------------
(defcfun ("IupSetFocus" IupSetFocus) :pointer
  (ih :pointer))

(defcfun ("IupGetFocus" IupGetFocus) :pointer)

(defcfun ("IupPreviousField" IupPreviousField) :pointer
  (ih :pointer))

(defcfun ("IupNextField" IupNextField) :pointer
  (ih :pointer))
;;--------------------------------------------------------------------------------------
(defcfun ("IupGetCallback" IupGetCallback) :pointer
  (ih :pointer)
  (name :string))

(defcfun ("IupSetCallback" IupSetCallback) :pointer
  (ih :pointer)
  (name :string)
  (func :pointer))

(defcfun ("IupSetCallbacks" IupSetCallbacks) :pointer
  (ih :pointer)
  (name :string)
  (func :pointer)
  &rest)
;;--------------------------------------------------------------------------------------
(defcfun ("IupGetFunction" IupGetFunction) :pointer
  (name :string))

(defcfun ("IupSetFunction" IupSetFunction) :pointer
  (name :string)
  (func :pointer))

;;--------------------------------------------------------------------------------------
(defcfun ("IupGetHandle" IupGetHandle) :pointer
  (name :string))

(defcfun ("IupSetHandle" IupSetHandle) :pointer
  (name :string)
  (ih :pointer))

(defcfun ("IupGetAllNames" IupGetAllNames) :int
  (names :pointer)
  (n :int))

(defcfun ("IupGetAllDialogs" IupGetAllDialogs) :int
  (names :pointer)
  (n :int))

(defcfun ("IupGetName" IupGetName) :string
  (ih :pointer))
;;--------------------------------------------------------------------------------------
(defcfun ("IupSetAttributeHandle" IupSetAttributeHandle) :void
  (ih :pointer)
  (name :string)
  (ih_named :pointer))

(defcfun ("IupGetAttributeHandle" IupGetAttributeHandle) :pointer
  (ih :pointer)
  (name :string))
;;--------------------------------------------------------------------------------------
(defcfun ("IupGetClassName" IupGetClassName) :string
  (ih :pointer))

(defcfun ("IupGetClassType" IupGetClassType) :string
  (ih :pointer))

(defcfun ("IupGetAllClasses" IupGetAllClasses) :int
  (names :pointer)
  (n :int))

(defcfun ("IupGetClassAttributes" IupGetClassAttributes) :int
  (classname :string)
  (names :pointer)
  (n :int))

(defcfun ("IupGetClassCallbacks" IupGetClassCallbacks) :int
  (classname :string)
  (names :pointer)
  (n :int))

(defcfun ("IupSaveClassAttributes" IupSaveClassAttributes) :void
  (ih :pointer))

(defcfun ("IupCopyClassAttributes" IupCopyClassAttributes) :void
  (src_ih :pointer)
  (dst_ih :pointer))

(defcfun ("IupSetClassDefaultAttribute" IupSetClassDefaultAttribute) :void
  (classname :string)
  (name :string)
  (value :string))

(defcfun ("IupClassMatch" IupClassMatch) :int
  (ih :pointer)
  (classname :string))
;;--------------------------------------------------------------------------------------
(defcfun ("IupCreate" IupCreate) :pointer
  (classname :string))

(defcfun ("IupCreatev" IupCreatev) :pointer
  (classname :string)
  (params :pointer))

(defcfun ("IupCreatep" IupCreatep) :pointer
  (classname :string)
  (first :pointer)
  &rest)
;;--------------------------------------------------------------------------------------
;;======================================================================================
;; Elements
;;======================================================================================
;;--------------------------------------------------------------------------------------
(defcfun ("IupFill" IupFill) :pointer)

(defcfun ("IupRadio" IupRadio) :pointer
  (child :pointer))

(defcfun ("IupVbox" IupVbox) :pointer
  (child :pointer)
  &rest)

(defcfun ("IupVboxv" IupVboxv) :pointer
  (children :pointer))

(defcfun ("IupZbox" IupZbox) :pointer
  (child :pointer)
  &rest)

(defcfun ("IupZboxv" IupZboxv) :pointer
  (children :pointer))

(defcfun ("IupHbox" IupHbox) :pointer
  (child :pointer)
  &rest)

(defcfun ("IupHboxv" IupHboxv) :pointer
  (children :pointer))
;;--------------------------------------------------------------------------------------
(defcfun ("IupNormalizer" IupNormalizer) :pointer
  (ih_first :pointer)
  &rest)

(defcfun ("IupNormalizerv" IupNormalizerv) :pointer
  (ih_list :pointer))
;;--------------------------------------------------------------------------------------
(defcfun ("IupCbox" IupCbox) :pointer
  (child :pointer)
  &rest)

(defcfun ("IupCboxv" IupCboxv) :pointer
  (children :pointer))

(defcfun ("IupSbox" IupSbox) :pointer
  (child :pointer))

(defcfun ("IupSplit" IupSplit) :pointer
  (child1 :pointer)
  (child2 :pointer))

(defcfun ("IupScrollBox" IupScrollBox) :pointer
  (child :pointer))

(defcfun ("IupGridBox" IupGridBox) :pointer
  (child :pointer)
  &rest)

(defcfun ("IupGridBoxv" IupGridBoxv) :pointer
  (children :pointer))

(defcfun ("IupExpander" IupExpander) :pointer
  (child :pointer))
;;--------------------------------------------------------------------------------------
(defcfun ("IupFrame" IupFrame) :pointer
  (child :pointer))
;;--------------------------------------------------------------------------------------
(defcfun ("IupImage" IupImage) :pointer
  (width :int)
  (height :int)
  (pixmap :pointer))

(defcfun ("IupImageRGB" IupImageRGB) :pointer
  (width :int)
  (height :int)
  (pixmap :pointer))

(defcfun ("IupImageRGBA" IupImageRGBA) :pointer
  (width :int)
  (height :int)
  (pixmap :pointer))
;;--------------------------------------------------------------------------------------
(defcfun ("IupItem" IupItem) :pointer
  (title :string)
  (action :string))

(defcfun ("IupSubmenu" IupSubmenu) :pointer
  (title :string)
  (child :pointer))

(defcfun ("IupSeparator" IupSeparator) :pointer)

(defcfun ("IupMenu" IupMenu) :pointer
  (child :pointer)
  &rest)

(defcfun ("IupMenuv" IupMenuv) :pointer
  (children :pointer))
;;--------------------------------------------------------------------------------------
(defcfun ("IupButton" IupButton) :pointer
  (title :string)
  (action :string))

(defcfun ("IupCanvas" IupCanvas) :pointer
  (action :string))

(defcfun ("IupDialog" IupDialog) :pointer
  (child :pointer))

(defcfun ("IupUser" IupUser) :pointer)

(defcfun ("IupLabel" IupLabel) :pointer
  (title :string))

(defcfun ("IupList" IupList) :pointer
  (action :string))

(defcfun ("IupText" IupText) :pointer
  (action :string))

(defcfun ("IupMultiLine" IupMultiLine) :pointer
  (action :string))

(defcfun ("IupToggle" IupToggle) :pointer
  (title :string)
  (action :string))

(defcfun ("IupTimer" IupTimer) :pointer)

(defcfun ("IupClipboard" IupClipboard) :pointer)

(defcfun ("IupProgressBar" IupProgressBar) :pointer)

(defcfun ("IupVal" IupVal) :pointer
  (type :string))

(defcfun ("IupTabs" IupTabs) :pointer
  (child :pointer)
  &rest)

(defcfun ("IupTabsv" IupTabsv) :pointer
  (children :pointer))

(defcfun ("IupTree" IupTree) :pointer)

(defcfun ("IupLink" IupLink) :pointer
  (url :string)
  (title :string))
;;--------------------------------------------------------------------------------------
(defcfun ("IupSpin" IupSpin) :pointer)

(defcfun ("IupSpinbox" IupSpinbox) :pointer
  (child :pointer))
;;--------------------------------------------------------------------------------------
(defcfun ("IupSaveImageAsText" IupSaveImageAsText) :int
  (ih :pointer)
  (file_name :string)
  (format :string)
  (name :string))
;;--------------------------------------------------------------------------------------
(defcfun ("IupTextConvertLinColToPos" IupTextConvertLinColToPos) :void
  (ih :pointer)
  (lin :int)
  (col :int)
  (pos :pointer))

(defcfun ("IupTextConvertPosToLinCol" IupTextConvertPosToLinCol) :void
  (ih :pointer)
  (pos :int)
  (lin :pointer)
  (col :pointer))
;;--------------------------------------------------------------------------------------
(defcfun ("IupConvertXYToPos" IupConvertXYToPos) :int
  (ih :pointer)
  (x :int)
  (y :int))


;;======================================================================================
;; Pre-defined dialogs
;;======================================================================================
;;--------------------------------------------------------------------------------------
(defcfun ("IupFileDlg" IupFileDlg) :pointer)

(defcfun ("IupMessageDlg" IupMessageDlg) :pointer)

(defcfun ("IupColorDlg" IupColorDlg) :pointer)

(defcfun ("IupFontDlg" IupFontDlg) :pointer)
;;--------------------------------------------------------------------------------------
(defcfun ("IupGetFile" IupGetFile) :int
  (arq :string))

(defcfun ("IupMessage" IupMessage) :void
  (title :string)
  (msg :string))

(defcfun ("IupMessagef" IupMessagef) :void
  (title :string)
  (format :string)
  &rest)

(defcfun ("IupAlarm" IupAlarm) :int
  (title :string)
  (msg :string)
  (b1 :string)
  (b2 :string)
  (b3 :string))

(defcfun ("IupScanf" IupScanf) :int
  (format :string)
  &rest)

(defcfun ("IupListDialog" IupListDialog) :int
  (type :int)
  (title :string)
  (size :int)
  (list :pointer)
  (op :int)
  (max_col :int)
  (max_lin :int)
  (marks :pointer))

(defcfun ("IupGetText" IupGetText) :int
  (title :string)
  (text :string))

(defcfun ("IupGetColor" IupGetColor) :int
  (x :int)
  (y :int)
  (r :pointer)
  (g :pointer)
  (b :pointer))
;;--------------------------------------------------------------------------------------
(defcfun ("IupGetParam" IupGetParam) :int
  (title :string)
  (action :pointer)
  (user_data :pointer)
  (format :string)
  &rest)

(defcfun ("IupGetParamv" IupGetParamv) :int
  (title :string)
  (action :pointer)
  (user_data :pointer)
  (format :string)
  (param_count :int)
  (param_extra :int)
  (param_data :pointer))
;;--------------------------------------------------------------------------------------
(defcfun ("IupLayoutDialog" IupLayoutDialog) :pointer
  (dialog :pointer))

(defcfun ("IupElementPropertiesDialog" IupElementPropertiesDialog) :pointer
  (elem :pointer))
;;--------------------------------------------------------------------------------------
;;======================================================================================
;; Constants
;;======================================================================================
;;--------------------------------------------------------------------------------------
;; Common Return Values 
(defconstant IUP_ERROR     1)
(defconstant IUP_NOERROR   0)
(defconstant IUP_OPENED   -1)
(defconstant IUP_INVALID  -1)
;;--------------------------------------------------------------------------------------
;; Callback Return Values            
(defconstant IUP_IGNORE    -1)
(defconstant IUP_DEFAULT   -2)
(defconstant IUP_CLOSE     -3)
(defconstant IUP_CONTINUE  -4)
;;--------------------------------------------------------------------------------------
;; IupPopup and IupShowXY Parameter Values
(defconstant IUP_CENTER        #xFFFF)
(defconstant IUP_LEFT          #xFFFE)
(defconstant IUP_RIGHT         #xFFFD)
(defconstant IUP_MOUSEPOS      #xFFFC)
(defconstant IUP_CURRENT       #xFFFB)
(defconstant IUP_CENTERPARENT  #xFFFA)
(defconstant IUP_TOP       IUP_LEFT)
(defconstant IUP_BOTTOM    IUP_RIGHT)
;;--------------------------------------------------------------------------------------
; Mouse Button Values and Macros                         
(defconstant IUP_BUTTON1   #\1)
(defconstant IUP_BUTTON2   #\2)
(defconstant IUP_BUTTON3   #\3)
(defconstant IUP_BUTTON4   #\4)
(defconstant IUP_BUTTON5   #\5)
;;--------------------------------------------------------------------------------------
; Pre-Defined Masks                               
(defparameter IUP_MASK_FLOAT    "[+/-]?(/d+/.?/d*|/./d+)")
(defparameter IUP_MASK_UFLOAT   "(/d+/.?/d*|/./d+)")
(defparameter IUP_MASK_EFLOAT   "[+/-]?(/d+/.?/d*|/./d+)([eE][+/-]?/d+)?")
(defparameter IUP_MASK_INT      "[+/-]?/d+")
(defparameter IUP_MASK_UINT     "/d+")
;;--------------------------------------------------------------------------------------
; IupGetParam Callback situations                    
(defconstant IUP_GETPARAM_OK     -1)
(defconstant IUP_GETPARAM_INIT   -2)
(defconstant IUP_GETPARAM_CANCEL -3)
(defconstant IUP_GETPARAM_HELP   -4)
;;--------------------------------------------------------------------------------------
;; Enums
;;--------------------------------------------------------------------------------------
; SHOW_CB Callback Values                                
(defconstant IUP_SHOW 0)
(defconstant IUP_RESTORE 1)
(defconstant IUP_MINIMIZE 2)
(defconstant IUP_MAXIMIZE 3)
(defconstant IUP_HIDE 4)
;;--------------------------------------------------------------------------------------
; SCROLL_CB Callback Values                              
(defconstant IUP_SBUP 0)
(defconstant IUP_SBDN 1)
(defconstant IUP_SBPGUP 2)
(defconstant IUP_SBPGDN 3)
(defconstant IUP_SBPOSV 4)
(defconstant IUP_SBDRAGV 5)
(defconstant IUP_SBLEFT 6)
(defconstant IUP_SBRIGHT 7)
(defconstant IUP_SBPGLEFT 8)
(defconstant IUP_SBPGRIGHT 9)
(defconstant IUP_SBPOSH 10)
(defconstant IUP_SBDRAGH 11)
;;--------------------------------------------------------------------------------------
; Record Input Modes                                 
(defconstant IUP_RECBINARY 0)
(defconstant IUP_RECTEXT 1)
;;--------------------------------------------------------------------------------------


(defun iup-open ()
  #+sbcl (sb-ext::set-floating-point-modes :traps nil)
  (IupOpen 
   (cffi:foreign-alloc :int :initial-element 0)
   (cffi:foreign-alloc 
    :pointer
    :initial-element
    (cffi:foreign-alloc :string
			:initial-element
			"Program")))
	(IupSetGlobal "UTF8MODE" "YES")) 
	
	
;;--------------------------------------------------------------------------------------
(defmacro with-iup (&body body)
  `(unwind-protect
	(progn
	  (iup-open)
	  ,@body)
     (IupClose)))
;;--------------------------------------------------------------------------------------
(defun get-fn-args (cb-args)
  (mapcar #'first cb-args))
   
(defmacro iup-defcallback (name args &body body)
  (let ((cb-name (intern (concatenate 'string "%" (string name) (string '#:-callback))))
	(fn-args (get-fn-args args)))
    `(progn
       (defun ,name ,fn-args ,@body)
       (cffi:defcallback ,cb-name :int ,args (,name ,@fn-args))
       (define-symbol-macro ,name (cffi:get-callback ',cb-name)))))
;;--------------------------------------------------------------------------------------
(defmacro iup-defcallback-default (name args &body body)
  `(iup-defcallback ,name ,args 
     (progn
       ,@body
       IUP_DEFAULT)))     
;;--------------------------------------------------------------------------------------
(defmacro iup-lambda-callback (args body)
  (let ((cb-name (gensym "%iup-cb-")))
    `(cffi:get-callback (cffi:defcallback ,cb-name :int ,args ,body))))
;;--------------------------------------------------------------------------------------
;=======================================================================================
;; IUP-DEFEVENT: in-current-package
;;--------------------------------------------------------------------------------------
(defmacro iup-register-event (object-name action cb-name)
  `(progn 
     (defvar *%iup-event-connections* nil)
     (pushnew (list ,object-name ,action ,cb-name) *%iup-event-connections* :test #'equal)))
;;--------------------------------------------------------------------------------------
(defmacro iup-defevent ((object &key (action "ACTION") 
				(name (intern (concatenate 
					       'string 
					       (string-trim "*" (string object)) 
					       "-" action)))
				(args nil)) &body body)
  (let ((cb-name (intern (concatenate 'string "%" (string name) (string '#:-callback))))
	(fn-args (get-fn-args args)))
    `(progn
       (iup-register-event ',object ,action ',cb-name)
       (defun ,name ,fn-args ,@body)
       (cffi:defcallback ,cb-name :int ,args (,name ,@fn-args))
       (define-symbol-macro ,name (cffi:get-callback ',cb-name)))))

(defmacro iup-defevent-default ((object &key 
					(action "ACTION") 
					(name (intern (concatenate 
						       'string 
						       (string-trim "*" (string object)) 
						       "-" action)))
					(args nil)) &body body)
  `(iup-defevent (,object :action ,action :name ,name :args ,args)
		 (progn
		   ,@body
		   IUP_DEFAULT)))
;;--------------------------------------------------------------------------------------
(defmacro iup-set-all-events ()
  '(mapcar #'(lambda (x)
	      (iupSetCallback (symbol-value (first x))
	       		      (second x)
	       		      (cffi:get-callback (print (third x)))))
	  *%iup-event-connections*))
;=======================================================================================
;;--------------------------------------------------------------------------------------
(defmacro %def-iup-container-macro (iupname iup-name)
  `(defmacro ,iup-name (child &rest childs)
     (append (list ',iupname child) 
	     (mapcan #'(lambda (x) 
			 (list :pointer x)) childs) '(:int 0))))

(%def-iup-container-macro iupvbox iup-vbox)
(%def-iup-container-macro iuphbox iup-hbox)
(%def-iup-container-macro iuptabs iup-tabs)
(%def-iup-container-macro iupgridbox iup-grid-box)
;;--------------------------------------------------------------------------------------

(defun start-gui (dlg)
	(IupShow dlg)
	(IupMainloop)
	(IupDestroy dlg))
