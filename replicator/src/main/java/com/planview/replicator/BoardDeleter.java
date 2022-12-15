package com.planview.replicator;

public class BoardDeleter {
	Debug d = new Debug();
    InternalConfig cfg = null;

    public BoardDeleter(InternalConfig config) {
        cfg = config;
		d.setLevel(config.debugLevel);
    }

	
    public void go() {
		LkUtils.deleteBoard(cfg, cfg.destination);
	}
}

