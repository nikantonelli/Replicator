package com.planview.replicator.leankit;

import java.util.Date;

public class PlanningIncrement {
    public String id, label;
    public Date startDate, endDate;
    public IncrementSeries series;
    public ParentIncrement parent;
}
