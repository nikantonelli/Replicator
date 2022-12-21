package com.planview.replicator;

public class BoardDeleter {
	Debug d = new Debug();
    InternalConfig cfg = null;

    public BoardDeleter(InternalConfig config) {
        cfg = config;
		d.setLevel(config.debugLevel);
    }
	
    public void go() {
		if ( LkUtils.deleteBoard(cfg, cfg.destination)){
			d.p(Debug.INFO, "Deleted board \"%s\"\n", cfg.destination.BoardName);
		} else {
			d.p(Debug.WARN, "Delete of board \"%s\" unsuccessful\n", cfg.destination.BoardName);
		}
	}
}