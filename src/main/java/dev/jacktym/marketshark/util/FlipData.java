package dev.jacktym.marketshark.util;

import com.google.gson.annotations.SerializedName;

public class FlipData {
    @SerializedName("id")
    public String auctionId;
    @SerializedName("target")
    public long coflWorth;
    @SerializedName("auction")
    public AuctionData auctionData;
    @SerializedName("finder")
    public String finder;

    public FlipData() {
    }

    public FlipData(String id, long target, AuctionData auctionData, String finder) {
        this.auctionId = id;
        this.coflWorth = target;
        this.auctionData = auctionData;
        this.finder = finder;
    }
}
