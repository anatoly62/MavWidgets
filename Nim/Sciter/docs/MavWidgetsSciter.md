# utils
initGui::String->Int->Int->IO (WinPtr, GuiPtr)

loadGui::String->Int->Int->IO (WinPtr, GuiPtr)

loopGui :: IO ()

tryE::String->IO()->IO()

message:: String->IO ()


<b>example for main window</b>

    (mainWindow,root)<-initGui "file://c:/sciter/index.html" 800 500
    windowShow mainWindow
    ...
    loopGui

<b>example for child window</b> 

    (childWindow,root)<-loadGui "file://c:/sciter/docSimple.html" 500 170
    windowShow childWindow
    tryE "Some error" $ do //if error message "Some error" ++ error will be shown
    ...
  
<b>example wait dataReady</b>

	(childWindow,root)<-loadGui "file://D:/sciter/docWithAgents.html" 520 170 
	windowShow childWindow
	windowOnEvent mainWindow $ \_ _ evtg param->do
	  when (evtg==handleBehaviorEvent) $ do
        cmd<-peekByteOff param 0 ::IO UINT
        when (cmd==documentReady) $ do
	      message "data is ready here"
      return False
    return() 	
_______________________________________________________________________________	

# button 
button::GuiPtr->String->IO MavButton

buttonGetText::MavButton>IO ByteString

buttontSetText::MavButton>ByteString->IO ()

buttonOnClick:: MavButton>IO ()->IO()

<b>example:</b> 

    <input id="buttonAdd" type="button" value="old caption"/>

    btAdd<-button root "buttonAdd"
    buttontSetText btAdd "new caption"
    buttonOnClick btAdd $ print $ buttonGetText btAdd
_______________________________________________________________________________

# checkButton
checkButton::GuiPtr->String->IO MavCheck

checkButtonIsChecked::MavCheck>IO Bool

checkButtonCheck::MavCheck>Bool->IO ()

checkButtonOnChange:: MavCheck>IO ()->IO()
_______________________________________________________________________________

# combo
combo::GuiPtr->String->IO MavCombo

comboGetText::MavCombo>IO ByteString

comboSetText::MavCombo>ByteString->IO UINT

comboGetIndex::MavCombo>IO Int

comboSetIndex::MavCombo>Int->IO ()  

comboFill::MavCombo>[ByteString]->IO () 

comboGetItems::MavCombo>IO[ByteString]

comboSearch::MavCombo>[ByteString]->ByteString->Int->IO()

comboOnChange:: MavCombo>IO ()->IO()

<b>example:</b> 

    <select id="comboUsers"></select>

    cbUsers<-combo root "comboUsers"
    comboFill cbUsers ["Ann","Ivan","Julia"]
    comboSetText  cbUsers "Julia" -- or comboSetIndex  cbUsers 2
    comboOnChange cbUsers $ print $ show $ comboGetIndex cbUsers
_______________________________________________________________________________
# datePick
datePick::GuiPtr->String->IO MavDate

datePickGetText::MavDate>IO ByteString

datePickSetText::MavDate>ByteString->IO ()

datePickOnChange:: MavDate>IO ()->IO()


<b>example:</b>
 
    <input id="curDate" type="date" lang="uk-Ukr" />

    dt<-datePick root "curDate"
    datePickSetText dt "2019-11-08"
    datePickOnChange $ print $ datePickGetText dt
_______________________________________________________________________________
# flatButton
flatButton::GuiPtr->String->IO MavFlatButton

flatButtonOnClick:: MavFlatButton>IO ()->IO()

<b>example:</b> 
    <img id="tbFind"src="btFind.png" />

    tbFind<-flatButton root "tbFind"
    flatButtonOnClick tbFind $ print "clicked"
_______________________________________________________________________________
# menuItem
menuItem::GuiPtr->String->IO MavItem

menuItemOnClick:: MavItem>IO ()->IO()
_______________________________________________________________________________

# menu
menu::GuiPtr->String->IO MavMenu

menuShow::MavMenu>GuiPtr->IO ()
_______________________________________________________________________________

# table
table::GuiPtr->String->IO MavTable

tableGetIndex::MavTable>IO Int

tableSetIndex::MavTable>Int->IO()

tableScroll::MavTable>IO()  

tableScrollTo::MavTable>Int->IO()

tableAddRow::MavTable>c->[ c->  ByteString]->IO ()

tableAddRowM::MavTable>c->[ c-> IO ByteString]->IO ()

tableChangeRow::MavTable>c->[ c-> ByteString]->IO ()

tableChangeRowM::MavTable>c->[ c-> IO ByteString]->IO ()

tableDeleteRow::MavTable>IO ()

tableFill::MavTable>[c]->[ c-> ByteString]-> IO UINT

tableFillM::MavTable>[c]->[ c->IO ByteString]-> IO ()

tableGetRow::MavTable>Word32->IO Addr

tableSetHeaderText::MavTable>Int->ByteString->IO() 
 
tableSearch::MavTable>[ByteString]->ByteString->Int->IO()

tableOnClick:: MavTable>(Int->IO())->IO()

tableOnDblClick:: MavTable>(Int->IO())->IO()

tableOnMenu:: MavTable>(Int->IO())->IO()

<b>example:</b>
 
    <table  border="0" >
    <thead ><tr><th>Id</th><th>Name</th></tr></thead>
    <tbody id="tblUsers" >
    </tbody>
    </table>

    data User=User{userId:: !Int,userName:: !ByteString}
    userStore=[User{userId=1,userName="Ann"},User{userId=2,userName="Ivan"}]
    let func=[fromInt.userId,userName]
    tbl<table root "tblUsers"
    tableFill tbl userStore func
    tableSetIndex tbl 1
    tableOnClick tbl $ \n-> print $ "clicked in line "++ show n
_______________________________________________________________________________

# text
text::GuiPtr->String->IO MavText

textGetText::MavText>IO ByteString

textSetText::MavText>ByteString->IO ()

textSelectAll::MavText>IO()

textOnChange:: MavText>IO ()->IO()

textOnKey:: MavText>(Int->IO())->IO()


<b>example:</b>

    <input id="textName" type="text"/>

    textName<text root "textName"
    textSetText textName "Ann"
    textOnChange textName $ print $textGetText textName
    textOnKey textName $ \n-> when(n==13) $ print "enter pressed"
_______________________________________________________________________________
# window
windowShow::MavWindow>IO() 

windowDestroy::MavWindow>IO() 

windowSetText::MavWindow>ByteString->IO() 


# widget
widgetSetFocus::GuiPtr->IO()

widgetEnable::GuiPtr->IO()

widgetDisable::GuiPtr->IO()

widgetShow::GuiPtr->IO()

widgetHide::GuiPtr->IO()
_______________________________________________________________________________

   

