package com.joymeng.game.domain.soldier;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.joymeng.game.domain.fight.FightConst;
import com.joymeng.game.domain.world.GameDataManager;

public class SoldierManager {
	static final Logger logger = LoggerFactory.getLogger(SoldierManager.class);
	private HashMap<Integer,Soldier > soldierMap = new HashMap<Integer, Soldier>();
	private HashMap<Integer,SoldierEqu > equMap = new HashMap<Integer, SoldierEqu>();
	private static SoldierManager instance;
	public static SoldierManager getInstance() {
		if (instance == null) {
			instance = new SoldierManager();
		}
		return instance;
	} 
	/**
	 * 根据兵种id获得兵种
	 * @param id
	 * @return
	 */
	public Soldier getSoldier(int id ){
		return soldierMap.get(id);
	}
	/**
	 * 载入兵种数据
	 * @param path
	 * @throws ClassNotFoundException 
	 */
	public void load(String path) throws ClassNotFoundException{
		List<Object> list = GameDataManager.loadData(path, Soldier.class);
		for (Object obj : list) {
			Soldier data = (Soldier) obj;
			soldierMap.put(data.getId(), data);
//			System.out.println("("+data.getId()+","+data.getName()+")");
		}
		FightConst.MAXTYPE=soldierMap.size();
		loadEqu(path);//加载兵装
	}
	
	public void loadEqu(String path) throws ClassNotFoundException{
		List<Object> list = GameDataManager.loadData(path, SoldierEqu.class);
		for (Object obj : list) {
			SoldierEqu data = (SoldierEqu) obj;
			equMap.put(data.getId(), data);
		}
	}
	
	public HashMap<Integer, Soldier> getSoldierMap() {
		return soldierMap;
	}
	public void setSoldierMap(HashMap<Integer, Soldier> _soldierMap) {
		soldierMap = _soldierMap;
	}
	/**
	 * 得到装备额类型
	 * @param type
	 * @return
	 */
	public SoldierEqu getEqu(int type){
		for(SoldierEqu e : equMap.values()){
			if(e.getSoldier() == type){
				return e;
			}
		}
		return null;
	}
	
}
