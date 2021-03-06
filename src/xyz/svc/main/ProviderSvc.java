package xyz.svc.main;

import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public interface ProviderSvc {
	
	public Map<String,Object> queryProviderList(String nameCn, 
			String type,
			int offset,
			int pagesize);

	public Map<String,Object> addProvider(String nameCn,
			String type,
			String level,
			String levelSystem,
			String phone,
			String qq,
			String email,
			String linkman,
			String address,
			String remark
			);
	
	public Map<String,Object> editProvider(String numberCode,
			String nameCn,
			String type,
			String level,
			String levelSystem,
			String phone,
			String qq,
			String email,
			String linkman,
			String address,
			String remark);
	
	public Map<String,Object> deleteProvider(String numberCodes);

	public Map<String, Object> editProviderImage(String numberCode,
			String imageUrl);
	
	public Map<String, Object> editOnlineFlag(String numberCode, int value);
}
