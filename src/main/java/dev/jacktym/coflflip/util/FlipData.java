package dev.jacktym.coflflip.util;

import com.google.gson.annotations.SerializedName;

public class FlipData {
    @SerializedName("id")
    public String auctionId;
    @SerializedName("target")
    public long coflWorth;
    @SerializedName("auction")
    public AuctionData auctionData;

    public FlipData() {
    }

    public FlipData(String id, long target, AuctionData auctionData) {
        this.auctionId = id;
        this.coflWorth = target;
        this.auctionData = auctionData;
    }
}
