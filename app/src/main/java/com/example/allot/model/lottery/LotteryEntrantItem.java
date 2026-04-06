package com.example.allot.model.lottery;

/**
 * Holds one entrant row shown on the lottery screen.
 */
public class LotteryEntrantItem {
    private final String entrantId;
    private final String displayName;
    private final int subtitleRes;

    /**
     * Creates a new LotteryEntrantItem instance.
     *
     * @param entrantId the entrant id
     * @param displayName the display name
     * @param subtitleRes the subtitle res
     */
    public LotteryEntrantItem(String entrantId, String displayName, int subtitleRes) {
        this.entrantId = entrantId;
        this.displayName = displayName;
        this.subtitleRes = subtitleRes;
    }

    /**
     * Returns the entrant id.
     *
     * @return the entrant id
     */
    public String getEntrantId() { return entrantId; }
    /**
     * Returns the display name.
     *
     * @return the display name
     */
    public String getDisplayName() { return displayName; }
    /**
     * Returns the subtitle res.
     *
     * @return the subtitle res
     */
    public int getSubtitleRes() { return subtitleRes; }
}









