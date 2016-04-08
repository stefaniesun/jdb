package xyz.svc.buyer;

import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public interface BuyerProviderSvc {

	public Map<String,Object> queryProviderList(String nameCn,String type,String minPrice,String maxPrice, 
			int offset,
			int pagesize);

	public Map<String, Object> getProvider(String numberCode);
}
