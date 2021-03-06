package xyz.svc.main;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public interface ProductStockSvc {
	public Map<String,Object> queryProductStockList(
			int offset,
			int pagesize,
			String product,
			Date dateStart,
			Date dateEnd);
	
	public Map<String, Object> queryProductStockAllList(String product);
	
	public Map<String,Object> addProductStock(
			String product, 
			int count, 
			String dateInfo, 
			BigDecimal price,
			int isAlterPrice);
	
	public Map<String,Object> deleteProductStock(String numberCodes);
	
	public Map<String,Object> deleteProductStock2(
			String product,
			String dateInfo
			);
	
	public Map<String,Object> queryLogProductStock(String numberCode,Date dateInfo);
	
	public Map<String,Object> queryProductForStockList(
			int offset,
			int pagesize,
			String product,
			Date dateInfo);
}
