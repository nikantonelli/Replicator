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

	
    public void go() {
		//Check to see if source and destination are on the same machine
		//If so, just create board from original (without cards)
		String src = cfg.source.Url;
		String dst = cfg.destination.Url;

		//Check whether user has mistyped url with / on end.
		if (src.endsWith("/")) src = src.substring(0, src.length()-1);
		if (dst.endsWith("/")) src = dst.substring(0, dst.length()-1);

		if (src.equals(dst)){
			Board brd = LkUtils.duplicateBoard( cfg);
			JSONObject details = new JSONObject();
			details.put("allowPlanviewIntegration", true);
			LkUtils.updateBoard(cfg, cfg.destination, brd.id, details);
		} else {

			//Here, we have to copy across all the set up of the original board

			//First create a new board
			Board dstBrd = LkUtils.createBoard(cfg, cfg.destination);
			Board srcBrd = LkUtils.getBoardByTitle(cfg, cfg.source);
			JSONObject updates = new JSONObject();
			updates.put("enableCustomIcon", true);
			updates.put("allowUsersToDeleteCards", true);
			updates.put("customIconFieldLabel", srcBrd.customIconFieldLabel);

			//TODO: Need to check and warn over mismatch in board levels
			updates.put("boardLevel", srcBrd.level.depth);
			
			//TODO: check that customIcons match between source and destination
			updates.put("enableCustomIcon", true);

			//Get the custom Fields from the source
			CustomIcon[] srcIcons = LkUtils.getCustomIcons(cfg, cfg.source);
			CustomIcon[] dstIcons = LkUtils.getCustomIcons(cfg, cfg.destination);

			Integer matchedIcons = 0;
			for (int i = 0; i < srcIcons.length; i++){
				boolean matched = false;
				for ( int j = 0; j < dstIcons.length; j++){
					if (dstIcons[j].name.equals(srcIcons[i].name)){
						matchedIcons++;
						break;
					}
				}
				if (!matched) {
					LkUtils.createCustomIcon( cfg,  cfg.destination,  srcIcons[i]);
					d.p(Debug.WARN, "Creating Icon %s on %s\n", srcIcons[i].name, cfg.destination.BoardName);
				}
			}
		}
	}
}

