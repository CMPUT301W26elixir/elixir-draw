package com.example.allot.model.lottery;

/**
 * Holds one entrant row shown on the lottery screen.
 */
public class LotteryEntrantItem {
    private final String entrantId;
    private final String displayName;
    private final int subtitleRes;

    /**
     * Creates a simple display model for a lottery entrant.
     *
     * @param entrantId the entrant identifier
     * @param displayName the name shown in the list
     * @param subtitleRes the string resource shown under the name
     */
    public LotteryEntrantItem(String entrantId, String displayName, int subtitleRes) {
        this.entrantId = entrantId;
        this.displayName = displayName;
        this.subtitleRes = subtitleRes;
    }

    /**
     * @return the entrant identifier
     */
    public String getEntrantId() { return entrantId; }
    /**
     * @return the display name shown in the list
     */
    public String getDisplayName() { return displayName; }
    /**
     * @return the subtitle string resource
     */
    public int getSubtitleRes() { return subtitleRes; }
}









