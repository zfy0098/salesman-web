package com.rhjf.salesman.web.controller;

import com.rhjf.account.modle.domain.salesman.LoginUser;
import com.rhjf.account.modle.domain.salesman.ParamterData;
import com.rhjf.account.modle.domain.salesman.TermKey;
import com.rhjf.salesman.core.constants.Constants;
import com.rhjf.salesman.core.constants.RespCode;
import com.rhjf.salesman.core.service.LoginService;
import com.rhjf.salesman.core.util.DESUtil;
import com.rhjf.salesman.core.util.PropertyUtils;
import com.rhjf.salesman.core.util.UtilsConstant;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 *    客户端接口访问入口
 * @author hadoop
 *
 */


@RestController
@RequestMapping("/request")
public class RequestEntryController {
	

	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	private Logger hearLog = LoggerFactory.getLogger("hearAppender");

	@RequestMapping(value = "" , method = RequestMethod.POST)
	public Object RequestEntry(@RequestParam(value = "data") String data , HttpServletRequest request){

		ParamterData paramter = null;

		try {

			StringBuffer sbf = new StringBuffer();
			Enumeration<String> hearkey = request.getHeaderNames();
			while (hearkey.hasMoreElements()) {
				String key = hearkey.nextElement();
				sbf.append(key);
				sbf.append("=");
				sbf.append(request.getHeader(key));
				sbf.append("&");
			}

			sbf.append("ip=");
			sbf.append(UtilsConstant.getRequestIP(request));

			hearLog.trace(sbf.toString());

			if(UtilsConstant.strIsEmpty(data)){
				log.info("终端请求报文为空,停止交易");
				JSONObject json = new JSONObject();
				json.put("respCode" ,RespCode.ParamsError[0]);
				json.put("respDesc" ,RespCode.ParamsError[1]);
				return paraFilterReturn(json);
			}
			log.info("请求报文：" + data.replace("\n", "").replace(" ", ""));

			JSONObject json = JSONObject.fromObject(data);
			Map<String,Object> map = UtilsConstant.jsonToMap(json);
			paramter = UtilsConstant.mapToBean(map, ParamterData.class);

			/** 发送时间  **/
			if(UtilsConstant.strIsEmpty(paramter.getSendTime())){
				log.info("发送时间sendTime为空");
				paramter.setRespCode(RespCode.ParamsError[0]);
				paramter.setRespDesc(RespCode.ParamsError[1]);
				return paraFilterReturn(paramter);
			}


			/** 请求交易类型 **/
			String Txndir = paramter.getTxndir();
			if(UtilsConstant.strIsEmpty(Txndir)){
				log.info("交易类型txndir为空");
				paramter.setRespCode(RespCode.TxndirError[0]);
				paramter.setRespDesc(RespCode.TxndirError[1]);
				return paramter;
			}

			String trade = PropertyUtils.getValue(Txndir);
			if(UtilsConstant.strIsEmpty(trade)){
				log.info("交易类型：" + Txndir + ", 系统未配置该交易类型");
				paramter.setRespCode(RespCode.TxndirError[0]);
				paramter.setRespDesc(RespCode.TxndirError[1]);
				return paraFilterReturn(paramter);
			}

			/** 终端流水号 **/
			String sendSeqID = paramter.getSendSeqId();
			if(UtilsConstant.strIsEmpty(sendSeqID)){
				log.info("终端流水号sendSeqId为空");
				paramter.setRespCode(RespCode.ParamsError[0]);
				paramter.setRespDesc(RespCode.ParamsError[1]);
				return paraFilterReturn(paramter);
			}

			/**  终端登录信息 **/
			String loginPSN = paramter.getTerminalInfo();
			if(UtilsConstant.strIsEmpty(loginPSN)){
				log.info("登录信息(PSN)terminalInfo为空");
				paramter.setRespCode(RespCode.ParamsError[0]);
				paramter.setRespDesc(RespCode.ParamsError[1]);
				return  paraFilterReturn(paramter);
			}


			log.info("请求class信息:" + trade);
			
			/** 执行beanid **/
			String className = trade.split(",")[0];
			/** 执行的方法名 **/
			String methodName = trade.split(",")[1];
			/** 是否需要登录信息 **/
			String needLogin = trade.split(",")[2];
			/** 是否需要mac **/
			String needmac = trade.split(",")[3];
			

			ApplicationContext applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(request.getServletContext());

			LoginUser user = null;

			String mackey = null;

			/** 是否需要登录信息  **/
			if("1".equals(needLogin)){
				/** 获取登录信息  **/


				if(applicationContext == null){
					log.info("applicationContext is null");
				}else{
					log.info("applicationContext  不是空的 ");
				}

				LoginService loginService = applicationContext.getBean("loginService" , LoginService.class);

				if(loginService == null){
					log.info("loginService is null");
				}else{
					log.info("loginService ------------------  不是空的");
				}


				user = loginService.userInfo(paramter.getLoginID());
				if(user == null){

					log.info("获取用户：" + paramter.getLoginID() + "失败，系统不存在该账户");

					paramter.setRespCode(RespCode.userDoesNotExist[0]);
					paramter.setRespDesc(RespCode.userDoesNotExist[1]);
				}else{

					if(!Txndir.startsWith("A")&&!Txndir.equals("C")){
						if(!paramter.getTerminalInfo().equals(user.getLoginPSN())){
							log.info(user.getLoginID() + "被其他设备登录 , 终端上传: " + paramter.getTerminalInfo() + ",数据库保存" +user.getLoginPSN() );
							paramter.setRespCode(RespCode.LOGINError[0]);
							paramter.setRespDesc(RespCode.LOGINError[1]);
							return paraFilterReturn(paramter);
						}
					}

					boolean flag = true;
					if("1".equals(needmac)){
						/** 需要校验mac **/
						//  获取用户秘钥信息
						TermKey termkey = loginService.userTermkey(user.getID());
						mackey = termkey.getMacKey();

 						String mac = makeMac( mackey , JSONObject.fromObject(data), user);
 						if(!mac.equals(paramter.getMac())){
							flag = false;
							paramter.setRespCode(RespCode.SIGNMACError[0]);
							paramter.setRespDesc(RespCode.SIGNMACError[1]);
							System.out.println("验证mac失败，终端上送mac=[" + paramter.getMac() + "],平台计算mac=" + mac);
							log.info("验证mac失败，终端上送mac=[" + paramter.getMac() + "],平台计算mac=" + mac);
 						}else{
 							log.info("mac校验通过 == " + mac);
 						}
					}
					if(flag){
						Object obj = applicationContext.getBean(className);
						Method m = obj.getClass().getMethod(methodName, new Class[]{LoginUser.class  ,   ParamterData.class});
						paramter = (ParamterData) m.invoke(obj , user , paramter);
					}
				}
			}else{
				/** 不需要登录信息 **/
				Object obj = applicationContext.getBean(className);
				Method m = obj.getClass().getMethod(methodName, new Class[]{ ParamterData.class});
				paramter = (ParamterData) m.invoke(obj , paramter);
			}


			/** 如果请求需要校验mac 那么响应报文中也需要添加mac字段. 加密数据为返回的报文 **/
			if(needmac.equals("1")){
				String mac = makeMac(mackey , JSONObject.fromObject(paramter), user);
				log.info("响应报文中的mac" + mac);
				paramter.setMac(mac);
			}

		} catch (Exception e) {
			log.error("请求异常"  , e);
			paramter.setRespDesc(RespCode.NETWORKError[0]);
			paramter.setRespDesc(RespCode.NETWORKError[1]);
		}

		Object obj = paraFilterReturn(paramter);
		log.info("响应报文：" + obj.toString());

		return obj;
	}
	
	
	/**
	 *    去除参数提中 value 为null 的字段
	 * @param obj
	 * @return
	 */
	public Object paraFilterReturn(Object obj) {
		
		Map<String, Object> sArray = UtilsConstant.jsonToMap(JSONObject.fromObject(obj));
		Map<String, Object> sArray2 = new HashMap<String, Object>();
		if (sArray == null || sArray.size() <= 0) {
			return "";
		}
		for (String key : sArray.keySet()) {
			String value = sArray.get(key) + "";
			if ((value == null || value.equals(""))) {
				continue;
			}
			sArray2.put(key + "", value);
		}
		return JSONObject.fromObject(sArray2);
	}
	
	
	/**
	 *   计算mac
	 * @param json
	 * @param user
	 * @return
	 */
    public String makeMac( String mackey , JSONObject json,LoginUser user){
    	
    	Map<String, Object> contentData = UtilsConstant.jsonToMap(json);
		String macStr = "";
		Object[] key_arr = contentData.keySet().toArray();
		Arrays.sort(key_arr);
		for (Object key : key_arr) {
			Object value = contentData.get(key);
			if (value != null&&!UtilsConstant.strIsEmpty(value.toString())){ 
				if (!key.equals("mac")&&!key.equals("signImg")) {
					macStr += value.toString();
				}
			}
		}
		log.info("加密原文：macStr:" + macStr);
		log.info("加密秘钥密文:" + mackey);
		String rMac = DESUtil.mac(macStr, mackey , Constants.DBINITKEY);
		return rMac;
    }
}
