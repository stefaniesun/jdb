package xyz.svc.core;

import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public interface SeekHelpSvc {
	
	public Map<String, Object> querySeekHelpList(int offset, int pagesize);
	
	public Map<String, Object> addSeekHelp(String openid, String location_x, String location_y, String label, String scale);
	
	public Map<String, Object> updateSeekHelp(String numberCode);
	
	public Map<String, Object> deleteSeekHelp(String numberCode);

}
