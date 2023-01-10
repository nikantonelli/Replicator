package com.planview.replicator;

import java.util.ArrayList;

import org.apache.commons.lang.ArrayUtils;
import org.json.JSONObject;

import com.planview.replicator.leankit.Board;
import com.planview.replicator.leankit.BoardLevel;
import com.planview.replicator.leankit.CustomIcon;

public class BoardCreator {
	Debug d = new Debug();
	InternalConfig cfg = null;

	public BoardCreator(InternalConfig config) {
		cfg = config;
		d.setLevel(config.debugLevel);
	}

	public Boolean go() {
		// Check to see if source and destination are on the same machine
		// If so, just create board from original (without cards)
		String src = cfg.source.Url;
		String dst = cfg.destination.Url;

		// Check whether user has mistyped url with / on end.
		if (src.endsWith("/"))
			src = src.substring(0, src.length() - 1);
		if (dst.endsWith("/"))
			dst = dst.substring(0, dst.length() - 1);

		JSONObject details = new JSONObject();
		details.put("allowPlanviewIntegration", true);

		if (src.equals(dst)) {
			Board brd = LkUtils.duplicateBoard(cfg);
			if (brd != null) {
				LkUtils.updateBoard(cfg, cfg.destination, brd.id, details);
				brd = LkUtils.getBoardById(cfg, cfg.destination, brd.id);
				d.p(Debug.INFO, "Created new board. id: %s, title: \"%s\"\n", brd.id, brd.title);
			} else {
				d.p(Debug.ERROR, "Cannot duplicate locally from \"%s\" to \"%s\" ... skipping\n", cfg.source.BoardName,
						cfg.destination.BoardName);
				return false;
			}
		} else {

			// Here, we have to copy across all the set up of the original board
			Board srcBrd = LkUtils.getBoardByTitle(cfg, cfg.source);
			if (srcBrd != null) {
				// Create a blank board if needed
				Board dstBrd = LkUtils.getBoardByTitle(cfg, cfg.destination);
				if (dstBrd == null) {
					dstBrd = LkUtils.createBoard(cfg, cfg.destination);
				}
				if (dstBrd == null) {
					d.p(Debug.ERROR, "Cannot create destination board \"%s\" ... skipping\n",
							cfg.destination.BoardName);
					return false;
				}
				details.put("allowUsersToDeleteCards", srcBrd.allowUsersToDeleteCards);
				details.put("baseWipOnCardSize", srcBrd.baseWipOnCardSize);
				details.put("enableCustomIcon", srcBrd.classOfServiceEnabled);
				details.put("customIconFieldLabel", srcBrd.customIconFieldLabel);
				details.put("description", srcBrd.description);

				/**
				 * 
				 * Check for correct board levels
				 * 
				 *  
				 **/ 
				ArrayList<BoardLevel> srcLevels = LkUtils.getBoardLevels(cfg, cfg.source);
				ArrayList<BoardLevel> dstLevels = LkUtils.getBoardLevels(cfg, cfg.destination);

				int gotDstLevels = 0;
				for (int i = 0; i < srcLevels.size(); i++) {
					for (int j = 0; j < dstLevels.size(); j++) {
						if (srcLevels.get(i).label.equals(dstLevels.get(j).label)) {
							gotDstLevels += 1;
						}
					}
				}
				if (gotDstLevels != srcLevels.size()) {
					d.p(Debug.WARN, "Mismatch between source and destination board levels - resetting destination\n");
					BoardLevel[] bla = {};
					for (int i = 0; i < srcLevels.size(); i++) {
						BoardLevel current = srcLevels.get(i);
						bla = (BoardLevel[]) ArrayUtils.add(bla,
								new BoardLevel(current.depth, current.label, current.color));
					}
					LkUtils.setBoardLevels(cfg, cfg.destination, bla);
					details.put("boardLevel", srcBrd.level.depth);

				}

				/**
				 * 
				 * Check for correct customIcons
				 * 
				 *  
				 **/ 
				
				 // Fetch the customIcons on the source, if there are some, then set enable on
				// destination - this shouldn't affect any boards that already have customIcons
				CustomIcon[] srcIcons = LkUtils.getCustomIcons(cfg, cfg.source);
				if (srcIcons != null) {
					details.put("customIconFieldLabel", srcBrd.customIconFieldLabel);
					LkUtils.enableCustomIcons(cfg, cfg.destination);
				}

				// Get the custom Fields from the destination, if they already exist
				CustomIcon[] dstIcons = LkUtils.getCustomIcons(cfg, cfg.destination);

				Integer matchedIcons = 0;
				Integer[] unMatched = {};
				for (int i = 0; i < srcIcons.length; i++) {
					boolean matched = false;
					// If we have them, check to see if it is the same name ('unique' identifier on
					// LK)
					if (dstIcons != null) {
						for (int j = 0; j < dstIcons.length; j++) {
							if (dstIcons[j].name.equals(srcIcons[i].name)) {
								matchedIcons++;
								matched = true;
								break;
							} else {
								unMatched = (Integer[]) ArrayUtils.add(unMatched, j);
							}
						}
					}
					if (!matched) {
						LkUtils.createCustomIcon(cfg, cfg.destination, srcIcons[i]);
						d.p(Debug.WARN, "Creating Icon %s on %s\n", srcIcons[i].name, cfg.destination.BoardName);
					}
				}

				/**
				 * 
				 * Push all the remaining updates
				 * 
				 */
				LkUtils.updateBoard(cfg, cfg.destination, dstBrd.id, details);
			} else {
				d.p(Debug.ERROR,
						"Cannot locate source boards \"%s\" for creation of destination \"%s\" .... skipping\n",
						cfg.source.BoardName, cfg.destination.BoardName);
				return false;
			}
		}
		return true;
	}
}
