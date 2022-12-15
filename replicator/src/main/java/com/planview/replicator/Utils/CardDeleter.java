package com.planview.replicator.Utils;

import java.util.ArrayList;

import com.planview.replicator.Debug;
import com.planview.replicator.InternalConfig;
import com.planview.replicator.LkUtils;
import com.planview.replicator.leankit.Card;
import com.planview.replicator.leankit.LeanKitAccess;

public class CardDeleter {
    Debug d = new Debug();
    InternalConfig cfg = null;

    public CardDeleter(InternalConfig config) {
        cfg = config;
		d.setLevel(config.debugLevel);
    }

    public void go() {
		ArrayList<Card> cards = LkUtils.getCardIdsFromBoard(cfg, cfg.destination);
		LeanKitAccess lka = new LeanKitAccess(cfg.destination, cfg.debugLevel);
		for (int i = 0; i < cards.size(); i++) {
			d.p(Debug.INFO, "Deleting card %s\n", cards.get(i).id);
			lka.deleteCard(cards.get(i).id);
		}
    }
}
