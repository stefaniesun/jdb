package xyz.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import xyz.exception.MyExceptionForRole;
import xyz.model.member.XyzSessionLogin;
import xyz.model.member.XyzSessionUtil;

@Component
public class MySpecialFilter implements Filter{
	private static String[] publicUrls = new String[]{
		"/SellerWS/loginOper.xyz",
		"/InitWS/init_1239127awdasd_api.xyz",
		"/LoginWS/login.xyz",
		"/LoginWS/alterPassword.xyz",
		"/PayWS/wxPayNotify.xyz",
		"/PayWS/alipayNotify.xyz",
		"/BuyerScenicWS/queryScenicList.app",
		"/BuyerScenicWS/getScenicProduct.app",
		"/BuyerHotelWS/queryHotelList.app",
		"/BuyerHotelWS/getHotelProduct.app",
		"/CustomerWS/login.app",
		"/CustomerWS/wxLogin.app",
		"/CustomerWS/decideLogin.app",
		"/CustomerWS/exit.app",
		"/CustomerWS/getRandCode.app",
		"/CustomerWS/verifyRandCode.app",
		"/CustomerWS/register.app",
		"/CustomerWS/alterPassword.app",
		"/CustomerWS/recoverPassword.app",
		"/ProductStockWS/queryProductStockList.app",
		"/BuyerOrderWS/paySuccessOper.app",
		"/BuyerProviderWS/queryProviderList.app",
		"/BuyerProviderWS/queryScenicProviderList.app",
		"/BuyerProviderWS/queryHotelProviderList.app",
		"/BuyerProviderWS/getProvider.app",
		"/WeChatWS/mall.xyz",
		"/WeixinWS/xztrip.xyz"
	};
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		;
	}

	@Override
	public void doFilter(
			ServletRequest request1, 
			ServletResponse response1,
			FilterChain chain) 
					throws IOException, ServletException {
		HttpServletRequest httpServletRequest = (HttpServletRequest)request1;
		String apikey = httpServletRequest.getParameter("apikey");
		if(apikey==null){
			Cookie[] ttt = httpServletRequest.getCookies();
			if(ttt!=null){
				for(Cookie cookie : ttt){
					if("XZ_LOGIN_KEY".equals(cookie.getName())){
						apikey = cookie.getValue();
					}
				}
			}
		}
		if(apikey==null){
			apikey = request1.getParameter("apikey");
		}
		if(apikey!=null){
			XyzSessionLogin xyzSessionLogin = XyzSessionUtil.logins.get(apikey);
			if(xyzSessionLogin!=null){
				request1.setAttribute("xyzSessionLogin", xyzSessionLogin);
			}
		}
		
		String path = httpServletRequest.getServletPath();
		boolean flag = false;
		for(String url : publicUrls){
			if(path.equals(url)){
				flag = true;
			}
		}
		if(flag){
			chain.doFilter(request1, response1);
		}else{
			throw new MyExceptionForRole("您无权访问！");
		}
	}
	
	@Override
	public void destroy() {
		;
	}
}
