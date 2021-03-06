package com.joymeng.game.domain.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;

import com.joymeng.core.base.net.response.AndroidMessageSender;
import com.joymeng.core.base.net.response.RespModuleSet;
import com.joymeng.core.fight.FightLog;
import com.joymeng.core.log.GameLog;
import com.joymeng.core.spring.local.I18nGreeting;
import com.joymeng.core.utils.TimeUtils;
import com.joymeng.game.ProcotolType;
import com.joymeng.game.common.GameConfig;
import com.joymeng.game.common.GameConst;
import com.joymeng.game.common.GameUtils;
import com.joymeng.game.common.Instances;
import com.joymeng.game.common.MessageUtil;
import com.joymeng.game.domain.hero.PlayerHero;
import com.joymeng.game.domain.item.equipment.EquiEffectResult;
import com.joymeng.game.domain.item.equipment.EquipPrototype;
import com.joymeng.game.domain.item.equipment.Equipment;
import com.joymeng.game.domain.item.equipment.EquipmentDismantling;
import com.joymeng.game.domain.item.equipment.EquipmentManager;
import com.joymeng.game.domain.item.equipment.EquipmentStrength;
import com.joymeng.game.domain.item.equipment.FirmEffect;
import com.joymeng.game.domain.item.props.Props;
import com.joymeng.game.domain.item.props.PropsManager;
import com.joymeng.game.domain.item.props.PropsPrototype;
import com.joymeng.game.domain.quest.QuestUtils;
import com.joymeng.game.domain.role.PlayerCharacter;
import com.joymeng.game.domain.world.TipMessage;
import com.joymeng.game.domain.world.TipUtil;
import com.joymeng.game.domain.world.World;
import com.joymeng.game.net.client.ClientModule;

/**
 * 用户背包数据
 * 
 * @author xufangliang 1.1
 */
public class PlayerStorageAgent implements Instances {
	static int HERO_FRAGMENT = 598;//武将碎片
	static int HERO_CARD = 599;//名将卡 
	
	I18nGreeting i18 = I18nGreeting.getInstance();
	// 日志
	private Logger logger = org.slf4j.LoggerFactory
			.getLogger(PlayerStorageAgent.class);

	static PropsManager propMgr = PropsManager.getInstance();
	static EquipmentManager equipMgr = EquipmentManager.getInstance();
	private PlayerCharacter owner;// 用户
	private final byte[] lock = new byte[0]; // 锁
	private PlayerItemEffect pis;
	List<Cell> cellDatas = new ArrayList<Cell>();
	

	/**
	 * @return the owner
	 */
	public PlayerCharacter getOwner() {
		return owner;
	}

	public PlayerItemEffect getPis() {
		return pis;
	}

	public void setPis(PlayerItemEffect pis) {
		this.pis = pis;
	}

	/**
	 * @param owner
	 *            the owner to set
	 */
	public void setOwner(PlayerCharacter owner) {
		this.owner = owner;
	}

	public void remove() {
		if (!checkCellDatas()) {
			List<Cell> del = new ArrayList<Cell>();
			for (Cell cell : cellDatas) {
				if (cell.getItemCount() == 0)
					del.add(cell);
			}
			cellDatas.removeAll(del);
		}

	}

	public void loadPis() {
		if (pis == null) {
			pis = new PlayerItemEffect();
		}
		pis.activation(owner, this);
	}
	
	public void clearAll(){
		cellDatas.clear();
		saveCells();
		System.out.println(cellDatas.size());
	}

	/**
	 * 用户的 背包数据
	 * 
	 * @param owner
	 * @return
	 */
	public List<Cell> loadPlayerCells(String storge) {
		loadPis();
//		logger.info("开始载入用户背包数据<<<<<<<<<<<<<<<<<<<");
		// List<Map<String, Object>> maps = gameDao.getSimpleJdbcTemplate()
		// .queryForList(GameWorldDAO.SQL_GET_PLAYER_CELLS,
		// getOwner().getData().getUserid());
		if (storge == null || "".equals(storge)) {
			cellDatas = new ArrayList<Cell>();
//			logger.info("用户背包使用格子数：" + cellDatas.size());
//			logger.info("载入用户背包数据完成<<<<<<<<<<<<<<<<<<<");
			return cellDatas;
		} else {
			String logString = storge;
			if (logString == null || "".equals(logString)) {
				cellDatas = new ArrayList<Cell>();
//				logger.info("用户背包使用格子数：" + cellDatas.size());
//				logger.info("载入用户背包数据完成<<<<<<<<<<<<<<<<<<<");
				return cellDatas;
			}
			logger.info("player:" + owner.getId() + "|msg:");
			cellDatas = createFromStrings(logString);
//			logger.info("用户背包使用格子数：" + cellDatas.size());
//			logger.info("载入用户背包数据完成<<<<<<<<<<<<<<<<<<<");

			return cellDatas;
		}
	}

	public boolean saveCells() {
		remove();// 清楚数量为0的数据
		String cellsMsg = createFromList(new ArrayList<Cell>(cellDatas));
		// owner.save(cellsMsg);//设置属性
		owner.getData().setPlayerCells(cellsMsg);
		
//		gameDao.saveRole(owner.getData());
		return true;
	}

	/**
	 * 将PlayerItem list转换成字符串 用以保存
	 * 
	 * @param PlayerItems
	 * @return
	 */
	public static String createFromList(List<Cell> cells) {
		StringBuffer sb = new StringBuffer();
		if (cells == null) {
			return "";
		} else {
			for (int i = 0; i < cells.size(); i++) {
				if (cells.get(i).getItemCount() != 0) {
					if (i == cells.size() - 1) {
						sb.append(cells.get(i).toString());
					} else {
						sb.append(cells.get(i).toString()).append(";");
					}
				}
			}
			return sb.toString();
		}
	}

	/**
	 * 返回对象LIST 每天数据间用';'分割
	 * 
	 * @param logString
	 * @return
	 */
	public static List<Cell> createFromStrings(String logString) {
		List<Cell> cells = new ArrayList<Cell>();
		if (logString.trim().length() == 0) {
			return null;
		}
		String[] split = logString.split(";");
		// System.out.println(split);
		for (String PlayerItemString : split) {
			Cell cell = createFromString(PlayerItemString);
			if (cell != null && cell.getItemCount() != 0) {
				// System.out.println("cell = " + PlayerItemString);
				cells.add(cell);
			}
		}
		return cells;
	}

	/**
	 * 返回对象
	 * 
	 * @param logString
	 * @return
	 */
	public static Cell createFromString(String logString) {
		int idx = 0;
		if (logString.trim().length() == 0) {
			return null;
		}
		String[] split = logString.split(",");
		if (split.length < 2) { // 物品字符串至少包括 classname 和 PlayerItemId
			GameLog.error("createFromString logString Syntax error!"
					+ logString, new Exception());
			return null;
		}
		String itemClassType = split[idx++];
		short itemType = Short.parseShort(itemClassType);
		if (itemType == Item.ITEM_EQUIPMENT) {
			String id = split[idx++];
			String count = split[idx++];
			String heroId = split[idx++];
			String effId = split[idx++];
			String effTime = split[idx++];
			Equipment e = new Equipment(equipMgr.getEquipment(Integer
					.parseInt(id)));
			e.setHeroId(Integer.parseInt(heroId));
			e.setEffectId(Integer.parseInt(effId));
			e.setEffectTime(Long.parseLong(effTime));
			Cell cell = new Cell();
			cell.setItem(e);
			cell.setItemCount(Integer.parseInt(count));
			return cell;
		} else if (itemType == Item.ITEM_PROPS) {
			String id = split[idx++];
			String count = split[idx++];
			Props p = new Props(propMgr.getProps(Integer.parseInt(id)));
			Cell cell = new Cell();
			cell.setItem(p);
			cell.setItemCount(Integer.parseInt(count));
			return cell;
		}
		return null;
	}

	/**
	 * 获取最大叠加数
	 * 
	 * @param oId
	 * @param playerItemType
	 * @return
	 */
	public int getMaxStackCount(int oId, boolean itemType) {
		if (itemType) {
			return propMgr.getProps(oId).getMaxStackCount();
		} else {
			return equipMgr.getEquipment(oId).getMaxStackCount();
		}
	}

	/**
	 * 取得物品栏开启的格子数
	 * 
	 * @return
	 */
	public int getSize() {
		if (null == cellDatas)
			return 0;
		return cellDatas.size();
	}

	/**
	 * 是否满了
	 * 
	 * @return
	 */
	public boolean isFull() {
		return cellDatas.size() >= GameConst.MAX_STORAGE;
	}

	/**
	 * 添加 item 到缓存
	 * 
	 * @param item
	 * @param num
	 * @param efficId
	 *            没有写0
	 */
	public void addPlayerItem(Item item, int num, int efficId) {
		if (num > 0) {
			synchronized (lock) { // 对应数据最大数量
				int maxStackCount = getMaxStackCount(item.getId(),
						item.isProp());
//				logger.info("叠加数量：" + maxStackCount);
				// 用户的 PlayerItem list
				if (maxStackCount > 1) {// 可以叠加，寻找相同类型和ID的
					if (num >= maxStackCount) {
						Cell cell = new Cell();
						cell.setItemCount(maxStackCount);
						cell.setItem(item);
						if (cellDatas.size() < GameConst.MAX_STORAGE) {
							cellDatas.add(cell);
						} else {
//							logger.info("owner=" + owner + ":storage size ="
//									+ GameConst.MAX_STORAGE + ":add cell id="
//									+ cell.getItem().getId() + ":cell count="
//									+ cell.getItemCount());
						}
						addPlayerItem(item, num - maxStackCount, efficId);
					} else if (num < maxStackCount && num > 0) {
						// 查询
						int add = num;
						for (Cell cell : cellDatas) {
							Item myItem = cell.getItem();
							if (item.getId() == myItem.getId()
									&& item.getType() == myItem.getType()
									&& cell.getItemCount() < maxStackCount) {
								int nowCount = cell.getItemCount();
								if (maxStackCount - nowCount > num) {
									cell.setItemCount(nowCount + add);
									add -= num;
								} else {
									cell.setItemCount(maxStackCount);
									add -= (maxStackCount - nowCount);
								}
							}
						}
						if (add > 0) {
							Cell cel = new Cell();
							cel.setItemCount(add);
							cel.setItem(item);
							add -= num;
							// 添加到缓存
							if (cellDatas.size() < GameConst.MAX_STORAGE) {
								cellDatas.add(cel);
							} else {
//								logger.info("owner=" + owner
//										+ ":storage size ="
//										+ GameConst.MAX_STORAGE
//										+ ":add cell id="
//										+ cel.getItem().getId()
//										+ ":cell count=" + cel.getItemCount());
							}
						}
						// addPlayerItem(item, add, efficId);
					}
				} else {
					// 循环添加
					for (int i = 0; i < num; i++) {
						Cell cell = new Cell();
						cell.setItemCount(1);
						cell.setItem(item);
						if (cellDatas.size() < GameConst.MAX_STORAGE) {
							cellDatas.add(cell);
						} else {
//							logger.info("owner=" + owner + ":storage size ="
//									+ GameConst.MAX_STORAGE + ":add cell id="
//									+ cell.getItem().getId() + ":cell count="
//									+ cell.getItemCount());
						}
					}

				}
				// 更新数据
//				 saveCells();
			}
		}

	}

	/**
	 * 添加一个props
	 * 
	 * @param props
	 * @return Props
	 */
	public Props addProps(int propsId, int num) {
		Props props = new Props(propMgr.getProps(propsId));
		if (props == null) {
			return null;
		}
		addPlayerItem(props, num, 0);
		saveCells();
//		logger.info("addPlayerItem  >>>>> player:" + owner + "|msg:"
//				+ props.getId() + ";" + props.getType() + ";" + num + ";" + 0);
		return props;
	}
	
	/**
	 * 添加一个props
	 * 
	 * @param props
	 * @return cell
	 */
	public Cell addPropsCell(int propsId, int num) {
		Props props = addProps(propsId, num);
		if (props != null) {
			Cell cell = new Cell();
			cell.setItem(props);
			cell.setItemCount(num);
			saveCells();
			return cell;
		}
		
		return null;
	}

	/**
	 * 添加一个Equipment
	 * 
	 * @param props
	 * @return Equipment
	 */
	public Equipment addEquipment(int eqId, int num, int efficId) {
		Equipment eq = new Equipment(equipMgr.getEquipment(eqId));
		addPlayerItem(eq, num, efficId);
		saveCells();
//		logger.info("addPlayerItem  >>>>> player:" + owner + "|msg:"
//				+ eq.getId() + ";" + eq.getType() + ";" + num + ";" + efficId);
		return eq;
	}
	
	/**
	 * 添加一个Equipment
	 * 
	 * @param props 
	 * @return cell
	 */
	public List<Cell> addEquis(Equipment e, int num) {
		List<Cell> cells = new ArrayList<Cell>();
		if (e != null) {
//			logger.info("添加物品:" + e.getId() + "|数量:" + num);
			cells = addEqui(e.getId(), num, 0);
		} else {
			logger.info("添加物品失败");
		}
		return cells;
	}
	/**
	 * 添加一个Equipment
	 * 
	 * @param props 
	 * @return cell
	 */
	public List<Cell> addEqui(int eqId, int num, int efficId) {
		Equipment e = addEquipment(eqId, num, efficId);
		List<Cell> cells = new ArrayList<Cell>();
		if (e != null) {
			Cell cell = new Cell();
			cell.setItem(e);
			cell.setItemCount(1);
			for (int i = 0; i < num; i++) {
				cells.add(cell);
			}
			saveCells();
			return cells;
		}
		return cells;
	}

	/**
	 * 取得物品的所有数量
	 * 
	 * @param item
	 * @return
	 */
	public int allCount(Item item) {
		int all = 0;
		if (cellDatas == null || cellDatas.size() == 0 || item == null)
			return all;
		for (Cell cell : cellDatas) {
			Item myItem = cell.getItem();
			if (item != null && myItem != null
					&& myItem.getType() == item.getType()
					&& item.getId() == myItem.getId()) {
				all += cell.getItemCount();
			}
		}
		return all;
	}

	/**
	 * 判断是否有足够的物品
	 * 
	 * @param item
	 * @param num
	 */
	public EquiEffectResult isDelete(Item item, int num) {
		if (item == null) {
			return EquiEffectResult.FAILED;
		}
		int allCount = allCount(item);
//		logger.info("材料id:" + item.getId() + "数量：" + allCount);
		if (num > allCount) {
			EquiEffectResult er = EquiEffectResult.FAILED;
			er.setEquiOrResult(item.getName() + "数量不足" + num);
			if(I18nGreeting.LANLANGUAGE_TIPS == 1){
				er.setEquiOrResult(item.getName() + " Not enough items " + num);
			}
			return er;
		}
		return EquiEffectResult.SUCCESSFULL;
	}

	/**
	 * 判断是否有足够的物品
	 * 
	 * @param item
	 * @param num
	 */
	public EquiEffectResult isDelete(int itemId, int num, byte t) {
		if (t == Item.ITEM_EQUIPMENT) {
			Equipment eq = new Equipment(equipMgr.getEquipment(itemId));
			return isDelete(eq, num);
		} else if (t == Item.ITEM_PROPS) {
			Props props = new Props(propMgr.getProps(itemId));
			return isDelete(props, num);
		}
		return EquiEffectResult.FAILED;
	}

	/**
	 * 删除 PlayerItem 到缓存
	 * 
	 * @param propsId
	 * @param num
	 * @return
	 */
	public void deletePlayerItem(Item item, int num) {
		int maxStackCount = getMaxStackCount(item.getId(), item.isProp());
//		synchronized (lock) {
//			logger.info("消除道具ALL：" + item.getId() + "|数量：" + num);
			if (num > 0 && item.isProp()) {
				int del = num;
				for (Cell cell : cellDatas) {
					if (del > 0) {
						Item myItem = cell.getItem();
						// 先消除未满的背包
						if (myItem.getType() == item.getType()
								&& myItem.getId() == item.getId()
								&& cell.getItemCount() < maxStackCount) {
							int count = cell.getItemCount();
							if (del > 0 && del < count && count > 0) {
								cell.setItemCount(count - del);
//								logger.info("消除未满道具 1：" + item.getId() + "|数量："
//										+ num);
								del -= num;
							} else if (del > 0 && count > 0) {
								// cellDatas.remove(cell);
								cell.setItemCount(0);
								del -= count;
//								logger.info("消除未满道具 2：" + item.getId() + "|数量："
//										+ count);
							}
						}
					}
				}
				while (del > 0) {
					for (Cell cell : cellDatas) {
						if (del > 0) {
							Item myItem = cell.getItem();
							// 未满的消除完成后只剩下满的
							if (myItem.getType() == item.getType()
									&& myItem.getId() == item.getId()
									&& cell.getItemCount() == maxStackCount) {
								int count = cell.getItemCount();
								if (del > 0 && del < count && count > 0) {
									cell.setItemCount(count - del);
//									logger.info("消除道具MAN 1：" + item.getId()
//											+ "|数量：" + del);
									del -= del;
									break;
								}
								logger.info(del + "||" + count);
								if (del > 0 && del >= count && count > 0) {
									// cellDatas.remove(cell);
									cell.setItemCount(0);
//									logger.info("消除道具MAN 2：" + item.getId()
//											+ "|数量：" + count);
									del = del - count;
									// deletePlayerItem(item, del - count);
								}
							}
						}
					}
				}
			} else if (num > 0 && !item.isProp()) {
				int del = num;
				for (Cell cell : cellDatas) {
					if (del > 0 && !cell.getItem().isProp()) {
						Equipment myItem = (Equipment) (cell.getItem());
						Equipment i = (Equipment) (item);
						// 先消除未满的背包
						if (myItem.getType() == item.getType()
								&& myItem.getId() == item.getId()
								&& myItem.getHeroId() == i.getHeroId()) {
							int count = cell.getItemCount();
							if (num <= count) {
								cell.setItemCount(count - del);
								del -= num;
							} else {
								cell.setItemCount(0);
								deletePlayerItem(item, del - count);
							}
						}
					}
				}
			}
		}
//	}

	/**
	 * 删除一个props
	 * 
	 * @param props
	 * @return
	 */
	public Props dellProps(int propsId, int num) {
		Props props = new Props(propMgr.getProps(propsId));
		EquiEffectResult er = isDelete(props, num);
		if (er.getStatus()) {
			deletePlayerItem(props, num);
			EquiEffectResult ers = isDelete(props, num);
			remove();
			saveCells();
			props.setIsAdd((byte) 1);
			return props;
		}
		logger.info("使用" + props.getName() + "失败");
		return null;
	}
	
	/**
	 * 删除物品  不区分道具装备
	 * @param id
	 * @param num
	 * @return
	 */
	public Cell dellCellAll(int id, int num) {
		Cell cell = getCell(id);
		if(cell != null){
			if(cell.getItem().isProp()){
				return dellCell(id, num);
			}else{
				Equipment eqment = getHeroEquipment(id, 0);
				Equipment e = dellEqui(eqment);
				return getCell(e);
			}
		}
		return null;
	}
	
	/**
	 * 删除物品
	 * @param propsId
	 * @param num
	 * @return
	 */
	public Cell dellCell(int propsId, int num) {
		Props p = dellProps(propsId, num);
		if (p != null) {
			Cell cell = new Cell();
			cell.setItem(p);
			cell.setItemCount(num);
			saveCells();
			return cell;
		}
		return null;
	}

	public Cell getCell(Props props, int num) {
		if (props != null) {
			Cell cell = new Cell();
			cell.setItem(props);
			cell.setItemCount(num);
			return cell;
		}
		return null;
	}

	/**
	 * 删除一个 Equi
	 * 
	 * @param Equi
	 * @return
	 */
	public Equipment dellEqui(Equipment eq) {
		EquiEffectResult er = isDelete(eq, 1);
		if (eq != null && eq.heroId != 0) {// 被使用
			logger.info("装备" + eq.getName() + "已使用，不能删除");
			return null;
		}
		if (er.getStatus()) {
			deletePlayerItem(eq, 1);
			remove();
			saveCells();
//			logger.info("delPlayerItem  begin >>>>> player:" + owner + "|msg:"
//					+ eq.getId() + ";" + eq.getType() + ";" + 1 + ";");
			eq.setIsAdd((byte) 1);
			return eq;
		}
		logger.info("使用" + eq.getName() + "失败");
		return null;
	}

	/**
	 * 是否有装备法宝
	 * 
	 * @param equip
	 * @return
	 */
	public boolean hasEquip(Equipment equip) {
		if (cellDatas != null && cellDatas.size() != 0) {
			for (Cell cell : cellDatas) {
				if (cell.getItem().getType() == equip.getType()
						&& cell.getItem().getId() == equip.getId())
					return true;
			}
		}
		return false;
	}

	/**
	 * 是否有某个道具
	 * 
	 * @param props
	 * @return
	 */
	public boolean hasProp(Props props) {
		if (cellDatas != null && cellDatas.size() != 0) {
			for (Cell cell : cellDatas) {
				if (cell.getItem().getType() == props.getType()
						&& cell.getItem().getId() == props.getId())
					return true;
			}
		}
		return false;
	}

	/**
	 * 获取某类用户物品 type 细分类型
	 * 
	 * @param type
	 */
	public List<Cell> getTypeItem(Item item) {
		List<Cell> typeDatas = new ArrayList<Cell>();
		if (cellDatas != null && cellDatas.size() != 0) {
			for (Cell cell : cellDatas) {
				Item myItem = cell.getItem();
				if (item.getType() == myItem.getType()
						&& item.getId() == myItem.getId()) {
					typeDatas.add(cell);
				}
			}
		}
		return typeDatas;
	}

	/**
	 * 判断 升级条件
	 * 
	 * @param e
	 * @param type
	 * @return
	 */
	public EquipmentStrength checkUpgrade(Equipment e, byte type) {
		if (equipMgr == null) {
			logger.info("equipMgr对象为空");
			return null;
		}
		if (e == null) {
			logger.info("装备为空");
		}
		byte[] by = equipMgr.getEquip(e, type);
		if (by == null) {
		}
		EquipmentStrength es = equipMgr
				.getEquipmentStrength(type, by[1], by[2]);

		if (es == null) {
			if(I18nGreeting.LANLANGUAGE_TIPS == 1){
				e.setTip(new TipMessage("Data doesn't exist.", ProcotolType.ITEMS_RESP,
						GameConst.GAME_RESP_FAIL));
			}else{
				e.setTip(new TipMessage("升级数据不存在", ProcotolType.ITEMS_RESP,
						GameConst.GAME_RESP_FAIL));
			}
			
			return null;
		}
		if (es.getPropsId() == 0) {
			// 判断用户金币 是否大于 es.getCostCount()
			if (owner.getData().getGameMoney() >= es.getCostCount()) {
				if(I18nGreeting.LANLANGUAGE_TIPS == 1){
					e.setTip(new TipMessage("Upgrade successfully.", ProcotolType.ITEMS_RESP,
							GameConst.GAME_RESP_FAIL));
				}else{
					e.setTip(new TipMessage("升级成功", ProcotolType.ITEMS_RESP,
							GameConst.GAME_RESP_SUCCESS));
				}
				
				return es;
			} else {
				if(I18nGreeting.LANLANGUAGE_TIPS == 1){
					e.setTip(new TipMessage("No enough Golds. Need Golds:" + es.getCostCount(),
							ProcotolType.ITEMS_RESP, GameConst.GAME_RESP_FAIL));
				}else{
					e.setTip(new TipMessage("装备：" + e.getId() + " 操作：" + type
							+ " 金币不足   需要金币：" + es.getCostCount(),
							ProcotolType.ITEMS_RESP, GameConst.GAME_RESP_FAIL));
				}
				
				logger.info("装备：" + e.getId() + " 操作：" + type + " 金币不足   需要金币："
						+ es.getCostCount());
				return null;
			}
		} else {
			Props props = new Props(propMgr.getProps(es.getPropsId()));
			EquiEffectResult ef = isDelete(props, es.getCostCount());// 删除材料
			if (ef.getStatus()) {
				if(I18nGreeting.LANLANGUAGE_TIPS == 1){
					e.setTip(new TipMessage("Upgrade successfully.", ProcotolType.ITEMS_RESP,
							GameConst.GAME_RESP_FAIL));
				}else{
					e.setTip(new TipMessage("升级成功", ProcotolType.ITEMS_RESP,
							GameConst.GAME_RESP_SUCCESS));
				}
				return es;
			} else {
				if(I18nGreeting.LANLANGUAGE_TIPS == 1){
					e.setTip(new TipMessage("No enough material.Need material:" + es.getCostCount(),
							ProcotolType.ITEMS_RESP, GameConst.GAME_RESP_FAIL));
				}else{
					e.setTip(new TipMessage("装备：" + e.getId() + " 操作：" + type
							+ " 金币不足   需要金币：" + es.getCostCount(),
							ProcotolType.ITEMS_RESP, GameConst.GAME_RESP_FAIL));
				}
				logger.info("装备：" + e.getId() + " 操作：" + type + " 材料："
						+ es.getPropsId() + "不足   需要材料：" + es.getCostCount());
				return null;
			}
		}
	}

	/**
	 * 加制 加制等级规则
	 * 
	 * @param e
	 * @param p
	 *            加制模具材料 使用数量 1个
	 * @return
	 */
	public Equipment checkUpgrade(Equipment equipment, Props p) {
		EquiEffectResult er = isDelete(p, 1);
		Cell cell = getCell(equipment);
		if (!er.getStatus() || cell == null) {
			logger.info("材料：" + p.getId() + " 需要数量：1  材料不足");
			return null;
		}
		// 加制石 id = 111
		int levelLimit = Byte.parseByte(p.getPrototype().getProperty2());// 强化等级

		// 判断强化等级
		if (levelLimit != 0
				&& equipment.getPrototype().getStrengthenLevel() <= levelLimit) {
			logger.info("装备强化等级："
					+ equipment.getPrototype().getStrengthenLevel()
					+ "   需要等级：" + levelLimit);
			if(I18nGreeting.LANLANGUAGE_TIPS == 1){
				equipment.setTip(new TipMessage("Equip intensified level:"
						+ equipment.getPrototype().getStrengthenLevel()
						+ "   Need level:" + levelLimit, ProcotolType.ITEMS_RESP,
						GameConst.GAME_RESP_FAIL));
			}else{
				equipment.setTip(new TipMessage("装备强化等级："
						+ equipment.getPrototype().getStrengthenLevel()
						+ "   需要等级：" + levelLimit, ProcotolType.ITEMS_RESP,
						GameConst.GAME_RESP_FAIL));
			}
			
			return equipment;
		}
//		logger.info("需要强化等级：" + levelLimit);
		EquipmentStrength es = equipMgr.getEquipmentStrength(
				EquipmentManager.UPGRADE_JIAZHI, (byte) p.getId(), (byte) 0);
		if (es != null) {
//			logger.info("需要强化数据：" + es.getId());
			EquiEffectResult efr = isDelete(es.getPropsId(), es.getCostCount(),
					Item.ITEM_PROPS);
			if (!efr.getStatus()) {
//				logger.info("材料：" + es.getPropsId() + " 需要数量："
//						+ es.getCostCount());
				
				if(I18nGreeting.LANLANGUAGE_TIPS == 1){
					equipment.setTip(new TipMessage("material：" + es.getPropsId()
							+ " Need material:" + es.getCostCount(),
							ProcotolType.ITEMS_RESP, GameConst.GAME_RESP_FAIL));
				}else{
					equipment.setTip(new TipMessage("材料：" + es.getPropsId()
							+ " 需要数量：" + es.getCostCount(),
							ProcotolType.ITEMS_RESP, GameConst.GAME_RESP_FAIL));
				}
				return equipment;
			}
			// 随机技能id
			int ids = equipment.getPrototype().getEquipmentType() * 1000
					+ p.getId();
//			logger.info("随机技能：" + ids);
			// 获取随机加制效果
			Map<Integer, List<FirmEffect>> firmLstDatas = equipMgr.firmLstDatas;
//			logger.info("加制效果数据：" + firmLstDatas.size());
			if (firmLstDatas == null || firmLstDatas.size() == 0) {
				if(I18nGreeting.LANLANGUAGE_TIPS == 1){
					equipment.setTip(new TipMessage("Error!",
							ProcotolType.ITEMS_RESP, GameConst.GAME_RESP_FAIL));
				}else{
					equipment.setTip(new TipMessage("加制效果错误",
							ProcotolType.ITEMS_RESP, GameConst.GAME_RESP_FAIL));
				}
				
				return equipment;
			}
			List<FirmEffect> feLst = firmLstDatas.get(ids);
//			logger.info("加制效果数据：" + feLst.size());
			if (feLst == null || feLst.size() == 0) {
				if(I18nGreeting.LANLANGUAGE_TIPS == 1){
					equipment.setTip(new TipMessage("Error!",
							ProcotolType.ITEMS_RESP, GameConst.GAME_RESP_FAIL));
				}else{
					equipment.setTip(new TipMessage("加制效果错误",
							ProcotolType.ITEMS_RESP, GameConst.GAME_RESP_FAIL));
				}
				return equipment;
			}
			int index = random(feLst.size());
//			logger.info("随机数：" + index);
			FirmEffect fe = feLst.get(index - 1);// 取得加制效果
			if (fe == null) {
				if(I18nGreeting.LANLANGUAGE_TIPS == 1){
					equipment.setTip(new TipMessage("Error!",
							ProcotolType.ITEMS_RESP, GameConst.GAME_RESP_FAIL));
				}else{
					equipment.setTip(new TipMessage("加制效果错误",
							ProcotolType.ITEMS_RESP, GameConst.GAME_RESP_FAIL));
				}
				return equipment;
			}
//			logger.info("加制效果id:" + fe.getId());
			// 保存到cell中
			equipment.setEffectId(fe.getId());
			equipment.setEffectTime(TimeUtils.nowLong()
					+ fe.getTime() * 1000);// 加制时间
			cell.setItem(equipment);
			Cell del = dellCellAll(es.getPropsId(), es.getCostCount());
			if (saveCells() && del != null) {
				saveCells();
				// 删除 加制 石头
				RespModuleSet rms = new RespModuleSet(ProcotolType.ITEMS_RESP);
				rms.addModule(del);
				AndroidMessageSender.sendMessage(rms, owner);
				// rms
//				logger.info("删除加制石：" + es.getPropsId() + "  数量："
//						+ es.getCostCount());
				
				if(I18nGreeting.LANLANGUAGE_TIPS == 1){
					equipment.setTip(new TipMessage("Enchanting successfully.",
							ProcotolType.ITEMS_RESP, GameConst.GAME_RESP_SUCCESS));
				}else{
					equipment.setTip(new TipMessage("装备加制成功",
							ProcotolType.ITEMS_RESP, GameConst.GAME_RESP_SUCCESS));
				}
				saveCells();
				return equipment;// 删除材料 加之石头
			} else {
				if(I18nGreeting.LANLANGUAGE_TIPS == 1){
					equipment.setTip(new TipMessage("Delete Enchant Ore:" + es.getPropsId()
							+ "  number：" + es.getCostCount() + " ERROR",
							ProcotolType.ITEMS_RESP, GameConst.GAME_RESP_FAIL));
				}else{
					equipment.setTip(new TipMessage("删除加制石：" + es.getPropsId()
							+ "  数量：" + es.getCostCount() + " 错误",
							ProcotolType.ITEMS_RESP, GameConst.GAME_RESP_FAIL));
				}
				return equipment;
			}
		}
		
		return null;
	}

	/**
	 * 获取成功率
	 * 
	 * @param e
	 * @param upgradeType
	 * @param PropsId
	 * @return
	 */
	public boolean isSucc(Equipment e, byte upgradeType, int PropsId) {
		float per = equipMgr.getProbability(e, upgradeType, PropsId);
		float p = new Random().nextFloat();
		logger.info("装备升级成功率 ： " + per + "   随机数为：" + p);
		if (p > 0 && p < per) {
			return true;
		} else {
			return false;
		}
	}
	/**
	 * 加制
	 * @param e
	 * @param pId
	 * @return
	 */
	public Equipment firm(Equipment e, int pId) {
		// Equipment e = getEquipmentTakeOff(eqId,heroId);
		// Equipment e = new Equipment(equipMgr.getEquipment(eqId));
//		logger.info("加制：" + e.getId());
		Props props = new Props(propMgr.getProps(pId));
		if (propMgr.getProps(pId) == null || props == null || e == null) {
			return null;
		}
//		logger.info("加制   ：" + props.getId());
		Equipment ers = checkUpgrade(e, props);// 检查加制数据
		if (ers != null && ers.getTip() != null
				&& ers.getTip().getResult() == 1) {
			// 加制数据 成功
			Cell del = dellCellAll(props.getId(), 1);// 删除
			// rms
			// 删除 加制 石头
			RespModuleSet rms = new RespModuleSet(ProcotolType.ITEMS_RESP);
			// rms.addModule(del);
			AndroidMessageSender.sendMessage(rms, owner);
			// rms
			saveCells();
			return ers;
		} else if (ers != null && ers.getTip() != null
				&& ers.getTip().getResult() == 0) {
			return ers;
		} else {
			return null;
		}

	}

	/**
	 * 武器 加工
	 * 
	 */
	public EquiEffectResult upgrade(Equipment e, byte type, int pId) {
		EquipmentStrength ers = checkUpgrade(e, type);
		if (e != null && ers != null) {
			// 扣钱。材料
			if (ers.getPropsId() == 0) {
				if(owner.saveResources(GameConfig.GAME_MONEY,
						-1 * ers.getCostCount()) == -1){
					//下发提示
					TipMessage tip = null;
					if(i18.LANLANGUAGE_TIPS == 1){
						tip = new TipMessage("No enough Golds!", ProcotolType.BUILDING_RESP, (byte)0);
					}else{
						tip = new TipMessage("金币不足", ProcotolType.BUILDING_RESP, (byte)0);
					}
					GameUtils.sendTip(tip, owner.getUserInfo(),GameUtils.FLUTTER);
					return EquiEffectResult.FAILED;
				}
			} else {
				Cell del = dellCellAll(ers.getPropsId(), ers.getCostCount());
				// 删除升级 石头
				RespModuleSet rms = new RespModuleSet(ProcotolType.ITEMS_RESP);
				rms.addModule(del);
				AndroidMessageSender.sendMessage(rms, owner);
				// rms
			}
			Cell dels = null;
			if (pId != 0) {//对应幸运石
				dels = dellCellAll(pId, 1);//删除对应幸运石
			}
			if (dels == null) {
				pId = 0;
			}
			if (isSucc(e, type, pId)) {
				EquiEffectResult eef = equipMgr.upgrade(e, type, owner.getData().getName());
				if(eef.getStatus()){
					e.setTip(new TipMessage("", ProcotolType.ITEMS_RESP,
							GameConst.GAME_RESP_SUCCESS));
				}
				return eef;
			} else {
				if(i18.LANLANGUAGE_TIPS == 1){
					e.setTip(new TipMessage("No enough Golds.", ProcotolType.ITEMS_RESP,
							GameConst.GAME_RESP_FAIL));
				}else{
					e.setTip(new TipMessage("升级失败", ProcotolType.ITEMS_RESP,
							GameConst.GAME_RESP_FAIL));
				}
				
				return EquiEffectResult.FAILED;
			}
		}
		return EquiEffectResult.FAILED;
	}

	/**
	 * 升级 并背包和数据库切换
	 * 
	 * @param eqId
	 * @param type
	 * @param pId
	 * @return
	 */
	public Equipment equiUpgrade(int eqId, byte type, int pId, int heroId) {
		FightLog.info("开始升阶！用户："+owner.getId()+"|装备：" + eqId + " 类型：" + type + " 材料id:" + pId);
		String sendTips = "";
		
		if (checkCellDatas()) {
			if(i18.LANLANGUAGE_TIPS == 1){
				sendTips = "Package data error!";
			}else{
				sendTips = "用户背包数据错误";
			}
			GameUtils.sendTip(new TipMessage(sendTips,
					ProcotolType.ITEMS_RESP, GameConst.GAME_RESP_FAIL), owner
					.getUserInfo(),GameUtils.FLUTTER);
			return null;
		}
		if (owner.getEquipment(eqId) == null) {
			if(i18.LANLANGUAGE_TIPS == 1){
				sendTips = "This equip doesn't exist!";
			}else{
				sendTips = "装备不存在";
			}
			GameUtils.sendTip(new TipMessage(sendTips, ProcotolType.ITEMS_RESP,
					GameConst.GAME_RESP_FAIL), owner.getUserInfo(),GameUtils.FLUTTER);
			return null;
		}
		PlayerHero hero = owner.getPlayerHeroManager().getHero(heroId);
		if (hero != null && hero.getStatus() != GameConst.HEROSTATUS_IDEL) {
			GameUtils.sendTip(new TipMessage("将领状态不对", ProcotolType.ITEMS_RESP,
					GameConst.GAME_RESP_FAIL), owner.getUserInfo(),GameUtils.FLUTTER);
			return null;
		}
		if (type == EquipmentManager.UPGRADE_JIAZHI)// 加制
		{
			Equipment e = getHeroEquipment(eqId, heroId);
			if (e == null) {
				if(i18.LANLANGUAGE_TIPS == 1){
					sendTips = "This equip doesn't exist!";
				}else{
					sendTips = "装备不存在";
				}
				GameUtils.sendTip(new TipMessage(sendTips,
						ProcotolType.ITEMS_RESP, GameConst.GAME_RESP_FAIL),
						owner.getUserInfo(),GameUtils.FLUTTER);
				FightLog.info("装备 不存在   ———— 用户："+owner.getId()+"|装备：" + eqId + "  英雄id:"
						+ heroId);
				return null;
			}
			Equipment eef = firm(e, pId);// 下发加值石头
			if (eef != null) {
				FightLog.info("用户："+owner.getId()+"|装备：" + eqId + "  加制成功！加制ID：" + eef.getEffectId());
				saveCells();// 保存
				RespModuleSet rms3 = new RespModuleSet(ProcotolType.ITEMS_RESP);
				if (hero != null) {
					hero.updateHeroAttri();
					rms3.addModule(hero);
				}
				AndroidMessageSender.sendMessage(rms3, owner);
				QuestUtils.checkFinish(owner, QuestUtils.TYPE25, true);
				return e;
			} else {
				FightLog.info("用户："+owner.getId()+"|装备：" + eqId + "  加制失败！");
				saveCells();
				if(i18.LANLANGUAGE_TIPS == 1){
					sendTips = "Equip：" + eqId + "  enchant failed!";
				}else{
					sendTips = "装备：" + eqId + "  加制失败！";
				}
				e.setTip(new TipMessage("装备：" + eqId + "  加制失败！",
						ProcotolType.ITEMS_RESP, GameConst.GAME_RESP_FAIL));
				GameUtils.sendTip(new TipMessage("", ProcotolType.ITEMS_RESP,
						GameConst.GAME_RESP_FAIL), owner.getUserInfo(),GameUtils.FLUTTER);
				return e;
			}
		} else {
			Equipment e = null;
			if (type == EquipmentManager.UPGRADE_JINGLIANG) {// 精炼
				if(heroId != 0){
					FightLog.info("用户："+owner.getId()+"|穿在将领身上不能 精炼   —————————— 装备：" + eqId + "  英雄id:"
							+ heroId + " 强化类型：" + type);
					return null;
				}else{
					e = getHeroEquipment(eqId, 0);
				}
			} else {
				e = getHeroEquipment(eqId, heroId);
			}
			if (e == null) {
				GameUtils.sendTip(new TipMessage("", ProcotolType.ITEMS_RESP,
						GameConst.GAME_RESP_FAIL), owner.getUserInfo(),GameUtils.FLUTTER);
				FightLog.info("用户："+owner.getId()+"|装备 不存在   —————————— 装备：" + eqId + "  英雄id:"
						+ heroId + " 强化类型：" + type);
				return null;
			}
			EquiEffectResult eef = upgrade(e, type, pId);
			if (type == EquipmentManager.UPGRADE_SHENGJIE) {
				QuestUtils.checkFinish(owner, 24, true);
			} else if (type == EquipmentManager.UPGRADE_JINGLIANG) {
				QuestUtils.checkFinish(owner, 23, true);
			}

			if (eef.getStatus()) {
				FightLog.info("用户："+owner.getId()+"|装备：" + eqId + "  升级类型：" + type + " 新装备 为："
						+ eef.getResult());
				Equipment ee = replaceEquipment(e,
						Integer.parseInt(eef.getResult()), heroId);
				saveCells();// 保存
				QuestUtils.checkFinish(owner, QuestUtils.TYPE11, true, ee.getPrototype()
						.getStrengthenLevel());
				return ee;
			} else {
				FightLog.info("用户："+owner.getId()+"|装备：" + eqId + "  升级类型：" + type + "  失败！");
				saveCells();
				GameUtils.sendTip(new TipMessage("", ProcotolType.ITEMS_RESP,
						GameConst.GAME_RESP_FAIL), owner.getUserInfo(),GameUtils.FLUTTER);
				return e;
			}
		}
	}

	/**
	 * 用户背包是否为空
	 * 
	 * @return
	 */
	public boolean checkCellDatas() {
		if (cellDatas == null || cellDatas.size() == 0)
			return true;
		return false;
	}

	/**
	 * 获取格子
	 * 
	 * @param id
	 * @return
	 */
	public Cell getCell(int id) {
		if (checkCellDatas())
			return null;
		for (Cell cell : cellDatas) {
			if (id == cell.getItem().getId()) {
				return cell;
			}
		}
		return null;
	}

	/**
	 * 获取格子
	 * 
	 * @param id
	 * @return
	 */
	public Cell getCell(Item e) {
		if (checkCellDatas())
			return null;
		for (Cell cell : cellDatas) {
			if (e == cell.getItem()) {
				return cell;
			}
		}
		return null;
	}

	/**
	 * 获取未装备的装备
	 * 
	 * @param id
	 * @return
	 */
	public Cell getUnCell(int id) {
		if (checkCellDatas())
			return null;
		for (Cell cell : cellDatas) {
			if (id == cell.getItem().getId() && !cell.getItem().isProp()) {
				Equipment ee = (Equipment) cell.getItem();
				if (ee.getHeroId() == 0) {
					return cell;
				}
			}
		}
		return null;
	}

	/**
	 * 用户背包数据
	 * 
	 * @param type
	 *            类型
	 * @return
	 */
	public List<Cell> getGoods(byte type) {
		if (checkCellDatas())
			return null;
		List<Cell> cells = new ArrayList<Cell>();
		for (Cell e : cellDatas) {
			if (type == Item.ITEM_EQUIPMENT) {
				if (!e.getItem().isProp()) {
					Equipment ee = (Equipment) e.getItem();
					cells.add(e);
				}
			} else if (type == Item.ITEM_PROPS)
				if (e.getItem().isProp()) {
					cells.add(e);
				}

		}
		return cells;
	}
	
	/**
	 * 获取所有的背包数据
	 * @return
	 */
	public List<Cell> getGoods() {
		if (checkCellDatas())
			return null;
		List<Cell> equim = getGoods(Item.ITEM_EQUIPMENT);
		//排序下发
		List<Cell> props = getGoods(Item.ITEM_PROPS);
		equim.addAll(props);
		System.out.println("!!!!!!!!!!!!!!!!!" + equim.size());

		return equim;
	}

	public int isDrive(PlayerHero hero, Equipment eq) {
		if (hero == null || eq == null) {
//			logger.info("参数为空");
			return 0;
		}
		byte type = eq.getPrototype().getEquipmentType();
		int flag = 0;
		switch (type) {
		case Equipment.SWORD:
			flag = hero.getWeapon();
			break;
		case Equipment.HELMET:
			flag = hero.getHelmet();
			break;
		case Equipment.ARMOR:
			flag = hero.getArmour();
			break;
		case Equipment.MOUNTS:
			flag = hero.getHorse();
			break;
		}
//		logger.info("英雄：" + hero.getId() + " 装备类型：" + type + " 装备id:" + flag);
		return flag;
	}
	/**
	 * 得到可以使用的装备
	 * @param id
	 * @param e
	 * @return
	 */
	public Equipment getUserEquipment(int id, Equipment e) {
		if (checkCellDatas())
			return null;
		for (Cell cell : cellDatas) {
			if (e == null && id == cell.getItem().getId()
					&& !cell.getItem().isProp()) {
				Equipment eee = (Equipment) cell.getItem();
				if (eee.getHeroId() == 0)
					return eee;
			} else if (e != null && e != cell.getItem()
					&& id == cell.getItem().getId() && !cell.getItem().isProp()) {
				Equipment eee = (Equipment) cell.getItem();
				if (eee.getHeroId() == 0)
					return eee;
			}
		}
		return null;
	}
	
	/**
	 * 得到将领装备的装备
	 * @param id
	 * @param heroid
	 * @return
	 */
	public Equipment getHeroEquipment(int id, int heroid) {
		if (checkCellDatas())
			return null;
		for (Cell cell : cellDatas) {
			if (id == cell.getItem().getId() && !cell.getItem().isProp()) {
				Equipment eee = (Equipment) cell.getItem();
				if (eee.getHeroId() == heroid) {
					return eee;
				}
			}
		}
		return null;
	}

	/**
	 * 脱装备
	 * 
	 * @param id
	 *            装备Id
	 * @return
	 */
	public Equipment takeOffEquipment(int id, int heroId) {
		PlayerHero playerHero = null;
		if (checkCellDatas()) {
			logger.info("背包为空！");
			return null;
		}
		Equipment eq = getHeroEquipment(id, heroId);
		if(eq == null){
			GameLog.error("脱装备[takeOffEquipment]==用户："+owner.getId()+"|英雄："+heroId+"|不存在装备："+id, null);
			PlayerHero p = owner.getPlayerHeroManager().getHero(heroId);
			Equipment unmE = new Equipment(equipMgr.getEquipment(id));
			if(p != null && unmE != null){
				p.unmountEquip(unmE);
				if(I18nGreeting.LANLANGUAGE_TIPS == 1){
					unmE.setTip(new TipMessage("Take off equip successfully.", ProcotolType.ITEMS_RESP,
							GameConst.GAME_RESP_SUCCESS,ProcotolType.CUTOVER_EQUIMENT));
				}else{
					unmE.setTip(new TipMessage("脱装备成功", ProcotolType.ITEMS_RESP,
							GameConst.GAME_RESP_SUCCESS,ProcotolType.CUTOVER_EQUIMENT));
				}
				
				return unmE;
			}else{
				GameLog.error("脱装备[takeOffEquipment]====用户："+owner.getId()+"|英雄："+heroId+"|不存在装备："+id+"|英雄或装备数据不存在", null);
			}
			return null;
		}
		if (eq.getHeroId() == 0 || heroId != eq.getHeroId()) {
//			logger.info("装备：" + eq + " 将领id:" + heroId);
			if (eq != null) {
//				logger.info("装备将领：" + eq.getHeroId());
				if(I18nGreeting.LANLANGUAGE_TIPS == 1){
					eq.setTip(new TipMessage("This equipment has been equipped by other hero.", ProcotolType.ITEMS_RESP,
							GameConst.GAME_RESP_FAIL,ProcotolType.CUTOVER_EQUIMENT));
				}else{
					eq.setTip(new TipMessage("装备已被其他将领装备", ProcotolType.ITEMS_RESP,
							GameConst.GAME_RESP_FAIL,ProcotolType.CUTOVER_EQUIMENT));
				}
				return eq;
			}
			return null;
		} else {
			playerHero = owner.getPlayerHeroManager().getHero(eq.getHeroId());
			if(playerHero != null){
				eq.setHeroId(0);
				saveCells();// 保存数据库
				// 将领脱装备
				playerHero.unmountEquip(eq);
				
				if(I18nGreeting.LANLANGUAGE_TIPS == 1){
					eq.setTip(new TipMessage("Take off equip successfully.", ProcotolType.ITEMS_RESP,
							GameConst.GAME_RESP_SUCCESS,ProcotolType.CUTOVER_EQUIMENT));
				}else{
					eq.setTip(new TipMessage("脱装备成功", ProcotolType.ITEMS_RESP,
							GameConst.GAME_RESP_SUCCESS,ProcotolType.CUTOVER_EQUIMENT));
				}
			}else{
				if(I18nGreeting.LANLANGUAGE_TIPS == 1){
					eq.setTip(new TipMessage("Take off equip successfully.", ProcotolType.ITEMS_RESP,
							GameConst.GAME_RESP_SUCCESS,ProcotolType.CUTOVER_EQUIMENT));
				}else{
					eq.setTip(new TipMessage("脱装备成功", ProcotolType.ITEMS_RESP,
							GameConst.GAME_RESP_SUCCESS,ProcotolType.CUTOVER_EQUIMENT));
				}
			}
			
			return eq;
		}
	}

	/**
	 * 脱装备
	 * 
	 * @param id
	 *            装备Id
	 * @return
	 */
	public Cell takeOffCell(int id, int heroId) {
		Equipment e = takeOffEquipment(id, heroId);
		if (e == null)
			return null;
		else {
			Cell cell = new Cell();
			cell.setItem(e);
			cell.setItemCount(1);
			saveCells();
			return cell;
		}
	}

	/**
	 * 使用装备
	 * 
	 * @param id
	 *            装备Id
	 * @param heroId
	 *            英雄ID
	 * @return
	 */
	public Equipment userEquipment(int id, int heroId, Equipment e) {
		FightLog.info("用户："+owner.getId());
		PlayerHero playerHero = owner.getPlayerHeroManager().getHero(heroId);
		if (checkCellDatas() || playerHero == null) {
			FightLog.info("用户："+owner.getId()+"|背包或将领为空");
			return null;
		}
		Equipment eq = getUserEquipment(id, e);
		if (eq == null || eq.getHeroId() != 0 || isDrive(playerHero, eq) != 0) {
			FightLog.info("用户："+owner.getId()+"|装备：" + eq + "|将领id:" + heroId + "|已装备");
			return null;
		} else if (isUser(id, heroId) == null
				|| isUser(id, heroId).getTip().getResult() == 0) {
			if(I18nGreeting.LANLANGUAGE_TIPS == 1){
				eq.setTip(new TipMessage("Need level:"
						+ eq.getPrototype().getNeedLevel() + "  Hero level:"
						+ playerHero.getLevel(), ProcotolType.ITEMS_RESP,
						GameConst.GAME_RESP_FAIL,ProcotolType.CUTOVER_EQUIMENT));
			}else{
				eq.setTip(new TipMessage("装备：" + eq + " 需要等级:"
						+ eq.getPrototype().getNeedLevel() + "  将领等级："
						+ playerHero.getLevel(), ProcotolType.ITEMS_RESP,
						GameConst.GAME_RESP_FAIL,ProcotolType.CUTOVER_EQUIMENT));
			}
			
			FightLog.info("用户："+owner.getId()+"|装备：" + eq + " 需要等级:"
					+ eq.getPrototype().getNeedLevel() + "  将领等级："
					+ playerHero.getLevel());
			return eq;
		} else {
			eq.setHeroId(heroId);
			saveCells();// 保存数据库
			// 将领穿装备
			playerHero.mountEquip(eq);
			if(I18nGreeting.LANLANGUAGE_TIPS == 1){
				eq.setTip(new
						 TipMessage("Put on equip successfully.",ProcotolType.ITEMS_RESP,GameConst.GAME_RESP_SUCCESS,ProcotolType.CUTOVER_EQUIMENT));
			}else{
				eq.setTip(new
						 TipMessage("装备穿着成功",ProcotolType.ITEMS_RESP,GameConst.GAME_RESP_SUCCESS,ProcotolType.CUTOVER_EQUIMENT));
			}
			
			return eq;
		}
	}

	public Equipment isUser(int id, int heroId) {
		PlayerHero playerHero = owner.getPlayerHeroManager().getHero(heroId);
		Equipment eq = getUserEquipment(id, null);
		if (playerHero == null || eq == null) {
			return null;
		}
		if (eq.getPrototype().getNeedLevel() > playerHero.getLevel()) {
//			logger.info("装备：" + id + " 将领id:" + playerHero.getId()
//					+ "  装备装备等级不足");
			
			if(I18nGreeting.LANLANGUAGE_TIPS == 1){
				eq.setTip(new TipMessage("Hero's level is too low.", ProcotolType.ITEMS_RESP,
						GameConst.GAME_RESP_FAIL));
			}else{
				eq.setTip(new TipMessage("将领等级不够无法装备！", ProcotolType.ITEMS_RESP,
						GameConst.GAME_RESP_FAIL));
			}
			return eq;
		}
		eq.setTip(new TipMessage("", ProcotolType.ITEMS_RESP,
				GameConst.GAME_RESP_SUCCESS));
		return eq;
	}

	/**
	 * 获取加制效果 技能id
	 * 
	 * @param eId
	 * @return
	 */
	public int getFirmId(int eId, int heroid) {
		Equipment eq = getHeroEquipment(eId, heroid);
		if (eq == null
				|| eq.getEffectId() == 0
				|| (TimeUtils.nowLong()
						- eq.getEffectTime() > 0) || equipMgr.firmDatas == null
				|| equipMgr.firmDatas.size() == 0)
			return 0;
		else {
			FirmEffect fe = equipMgr.firmDatas.get(eq.getEffectId());
			if (fe != null && fe.getType() == 2)
				return fe.getEffectIdNum();
		}
		return 0;
	}

	/**
	 * 获取加制效果 技能id
	 * 
	 * @param eId
	 * @return
	 */
	public int getFirmPoint(int eId, int heroid) {
		Equipment eq = getHeroEquipment(eId, heroid);
		if (eq == null)
			return 0;
		int point = equipMgr.getEquimentPoint(eId);// 基本数值
		if (eq.getEffectId() == 0
				|| (TimeUtils.nowLong()
						- eq.getEffectTime() > 0) || equipMgr.firmDatas == null
				|| equipMgr.firmDatas.size() == 0)
			return point;
		else {
			FirmEffect fe = equipMgr.firmDatas.get(eq.getEffectId());
			if (fe != null && fe.getType() == 1) {// 基本属性加制
				return point + eq.toAddPoint();// 加成数值
			}
			return point;
		}
	}

	/**
	 * 换装备
	 * 
	 * @param id1
	 *            装备1
	 * @param id2
	 *            装备2
	 * @param heroId
	 *            英雄id
	 * @return
	 */
	public synchronized MessageUtil cutoverEquipment(int id1, int id2, int heroId) {
		logger.info("切换装备：" + id1 + "  穿上装备：" + id2 + " 英雄：" + heroId+"--->开始");
		MessageUtil mess = new MessageUtil();
		List<ClientModule> cliLst = new ArrayList<ClientModule>();
		Equipment eq2 = null;
		Equipment eq1 = null;
		PlayerHero hero = owner.getPlayerHeroManager().getHero(heroId);
		if (hero != null && hero.getStatus() != GameConst.HEROSTATUS_IDEL) {
			
			if(I18nGreeting.LANLANGUAGE_TIPS == 1){
				mess.setTip(new TipMessage("The hero is busy, can't equip!",
						ProcotolType.ITEMS_RESP, GameConst.GAME_RESP_FAIL,ProcotolType.CUTOVER_EQUIMENT));
			}else{
				mess.setTip(new TipMessage("将领状态驻防,无法操作装备",
						ProcotolType.ITEMS_RESP, GameConst.GAME_RESP_FAIL,ProcotolType.CUTOVER_EQUIMENT));
			}
			logger.info("将领状态驻防,无法操作装备");
			return mess;
		}
		if (id1 != 0 && id2 == 0) {
			eq1 = takeOffEquipment(id1, heroId);
			if (eq1 == null)
				if(I18nGreeting.LANLANGUAGE_TIPS == 1){
					mess.setTip(new TipMessage("Equip doesn't exist.",
							ProcotolType.ITEMS_RESP, GameConst.GAME_RESP_FAIL,ProcotolType.CUTOVER_EQUIMENT));
				}else{
					mess.setTip(new TipMessage("用户装备不存在", ProcotolType.ITEMS_RESP,
							GameConst.GAME_RESP_FAIL,ProcotolType.CUTOVER_EQUIMENT));
				}
				
			else {
				mess.setTip(eq1.getTip());
			}
			hero = owner.getPlayerHeroManager().getHero(heroId);
		} else if (id2 != 0 && id1 == 0) {
			eq2 = userEquipment(id2, heroId, null);
			if (eq2 == null)
				if(I18nGreeting.LANLANGUAGE_TIPS == 1){
					mess.setTip(new TipMessage("Equip doesn't exist, or equip has been equipped.",
							ProcotolType.ITEMS_RESP, GameConst.GAME_RESP_FAIL,ProcotolType.CUTOVER_EQUIMENT));
				}else{
					mess.setTip(new TipMessage("用户装备不存在或已经被装备",
							ProcotolType.ITEMS_RESP, GameConst.GAME_RESP_FAIL,ProcotolType.CUTOVER_EQUIMENT));
				}
				
			else {
				mess.setTip(eq2.getTip());
			}
			hero = owner.getPlayerHeroManager().getHero(heroId);
		} else if (id1 != 0 && id2 != 0) {
			EquipPrototype e1 = equipMgr.equipmentDatas.get(id1);// owner.getEquipment(id1);
			EquipPrototype e2 = equipMgr.equipmentDatas.get(id2);
			if (e1 != null && e2 != null
					&& e1.getEquipmentType() == e2.getEquipmentType()) {
				if (isUser(id2, heroId) != null
						&& isUser(id2, heroId).getTip().getResult() == 1) {
					eq1 = takeOffEquipment(id1, heroId);
					eq2 = userEquipment(id2, heroId, eq1);
					if (eq1 == null || eq2 == null) {
						hero= null;
//						logger.info("穿上装备失败");
					} else {
						if (eq1.getTip().getResult() == 1
								&& eq2.getTip().getResult() == 1) {
							hero = owner.getPlayerHeroManager().getHero(heroId);
						} else if (eq1.getTip().getResult() == 1
								&& eq2.getTip().getResult() == 0) {
							mess.setTip(eq2.getTip());
						} else if (eq1.getTip().getResult() == 0
								&& eq2.getTip().getResult() == 1) {
							mess.setTip(eq2.getTip());
						}
					}

				} else {
					logger.info("切换装备失败");
				}
			} else {
				if(I18nGreeting.LANLANGUAGE_TIPS == 1){
					mess.setTip(new TipMessage("The equipments you changed don't belong to the same position.",
							ProcotolType.ITEMS_RESP, GameConst.GAME_RESP_FAIL,ProcotolType.CUTOVER_EQUIMENT));
				}else{
					mess.setTip(new TipMessage("用户切换装备不在同一部位",
							ProcotolType.ITEMS_RESP, GameConst.GAME_RESP_FAIL,ProcotolType.CUTOVER_EQUIMENT));
				}
				
				return mess;
			}
		}
		if (hero != null) {
			
			if (eq1 != null){
				eq1.setIsAdd((byte) 2);
				eq1.setOriginal(hero.getId());
				
			}
			if (eq2 != null){
				eq2.setIsAdd((byte) 2);
				eq2.setOriginal(hero.getId());

			}
			cliLst.add(eq1);
			cliLst.add(eq2);
			cliLst.add(hero);
			RespModuleSet rms5 = new RespModuleSet(ProcotolType.ITEMS_RESP);
			rms5.addModules(cliLst);
			AndroidMessageSender.sendMessage(rms5, owner);
			if (eq1 != null){
				eq1.setIsAdd((byte) 1);
				eq1.setOriginal(0);
			}
			if (eq2 != null){
				eq2.setIsAdd((byte) 1);
				eq2.setOriginal(0);
			}
			saveCells();	
			String msg = I18nGreeting.getInstance().getMessage(
					"equipment.change", null);
			mess.setTip(new TipMessage(msg,
					ProcotolType.ITEMS_RESP,
					GameConst.GAME_RESP_SUCCESS,ProcotolType.CUTOVER_EQUIMENT));
		} else {
			if(I18nGreeting.LANLANGUAGE_TIPS == 1){
				mess.setTip(new TipMessage("Fail!",
						ProcotolType.ITEMS_RESP, GameConst.GAME_RESP_FAIL,ProcotolType.CUTOVER_EQUIMENT));
			}else{
				mess.setTip(new TipMessage("用户脱穿失败", ProcotolType.ITEMS_RESP,
						GameConst.GAME_RESP_FAIL,ProcotolType.CUTOVER_EQUIMENT));
			}
			
//			logger.info("脱穿失败，返回为空");
		}
		logger.info("切换装备：" + id1 + "  穿上装备：" + id2 + " 英雄：" + heroId+"--->结束");
		// mess.setModuleLst(cliLst);
		return mess;
	}

	/**
	 * id2 替换 id1
	 * 
	 * @param id1
	 * @param id2
	 * @return
	 */
	public Equipment replaceEquipment(Equipment ee, int id2, int heroId) {
		FightLog.info("replaceEquipment==用户："+owner.getId()+"|原装备："+ee.getId()+"|新装备："+id2+"|英雄："+heroId);
		if (checkCellDatas()) {
			return null;
		}
		Cell cell1 = getCell(ee);
		EquipPrototype e = equipMgr.getEquipment(id2);
		if (cell1 == null) {
			return null;
		}
		if (e == null) {
			return null;
		}
		if(ee.getPrototype().getEquipmentType() != e.getEquipmentType()){
			FightLog.info("replaceEquipment==用户："+owner.getId()+"|原装备："+ee.getId()+"|新装备："+id2+"|英雄："+heroId+"|新旧装备不在同一部位");
			return null;
		}
		if (heroId == 0) {
			Equipment eq = new Equipment(e);
			eq.setHeroId(ee.getHeroId());
			eq.setEffectId(eq.getEffectId());
			eq.setEffectTime(ee.getEffectTime());
			eq.setTip(ee.getTip());
			cell1.setItem(eq);//装备替换
			saveCells();// 保存数据库
			FightLog.info("replaceEquipment==用户："+owner.getId()+"|原装备："+ee.getId()+"|新装备："+id2+"|英雄："+heroId+"|cell:"+cell1.getItem().getId());
			return (Equipment) cell1.getItem();
		} else {
			PlayerHero hero = owner.getPlayerHeroManager().getHero(heroId);
			if (hero == null) {
//				logger.info("英雄为空");
				return null;
			} else {
				Equipment eq = new Equipment(e);
				eq.setHeroId(ee.getHeroId());
				eq.setEffectId(eq.getEffectId());
				eq.setEffectTime(ee.getEffectTime());
				
				hero.unmountEquip(ee);//将领脱掉ee
				hero.mountEquip(eq);//将领穿上eq
				eq.setTip(ee.getTip());
				cell1.setItem(eq);//装备替换
				// ************ rms
				RespModuleSet rms = new RespModuleSet(ProcotolType.HERO_RESP);// 模块消息
				rms.addModule(owner.getPlayerHeroManager().getHero(heroId));
				AndroidMessageSender.sendMessage(rms, owner);
				// ************ rms
				saveCells();// 保存数据库
				FightLog.info("replaceEquipment==用户："+owner.getId()+"|原装备："+ee.getId()+"|新装备："+id2+"|英雄："+heroId+"|cell:"+cell1.getItem().getId());
				return (Equipment) cell1.getItem();
			}
		}

	}

	/**
	 * 获取某个英雄装备
	 * 
	 * @param heroId
	 * @return
	 */
	public List<Cell> getHeroEquipment(int heroId) {
		List<Cell> lst = new ArrayList<Cell>();
		if (checkCellDatas())
			return null;
		else {
			for (Cell cell : cellDatas) {
				if (!cell.getItem().isProp()) {
					Equipment eq = (Equipment) (cell.getItem());
					if (eq.getHeroId() == heroId) {
						lst.add(cell);
					}
				}
			}
		}

		return lst;
	}

	/**
	 * 获取莫格装备
	 * 
	 * @param id
	 * @return
	 */
	public Equipment getUserEquipment(int id) {
		if (checkCellDatas())
			return null;
		else {
			return (Equipment) getCell(id).getItem();
		}
	}

	/**
	 * 获取莫格物品
	 * 
	 * @param id
	 * @return
	 */
	public Props getUserProps(int id) {
		if (checkCellDatas())
			return null;
		else {
			return (Props) getCell(id).getItem();
		}
	}


	/**
	 * 
	 * @param id
	 *            道具id
	 * @return int 道具数量
	 */
	public int countCertainProps(int id) {
		Cell cell = getCell(id);
		int num = 0;
		if (null != cell) {
			num = cell.getItemCount();
		}
		return num;

	}

	/**
	 * 获取拆解材料集合
	 * 
	 * @param eId
	 * @return
	 */
	public List<Integer> dismantProps(int eId) {
		Equipment eqment = getHeroEquipment(eId, 0);
		EquiEffectResult er = isDelete(eqment, 1);
		if (eqment != null && eqment.heroId != 0) {// 被使用
			logger.info("装备" + eqment.getName() + "已使用，不能删除");
			return null;
		}
		EquipPrototype ep = eqment.getPrototype();
		if (ep == null) {
			return null;
		}
		List<Integer> dismantLst = new ArrayList<Integer>();
		EquipmentDismantling ed = equipMgr.getBasicDismant(
				ep.getStrengthenLevel(), ep.getEquipmentLevel());// 获取拆解基本材料
		if (ed == null) {
			logger.info(">>>>>>>>>>>>拆解基本材料...错误<<<<<<<<<<<<<<<");
			return dismantLst;
		}
		logger.info(">>>>>>>>>>>>拆解基本材料=" + ed.toString() + "<<<<<<<<<<<<<<<");
		for (int i = 0; i < ed.getNum(); i++) {
			dismantLst.add((int) ed.getPropId());
		}
		List<Integer> addIds = equipMgr.getAddDismant(ep.getStrengthenLevel(),
				ep.getEquipmentLevel(), ep.getQualityColor());
		if (addIds != null && addIds.size() > 0) {
			logger.info(">>>>>>>>>>>>拆解特殊材料 数量=" + addIds.size()
					+ "<<<<<<<<<<<<<<<");
			dismantLst.addAll(addIds);
		}
		logger.info(">>>>>>>>>>>>删除装备...");
		dellEqui(eqment);// 删除装备
		// 删除 装备
//		RespModuleSet rms = new RespModuleSet(ProcotolType.ITEMS_RESP);
//		 rms.addModule(del);
//		AndroidMessageSender.sendMessage(rms, owner);
		// rms
		for (int i = 0; i < dismantLst.size(); i++) {
			addProps(dismantLst.get(i), 1);// 添加背包
		}
		QuestUtils.checkFinish(owner, QuestUtils.TYPE22, true);
		logger.info(">>>>>>>>>>>>材料添加背包...");
		logger.info(">>>>>>>>>>>>拆解出材料..." + dismantLst.size()
				+ "种<<<<<<<<<<<<<<<");
		return dismantLst;
	}

	// 添加测试数据
	public void addE() {
//		System.out.println("**********" + owner.getId());
		Map<Integer, EquipPrototype> map = equipMgr.equipmentDatas;
		int i = 0;
		for (EquipPrototype p : map.values()) {
			if (i < 100) {
				addEquipment(p.getId(), 1, 0);
				i++;
			}
		}
		Map<Integer, PropsPrototype> maps = propMgr.propsDatas;
		int j = 0;
		for (PropsPrototype pp : maps.values()) {
			if (j < 58) {
				addProps(pp.getId(), 100);
				j++;
			}
		}
		saveCells();
	}

	// 添加测试数据
	public void test() {
		Cell cell = getCell(70406);
		Equipment e = new Equipment(equipMgr.getEquipment(70406));
		e.setEffectId(11);
		cell.setItem(e);
		saveCells();
	}

	/**
	 * 求随机数
	 * 
	 * @param num
	 *            范围
	 * @return
	 */
	public static int random(int num) {
		return (int) (Math.random() * num + 1);
	}

	// 是否是书
	public boolean isBook(int itemId) {
		if (pis == null)
			return false;
		return pis.isBook(itemId);
	}

	// 显示技能id
	public int getBookSkill(int itemId) {
		if (pis == null)
			return 0;
		return pis.getBookSkill(itemId);
	}

	// 显示技能id,只判断玩家背包中是否有
	public Props getBookFromSkill(int skillId) {
		if (pis == null)
			return null;
		return pis.getBookFromSkill(skillId);
	}

	// 显示技能id，从全部道具表中判断
	public Props getBookFromAllSkill(int skillId) {
		if (pis == null)
			return null;
		return pis.getBookFromAllSkill(skillId);
	}

	public TipUtil spyOn(int userId, int heroId, Props props) {
		TipUtil tip = new TipUtil(ProcotolType.ITEMS_RESP);
		tip.setFailTip("fail");
//		logger.info("侦查>>>user:" + userId + "|将领id:" + heroId);

		PlayerCharacter p = null;
		if (userId == owner.getData().getUserid()) {
			p = owner;
		} else {
			p = World.getInstance().getPlayer(userId);
		}
		if (p != null) {
			PlayerHero hero = p.getPlayerHeroManager().getHero(heroId);
			if (hero != null) {
				tip = owner.getPlayerStorageAgent().getPis()
						.userProps(props.getId(), false);
				if (props.getPrototype().getPropsType() != ItemConst.SPY_ON_TYPE
						|| !tip.isResult()) {
					return tip;
				}
//				logger.info("侦查  成功>>>user:" + userId + "|将领id:" + heroId);
				// ******rms
				// RespModuleSet rms = new
				// RespModuleSet(ProcotolType.BUILDING_RESP);
				tip.getLst().add(hero);
				// rms.addModuleBase(tip.getLst());
				// AndroidMessageSender.sendMessage(rms, owner);
				// ********rms
				
				if(I18nGreeting.LANLANGUAGE_TIPS == 1){
					return tip.setSuccTip("Scout successfully.");
				}else{
					return tip.setSuccTip("侦查成功");
				}
			} else {
//				logger.info("将领不存在不存在" + heroId);
				tip.setFailTip("将领不存在");
				return tip;
			}
		} else {
			String msg = i18.getMessage("spyOn.no", null);
			logger.info(msg+":"+ userId);
			tip.setFailTip(msg);
			return tip;
		}
	}

	/**
	 * 兑换名将卡
	 */
	public TipMessage exchangeStar() {// 99个武将碎片（598）兑换一个武将名将卡（599）
		TipMessage tip = new TipMessage("fail", ProcotolType.ITEMS_RESP,
				GameConst.GAME_RESP_SUCCESS);  	 	
		int allCount = allCount(new Props(propMgr.getProps(HERO_FRAGMENT)));
		int count = allCount / 99;
		if(isFull()){
			if(I18nGreeting.LANLANGUAGE_TIPS == 1){
				tip.setMessage("Your bag is full. You can't exchange now.");
			}else{
				tip.setMessage("背包已满！无法兑换！");
			}
			
		}
		if (count > 0 && isDelete(HERO_FRAGMENT, count * 99, Item.ITEM_PROPS).getStatus()) {
			Cell del = dellCellAll(HERO_FRAGMENT, count * 99);// 删除99个武将碎片（598）
			Cell add = addPropsCell(HERO_CARD, count);// 生成一个武将名将卡（599）
			// 删除 装备
			RespModuleSet rms = new RespModuleSet(ProcotolType.ITEMS_RESP);
			rms.addModule(del);
			rms.addModule(add);
			AndroidMessageSender.sendMessage(rms, owner);

			String succ = I18nGreeting.getInstance().getMessage(
					"exchange.star.succ", new Object[] { count });
			tip.setMessage(succ);
		} else {
			String fail = I18nGreeting.getInstance().getMessage(
					"exchange.star.fail", new Object[] {});
			tip.setMessage(fail);
		}
		return tip;
	}
	
	public static void main(String[] args) {
		List lst = new ArrayList();
		lst.add("a");
		lst.add("b");
		System.out.println(lst.get(lst.size() - 1));
	}
}
