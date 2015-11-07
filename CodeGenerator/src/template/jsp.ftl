<%@ page contentType="text/html;charset=utf-8"%>
<%@ include file="/common/include.jsp"%>
<html>
<head>
<script type="text/javascript">
	var grid;
	dojo.ready(function(){
		var url = "${class?lower_case}Query.do";
		//please change translator.value
		var head=[[
			<#list columns as item>
			{'name':'<gcs:translator value="${item.column}"/>','field':'${item.column}','width':'auto'},
  			</#list> 
			]];
		grid = new GcsDataGrid(url, head, );
		ttgrid.setSelectionMode("check");
	});
	
	function query(){
		<#list columns as item>
		var ${item.column} = $("#${item.column}").val();
  		</#list>
		var param = {
			<#list columns as item>
			"${item.column}" : ${item.column}<#if item_has_next>,</#if>
  			</#list>
		};
		grid.setPostParam(param);
		grid.startUp();
	}
</script>
</head>
<body class=<c:out value="${r'${_theme}'}"/>>
<center>
<div id="" data-dojo-type="dijit/TitlePane" title= style="width:100%;text-align:left;">
<div style="text-align:center;">
<table>
<#list columns as item>
<td><gcs:translator value=""/>:</td>
<td><input data-dojo-type="dijit/form/TextBox" type="text" name="${item.column}" id="${item.column}"/></td>
</#list>
<tr>
<td><button data-dojo-type="dijit/form/Button" onclick="query();"><gcs:translator value=""/></button></td>
</tr>
</table>
</div>
</div>
</center>

<center>
<div data-dojo-type = "dijit/TitlePane" title=<gcs:translator value=""/> style = "width:100%;text-align:left;">
<div id = "result"><div>
</div>
</center>
</body>
</html>