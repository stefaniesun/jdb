package xyz.svc.buyer.imp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import xyz.dao.CommonDao;
import xyz.filter.ReturnUtil;
import xyz.model.main.Provider;
import xyz.svc.buyer.BuyerProviderSvc;

@Service
public class BuyerProviderSvcImp implements BuyerProviderSvc {

	@Autowired
	CommonDao commonDao;
	
	@Override
	public Map<String, Object> queryProviderList(String nameCn, String type,
			String minPrice, String maxPrice, int offset, int pagesize) {


		String hql=" from Provider where onlineFlag=1 and price is not null ";
		if(!"".equals(type)&&type!=null){
			hql+=" and type = '"+type+"' ";
		}
		if(!"".equals(minPrice)&&minPrice!=null){
			hql+=" and price>="+minPrice;
		}
		if(!"".equals(maxPrice)&&maxPrice!=null){
			hql+=" and price<="+maxPrice;
		}
		if(!"".equals(nameCn)&&nameCn!=null){
			hql+=" and nameCn like '%"+nameCn+"%' ";
		}
		
		String countHql = "select count(numberCode) "+hql;
		Query countQuery = commonDao.getQuery(countHql);
		Number countTemp = (Number)countQuery.uniqueResult();
		int count = countTemp==null?0:countTemp.intValue();
		
		Query query = commonDao.getQuery(hql);
		query.setMaxResults(pagesize);
		query.setFirstResult(offset);
		@SuppressWarnings("unchecked")
		List<Provider> providerList=query.list();
		Map<String,Object> mapContent=new HashMap<String, Object>();
		mapContent.put("total", count);
		mapContent.put("rows",providerList);
		return ReturnUtil.returnMap(1, mapContent);
	
	}

	@Override
	public Map<String, Object> getProvider(String numberCode) {
		Provider provider=(Provider) commonDao.getObjectByUniqueCode("Provider", "numberCode", numberCode);
		if(provider==null){
			return ReturnUtil.returnMap(0, null);
		}
		return ReturnUtil.returnMap(1, provider);
	}

}
