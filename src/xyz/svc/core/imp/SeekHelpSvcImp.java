package xyz.svc.core.imp;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import xyz.dao.CommonDao;
import xyz.filter.JSON;
import xyz.filter.ReturnUtil;
import xyz.model.core.SeekHelp;
import xyz.model.member.WeixinUserInfo;
import xyz.svc.core.SeekHelpSvc;
import xyz.util.StringTool;
import xyz.util.UUIDUtil;

@Service
public class SeekHelpSvcImp implements SeekHelpSvc {

	@Autowired
	private CommonDao commonDao;
	
	@Override
	public Map<String, Object> querySeekHelpList(int offset, int pagesize) {
		String hql = "from SeekHelp order by addDate desc";
		Query queryNum = commonDao.getQuery("select count(iidd) "+hql);
		Number num = (Number)queryNum.uniqueResult();
		int count = num==null?0:num.intValue();
		
		Query query = commonDao.getQuery(hql);
		query.setMaxResults(pagesize);
		query.setFirstResult(offset);
		@SuppressWarnings("unchecked")
		List<SeekHelp> list = query.list();
		
		List<String> openidList = new ArrayList<String>();
		for(SeekHelp seekHelp : list){
			openidList.add(seekHelp.getOpenid());
		}
		@SuppressWarnings("unchecked")
		List<Object[]> phoneList = commonDao.getSqlQuery("select openid,phone from weixin_user_info where openid in("+StringTool.listToSqlString(openidList)+")").list();
		for(SeekHelp seekHelp : list){
			String openid = seekHelp.getOpenid();
			for(Object[] arr : phoneList){
				if(openid!=null && openid.equals(arr[0])){
					seekHelp.setPhone(arr[1]==null?"":arr[1].toString());
					break;
				}
			}
		}
		
		Map<String,Object> mapContent = new HashMap<String, Object>();
		mapContent.put("total",count);
		mapContent.put("rows",list);
		return ReturnUtil.returnMap(1,mapContent);
	}

	@Override
	public Map<String, Object> addSeekHelp(String openid, String location_x,
			String location_y, String label, String scale) {
		if(openid==null || "".equals(openid)){
			return ReturnUtil.returnMap(0, "缺少openid");
		}
		if(location_x==null || "".equals(location_x)){
			return ReturnUtil.returnMap(0, "经纬度获取异常");
		}
		if(location_y==null || "".equals(location_y)){
			return ReturnUtil.returnMap(0, "经纬度获取异常");
		}
		WeixinUserInfo userInfo = (WeixinUserInfo)commonDao.getObjectByUniqueCode("WeixinUserInfo", "openid", openid);
		SeekHelp seekHelp = new SeekHelp();
		if(userInfo!=null){
			seekHelp.setHeadimgurl(userInfo.getHeadimgurl());
			seekHelp.setNickname(userInfo.getNickname());
		}
		Date date = new Date();
		seekHelp.setNumberCode(UUIDUtil.getUUIDStringFor32());
		seekHelp.setOpenid(openid);
		seekHelp.setLabel(label);
		seekHelp.setLocationX(location_x);
		seekHelp.setLocationY(location_y);
		seekHelp.setScale(scale);
		seekHelp.setStatus(0);
		seekHelp.setAddDate(date);
		seekHelp.setAlterDate(date);
		commonDao.save(seekHelp);
		String tips = "求助已发出！";
		if(userInfo!=null && userInfo.getPhone()!=null && "".equals(userInfo.getPhone())){
			tips += "\n我们将尽快与您取得联系，您预留的联系手机号是【"+userInfo.getPhone()+"】";
		}
		return ReturnUtil.returnMap(1, tips);
	}

	@Override
	public Map<String, Object> updateSeekHelp(String numberCode) {
		SeekHelp seekHelp = (SeekHelp)commonDao.getObjectByUniqueCode("SeekHelp", "numberCode", numberCode);
		if(seekHelp==null){
			return ReturnUtil.returnMap(0, "数据不存在");
		}
		seekHelp.setStatus(seekHelp.getStatus()==0?1:0);
		commonDao.update(seekHelp);
		return ReturnUtil.returnMap(1, null);
	}

	@Override
	public Map<String, Object> deleteSeekHelp(String numberCode) {
		SeekHelp seekHelp = (SeekHelp)commonDao.getObjectByUniqueCode("SeekHelp", "numberCode", numberCode);
		if(seekHelp==null){
			return ReturnUtil.returnMap(0, "数据不存在");
		}
		commonDao.delete(seekHelp);
		return ReturnUtil.returnMap(1, null);
	}

}
