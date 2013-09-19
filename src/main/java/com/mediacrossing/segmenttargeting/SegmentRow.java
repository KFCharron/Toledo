package com.mediacrossing.segmenttargeting;

public class SegmentRow {
    private String segmentId;
    private String name;
    private Campaign[] campaigns;

    public SegmentRow(String segmentId, String name, Campaign[] campaigns) {
        this.segmentId = segmentId;
        this.name = name;
        this.campaigns = campaigns;
    }
}
