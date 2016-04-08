package com.alipay.web.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import xyz.filter.JSON;

import com.alipay.config.AlipayConfig;
import com.alipay.sign.MD5;
import com.alipay.sign.RSA;
import com.alipay.sign.SignUtils;
import com.alipay.util.AlipayNotify;
import com.alipay.util.AlipaySubmit;
import com.alipay.web.dao.BasicDao;
import com.alipay.web.model.Bill;

public class PayService {
	private final static PayService instance = new PayService();

	private PayService() {
	}

	public static PayService getInstance() {
		return instance;
	}
	
	public String doReturn(HttpServletRequest request) throws UnsupportedEncodingException{
		//获取支付宝POST过来反馈信息
				Map<String,String> params = new HashMap<String,String>();
				Map requestParams = request.getParameterMap();
				for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
					String name = (String) iter.next();
					String[] values = (String[]) requestParams.get(name);
					String valueStr = "";
					for (int i = 0; i < values.length; i++) {
						valueStr = (i == values.length - 1) ? valueStr + values[i]
								: valueStr + values[i] + ",";
					}
					//乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
					valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
					params.put(name, valueStr);
				}
				
				//获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以下仅供参考)//
				//商户订单号

				String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"),"UTF-8");

				//支付宝交易号

				String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"),"UTF-8");

				//交易状态
				String trade_status = new String(request.getParameter("trade_status").getBytes("ISO-8859-1"),"UTF-8");

				//获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以上仅供参考)//
				
				Bill oldBill = viewBill(out_trade_no);
				if(null!=oldBill){
					if(AlipayNotify.verify(params)){//验证成功
						//////////////////////////////////////////////////////////////////////////////////////////
						//请在这里加上商户的业务逻辑程序代码
						
						//——请根据您的业务逻辑来编写程序（以下代码仅作参考）——
						
						Bill bill = new Bill();
						bill.setBill_no(out_trade_no);
						bill.setBill_pay_time(new Date());
						bill.setNotify_status(trade_status);
						bill.setThird_trade_no(trade_no);
						bill.setNotify_time(new Date());
						
						bill.setBill_status("1");//处理中
						
						if(trade_status.equals("TRADE_FINISHED")){
							//判断该笔订单是否在商户网站中已经做过处理
							//如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
							//请务必判断请求时的total_fee、seller_id与通知时获取的total_fee、seller_id为一致的
							//如果有做过处理，不执行商户的业务程序
							bill.setBill_pay_time(new Date());
							bill.setBill_status("4");//交易成功，可退货
							//注意：
							//退款日期超过可退款期限后（如三个月可退款），支付宝系统发送该交易状态通知
						} else if (trade_status.equals("TRADE_SUCCESS")){
							//判断该笔订单是否在商户网站中已经做过处理
							//如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
							//请务必判断请求时的total_fee、seller_id与通知时获取的total_fee、seller_id为一致的
							//如果有做过处理，不执行商户的业务程序
							bill.setBill_pay_time(new Date());
							bill.setBill_status("5");//交易完成，不能退货
							//注意：
							//付款完成后，支付宝系统发送该交易状态通知
						}
						
						updateBill(bill);
						
						//——请根据您的业务逻辑来编写程序（以上代码仅作参考）——
						
						return (bill.getBill_status().equals("4")||bill.getBill_status().equals("5"))?oldBill.getSuccess_url():oldBill.getFail_url();	//请不要修改或删除
						
						//////////////////////////////////////////////////////////////////////////////////////////
					}else{//验证失败
						return oldBill.getFail_url();
					}
				}else{
					return "fail.html";
				}

	}
	
	public String doNotify(HttpServletRequest request) throws UnsupportedEncodingException{
		//获取支付宝POST过来反馈信息
		Map<String,String> params = new HashMap<String,String>();
		Map requestParams = request.getParameterMap();
		for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
			String name = (String) iter.next();
			String[] values = (String[]) requestParams.get(name);
			String valueStr = "";
			for (int i = 0; i < values.length; i++) {
				valueStr = (i == values.length - 1) ? valueStr + values[i]
						: valueStr + values[i] + ",";
			}
			//乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
			//valueStr = new String(valueStr.getBytes("ISO-8859-1"), "gbk");
			params.put(name, valueStr);
		}
		
		System.out.println("====================alipay回调start================");
		System.out.println(JSON.toJosn(requestParams));
		System.out.println("====================alipay回调end================");
		
		//获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以下仅供参考)//
		//商户订单号

		String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"),"UTF-8");

		//支付宝交易号

		String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"),"UTF-8");

		//交易状态
		String trade_status = new String(request.getParameter("trade_status").getBytes("ISO-8859-1"),"UTF-8");

		//获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以上仅供参考)//

		if(AlipayNotify.verify(params)){//验证成功
			//////////////////////////////////////////////////////////////////////////////////////////
			//请在这里加上商户的业务逻辑程序代码

			//——请根据您的业务逻辑来编写程序（以下代码仅作参考）——
			
			Bill bill = new Bill();
			bill.setBill_no(out_trade_no);
			bill.setBill_pay_time(new Date());
			bill.setNotify_status(trade_status);
			bill.setThird_trade_no(trade_no);
			bill.setNotify_time(new Date());
			
			bill.setBill_status("1");//处理中
			
			if(trade_status.equals("TRADE_FINISHED")){
				//判断该笔订单是否在商户网站中已经做过处理
					//如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
					//请务必判断请求时的total_fee、seller_id与通知时获取的total_fee、seller_id为一致的
					//如果有做过处理，不执行商户的业务程序
					bill.setBill_pay_time(new Date());
					bill.setBill_status("4");//交易成功，可退货
				//注意：
				//退款日期超过可退款期限后（如三个月可退款），支付宝系统发送该交易状态通知
			} else if (trade_status.equals("TRADE_SUCCESS")){
				//判断该笔订单是否在商户网站中已经做过处理
					//如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
					//请务必判断请求时的total_fee、seller_id与通知时获取的total_fee、seller_id为一致的
					//如果有做过处理，不执行商户的业务程序
				bill.setBill_pay_time(new Date());
				bill.setBill_status("5");//交易完成，不能退货
				//注意：
				//付款完成后，支付宝系统发送该交易状态通知
			}
			
			updateBill(bill);

			//——请根据您的业务逻辑来编写程序（以上代码仅作参考）——
				
			return "success";	//请不要修改或删除

			//////////////////////////////////////////////////////////////////////////////////////////
		}else{//验证失败
			return "fail";
		}
	}

	public String getAlipayForm(String bill_no) throws UnsupportedEncodingException {
		Bill bill = viewBill(bill_no);
		// 支付类型
		String payment_type = "1";
		// 必填，不能修改
		// 服务器异步通知页面路径
		String notify_url = bill.getNotify_url();
		// 需http://格式的完整路径，不能加?id=123这类自定义参数

		// 页面跳转同步通知页面路径
		String return_url = bill.getReturn_url();
		// 需http://格式的完整路径，不能加?id=123这类自定义参数，不能写成http://localhost/

		// 商户订单号
		String out_trade_no = bill.getBill_no();
		// 商户网站订单系统中唯一订单号，必填

		// 订单名称
		String subject = bill.getOrder_subject();
		// 必填

		// 付款金额
		String total_fee = bill.getOrder_amount();
		// 必填

		// 商品展示地址
		String show_url = bill.getShow_url();
		// 必填，需以http://开头的完整路径，例如：http://www.商户网址.com/myorder.html

		// 订单描述
		String body = bill.getOrder_body();
		// 选填

		// 超时时间
		String it_b_pay = "";
		// 选填

		// 钱包token
		String extern_token ="";
		// 选填

		// ////////////////////////////////////////////////////////////////////////////////

		// 把请求参数打包成数组
		Map<String, String> sParaTemp = new HashMap<String, String>();
		sParaTemp.put("service", "alipay.wap.create.direct.pay.by.user");
		// sParaTemp.put("service", "create_direct_pay_by_user");
		sParaTemp.put("partner", AlipayConfig.partner);
		sParaTemp.put("seller_id", AlipayConfig.seller_id);
		sParaTemp.put("_input_charset", AlipayConfig.input_charset);
		sParaTemp.put("payment_type", payment_type);
		sParaTemp.put("notify_url", notify_url);
		sParaTemp.put("return_url", return_url);
		sParaTemp.put("out_trade_no", out_trade_no);
		sParaTemp.put("subject", subject);
		sParaTemp.put("total_fee", total_fee);
		sParaTemp.put("show_url", show_url);
		sParaTemp.put("body", body);
		sParaTemp.put("it_b_pay", it_b_pay);
		sParaTemp.put("extern_token", extern_token);

		// 建立请求
		String sHtmlText = AlipaySubmit.buildRequest(sParaTemp, "get", "付款");
		
		
		System.out.println("getAlipayMobile======"+getAlipayMobile(bill_no));
		
		return sHtmlText;
	}

	public Bill viewBill(String bill_no) {
		Connection conn = null;
		PreparedStatement ps = null;
		Bill bill = null;
		try {
			// 插入新库
			String sql = "select * from bill where bill_no = ?";
			conn = BasicDao.getConnection();
			ps = conn.prepareStatement(sql);
			ps.setObject(1, bill_no);
			ResultSet rs = ps.executeQuery();
			if (null != rs && rs.next()) {
				bill = new Bill();
				bill.setBill_no(rs.getString("bill_no"));
				bill.setPay_type(rs.getString("pay_type"));
				bill.setOrder_amount(rs.getString("order_amount"));
				bill.setOrder_no(rs.getString("order_no"));
				bill.setOrder_subject(rs.getString("order_subject"));
				bill.setOrder_body(rs.getString("order_body"));
				bill.setBill_create_time(rs.getDate("bill_create_time"));
				bill.setBill_pay_time(rs.getDate("bill_pay_time"));
				bill.setBill_status(rs.getString("bill_status"));
				bill.setNotify_time(rs.getDate("notify_time"));
				bill.setNotify_msg(rs.getString("notify_msg"));
				bill.setNotify_status(rs.getString("notify_status"));
				bill.setBack_url(rs.getString("back_url"));
				bill.setReturn_url(rs.getString("return_url"));
				bill.setNotify_url(rs.getString("notify_url"));
				bill.setShow_url(rs.getString("show_url"));
				bill.setThird_trade_no(rs.getString("third_trade_no"));
				bill.setSuccess_url(rs.getString("success_url"));
				bill.setFail_url(rs.getString("fail_url"));
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return bill;
	}

	public void updateBill(Bill bill) {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			// 插入新库
			String sql = "update bill set bill_status = ? , bill_pay_time = ? , notify_time = ? , notify_status = ? , notify_msg = ?,third_trade_no=? where bill_no = ?";
			conn = BasicDao.getConnection();
			ps = conn.prepareStatement(sql);
			ps.setObject(1, bill.getBill_status());
			ps.setObject(2, bill.getBill_pay_time());
			ps.setObject(3, bill.getNotify_time());
			ps.setObject(4, bill.getNotify_status());
			ps.setObject(5, bill.getNotify_msg());
			ps.setObject(6, bill.getThird_trade_no());
			ps.setObject(7, bill.getBill_no());
			ps.execute();
			conn.commit();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public void createBill(Bill bill) {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			// 插入新库
			String sql = "insert into bill(bill_no,pay_type,order_no,order_subject,order_amount,order_body,bill_status,bill_create_time,back_url,success_url,fail_url,return_url,notify_url,show_url) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			conn = BasicDao.getConnection();
			ps = conn.prepareStatement(sql);
			ps.setObject(1, bill.getBill_no());
			ps.setObject(2, bill.getPay_type());
			ps.setObject(3, bill.getOrder_no());
			ps.setObject(4, bill.getOrder_subject());
			ps.setObject(5, bill.getOrder_amount());
			ps.setObject(6, bill.getOrder_body());
			ps.setObject(7, bill.getBill_status());
			ps.setObject(8, bill.getBill_create_time());
			ps.setObject(9, bill.getBack_url());
			ps.setObject(10, bill.getSuccess_url());
			ps.setObject(11, bill.getFail_url());
			ps.setObject(12, bill.getReturn_url());
			ps.setObject(13, bill.getNotify_url());
			ps.setObject(14, bill.getShow_url());
			ps.execute();
			conn.commit();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public void toPay(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// //////////////////////////////////请求参数//////////////////////////////////////
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		// 支付类型
		String payment_type = "1";
		// 必填，不能修改
		// 服务器异步通知页面路径
		String notify_url = "http://localhost:8080/alipaywap/notify_url.jsp";
		// 需http://格式的完整路径，不能加?id=123这类自定义参数

		// 页面跳转同步通知页面路径
		String return_url = "http://localhost:8080/alipaywap/return_url.jsp";
		// 需http://格式的完整路径，不能加?id=123这类自定义参数，不能写成http://localhost/

		// 商户订单号
		String out_trade_no = new String(request
				.getParameter("WIDout_trade_no").getBytes("ISO-8859-1"),
				"UTF-8");
		// 商户网站订单系统中唯一订单号，必填

		// 订单名称
		String subject = new String(request.getParameter("WIDsubject")
				.getBytes("ISO-8859-1"), "UTF-8");
		// 必填

		// 付款金额
		String total_fee = new String(request.getParameter("WIDtotal_fee")
				.getBytes("ISO-8859-1"), "UTF-8");
		// 必填

		// 商品展示地址
		String show_url = new String(request.getParameter("WIDshow_url")
				.getBytes("ISO-8859-1"), "UTF-8");
		// 必填，需以http://开头的完整路径，例如：http://www.商户网址.com/myorder.html

		// 订单描述
		String body = new String(request.getParameter("WIDbody").getBytes(
				"ISO-8859-1"), "UTF-8");
		// 选填

		// 超时时间
		String it_b_pay = new String(request.getParameter("WIDit_b_pay")
				.getBytes("ISO-8859-1"), "UTF-8");
		// 选填

		// 钱包token
		String extern_token = new String(request
				.getParameter("WIDextern_token").getBytes("ISO-8859-1"),
				"UTF-8");
		// 选填

		// ////////////////////////////////////////////////////////////////////////////////

		// 把请求参数打包成数组
		Map<String, String> sParaTemp = new HashMap<String, String>();
		sParaTemp.put("service", "alipay.wap.create.direct.pay.by.user");
		// sParaTemp.put("service", "create_direct_pay_by_user");
		sParaTemp.put("partner", AlipayConfig.partner);
		sParaTemp.put("seller_id", AlipayConfig.seller_id);
		sParaTemp.put("_input_charset", AlipayConfig.input_charset);
		sParaTemp.put("payment_type", payment_type);
		sParaTemp.put("notify_url", notify_url);
		sParaTemp.put("return_url", return_url);
		sParaTemp.put("out_trade_no", out_trade_no);
		sParaTemp.put("subject", subject);
		sParaTemp.put("total_fee", total_fee);
		sParaTemp.put("show_url", show_url);
		sParaTemp.put("body", body);
		sParaTemp.put("it_b_pay", it_b_pay);
		sParaTemp.put("extern_token", extern_token);

		// 建立请求
		String sHtmlText = AlipaySubmit.buildRequest(sParaTemp, "get", "付款");
		// String sHtmlText = AlipaySubmit.buildRequest("","",sParaTemp);
		response.getWriter().println(sHtmlText);
	}
	
	/**
	 * 支付宝移动应用支付order字符串构建
	 * @return
	 */
	public String getAlipayMobile(String bill_no){
		Bill bill = viewBill(bill_no);
		// 签约合作者身份ID
		String orderInfo = "partner=" + "\"" + AlipayConfig.partner + "\"";

		// 签约卖家支付宝账号
		orderInfo += "&seller_id=" + "\"" + AlipayConfig.seller_id + "\"";

		// 商户网站唯一订单号
		orderInfo += "&out_trade_no=" + "\"" + bill.getBill_no() + "\"";

		// 商品名称
		orderInfo += "&subject=" + "\"" + bill.getOrder_subject() + "\"";

		// 商品详情
		orderInfo += "&body=" + "\"" + bill.getOrder_body() + "\"";

		// 商品金额
		orderInfo += "&total_fee=" + "\"" + bill.getOrder_amount() + "\"";

		// 服务器异步通知页面路径
		orderInfo += "&notify_url=" + "\"" + bill.getNotify_url() + "\"";

		// 服务接口名称， 固定值
		orderInfo += "&service=\"mobile.securitypay.pay\"";

		// 支付类型， 固定值
		orderInfo += "&payment_type=\"1\"";

		// 参数编码， 固定值
		orderInfo += "&_input_charset=\""+ AlipayConfig.input_charset +"\"";

		// 设置未付款交易的超时时间
		// 默认30分钟，一旦超时，该笔交易就会自动被关闭。
		// 取值范围：1m～15d。
		// m-分钟，h-小时，d-天，1c-当天（无论交易何时创建，都在0点关闭）。
		// 该参数数值不接受小数点，如1.5h，可转换为90m。
		orderInfo += "&it_b_pay=\"30m\"";

		// extern_token为经过快登授权获取到的alipay_open_id,带上此参数用户将使用授权的账户进行支付
		// orderInfo += "&extern_token=" + "\"" + extern_token + "\"";

		// 支付宝处理完请求后，当前页面跳转到商户指定页面的路径，可空
		orderInfo += "&return_url=\""+ bill.getReturn_url() +"\"";

		// 调用银行卡支付，需配置此参数，参与签名， 固定值 （需要签约《无线银行卡快捷支付》才能使用）
		// orderInfo += "&paymethod=\"expressGateway\"";
		
		String sign = SignUtils.sign(orderInfo, AlipayConfig.rsa_private_key);
		
//		String sign = MD5.sign(orderInfo, AlipayConfig.private_key, AlipayConfig.input_charset);
		
		try {
			sign = URLEncoder.encode(sign, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		final String payInfo = orderInfo+"&sign=\""+sign+"\"&sign_type=\"RSA\"";
		return payInfo;
	}
	
}
