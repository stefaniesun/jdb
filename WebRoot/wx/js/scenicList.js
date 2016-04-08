$(function(){
	queryList();
	$("#search").click(function(){
		search();
	});
});


function submitForm(){
    if(event.keyCode==13){
    	search();
	}
}

function search(){
	var nameCn=$("#spotNameForm").val();
	queryList(nameCn);
}

function queryList(nameCn){
	var rows = 0; //记录条数
	var page = 0; //每页数
	$(".dropload-down").remove(); 
	$('#pullrefresh_order').dropload({
		scrollArea : window,
		loadDownFn : function(me){
			rows += 5; //记录以及加载的条数
			page++ ; //每调用一次  页数加1
			xyz.ajax({
				url:'BuyerProviderWS/queryProviderList.app',
				progress:false,
				data : {
					nameCn : nameCn,
					page : page,
					rows : 5,
					providerType : 'SC'
				},
				success : function(data) {
					if(data.status==1){
						
						var total = data.content.total; //总数据条数
						var result = data.content;//当前数据条数
						var table = document.body.querySelector('#tableList');
    					if(page==1){
    						table.innerHTML = '';
    					}
    					createHtml(table , result.rows);
    					if(total<=rows){
							me.lock();
							me.noData();
    					}
					}else{
						alert(data.msg);
					}
					//重置
					me.resetload();
				},error: function(xhr, type){
	                alert('Ajax error!');
	                me.resetload();
	            }
			});
		}
	});
}

function createHtml(table , list){
	for(var i=0; i<list.length; i++){
		var o = list[i];
		var li = document.createElement('li');
		li.className = 'index-ticket-list';
		var url = xyz.setUrlparam(xyz.getFullurl('scenic_detail.html'),{provider:o.numberCode,nameCn:o.nameCn});
		var html = '<div class="index-hotel-img">';
        html +='<a href="javascript:void(0);" title="" onclick="window.location.href=\''+url+'\'"><img src="images/ticket.jpg" alt=""/></a>';
        html +='</div>';
        html +='<div class="index-hotel-info">';
        html +='<p class="index-hotel-name">';
        html +='<a href="javascript:void(0);">'+o.nameCn+'</a>';
        html +='</p>';
        html +='<p class="index-hotel-price">';
        if(o.price && !isNaN(o.price)){
        	html +='<i>¥'+o.price+'</i>起';
        }else{
        	html +='<i>暂无报价</i>';
        }
        html +='</p>';
        html +='</div>';
		
		//html += '<span id="json_'+o.numberCode+'" style="display:none;">'+JSON.stringify(o)+'</span>';
		li.innerHTML = html;
		table.appendChild(li);
	}
}



