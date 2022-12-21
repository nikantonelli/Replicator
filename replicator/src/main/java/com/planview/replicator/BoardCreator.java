package com.planview.replicator;

import org.json.JSONObject;

import com.planview.replicator.leankit.Board;
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
				// Create a blank board
				Board dstBrd = LkUtils.createBoard(cfg, cfg.destination);
				if (dstBrd == null) {
					d.p(Debug.ERROR, "Cannot create destination board \"%s\" ... skipping\n",
							cfg.destination.BoardName);
					return false;
				}
				details.put("allowUsersToDeleteCards", true);
				details.put("customIconFieldLabel", srcBrd.customIconFieldLabel);

				// TODO: Need to check and warn over mismatch in board levels
				details.put("boardLevel", srcBrd.level.depth);

				// TODO: check that customIcons match between source and destination
				// Fetch the customIcons on the source, if there are some, then set enable on
				// destination
				CustomIcon[] cis = LkUtils.getCustomIcons(cfg, cfg.source, srcBrd.id);
				if (cis != null) {
					details.put("customIconFieldLabel", srcBrd.customIconFieldLabel);
					details.put("enableCustomIcon", true);
				}

				// Get the custom Fields from the source
				CustomIcon[] srcIcons = LkUtils.getCustomIcons(cfg, cfg.source);
				CustomIcon[] dstIcons = LkUtils.getCustomIcons(cfg, cfg.destination);

				Integer matchedIcons = 0;
				for (int i = 0; i < srcIcons.length; i++) {
					boolean matched = false;
					for (int j = 0; j < dstIcons.length; j++) {
						if (dstIcons[j].name.equals(srcIcons[i].name)) {
							matchedIcons++;
							break;
						}
					}
					if (!matched) {
						LkUtils.createCustomIcon(cfg, cfg.destination, srcIcons[i]);
						d.p(Debug.WARN, "Creating Icon %s on %s\n", srcIcons[i].name, cfg.destination.BoardName);
					}
				}
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
