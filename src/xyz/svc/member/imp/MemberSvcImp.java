package xyz.svc.member.imp;

import java.util.Date;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import xyz.dao.CommonDao;
import xyz.filter.MyRequestUtil;
import xyz.filter.ReturnUtil;
import xyz.filter.RmiUtil;
import xyz.model.member.Customer;
import xyz.model.member.WeixinUserInfo;
import xyz.model.member.XyzSessionLogin;
import xyz.model.member.XyzSessionUtil;
import xyz.svc.member.MemberSvc;
import xyz.util.Constant;
import xyz.util.EncryptionUtil;
import xyz.util.UUIDUtil;

@Service
public class MemberSvcImp  implements MemberSvc{
	@Resource
	CommonDao commonDao;
	
	@Autowired
	RmiUtil rmiUtil;
	
	@Override
	public Map<String, Object> loginOper(
			String username,
			String password) {
		String passwordSe = EncryptionUtil.md5(password+"{"+username+"}");
		
		String hql1 = "from Customer t where t.username = '"+username+"'  and t.password = '"+passwordSe+"'"; 
		Customer customer = (Customer) commonDao.queryUniqueByHql(hql1);
		
		/**
		 * 验证用户登录的账号是否合格
		 */
		if(customer == null){
			return ReturnUtil.returnMap(0,"用户名或密码错误！");
		}
		if(customer.getEnabled()==0){
			return ReturnUtil.returnMap(0,"账户受限,暂不允许登录!");
		}
		
		String apikey = UUIDUtil.getUUIDStringFor32();
		XyzSessionLogin xyzSessionLogin = new XyzSessionLogin();
		xyzSessionLogin.setApikey(apikey);
		xyzSessionLogin.setUsername(customer.getUsername());
		xyzSessionLogin.setExpireDate(new Date(new Date().getTime()+Constant.sessionTimes));
		XyzSessionUtil.logins.put(apikey, xyzSessionLogin);
		
		return ReturnUtil.returnMap(1, xyzSessionLogin);
	}

	@Override
	public Map<String, Object> memberExit() {
		XyzSessionLogin xyzSessionLogin = MyRequestUtil.getXyzSessionLogin();
		if(xyzSessionLogin==null){
			return ReturnUtil.returnMap(1,null);
		}
		String apikey = xyzSessionLogin.getApikey();
		XyzSessionUtil.logins.remove(apikey);
		return ReturnUtil.returnMap(1,null);
	}

	@Override
	public Map<String, Object> addWeixinUserInfo(WeixinUserInfo weixinUserInfo) {
		if(weixinUserInfo.getOpenid()==null || "".equals(weixinUserInfo.getOpenid())){
			return ReturnUtil.returnMap(0, "openid不能为空");
		}
		WeixinUserInfo userInfo = (WeixinUserInfo)commonDao.getObjectByUniqueCode("WeixinUserInfo", "openid", weixinUserInfo.getOpenid());
		if(userInfo==null){
			commonDao.save(weixinUserInfo);
			return ReturnUtil.returnMap(1, null);
		}
		userInfo.setCity(weixinUserInfo.getCity());
		userInfo.setCountry(weixinUserInfo.getCountry());
		userInfo.setGroupid(weixinUserInfo.getGroupid());
		userInfo.setHeadimgurl(weixinUserInfo.getHeadimgurl());
		userInfo.setLanguage(weixinUserInfo.getLanguage());
		userInfo.setNickname(weixinUserInfo.getNickname());
		userInfo.setProvince(weixinUserInfo.getProvince());
		userInfo.setRemark(weixinUserInfo.getRemark());
		userInfo.setSex(weixinUserInfo.getSex());
		userInfo.setSubscribe(weixinUserInfo.getSubscribe());
		userInfo.setSubscribe_time(weixinUserInfo.getSubscribe_time());
		userInfo.setUnionid(weixinUserInfo.getUnionid());
		commonDao.update(userInfo);
		return ReturnUtil.returnMap(1, null);
	}

	@Override
	public Map<String, Object> addUserRelation(String openid, String phone) {
		if(openid==null || "".equals(openid)){
			return ReturnUtil.returnMap(0, "openid不能为空");
		}
		if(phone==null || "".equals(phone)){
			return ReturnUtil.returnMap(0, "phone不能为空");
		}
		WeixinUserInfo userInfo = (WeixinUserInfo)commonDao.getObjectByUniqueCode("WeixinUserInfo", "openid", openid);
		if(userInfo==null){
			//commonDao.save(weixinUserInfo);
			return ReturnUtil.returnMap(0, "绑定失败，请尝试取消关注后再重新关注然后绑定~");
		}
		userInfo.setPhone(phone);
		commonDao.update(userInfo);
		return ReturnUtil.returnMap(1, "绑定成功！\n您绑定的手机号为："+phone);
	}
}
