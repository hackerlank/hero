package com.joymeng.game.test;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;
import hirondelle.date4j.DateTime;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.poi.hssf.record.formula.functions.T;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.quartz.CronTrigger;
import org.quartz.DateBuilder;
import org.quartz.JobDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springside.modules.test.data.DataFixtures;

import com.joymeng.core.base.domain.BaseUser;
import com.joymeng.core.db.cache.couchbase.CouchBaseUtil;
import com.joymeng.core.log.GameLog;
import com.joymeng.core.log.LogEvent;
import com.joymeng.core.scheduler.SchedulerServer;
import com.joymeng.core.scheduler.SimpleJob;
import com.joymeng.core.utils.FileUtil;
import com.joymeng.core.utils.MessageQueue;
import com.joymeng.core.utils.TimeUtils;
import com.joymeng.game.GameServerApp;
import com.joymeng.game.common.GameUtils;
import com.joymeng.game.db.DBManager;
import com.joymeng.game.domain.hero.HeroOptType;
import com.joymeng.game.domain.world.TipMessage;
import com.joymeng.game.job.ArenaRewardJob;
import com.joymeng.web.entity.User;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class Test {
	static Logger logger = LoggerFactory.getLogger(Test.class);
	int id = 0;

	public void setId(int i) {
		id = i;
	}

	public int getId() {
		return id;
	}

	private static final String REGEX = "\\d";
	private static final String INPUT = "one9two4three7four1five";

	/**
	 * @param args
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 * @throws JSONException 
	 * @throws IllegalStateException 
	 */
	public static void main(String[] args) throws Exception {
		MessageQueue queue=new MessageQueue();
		TipMessage tip1=new TipMessage("111", 0,(byte)0 );
		tip1.setPriotity(0);
		TipMessage tip2=new TipMessage("222", 0,(byte) 0);
		tip2.setPriotity(1);
		TipMessage tip3=new TipMessage("333", 0, (byte)0);
		tip3.setPriotity(3);
		TipMessage tip4=new TipMessage("444",0, (byte)0);
		tip4.setPriotity(4);
		queue.add(tip2);
		queue.add(tip3);
		queue.add(tip4);
		queue.add(tip1);
		System.out.println(queue.getMessage().getMessage());
		System.out.println(queue.getMessage().getMessage());
		System.out.println(queue.getMessage().getMessage());
		System.out.println(queue.getMessage().getMessage());
//		GameUtils.httpGet("http://127.0.0.1:8080/server");
//		GameUtils.httpPos("http://localhost:8080/server/"+65450+".json");
//		long time =Long.parseLong("1358497155472");
//		System.out.println(TimeUtils.getTime(time).format(TimeUtils.FORMAT1));
//		time =Long.parseLong("1358241481000");
//		System.out.println(TimeUtils.getTime(time).format(TimeUtils.FORMAT1));
//		System.out.println(StringUtils.trim("李 ")+" length="+StringUtils.trim("李 ").length());
//		System.out.println(StringUtils.trim("李 ").length()+" length="+StringUtils.trim("李 ").length());
//		System.out.println(" ".hashCode());
//		System.out.println("".hashCode());
//		System.out.println(" ".hashCode());
//		System.out.println(" ".hashCode());
//		System.out.println(Integer.MAX_VALUE+" "+Integer.MIN_VALUE);
//		String s="hello你好";
//		for(int i=0;i<s.length();i++){
//			System.out.println(s.charAt(i));
//		}
//		FileUtil.getFile("E://java//LoginServer2//lib");
		// User user=new User();
		// user.setName("hello");
		// int number1=123_456_789;
		// System.out.println(number1);
		/////////////////数据库脚本测试/////////////////
//		DBManager dbManager = DBManager.getInstance();
//		dbManager.init();
//		long time = TimeUtils.nowLong();
//		DataSource ds = dbManager.getWorldDAO().getDataSource();
//		DataFixtures.executeScript(ds,
//				"classpath:resource2/data/game_server1-init.sql",
//				"classpath:resource2/data/game_server1-clear.sql");
//		logger.info("初始数据库脚本耗时=" + (TimeUtils.nowLong() - time) / 1000);
		////////////////////任务调度测试/////////////////////////////
//		SchedulerServer scheduler = new SchedulerServer();
//		JobDetail job = null;
//		CronTrigger trigger = null;
////		 在当前时间15秒后运行
//		 Date startTime = DateBuilder.nextGivenSecondDate(null,15);
////		 当前时间的加上5分钟
//		 Date endTime = DateBuilder.nextGivenMinuteDate(null, 10);
//		 job = newJob(SimpleJob.class).withIdentity("job1", "group1").build();
//		 trigger = newTrigger().withIdentity("trigger1",
//		 "group1").startAt(startTime).endAt(endTime)
//		 .withSchedule(cronSchedule("0 0/1 * * * ?")).build();
//		 scheduler.addJob(job, trigger);
//		 try {
//			System.out.println("1="+ TimeUtils.nowLong());
//			scheduler.run();
//			System.out.println("2="+ TimeUtils.nowLong());
//			Thread.sleep(60*1000);
//			System.out.println("3="+ TimeUtils.nowLong());
//			scheduler.del(job.getKey());
//			Thread.sleep(60*1000);
//			System.out.println("4="+ TimeUtils.nowLong());
//			scheduler.addJob(job, trigger);
//			System.out.println("5="+ TimeUtils.nowLong());
//			System.exit(0);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		///////////////////////////////////////
//		new Thread(new Runnable(){
//			public void run(){
//				while(true){
//					System.out.println("1111");
//				}
//			}
//		}).run();
//		while(true){
//			
//		}
		// Pattern p = Pattern.compile(REGEX);
		// String[] items = p.split(INPUT);
		// for(String s : items) {
		// System.out.println(s);
		// }
		// mongoTest();
		// String str="651|LOGIN|2012-08-17 15:36:41|";
		// // String str="a,b,c,d,e";
		// String s[]=str.split("\\|");
		// for(int i=0;i<s.length;i++){
		// System.out.println("s=="+s[i]);
		// }
		// try {
		// CouchBaseUtil.connect(CouchBaseUtil.serverAddress);
		// CouchBaseUtil.put("r_1", "10", 0);
		// CouchBaseUtil.put("r_2", "11", 0);
		// CouchBaseUtil.put("r_3", "12", 0);
		// List<String> list=new ArrayList<String>();
		// list.add("r_1");
		// list.add("r_2");
		// list.add("r_3");
		// CouchBaseUtil.get(list);
		// } catch (Exception e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// RoleData role=RoleData.create();
		// System.out.println(role.toString());
		// GameLog.logEvent(LogEvent.ALLY_ADD_BUFF_SPEED);
		// Test a=new Test();
		// Test b=new Test();
		// Map map=new HashMap<Integer,Test>();
		// map.put(1, a);
		// map.put(2, b);
		// System.out.println(map.get(1));
		// System.out.println(map.get(2));
		// a=(Test)map.get(1);
		// a.setId(2);
		// map.put(1, a);
		// System.out.println(map.get(1));
		// System.out.println(map.get(2));
		// System.out.println(map.size());
		// System.out.println("".split(";").length);
		// short a=5;
		// System.out.println(((Integer)(4+1)).shortValue());
		// int[] a=new int[]{1,2,3,4,5};
		// System.out.println(GameUtils.getIntArrayLog(a, "a"));
		// String[] b={"1","2","3","4","5"};
		// System.out.println(GameUtils.getStringArrayLog(b, "b"));
		// ArrayList<Integer> c=new ArrayList<Integer>();
		// c.add(1);
		// c.add(2);
		// System.out.println(GameUtils.getListLog(c));
		// System.out.println("=="+0x6+" ="+0x06);
		// int j=0;
		// for(int i=0;i<10000;i++){
		// int id=MathUtils.getRandomId(new int[]{0,1,2,3,4,5}, new int[]{66,
		// 19, 14, 1, 0, 0}, 100);
		// if(id==0){
		// j++;
		// }
		// // logger.info(String.valueOf(id));
		// // }
		// JoyBuffer out=JoyBuffer.allocate(1024);
		// int index=out.position();
		// out.put((byte)1);
		// System.out.println("index="+out.position());
		// out.putInt(4);
		// System.out.println("index="+out.position());
		// System.out.println(0xFFDD);
		// System.out.println(0xFFBB);
		// System.out.println(0xFFCC);
		// createModelClient(Arena.class);
		// createModelClient(Arena.class);
		// addSql(Arena.class);
		// saveSql(Arena.class);
		// delSql(Arena.class);
		// selectSql(Arena.class);
		// System.out.println(TimeUtils.now().format(TimeUtils.FORMAT1));
		// String endTimestr = "14:37:00";
		// String str=TimeUtils.now().format("YYYY-MM-DD");
		// System.out.println(str+" "+endTimestr);
		// if(TimeUtils.getTime(str+" "+endTimestr).lteq(TimeUtils.now())){
		// System.out.println("<=now");
		// }else{
		// System.out.println(">=now");
		// }
		// System.out.println("today="+TimeUtils.today());
		// DateTime time=TimeUtils.today().plus(0, 0, 0, 2, 3, 4,
		// DateTime.DayOverflow.Abort);
		// System.out.println("today="+time);
		// String original = new String("A" + "\u00ea" + "\u00f1" + "\u00fc" +
		// "C");
		// try {
		// byte[] utf8Bytes = original.getBytes("UTF8");
		// byte[] defaultBytes = original.getBytes();
		//
		// String roundTrip = new String(utf8Bytes, "UTF8");
		// System.out.println("roundTrip=="+roundTrip);
		// String sss=new String(defaultBytes);
		// System.out.println("sss=="+sss);
		// System.out.println("roundTrip = " + roundTrip);
		// System.out.println();
		// printBytes(utf8Bytes, "utf8Bytes");
		// System.out.println();
		// printBytes(defaultBytes, "defaultBytes");
		//
		// byte[] data=new byte[]{-1, 1, 1, 1, 1, 2};
		// String str = new String(data, "ISO-8859-1");
		// byte data1[]=str.getBytes("ISO-8859-1");
		// for(int i=0;i<data1.length;i++){
		// System.out.println("data=="+data1[i]);
		// }
		// }
		// catch (UnsupportedEncodingException e) {
		// e.printStackTrace();
		// }
		// byte[] byteArray = new byte[] {1, 1, 1, 1, 1, 1};
		//
		// String value = new String(byteArray);
		//
		// byte data[]=value.getBytes();
		//
		// System.out.println(value);
	}

	public static void printBytes(byte[] array, String name) {
		for (int k = 0; k < array.length; k++) {
			// System.out.println(name + "[" + k + "] = " + "0x" +
			// UnicodeFormatter.byteToHex(array[k]));
		}
	}

	/**
	 * 生成client模块的序列化方法
	 */
	public static void createModelClient(Class<?> T) {
		System.out.println("---------serialize-----------");
		try {
			Object data = T.newInstance();// 创建对象
			Field[] fs = T.getDeclaredFields();// 获得属性
			String str = "out.put";
			System.out.println("out.put(getModuleType());");// 第一行
			for (int i = 0; i < fs.length; i++) {
				Field f = fs[i];

				f.setAccessible(true); // 设置些属性是可以访问的
				if (f.getName().startsWith("tmp_")) {
					continue;
				}
				if (f.getType() == int.class) {
					System.out.println(str + "Int(" + f.getName() + ");");
				} else if (f.getType() == long.class) {
					System.out.println(str + "Long(" + f.getName() + ");");
				} else if (f.getType() == boolean.class) {
					System.out.println("error");
				} else if (f.getType() == byte.class) {
					System.out.println(str + "(" + f.getName() + ");");
				} else if (f.getType() == String.class) {
					System.out.println(str + "PrefixedString(" + f.getName()
							+ ");");
				} else if (f.getType() == short.class) {
					System.out.println(str + "Short(" + f.getName() + ");");
				} else {
					// if (f.getType().toString().equals("class [I")) {// int[]
					// System.out.println("JoyBufferPlus.putIntArray(out, "
					// + f.getName() + ");");
					// } else if (f.getType().toString().equals("class [S")) {//
					// short[]
					// System.out.println("JoyBufferPlus.putShortArray(out, "
					// + f.getName() + ");");
					//
					// } else if (f.getType().toString().equals("class [B")) {//
					// byte[]
					// System.out.println("JoyBufferPlus.putByteArray(out, "
					// + f.getName() + ");");
					//
					// } else if (f.getType().toString()
					// .equals("class [Ljava.lang.String;")) {// string[]
					// System.out.println("JoyBufferPlus.putStringArray(out, "
					// + f.getName() + ");");
					//
					// } else {
					System.out
							.println("error type2==" + f.getType().toString());
					// }

				}
			}
			System.out.println("---------deserialize-----------");
			// sss=in.get();
			System.out.println("byte modelType=in.get();");

			for (int i = 0; i < fs.length; i++) {
				Field f = fs[i];

				f.setAccessible(true); // 设置些属性是可以访问的
				if (f.getName().startsWith("tmp_")) {
					continue;
				}
				Method[] methodlist = T.getDeclaredMethods();
				for (Method method : methodlist) {
					String mname = method.getName();

					if (mname.toLowerCase().equals(
							"get" + f.getName().toLowerCase())) {
						str = "this." + f.getName() + "=in.get";
						if (f.getType() == int.class) {
							System.out.println(str + "Int();");
						} else if (f.getType() == long.class) {
							System.out.println(str + "Long();");
						} else if (f.getType() == boolean.class) {
							System.out.println("error");
						} else if (f.getType() == byte.class) {
							System.out.println(str + "();");
						} else if (f.getType() == String.class) {
							System.out.println(str + "PrefixedString();");
						} else if (f.getType() == short.class) {
							System.out.println(str + "Short();");
						} else {
							System.out.println("error");
						}
					}
				}
			}
			System.out.println("---------" + T.getSimpleName()
					+ "export-----------");
			for (int i = 0; i < fs.length; i++) {
				Field f = fs[i];

				f.setAccessible(true); // 设置些属性是可以访问的
				Method[] methodlist = T.getDeclaredMethods();
				for (Method method : methodlist) {
					String mname = method.getName();
					if (mname.toLowerCase().equals(
							"get" + f.getName().toLowerCase())) {
						System.out.println("System.out.println(\""
								+ f.getName() + "==\"+" + mname + "());");
					}
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 测试enum
	 */
	public static void testEnum() {
		HeroOptType opt = HeroOptType.HERO_ADDSKILL;
		// System.out.println(HeroOptType.HERO_ADDSKILL.values());
		System.out.println(opt.ordinal());
		HeroOptType[] values = HeroOptType.values();
		for (HeroOptType op : values) {
			System.out.println("op=" + op);
		}
		System.out.println(HeroOptType.HERO_ADDSKILL.getValue());
		System.out.println(HeroOptType.HERO_ADDSKILL.name());
		System.out.println(HeroOptType.values()[3]);
	}

	// 生成插入sql语句
	public static void addSql(Class<?> T) throws InstantiationException,
			IllegalAccessException {
		System.out.println("---------insert-----------");
		String name = T.getSimpleName().toLowerCase();
		String sql = "insert into " + name + " (";
		Object data = T.newInstance();// 创建对象
		Method[] methodlist = T.getDeclaredMethods();// 获得方法
		Field[] fs = T.getDeclaredFields();// 获得属性
		List<String> list = new ArrayList<String>();
		int index = 0;
		for (int i = 0; i < fs.length; i++) {
			Field f = fs[i];
			f.setAccessible(true); // 设置些属性是可以访问的
			if (f.getType() == int.class) {

			} else if (f.getType() == long.class) {

			} else if (f.getType() == boolean.class) {
			} else if (f.getType() == byte.class) {
			} else if (f.getType() == String.class) {
			} else if (f.getType() == short.class) {
			} else {
				continue;
			}
			if (f.getName().equals("id")) {// 跳过自增的id，自己设置
				continue;
			}
			sql += f.getName();
			if (i != fs.length - 1) {
				sql += ",";
			}
			String sss = "ps.";
			for (Method method : methodlist) {
				String mname = method.getName();
				if (mname.toLowerCase().equals(
						"get" + f.getName().toLowerCase())) {
					if (f.getType() == int.class) {
						sss += "setInt(" + (index + 1) + ","
								+ T.getSimpleName().toLowerCase() + "." + mname
								+ "());";
					} else if (f.getType() == long.class) {
						sss += "setLong(" + (index + 1) + ","
								+ T.getSimpleName().toLowerCase() + "." + mname
								+ "());";
					} else if (f.getType() == boolean.class) {
						sss += "setBoolean(" + (index + 1) + ","
								+ T.getSimpleName().toLowerCase() + "." + mname
								+ "());";
					} else if (f.getType() == byte.class) {
						sss += "setByte(" + (index + 1) + ","
								+ T.getSimpleName().toLowerCase() + "." + mname
								+ "());";
					} else if (f.getType() == String.class) {
						sss += "setString(" + (index + 1) + ","
								+ T.getSimpleName().toLowerCase() + "." + mname
								+ "());";
					} else if (f.getType() == short.class) {
						sss += "setShort(" + (index + 1) + ","
								+ T.getSimpleName().toLowerCase() + "." + mname
								+ "());";
					} else {
						System.out
								.println("error=" + f.getName().toLowerCase());
					}
					// setString(1, user.getName());
				}
			}
			index++;

			list.add(sss);
		}
		sql += ")values(";
		for (int i = 0; i < index; i++) {
			sql += "?";
			if (i != index - 1) {
				sql += ",";
			}
		}
		sql += ")";
		System.out.println(sql);
		// insert into user(username,passward)values(?,?)
		// ps.setString(1, user.getName());
		for (int i = 0; i < list.size(); i++) {
			System.out.println(list.get(i));
		}

	}

	// 生成删除sql语句
	public static void delSql(Class<?> T) throws InstantiationException,
			IllegalAccessException {
		// delete from playerhero where 1 = 1 and id = ?
	}

	public static void selectSql(Class<?> T) throws InstantiationException,
			IllegalAccessException {
		System.out.println("-----------select---------");
		Method[] methodlist = T.getDeclaredMethods();
		Field[] fs = T.getDeclaredFields();
		for (int i = 0; i < fs.length; i++) {
			Field f = fs[i];
			f.setAccessible(true); // 设置些属性是可以访问的
			if (f.getType() == int.class) {

			} else if (f.getType() == long.class) {

			} else if (f.getType() == boolean.class) {
			} else if (f.getType() == byte.class) {
			} else if (f.getType() == String.class) {
			} else if (f.getType() == short.class) {
			} else {
				continue;
			}
			// System.out.println("name:"+f.getName()+"\t value = "+val);
			for (Method method : methodlist) {
				String mname = method.getName();
				String str = T.getSimpleName().toLowerCase() + "." + mname
						+ "(rs.";
				if (mname.toLowerCase().equals(
						"set" + f.getName().toLowerCase())) {
					if (f.getType() == int.class) {
						str += "getInt";
					} else if (f.getType() == long.class) {
						str += "getLong";
					} else if (f.getType() == boolean.class) {
						str += "getBoolean";
					} else if (f.getType() == byte.class) {
						str += "getByte";
					} else if (f.getType() == String.class) {
						str += "getString";
					} else if (f.getType() == short.class) {
						str += "getShort";
					} else {
						System.out.println("error=" + mname);
					}
					System.out.println(str + "(\"" + f.getName() + "\"));");
				}
			}
		}
		// playerHero.setName(rs.getString("name"));
	}

	/*
	 * 生成保存sql语句
	 */
	public static void saveSql(Class<?> T) throws InstantiationException,
			IllegalAccessException {
		System.out.println("-----------save---------");
		Object data = T.newInstance();

		String str = "update " + T.getSimpleName().toLowerCase() + " set ";
		Field[] fs = T.getDeclaredFields();
		Method[] methodlist = T.getDeclaredMethods();
		List<String> list = new ArrayList();
		for (int i = 0; i < fs.length; i++) {
			Field f = fs[i];
			f.setAccessible(true); // 设置些属性是可以访问的
			if (f.getType() == int.class) {

			} else if (f.getType() == long.class) {

			} else if (f.getType() == boolean.class) {
			} else if (f.getType() == byte.class) {
			} else if (f.getType() == String.class) {
			} else if (f.getType() == short.class) {
			} else {
				continue;
			}
			// String type = f.getType().toString();//得到此属性的类型

			// Object val = f.get(cla);//得到此属性的值
			if (f.getName().equals("id")) {
				continue;
			}
			// System.out.println("~~~~name="+f.getName());
			// System.out.println("value="+f.get(data));
			// System.out.println("type="+f.getType());
			// if(i%5==0){
			// str+="\n";
			// }
			str += f.getName() + "=?, ";
			// System.out.println("name:"+f.getName()+"\t value = "+val);
			for (Method method : methodlist) {
				String mname = method.getName();
				// System.out.println("方法＝"+method.getName());
				// if(mname.startsWith("set")||mname.startsWith("getId")){
				// continue;
				// }
				// System.out.println("====="+mname.toLowerCase());
				if (mname.toLowerCase().equals(
						"get" + f.getName().toLowerCase())) {
					list.add(T.getSimpleName().toLowerCase() + "." + mname
							+ "(),");
					break;
				}
				// System.out.println("player."+mname+"(),");
			}

		}
		// 找到最后一个“，”去除

		str += "where id=?";
		int index = str.lastIndexOf(',');
		String newStr = str.substring(0, index)
				+ str.substring(index + 1, str.length());

		// System.out.println("len="+str.length()+" index="+index);
		System.out.println(newStr);
		//
		for (int i = 0; i < list.size(); i++) {
			System.out.println(list.get(i));
		}

	}

	/**
	 * 增加一个玩家
	 */
	public static void testAddUser() {
		try {
			BaseUser user = new BaseUser();
			user.setName("aaaaaaaaa");
			user.setPassword("bbb");
			System.out.println(add(user));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static final String ADD_USER = "insert into user(username,passward)values(?,?)";

	public static int add(final BaseUser user) {
		try {
			DBManager db = DBManager.getInstance();
			KeyHolder keyHolder = new GeneratedKeyHolder();
			db.getWorldDAO().getJdbcTemplate()
					.update(new PreparedStatementCreator() {
						@Override
						public PreparedStatement createPreparedStatement(
								Connection conn) throws SQLException {
							PreparedStatement ps = conn.prepareStatement(
									ADD_USER, Statement.RETURN_GENERATED_KEYS);
							ps.setString(1, user.getName());
							ps.setString(2, user.getPassword());

							return ps;
						}

					}, keyHolder);

			return keyHolder.getKey().intValue();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return 0;
	}

	public static void mongoTest() {
		try {
			// 实例化Mongo对象，连接27017端口
			Mongo mongo = new Mongo("localhost", 27017);
			// 连接名为yourdb的数据库，假如数据库不存在的话，mongodb会自动建立
			DB db = mongo.getDB("hero");
			// Get collection from MongoDB, database named "yourDB"
			// 从Mongodb中获得名为yourColleection的数据集合，如果该数据集合不存在，Mongodb会为其新建立
			DBCollection collection = db.getCollection("hero");
			// 使用BasicDBObject对象创建一个mongodb的document,并给予赋值。
			BasicDBObject document = new BasicDBObject();
			document.put("id", 1001);
			document.put("msg", "hello world mongoDB in Java");
			// 将新建立的document保存到collection中去
			collection.insert(document);
			// 创建要查询的document
			BasicDBObject searchQuery = new BasicDBObject();
			searchQuery.put("id", 1001);
			// 使用collection的find方法查找document
			DBCursor cursor = collection.find(searchQuery);
			// 循环输出结果
			while (cursor.hasNext()) {
				System.out.println(cursor.next());
			}
			System.out.println("Done");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (MongoException e) {
			e.printStackTrace();
		}
	}

	public void equipId() {
		// // int a=0x00010101;
		// int b=0x00010201;
		// int a=65793;
		// System.out.println(a);
		// // System.out.println(0x00010000);
		// System.out.println((a&0xFFFF0000)>>>16);
		// // System.out.println(0x00000100);
		// System.out.println((a&0x0000FF00)>>>8);
		// // System.out.println(0x00000001);
		// System.out.println((a&0x000000ff));
	}

	public class CustomResource implements AutoCloseable {
		public void close() throws Exception {
			System.out.println("进行资源释放。");
		}
	}

	public void useCustomResource() throws Exception {
		try (CustomResource resource = new CustomResource()) {
			System.out.println("使用资源。");
		}
	}
}
