$(document).ready(function() {
	
	product = $("#dialogFormDiv_managerProductUserTagIframe",window.parent.document).attr("name");

	$("#productButton").click(function(){
		loadTable();
	});
	
	initTable();
	
});

function initTable(){
	var toolbar = [];
	if(xyzControlButton('buttonCode_h20160317115001')){
		toolbar[toolbar.length]={
				text: '新增标签价格库存',
				border:'1px solid #bbb',
				iconCls: 'icon-add',
				handler: function(){var title=$(this).text();addProductUserTagButton(title);}
		};
		toolbar[toolbar.length]='-';
	}
	if(xyzControlButton('buttonCode_h20160317115002')){
		toolbar[toolbar.length]={
				text: '编辑标签价格库存',
				border:'1px solid #bbb',
				iconCls: 'icon-edit',
				handler: function(){var title=$(this).text();editProductUserTagButton(title);}
		};
		toolbar[toolbar.length]='-';
	}
	if(xyzControlButton('buttonCode_h20160317115003')){
		toolbar[toolbar.length]={
				text: '删除标签价格库存',
				border:'1px solid #bbb',
				iconCls: 'icon-remove',
				handler: function(){deleteProductUserTagButton();}
		};
		toolbar[toolbar.length]='-';
	}
	
	xyzgrid({
		table : 'productUserTagManagerTable',
		title : '价格库存列表',
		url:'../ProductUserTagWS/queryProductUserTagList.do',
		pageList : [5,10,15,30,50],
		pageSize : 15,
		toolbar:toolbar,
		singleSelect : false,
		idField : 'numberCode',
		height:'auto',
		columns : [[
		    {field:'checkboxTemp',checkbox:true},
			{field:'numberCode',title:'编号',hidden:true},
			{field:'nameCn',title:'名称',width:100},
			{field:'userTagNameCn',title:'标签',width:100},
			{field:'operTemp1',title:'特价设置',
				formatter: function(value,row){
					return "<a href='javascript:void(0);' onclick='managerProductUserTagStock(\""+row.numberCode+"\",\""+row.nameCn+"\")'>设置</a>";
				}	
			},
		]],
		queryParams : {
			product : product
		}
	});
}

function loadTable(){
	var nameCn=$("#nameCn").val();
	$("#productUserTagManagerTable").datagrid("load",{
		nameCn:nameCn,
		product:product
	});
}

function addProductUserTagButton(title){
	
	 var contentHtml = "<table>";
	 contentHtml += "<tr>";
	 contentHtml += "<td align='right'>";
	 contentHtml += "特殊价格名称 ：";
	 contentHtml += "</td>";
	 contentHtml += "<td>";
	 contentHtml += "<input type='text' id='nameCnForm' style='width:250px;' />";
	 contentHtml += "</td>";
	 contentHtml += "</tr>";
	 contentHtml += "<tr>";
	 contentHtml += "<td align='right'>";
	 contentHtml += "用户标签 ：";
	 contentHtml += "</td>";
	 contentHtml += "<td>";
	 contentHtml += '<input id="userTagForm" class="easyui-combobox" data-options="editable:false"  style="width:250px;"/>';
	 contentHtml += "</td>";
	 contentHtml += "</tr>";
	 contentHtml += "</table>";
	
	xyzdialog({
		dialog : 'dialogFormDiv_addProductUserTag',
		title : title,
	    content : contentHtml,
	    fit:false,
	    height:300,
	    width:500,
	    buttons:[{
			text:'确定',
			handler:function(){
				addProductUserTagSubmit();
			}
		},{
			text:'取消',
			handler:function(){
				$("#dialogFormDiv_addProductUserTag").dialog("destroy");
			}
		}],
		onOpen:function(){
			xyzCombobox({
				combobox : 'userTagForm',
				url : '../ListWS/getUserTagList.do',
				lazy : false,
				mode : 'remote'
			});
		}
	});
	
}

function editProductUserTagButton(title){
	
	var tag = $("#productUserTagManagerTable").datagrid("getChecked");
	if(tag.length != 1){
		top.$.messager.alert("提示","请先选中单个对象！","info");
		return;
	}
	var row = tag[0];

	
	 var contentHtml = "<table>";
	 contentHtml += "<tr>";
	 contentHtml += "<td align='right'>";
	 contentHtml += "特殊价格名称 ：";
	 contentHtml += "</td>";
	 contentHtml += "<td>";
	 contentHtml += "<input type='text' id='nameCnForm' style='width:250px;' />";
	 contentHtml += "</td>";
	 contentHtml += "</tr>";
	 contentHtml += "<tr>";
	 contentHtml += "<td align='right'>";
	 contentHtml += "用户标签 ：";
	 contentHtml += "</td>";
	 contentHtml += "<td>";
	 contentHtml += '<input id="userTagForm" class="easyui-combobox" data-options="editable:false"  style="width:250px;"/>';
	 contentHtml += "</td>";
	 contentHtml += "</tr>";
	 contentHtml += "</table>";
	
	xyzdialog({
		dialog : 'dialogFormDiv_editProductUserTag',
		title : title,
	    content : contentHtml,
	    fit:false,
	    height:300,
	    width:500,
	    buttons:[{
			text:'确定',
			handler:function(){
				editProductUserTagSubmit(row.numberCode);
			}
		},{
			text:'取消',
			handler:function(){
				$("#dialogFormDiv_editProductUserTag").dialog("destroy");
			}
		}],
		onOpen:function(){
			xyzCombobox({
				combobox : 'userTagForm',
				url : '../ListWS/getUserTagList.do',
				lazy : false,
				mode : 'remote'
			});
			
			$("#userTagForm").combobox('setValue',row.userTag);
			$("#nameCnForm").val(row.nameCn);
		}
	});
}

function addProductUserTagSubmit(target){
	var nameCn=$("#nameCnForm").val();
	var userTag=$("#userTagForm").combobox("getValue");
	
	if(!$("form").form('validate')){
		return;
	}
	
	xyzAjax({
		url:"../ProductUserTagWS/addProductUserTag.do",
		data:{
			nameCn:nameCn,
			userTag:userTag,
			product:product
		},
		success:function(data){
			if(data.status==1){
				top.$.messager.alert("提示","操作成功","info");
				$("#dialogFormDiv_addProductUserTag").dialog("destroy");
				$("#productUserTagManagerTable").datagrid("reload");
			}else{
				top.$.messager.alert("警告",data.msg,"warning");
			}
		}
	});
}

function editProductUserTagSubmit(numberCode){

	var nameCn=$("#nameCnForm").val();
	var userTag=$("#userTagForm").combobox("getValue");

	if(!$("form").form('validate')){
		return;
	}
	
	xyzAjax({
		url:"../ProductUserTagWS/editProductUserTag.do",
		data:{
			numberCode:numberCode,
			nameCn:nameCn,
			userTag:userTag
		},
		success:function(data){
			if(data.status==1){
				top.$.messager.alert("提示","操作成功","info");
				$("#dialogFormDiv_editProductUserTag").dialog("destroy");
				$("#productUserTagManagerTable").datagrid("reload");
			}else{
				top.$.messager.alert("警告",data.msg,"warning");
			}
		}
	});
	
}

function deleteProductUserTagButton(){
	var tags = $.map($("#productUserTagManagerTable").datagrid("getChecked"),function(p){return p.numberCode;}).join(",");
	if(tags == null || tags == ''){
		top.$.messager.alert("提示","请先选中需要删除的对象！","info");
		return;
	}
	if(!confirm("彻底删除对象，确定？")){
		return;
	}
	
	xyzAjax({
		url : '../ProductUserTagWS/deleteProductUserTag.do',
		data : {
			numberCodes : tags
		},
		success : function(data) {
			if(data.status==1){
				top.$.messager.alert("提示","操作成功","info");
				$("#productUserTagManagerTable").datagrid("reload");
			}else{
				top.$.messager.alert("警告",data.msg,"warning");
			}
		}
	});
}


function editImageUrl(numberCode,imageUrl){
	
	$("#numberCode").val(numberCode);
	$("#url").val(imageUrl);
	
	xyzdialog({
		dialog : 'dialogFormDiv_editProviderImageUrl',
		title : '编辑图片',
	    href : 'editProviderImageUrl.html?numberCode='+numberCode+'&url='+imageUrl,
	    fit:false,
	    height:750,
	    width:1150,
	    buttons:[{
			text:'关闭',
			handler:function(){
				$("#dialogFormDiv_editProviderImageUrl").dialog("destroy");
			}
		}]
	});
	
	/*
	layer.open({
		title:'上传图片',
	    type: 2,
	    area: ['1100px', '700px'],
	    fix: true, //不固定
	    maxmin: false,
	    content: 'editProviderImageUrl.html?numberCode='+numberCode+'&url='+imageUrl
	});*/
	
}

function managerProductUserTagStock(numberCode,nameCn){
	xyzdialog({
		dialog : 'dialogFormDiv_managerProductUserTagStock',
		title : "配置标签["+nameCn+"]",
	    content : "<iframe id='dialogFormDiv_managerProductUserTagStockIframe' frameborder='0' name='"+numberCode+"'></iframe>",
	    buttons:[{
			text:'返回',
			handler:function(){
				$("#dialogFormDiv_managerProductUserTagStock").dialog("destroy");
			}
		}]
	});
	var tempWidth = $("#dialogFormDiv_managerProductUserTagStock").css("width");
	var tempHeight = $("#dialogFormDiv_managerProductUserTagStock").css("height");
	var tempWidth2 = parseInt(tempWidth.split("px")[0]);
	var tempHeight2 = parseInt(tempHeight.split("px")[0]);
	$("#dialogFormDiv_managerProductUserTagStockIframe").css("width",(tempWidth2-20)+"px");
	$("#dialogFormDiv_managerProductUserTagStockIframe").css("height",(tempHeight2-50)+"px");
	$("#dialogFormDiv_managerProductUserTagStockIframe").attr("src","../jsp_main/productUserTagStock.html");
}