package com.planview.lkutility.leankit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.planview.lkutility.Access;
import com.planview.lkutility.InternalConfig;

public class AccessCache {

	/**
	 * For now, we will put everything into HashMaps
	 * At some later date, we could create a better organisation of info
	 */
	HashMap<String, Board> boardMap = new HashMap<>();
	HashMap<String, Card> cardMap = new HashMap<>();
	HashMap<String, User> userIdMap = new HashMap<>();
	HashMap<String, User> usernameMap = new HashMap<>();
	HashMap<String, CustomField[]> customFieldMap = new HashMap<>();
	HashMap<String, CustomIcon[]> customIconMap = new HashMap<>();
	HashMap<String, ArrayList<Lane>> taskBoardMap = new HashMap<>();
	HashMap<String, ArrayList<BoardUser>> boardUserMap = new HashMap<>();
	HashMap<String, Task> taskMap = new HashMap<>();
	InternalConfig iCfg;
	Access accessCfg;

	public AccessCache(InternalConfig cfg, Access accCfg) {
		iCfg = cfg;
		accessCfg = accCfg;
	}

	public void setCustomFields(CustomField[] cfm, String title) {
		if (customFieldMap.get(title) != null) {
			customFieldMap.remove(title);
		}
		customFieldMap.put(title, cfm);
	}

	public CustomField[] getCustomFields() {
		return getCustomFields(accessCfg.BoardName);
	}

	public CustomField[] getCustomFields(String title) {
		CustomField[] cfm = customFieldMap.get(title);
		if (cfm == null) {
			LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
			Board brd = getBoardByTitle(title);
			if (brd == null) {
				return null;
			}
			cfm = lka.fetchCustomFields(brd.id).customFields;
			if (cfm != null) {
				setCustomFields(cfm, title);
			}
		}
		return cfm;
	}

	public void setBoard(Board brd) {
		if (boardMap.get(brd.title) != null) {
			boardMap.remove(brd.title);
		}
		boardMap.put(brd.title, brd);
	}

	public Board getBoardByTitle(String title) {
		Board brd = boardMap.get(title);
		if (brd == null) {
			LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
			brd = lka.fetchBoardFromTitle(title);
			if (brd != null) {
				brd = lka.fetchBoardFromId(brd.id);
				setBoard(brd);
			}
		}
		return brd;
	}

	public Board getBoardById(String id) {
		Board brd = null;
		Iterator<Map.Entry<String, Board>>  es = boardMap.entrySet().iterator();
		while (es.hasNext()){
			Entry<String, Board> ent = es.next();
			if (ent.getValue().id.equals(id)){
				brd = ent.getValue();
			}
		}
		if (brd == null){
			LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
			brd = lka.fetchBoardFromId(id);
		}
		return brd;
	}

	public User getUserById(String id) {
		User user = userIdMap.get(id);
		if (user == null) {
			LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
			user = lka.fetchUserById(id);
			if (user != null) {
				setUser(user);
			}
		}
		return user;
	}

	public User getUserByName(String username) {
		User user = usernameMap.get(username);
		if (user == null) {
			LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
			user = lka.fetchUserByName(username);
			if (user != null) {
				setUser(user);
			}
		}
		return user;
	}

	public void setUser(User user) {
		if (userIdMap.get(user.id) != null) {
			userIdMap.remove(user.id);
		}
		if (usernameMap.get(user.username) != null) {
			usernameMap.remove(user.username);
		}
		userIdMap.put(user.id, user);
		usernameMap.put(user.username, user);
	}

	public void setCard(Card card) {
		if (cardMap.get(card.title) != null) {
			cardMap.remove(card.title);
		}
		cardMap.put(card.title, card);
	}

	public Card getCard(String cardId) {
		Card card = cardMap.get(cardId);
		if (card == null) {
			LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
			card = lka.fetchCard(cardId);
			if (card != null) {
				setCard(card);
			}
		}
		return card;
	}

	public Card getCardByTitle(String title) {
		Card card = cardMap.get(title);
		if (card == null) {
			LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
			card = lka.fetchCardByTitle(null, title);
			if (card != null) {
				setCard(card);
			}
		}
		return card;
	}

	public void setTaskBoard(String cardId, ArrayList<Lane> lanes) {
		if (taskBoardMap.get(cardId) != null) {
			taskBoardMap.remove(cardId);
		}
		taskBoardMap.put(cardId, lanes);
	}

	public ArrayList<Lane> getTaskBoard(String cardId) {
		ArrayList<Lane> lanes = taskBoardMap.get(cardId);
		if (lanes == null) {
			LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
			lanes = lka.fetchTaskLanes(cardId);
			if (lanes != null) {
				setTaskBoard(cardId, lanes);
			}
		}
		return lanes;
	}

	public void setBoardUsers(String boardname, ArrayList<BoardUser> users) {
		if (boardUserMap.get(boardname) != null) {
			boardUserMap.remove(boardname);
		}
		boardUserMap.put(boardname, users);
	}

	public ArrayList<BoardUser> getBoardUsers() {
		ArrayList<BoardUser> users = boardUserMap.get(accessCfg.BoardName);
		if (users == null) {
			LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
			Board brd = lka.fetchBoardFromTitle(accessCfg.BoardName);
			users = lka.fetchUsers(brd.id);
			if (users != null) {
				setBoardUsers(accessCfg.BoardName, users);
			}
		}
		return users;
	}

	public void setCustomIcons(CustomIcon[] cfm, String BoardName) {
		if (customIconMap.get(BoardName) != null) {
			customIconMap.remove(BoardName);
		}
		customIconMap.put(BoardName, cfm);
	}

	public CustomIcon[] getCustomIcons() {
		CustomIcon[] cfm = customIconMap.get(accessCfg.BoardName);
		if (cfm == null) {
			LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
			Board brd = lka.fetchBoardFromTitle(accessCfg.BoardName);
			cfm = lka.fetchCustomIcons(brd.id).customIcons;
			if (cfm != null) {
				setCustomIcons(cfm, accessCfg.BoardName);
			}
		}
		return cfm;
	}

}
