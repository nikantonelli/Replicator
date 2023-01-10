package com.planview.replicator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.lang.ArrayUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planview.replicator.leankit.Board;
import com.planview.replicator.leankit.BoardLevel;
import com.planview.replicator.leankit.BoardUser;
import com.planview.replicator.leankit.Card;
import com.planview.replicator.leankit.CardType;
import com.planview.replicator.leankit.CustomField;
import com.planview.replicator.leankit.CustomIcon;
import com.planview.replicator.leankit.Lane;
import com.planview.replicator.leankit.LeanKitAccess;
import com.planview.replicator.leankit.Task;
import com.planview.replicator.leankit.User;

public class LkUtils {

	/**
	 * Next up is all the Leankit access routines
	 * 
	 */

	public static String getLanePathFromId(InternalConfig iCfg, Access accessCfg, String laneId) {
		Board brd = null;
		Lane[] lanes = null;
		if (iCfg.cache != null) {
			brd = iCfg.cache.getBoardByTitle(accessCfg.BoardName);
			if (brd != null) {
				lanes = brd.lanes;
			}
		} else {
			LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
			brd = lka.fetchBoardFromTitle(accessCfg.BoardName);
			if (brd != null) {
				brd = lka.fetchBoardFromId(brd.id);
				lanes = brd.lanes;
			}
		}

		Lane lane = LkUtils.getLaneFromId(lanes, laneId);
		String lanePath = "";
		if (lane == null) {
			return lanePath;
		} else {
			lanePath = lane.name;
		}

		while (lane.parentLaneId != null) {
			Lane parentLane = LkUtils.getLaneFromId(lanes, lane.parentLaneId);
			if (parentLane != null) {
				lanePath = parentLane.name + "^" + lanePath;
			}
			lane = parentLane;
		}
		return lanePath;
	}

	static Lane getLaneFromId(Lane[] lanes, String id) {
		for (int i = 0; i < lanes.length; i++) {
			if (lanes[i].id.equals(id)) {
				return lanes[i];
			}
		}

		return null;
	}

	public static String getUrl(InternalConfig iCfg, Access accessCfg) {
		LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
		return lka.getCurrentUrl();
	}

	public static Card getCard(InternalConfig iCfg, Access accessCfg, String id) {
		Card card = null;
		if (iCfg.cache != null) {
			card = iCfg.cache.getCard(id);
		} else {
			LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
			card = lka.fetchCard(id);
		}
		return card;
	}

	public static byte[] getAttachment(InternalConfig iCfg, Access accessCfg, String cardId, String attId) {
		LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
		return lka.fetchAttachment(cardId, attId);
	}

	public static Board getBoardByTitle(InternalConfig iCfg, Access accessCfg) {
		Board brd = null;
		if (iCfg.cache != null) {
			brd = iCfg.cache.getBoardByTitle(accessCfg.BoardName);
		} else {
			LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
			brd = lka.fetchBoardFromTitle(accessCfg.BoardName);
			if (brd != null)
				brd = lka.fetchBoardFromId(brd.id);
		}
		return brd;
	}

	public static Board getBoardById(InternalConfig iCfg, Access accessCfg, String id) {
		Board brd = null;
		if (iCfg.cache != null) {
			brd = iCfg.cache.getBoardById(id);
		} else {
			LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
			brd = lka.fetchBoardFromId(id);
		}
		return brd;
	}

	public static ArrayList<Card> getCardIdsFromBoard(InternalConfig iCfg, Access accessCfg) {
		LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
		Board brd = getBoardByTitle(iCfg, accessCfg);
		ArrayList<Card> cards = null;
		if (brd != null) {
			cards = lka.fetchCardIdsFromBoard(brd.id, iCfg.exportArchived);
		}
		return cards;
	}

	public static ArrayList<Task> getTaskIdsFromCard(InternalConfig iCfg, Access accessCfg, String cardId) {
		LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
		ArrayList<Task> tasks = lka.fetchTaskIds(cardId);
		return tasks;
	}

	public static ArrayList<Task> getTasksFromCard(InternalConfig iCfg, Access accessCfg, String cardId) {
		LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
		ArrayList<Task> tasks = lka.fetchTasks(cardId);
		return tasks;
	}

	public static ArrayList<CardType> getCardsTypesFromBoard(InternalConfig iCfg, Access accessCfg,
			String boardName) {
		LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
		ArrayList<CardType> types = null;
		if (iCfg.cache != null) {
			Board brd = iCfg.cache.getBoardByTitle(boardName);
			if (brd != null) {
				types = new ArrayList<>();
				for (int i = 0; i < brd.cardTypes.length; i++) {
					types.add(brd.cardTypes[i]);
				}
			}
		} else {
			Board brd = lka.fetchBoardFromTitle(boardName);
			if (brd != null)
				types = lka.fetchCardTypes(brd.id);
		}
		return types;
	}

	public static Card getCardByTitle(InternalConfig iCfg, Access accessCfg, String title) {
		LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
		Board brd = lka.fetchBoardFromTitle(accessCfg.BoardName);
		return (brd != null) ? lka.fetchCardByTitle(brd.id, title) : null;
	}

	public static Card getCardByTitle(InternalConfig iCfg, Access accessCfg, String boardName, String title) {
		LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
		Board brd = lka.fetchBoardFromTitle(boardName);
		return (brd != null) ? lka.fetchCardByTitle(brd.id, title) : null;
	}

	public static CardType getCardTypeFromBoard(InternalConfig iCfg, Access accessCfg, String name,
			String boardName) {
		return LkUtils.getCardTypeFromList(getCardsTypesFromBoard(iCfg, accessCfg, boardName), name);
	}

	public static CardType getCardTypeFromList(ArrayList<CardType> cardTypes, String name) {
		if (cardTypes != null) {
			Iterator<CardType> cti = cardTypes.iterator();
			while (cti.hasNext()) {
				CardType ct = cti.next();
				if (ct.name.equals(name)) {
					return ct;
				}
			}
		}
		return null;
	}

	public static Card createCard(InternalConfig iCfg, Access accessCfg, JSONObject fieldLst) {
		LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
		// First create an empty card and get back the full structure

		Card newCard = lka.createCard(fieldLst);
		if (newCard != null) {
			if (iCfg.cache != null) {
				iCfg.cache.setCard(newCard);
			}
		}
		return newCard;
	}

	public static Card updateCard(InternalConfig iCfg, Access accessCfg, String cardId, JSONObject updates) {
		LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
		Card card = null;
		Board brd = null;

		if (iCfg.cache != null) {
			card = iCfg.cache.getCard(cardId);
			brd = iCfg.cache.getBoardByTitle(accessCfg.BoardName);
		} else {
			card = lka.fetchCard(cardId);
			brd = lka.fetchBoardFromId(accessCfg.BoardName);
		}
		card = lka.updateCardFromId(brd, card, updates);
		if (card != null) {
			if (iCfg.cache != null) {
				iCfg.cache.setCard(card);
			}
		}
		return card;
	}

	public static Board updateBoard(InternalConfig iCfg, Access accessCfg, String boardId, JSONObject updates) {
		LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
		Board brd = null;

		if (iCfg.cache != null) {
			brd = iCfg.cache.getBoardById(boardId);
		} else {
			brd = lka.fetchBoardFromTitle(accessCfg.BoardName);
		}
		lka.updateBoardById(brd.id, updates); // returns 204 No Content
		brd = lka.fetchBoardFromTitle(brd.id); // so refetch
		if (brd != null) {
			if (iCfg.cache != null) {
				iCfg.cache.setBoard(brd);
			}
		}
		return brd;
	}

	public static void archiveBoardById(InternalConfig iCfg, Access accessCfg, String boardId) {
		LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
		if (iCfg.cache != null) {
			iCfg.cache.unsetBoardById(boardId);
		}
		lka.archiveBoard(boardId);
	}

	public static boolean deleteBoard(InternalConfig iCfg, Access accessCfg) {
		Board brd = getBoardByTitle(iCfg, accessCfg);
		if (brd != null) {
			deleteBoardById(iCfg, accessCfg, brd.id);
			return true;
		}
		return false;
	}

	public static void deleteBoardById(InternalConfig iCfg, Access accessCfg, String boardId) {
		LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
		if (iCfg.cache != null) {
			iCfg.cache.unsetBoardById(boardId);
		}
		lka.deleteBoard(boardId);
	}

	public static void deleteCard(InternalConfig iCfg, Access accessCfg, String title) {
		Card crd = getCardByTitle(iCfg, accessCfg, title);
		if (crd != null)
			deleteCardById(iCfg, accessCfg, crd.id);
	}

	public static void deleteCardById(InternalConfig iCfg, Access accessCfg, String cardId) {
		LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
		if (iCfg.cache != null) {
			iCfg.cache.unsetCardById(cardId);
		}
		lka.deleteCard(cardId);
	}

	static ArrayList<Lane> getLanesFromName(ArrayList<Lane> lanes, String name) {
		ArrayList<Lane> ln = new ArrayList<>();
		for (int i = 0; i < lanes.size(); i++) {
			String laneName = lanes.get(i).name;
			if (laneName.equals(name)) {
				ln.add(lanes.get(i));
			}
		}
		return ln;
	}

	public static Lane getLaneFromBoardTitle(InternalConfig iCfg, Access accessCfg, String boardName, String name) {
		LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
		Board brd = null;
		if (iCfg.cache != null) {
			brd = iCfg.cache.getBoardByTitle(boardName);
		} else {
			brd = lka.fetchBoardFromTitle(boardName);
			if (brd != null) {
				brd = lka.fetchBoardFromId(brd.id);
			}
		}
		if (brd != null) {
			return LkUtils.getLaneFromString(brd, name);
		}
		return null;
	}

	public static Lane getLaneFromBoardId(InternalConfig iCfg, Access accessCfg, String id, String name) {
		Board brd = null;
		if (iCfg.cache != null) {
			brd = iCfg.cache.getBoardById(id);
		} else {
			LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
			brd = lka.fetchBoardFromId(id);
		}
		if (brd != null) {
			return LkUtils.getLaneFromString(brd, name);
		}
		return null;
	}

	static Lane getLaneFromId(ArrayList<Lane> allLanes, String id) {
		Lane lane = null;
		Iterator<Lane> iter = allLanes.iterator();
		while (iter.hasNext()) {
			Lane ln = iter.next();
			if (ln.id.equals(id)) {
				lane = ln;
				break;
			}
		}
		return lane;
	}

	static Lane getParentLane(ArrayList<Lane> searchLanes, Lane lane) {
		Lane parentLane = null;
		if (lane.parentLaneId != null) {
			parentLane = getLaneFromId(searchLanes, lane.parentLaneId);
		}
		return parentLane;
	}

	static Lane getLaneFromString(Board brd, String name) {
		// Split lane in spreadhseet into bits
		String[] lanes = name.split("\\^");

		// Get the list of lanes in the target board
		ArrayList<Lane> searchLanes = new ArrayList<>(Arrays.asList(brd.lanes));

		// Work out the default drop lane in case we can't locate the right lane
		Lane foundLane = null;
		Lane defaultLane = null;
		Iterator<Lane> ddlIter = searchLanes.iterator();
		while (ddlIter.hasNext()) {
			Lane cl = ddlIter.next();
			if (cl.isDefaultDropLane) {
				defaultLane = cl;
				break;
			}
		}

		// For each possible lane, check up its hierarchy to see if it matches the bits
		// we have
		int j = lanes.length - 1;
		// Find those lanes that match the 'bit'
		ArrayList<Lane> lanesToCheck = getLanesFromName(searchLanes, lanes[j]);

		Iterator<Lane> ltcIt = lanesToCheck.iterator();

		while (ltcIt.hasNext()) {
			Boolean found = true;
			Lane thisLane = ltcIt.next();
			Lane parentLane = getParentLane(searchLanes, thisLane);
			int k = j;
			while (parentLane != null) {
				if ((k > 0) && parentLane.name.equals(lanes[--k])) {
					parentLane = getParentLane(searchLanes, parentLane);
				} else {
					found = false;
					break;
				}
				if ((k > 0) && (parentLane == null)) {
					found = false;
				}
			}
			if ((k == 0) && found) {
				foundLane = thisLane;
				break;
			}
		}

		if (foundLane == null) {
			return defaultLane;
		}

		return foundLane;
	}

	public static Lane getLaneFromCard(InternalConfig iCfg, Access accessCfg, String cardId, String laneType) {
		Lane lane = null;
		ArrayList<Lane> lanes = null;

		if (iCfg.cache != null) {
			lanes = iCfg.cache.getTaskBoard(cardId);
		} else {
			LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
			lanes = lka.fetchTaskLanes(cardId);
		}
		if (lanes != null) {
			Iterator<Lane> lIter = lanes.iterator();
			while (lIter.hasNext()) {
				Lane laneToCheck = lIter.next();
				if (laneToCheck.laneType.equals(laneType)) {
					lane = laneToCheck;
				}
			}
		}
		return lane;
	}

	public static User getUser(InternalConfig iCfg, Access accessCfg, String id) {
		User user = null;
		if (iCfg.cache != null) {
			user = iCfg.cache.getUserById(id);
		} else {
			LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
			user = lka.fetchUserById(id);
		}
		return user;
	}

	public static ArrayList<BoardLevel> getBoardLevels(InternalConfig iCfg, Access accessCfg) {
		LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
		return lka.fetchBoardLevels();
	}

	public static CustomField getCustomField(InternalConfig iCfg, Access accessCfg, String name) {
		CustomField[] cfs = LkUtils.getCustomFields(iCfg, accessCfg);
		for (int j = 0; j < cfs.length; j++) {
			if (cfs[j].label.equals(name)) {
				return cfs[j];
			}
		}
		return null;
	}

	public static CustomIcon getCustomIcon(InternalConfig iCfg, Access accessCfg, String name) {
		CustomIcon[] cis = LkUtils.getCustomIcons(iCfg, accessCfg);
		if (cis != null) {
			for (int j = 0; j < cis.length; j++) {
				if (cis[j].name.equals(name)) {
					return cis[j];
				}
			}
		}
		return null;
	}

	public static CustomIcon getCustomIcon(InternalConfig iCfg, Access accessCfg, String name, String boardId) {
		CustomIcon[] cis = LkUtils.getCustomIcons(iCfg, accessCfg, boardId);
		for (int j = 0; j < cis.length; j++) {
			if (cis[j].name.equals(name)) {
				return cis[j];
			}
		}
		return null;
	}

	public static CustomField[] getCustomFields(InternalConfig iCfg, Access accessCfg) {
		LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
		CustomField[] fields = null;
		Board brd = null;
		if (iCfg.cache != null) { // and store it in the cache if we have one
			brd = iCfg.cache.getBoardByTitle(accessCfg.BoardName);
		} else {
			brd = lka.fetchBoardFromTitle(accessCfg.BoardName);
			if (brd != null)
				brd = lka.fetchBoardFromId(brd.id); // Fetch the FULL listing of the board this time
		}
		if (brd != null)
			fields = LkUtils.getCustomFields(iCfg, accessCfg, brd.id);
		return fields;
	}

	public static CustomField[] getCustomFields(InternalConfig iCfg, Access accessCfg, String boardId) {
		CustomField[] fields = null;
		if (iCfg.cache != null) {
			fields = iCfg.cache.getCustomFields();
		} else {
			LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
			fields = lka.fetchCustomFields(boardId).customFields;
		}
		return fields;
	}

	public static CustomIcon[] getCustomIcons(InternalConfig iCfg, Access accessCfg, String id) {
		CustomIcon[] fields = null;
		if (iCfg.cache != null) {
			fields = iCfg.cache.getCustomIcons();
		} else {
			LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
			Board brd = lka.fetchBoardFromId(id);
			if (brd != null)
				fields = lka.fetchCustomIcons(brd.id).customIcons;
		}
		return fields;
	}

	public static CustomIcon[] getCustomIcons(InternalConfig iCfg, Access accessCfg) {
		CustomIcon[] fields = null;
		if (iCfg.cache != null) {
			fields = iCfg.cache.getCustomIcons();
		} else {
			LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
			Board brd = lka.fetchBoardFromTitle(accessCfg.BoardName);
			if (brd != null)
				fields = lka.fetchCustomIcons(brd.id).customIcons;
		}
		return fields;
	}

	public static User getUserByName(InternalConfig iCfg, Access accessCfg, String username) {
		User user = null;
		if (iCfg.cache != null) {
			user = iCfg.cache.getUserByName(username);
		} else {
			LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
			user = lka.fetchUserByName(username);
		}
		return user;
	}

	public static ArrayList<BoardUser> getUsers(InternalConfig iCfg, Access accessCfg) {

		ArrayList<BoardUser> users = null;
		if (iCfg.cache != null) {
			users = iCfg.cache.getBoardUsers();
		} else {
			LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
			Board brd = lka.fetchBoardFromTitle(accessCfg.BoardName);
			if (brd != null)
				users = lka.fetchUsers(brd.id);
		}
		return users;
	}

	public static Card addTask(InternalConfig iCfg, Access accessCfg, String cardId, JSONObject item) {
		LeanKitAccess lka = new LeanKitAccess(accessCfg, iCfg.debugLevel);
		Card card = lka.addTaskToCard(cardId, item);
		if (iCfg.cache != null) {
			iCfg.cache.setCard(card);
		}
		return card;
	}

	public static Board duplicateBoard(InternalConfig cfg) {
		LeanKitAccess lka = new LeanKitAccess(cfg.source, cfg.debugLevel);
		Board brd = lka.fetchBoardFromTitle(cfg.source.BoardName);
		if (brd != null) {
			JSONObject details = new JSONObject();
			details.put("title", cfg.destination.BoardName);
			details.put("fromBoardId", brd.id);
			details.put("includeExistingUsers", true);
			details.put("includeCards", false);
			details.put("isShared", true);
			details.put("sharedBoardRole", "boardUser");
			details.put("excludeCompletedAndArchiveViolations", true);
			return lka.createBoard(details);
		} else {
			return null;
		}
	}

	public static Board createBoard(InternalConfig cfg, Access accessCfg) {
		LeanKitAccess lka = new LeanKitAccess(accessCfg, cfg.debugLevel);
		JSONObject details = new JSONObject();
		details.put("title", accessCfg.BoardName);
		return lka.createBoard(details);
	}

	public static Boolean enableCustomIcons(InternalConfig cfg, Access accessCfg){
		LeanKitAccess lka = new LeanKitAccess(cfg.destination, cfg.debugLevel);
		Board brd = lka.fetchBoardFromTitle(cfg.destination.BoardName);
		Boolean state = lka.fetchCustomIcons(brd.id) != null;
		if (brd != null) {
			JSONObject details = new JSONObject();
			details.put("enableCustomIcon", true);
			LkUtils.updateBoard(cfg, accessCfg, brd.id, details);
		}
		return state;
	}
	public static void createCustomIcon(InternalConfig cfg, Access accessCfg, CustomIcon customIcon) {
		JSONObject ci = new JSONObject(customIcon);
		ci.remove("id");
	}

	public static void setBoardLevels(InternalConfig cfg, Access accessCfg, BoardLevel[] srcLevels) {
		LeanKitAccess lka = new LeanKitAccess(accessCfg, cfg.debugLevel);
		JSONObject levels = new JSONObject();
		levels.put("boardLevels", srcLevels);
		lka.updateBoardLevels(levels);
	}
}
