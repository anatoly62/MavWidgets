(load "lisp-iup.lisp")
(defpackage :my-iup
    (:use :common-lisp :lisp-iup))
	
(in-package :my-iup)	
	  
(defparameter *dialog* nil)
(defparameter *button* nil)
(defparameter *text* nil)

(iup-defcallback msg-cb ()
	(progn
		(IupSetStrAttribute *text* "VALUE" "Hello")	lisp-iup:IUP_DEFAULT))

(defun main()
	(with-iup
		(setf *button* (IupButton "Greeting" ""))
		(setf *text* (IupText ""))
		(setf *dialog*(IupDialog
			(IupSetAttributes (iup-hbox *text* *button*) "GAP=5")))
		(IupSetAttributes *dialog* "TITLE=LispIUP, SIZE=150x50")
		(IupSetCallback *button* "ACTION" msg-cb)
		(start-gui *dialog*)))

(main)	

;;(sb-ext:save-lisp-and-die "iup_lisp.exe" :executable t :application-type :gui :toplevel #'main)	
