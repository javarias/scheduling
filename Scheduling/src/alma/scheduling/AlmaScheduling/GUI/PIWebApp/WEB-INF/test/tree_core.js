//Author:linss@ms1.url.com.tw  LastModify:2002/1/30
var RootText, ArrText, FromObject, TempObject
var Items=new Array
var ArrPos="00",ArrIcon="root",TmpPos="00"
var isLoading=false,undefined
var vertline,isEndTree,isSubTree
function BuildTree(){
if(eval('parent.Explorer_Menu.d'+FromObject)==undefined){ //避免重新整理時載入
parent.Explorer_Menu.isLoading=false
return
}
TempObject="<table border=0 cellpadding=0 cellspacing=0>"
for(i=1;i<Items.length;i++){
Items[i]=Items[i].split("#");
ItemText=(Items[i][0]).replace(/^[\s]+/gi,"").replace(/[\s]+$/gi,"")
ItemID  =(Items[i][1]).replace(/^[\s]+/gi,"").replace(/[\s]+$/gi,"")
ItemSub =(Items[i][2]).replace(/^[\s]+/gi,"").replace(/[\s]+$/gi,"")
ItemIcon=(Items[i][3]).replace(/^[\s]+/gi,"").replace(/[\s]+$/gi,"")
if(ItemSub=="0"){
isSubTree="n"
SubClick=""
}else{
isSubTree="s"
SubClick=" onclick=\"GetSubFolder('"+ItemID+"',1);\""
}
if(i==Items.length-1){
vertline=""
isSubTree+="1"
}else{
vertline=" background=./pict/vert_line.gif"
isSubTree+="0"
}
TempObject+="<tr><td width=19 height=16 valign=top"+vertline+"><img id=\"a"+ItemID+"\" class=\"FdReady\" src=./pict/vert_line_"+isSubTree+"0.gif"+SubClick+"></td><td height=16 nowrap><img id=\"b"+ItemID+"\" src=./pict/"+ItemIcon+"0.gif align=top onclick=\"GetFolderItem('"+ItemID+"','"+ItemIcon+"');\" onDblClick=\"a"+ItemID+".click();\"><span id=\"c"+ItemID+"\" class=FdText onclick=\"b"+ItemID+".click();\" onDblClick=\"a"+ItemID+".click();\">"+ItemText+"</span><div id=\"d"+ItemID+"\" class=FdTemp></div></td></tr>"
}
TempObject+="</table>"
eval('parent.Explorer_Menu.d'+FromObject).innerHTML=TempObject
parent.Explorer_Menu.isLoading=false
if(FromObject=="00")top.document.title=RootText
}
/*
function onSubFolderError(id){ //偵測是否錯誤
if(isLoading){
var ErrText=eval('c'+id).innerText
alert(ErrText+" 的目錄結構不正常，無法被載入！")
eval('d'+id).innerHTML="<span class=FdError>無法載入...</span>"
isLoading=false
return
}
}
*/
function GetSubFolder(id,v){
if(eval('a'+id).className=="FdReady"){ //取得樹狀結構
if(isLoading){ //防止同時開啟兩個項目
return
}
eval('d'+id).innerHTML="<span class=FdWait>載入資料中...</span>"
eval('a'+id).className="FdOpen"
eval('a'+id).src="./pict/vert_line_s"+v+"1.gif"
parent.Explorer_Temp.location.replace("temp"+id+".htm");
isLoading=true
//window.setTimeout("onSubFolderError('"+id+"')",4000) //無法取得樹狀結構的回應時間
}else
if(eval('a'+id).className=="FdOpen"){ //關閉樹狀結構
eval('a'+id).className="FdClose"
eval('a'+id).src="./pict/vert_line_s"+v+"0.gif"
eval('d'+id).style.position="absolute"
eval('d'+id).style.visibility="hidden"
TmpPos=id
if(TmpPos!=ArrPos){
if(ArrPos.indexOf(TmpPos)==0){
GetFolderItem(id,'folder');
}
}
}else
if(eval('a'+id).className=="FdClose"){ //開啟樹狀結構
eval('a'+id).className="FdOpen"
eval('a'+id).src="./pict/vert_line_s"+v+"1.gif"
eval('d'+id).style.position=""
eval('d'+id).style.visibility=""
}
}
function GetFolderItem(id,icon){ //開啟選取的物件
if(ArrPos!=id){
//parent.Explorer_Main.location.replace("main"+id+".htm");
eval('b'+ArrPos+'.src="./pict/'+ArrIcon+'0.gif"')
eval('b'+id+'.src="./pict/'+icon+'1.gif"')
eval('c'+ArrPos).className="FdText"
eval('c'+id).className="FdRead"
ArrText=eval('c'+id).innerText
ArrPos=id
ArrIcon=icon
top.document.title=ArrText
}
}
function GetRootItem(){ //開啟根目錄物件
//parent.Explorer_Main.location.replace("main.htm");
eval('b'+ArrPos+'.src="./pict/'+ArrIcon+'0.gif"')
eval('c'+ArrPos).className="FdText"
c00.className="FdRead"
ArrText=RootText
ArrPos="00"
ArrIcon="root"
top.document.title=RootText
}