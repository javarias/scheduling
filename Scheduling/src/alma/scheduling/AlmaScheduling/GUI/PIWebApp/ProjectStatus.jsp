<HTML>
<HEAD>
<TITLE>Project Status information</TITLE>
</HEAD>
<BODY>
<% String ProjName = (String)request.getAttribute("ProjectName"); %>
<% String PI = (String)request.getAttribute("PIName"); %>
<% String ProjStatus = (String)request.getAttribute("ProjectStatus"); %>
<% String ReadyTime = (String)request.getAttribute("ReadyTime"); %>
<% String StartTime = (String)request.getAttribute("StartTime"); %>
<% String EndTime = (String)request.getAttribute("EndTime"); %>
<% String TotalSB = (String)request.getAttribute("TotalSB"); %>
<% String NumberSBComplete = (String)request.getAttribute("NumberSBComplete"); %>
<% String NumberSBFail = (String)request.getAttribute("NumberSBFail"); %>
<P>
<p>

<h2>Project Status</h2>
<HR align=left size=5 width=80% noshade>
<table border=1>
<tr><td> Name</td><td>value</td></tr>
<tr><td >Project Title</td><td><%= ProjName%></td></tr>
<tr><td >PI Name</td><td><%= PI%></td></tr>
<tr><td >Project Start time</td><td><%= StartTime%></td></tr>
<tr><td >Project Status</td><td><%= ProjStatus%></td></tr>
<tr><td >Number of SchedBlocks</td><td><%= TotalSB%></td></tr>
<tr><td >Number of Completed SchedBlocks</td><td><%= NumberSBComplete%></td></tr>
<tr><td >Number of Failed SchedBlocks</td><td><%= NumberSBFail%></td></tr>
</table>
</BODY>
</html>
