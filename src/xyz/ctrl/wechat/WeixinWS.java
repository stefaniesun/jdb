package xyz.ctrl.wechat;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import xyz.filter.JSON;
import xyz.filter.ReturnUtil;
import xyz.model.member.WeixinUserInfo;
import xyz.svc.wechat.WeixinUserInfoSvc;
import xyz.util.HttpUtil;
import xyz.util.WeixinUtil;

@Controller
@RequestMapping(value="/WeixinWS")
public class WeixinWS {
	
	@Autowired
	private WeixinUserInfoSvc weixinUserInfoSvc;
	
	@RequestMapping(value="/xztrip")
	@ResponseBody
	public Map<String,Object> xztrip(HttpServletRequest request) throws UnsupportedEncodingException{
		String requestData = HttpUtil.parseHttpStream(request);
		System.out.println("dataStr===="+requestData);
		if("".equals(requestData)){
			return ReturnUtil.returnMap(0, "参数丢失！");
		}
		@SuppressWarnings("unchecked")
		Map<String,Object> jsonMap = JSON.toObject(requestData, Map.class);
		String msgType = WeixinUtil.getString(jsonMap, "MsgType");  //消息类型
		String event = WeixinUtil.getString(jsonMap, "Event");  
		String eventKey = WeixinUtil.getString(jsonMap, "EventKey");
		
		if("event".equals(msgType)){	//推送事件
			Map<String,Object> map = null;
			if("VIEW".equals(event)){
				
			}else if("subscribe".equals(event)){	//关注公众号
				@SuppressWarnings("unchecked")
				Map<String,Object> userMap = (Map<String, Object>) jsonMap.get("WeixinUser");
				saveUserInfo(userMap);
				
				map = new HashMap<String,Object>();
				map.put("type", "subscribe");
				map.put("toUserName", WeixinUtil.getString(jsonMap, "FromUserName"));
				map.put("fromUserName", WeixinUtil.getString(jsonMap, "ToUserName"));
				map.put("content", "欢迎关注西藏智慧旅游！为了您能够享受到更好地服务，我们建议您<a href=\"http://testdis.oicp.net/weixinServer/jsp_main/weixinregister.html?openid="+WeixinUtil.getString(jsonMap, "FromUserName")+"&outkey="+userMap.get("outkey")+"\">绑定账户</a>");
				return ReturnUtil.returnMap(1, map);
			}else if("unsubscribe".equals(event)){	//取消关注
				weixinUserInfoSvc.editWeixinUser(WeixinUtil.getString(jsonMap, "FromUserName"), null,null,null,null);
				map = new HashMap<String,Object>();
				map.put("type", "unsubscribe");
				map.put("toUserName", "");
				map.put("fromUserName", "");
				map.put("content", "");
				return ReturnUtil.returnMap(1, map);
			}else if("LOCATION".equals(event)){	//用户上报地理位置
				weixinUserInfoSvc.editWeixinUser(WeixinUtil.getString(jsonMap, "FromUserName"), null, WeixinUtil.getString(jsonMap, "Longitude"), WeixinUtil.getString(jsonMap, "Latitude"), WeixinUtil.getString(jsonMap, "CreateTime"));
				return ReturnUtil.returnMap(0, null);
			}else if("CLICK".equals(event)){	
				if("FIX".equals(eventKey)){	//加油修车
					Map<String,Object> weixinUserInfoMap = weixinUserInfoSvc.getWeixinUserInfo(WeixinUtil.getString(jsonMap, "FromUserName"), null);
					String content = "";
					if(Integer.parseInt(weixinUserInfoMap.get("status").toString())==1){
						WeixinUserInfo weixinUserInfo = (WeixinUserInfo) weixinUserInfoMap.get("content");
						if(weixinUserInfo != null){
							long nowTime = System.currentTimeMillis()/1000;
							String positionTime = weixinUserInfo.getPositionTime();
							if(positionTime != null && !"".equals(positionTime)){
								int mistime = (int) ((nowTime-Long.parseLong(positionTime))/60);	//地理位置上报时间与当前时间差
								if(mistime>5){	//时差大于5分钟
									content = "不能获取到您的地理位置信息，请先到公众号首页打开【提供位置信息】，再打开手机GPS，然后重新进入公众号重新点击【加油修车】即可。或者您也可以拨打我们的求助热线：023-1111-2222联系我们";
								}else{
									content = "求助已发出，工作人员收到后将在第一时间联系您，请保持电话畅通。\n费用说明：距离50公里以内，出行费200元；50-100公里，出行费300元；100公里以上按照里每公里2元的价格累加；修理费用和油费(如若您需要)另算。";
								}
							}else{
								content = "不能获取到您的地理位置信息，请先到公众号首页打开【提供位置信息】，再打开手机GPS，然后重新进入公众号重新点击【加油修车】即可。或者您也可以拨打我们的求助热线：023-1111-2222联系我们";
							}
						}
					}else{
						content = "对不起，发送求助失败，您可以还可以拨打我们的求助热线：023-1111-2222联系我们";
					}
					map = new HashMap<String,Object>();
					map.put("type", "unsubscribe");
					map.put("toUserName", WeixinUtil.getString(jsonMap, "FromUserName"));
					map.put("fromUserName", WeixinUtil.getString(jsonMap, "ToUserName"));
					map.put("content", content);
					return ReturnUtil.returnMap(1, map);
				}else if("TRANSFER".equals(eventKey)){	//拖车服务
					
				}else if("URGENCY".equals(eventKey)){	//紧急救援
					
				}
			}
		}
		return ReturnUtil.returnMap(0, null);
	}
	
	/**
	 * 保存用户信息
	 * @param requestMap
	 * @param accountMp
	 */
	private void saveUserInfo(Map<String,Object> userMap){
		if(!userMap.isEmpty()){
			WeixinUserInfo weixinUserInfo = new WeixinUserInfo();
			weixinUserInfo.setCity(WeixinUtil.getString(userMap,"city"));
			weixinUserInfo.setCountry(WeixinUtil.getString(userMap,"country"));
			weixinUserInfo.setGroupid(WeixinUtil.getString(userMap,"groupid"));
			weixinUserInfo.setHeadimgurl(WeixinUtil.getString(userMap,"headimgurl"));
			weixinUserInfo.setLanguage(WeixinUtil.getString(userMap,"language"));
			weixinUserInfo.setNickname(WeixinUtil.getString(userMap,"nickname"));
			weixinUserInfo.setOpenid(WeixinUtil.getString(userMap,"openid"));
			weixinUserInfo.setProvince(WeixinUtil.getString(userMap,"province"));
			weixinUserInfo.setRemark(WeixinUtil.getString(userMap,"remark"));
			weixinUserInfo.setSex(WeixinUtil.getString(userMap,"sex"));
			weixinUserInfo.setSubscribe(WeixinUtil.getString(userMap,"subscribe"));
			weixinUserInfo.setSubscribe_time(WeixinUtil.getString(userMap,"subscribe_time"));
			weixinUserInfo.setUnionid(WeixinUtil.getString(userMap,"unionid"));
			weixinUserInfo.setOutkey(WeixinUtil.getString(userMap,"outkey"));
			
			weixinUserInfoSvc.addWeixinUser(weixinUserInfo);
		}
	}
	
}
