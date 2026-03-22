package com.example.allot.model.lottery;
public class LotteryEntrantItem {
    private final String entrantId;
    private final String displayName;
    private final int subtitleRes;

    public LotteryEntrantItem(String entrantId, String displayName, int subtitleRes) {
        this.entrantId = entrantId;
        this.displayName = displayName;
        this.subtitleRes = subtitleRes;
    }

    public String getEntrantId() { return entrantId; }
    public String getDisplayName() { return displayName; }
    public int getSubtitleRes() { return subtitleRes; }
}









