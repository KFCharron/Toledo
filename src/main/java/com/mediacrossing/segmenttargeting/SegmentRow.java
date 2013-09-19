package com.mediacrossing.segmenttargeting;

import java.util.ArrayList;

public class SegmentRow {
    private String segmentId;
    private String name;
    private ArrayList<Campaign> campaigns;

    public SegmentRow(String segmentId, String name, ArrayList<Campaign> campaigns) {
        this.segmentId = segmentId;
        this.name = name;
        this.campaigns = campaigns;
    }
}
