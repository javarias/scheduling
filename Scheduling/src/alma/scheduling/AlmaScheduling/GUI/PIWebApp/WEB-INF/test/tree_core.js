//Author:linss@ms1.url.com.tw  LastModify:2002/1/30
var RootText, ArrText, FromObject, TempObject
var Items=new Array
var ArrPos="00",ArrIcon="root",TmpPos="00"
var isLoading=false,undefined
var vertline,isEndTree,isSubTree
function BuildTree(){
if(eval('parent.Explorer_Menu.d'+FromObject)==undefined){ //�קK���s��z�ɸ��J
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
function onSubFolderError(id){ //�����O�_���~
if(isLoading){
var ErrText=eval('c'+id).innerText
alert(ErrText+" ���ؿ����c�����`�A�L�k�Q���J�I")
eval('d'+id).innerHTML="<span class=FdError>�L�k���J...</span>"
isLoading=false
return
}
}
*/
function GetSubFolder(id,v){
if(eval('a'+id).className=="FdReady"){ //���o�𪬵��c
if(isLoading){ //����P�ɶ}�Ҩ�Ӷ���
return
}
eval('d'+id).innerHTML="<span class=FdWait>���J��Ƥ�...</span>"
eval('a'+id).className="FdOpen"
eval('a'+id).src="./pict/vert_line_s"+v+"1.gif"
parent.Explorer_Temp.location.replace("temp"+id+".htm");
isLoading=true
//window.setTimeout("onSubFolderError('"+id+"')",4000) //�L�k���o�𪬵��c���^���ɶ�
}else
if(eval('a'+id).className=="FdOpen"){ //�����𪬵��c
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
if(eval('a'+id).className=="FdClose"){ //�}�Ҿ𪬵��c
eval('a'+id).className="FdOpen"
eval('a'+id).src="./pict/vert_line_s"+v+"1.gif"
eval('d'+id).style.position=""
eval('d'+id).style.visibility=""
}
}
function GetFolderItem(id,icon){ //�}�ҿ��������
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
function GetRootItem(){ //�}�Үڥؿ�����
//parent.Explorer_Main.location.replace("main.htm");
eval('b'+ArrPos+'.src="./pict/'+ArrIcon+'0.gif"')
eval('c'+ArrPos).className="FdText"
c00.className="FdRead"
ArrText=RootText
ArrPos="00"
ArrIcon="root"
top.document.title=RootText
}