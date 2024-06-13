package dev.jacktym.marketshark.util;

import com.google.gson.annotations.SerializedName;

public class AuctionData {
    @SerializedName("uuid")
    public String uuid;
    @SerializedName("auctioneerId")
    public String sellerUuid;
    @SerializedName("tag")
    public String skyblockId;
    @SerializedName("start")
    public String start;

    public AuctionData(String uuid, String sellerUuid, String skyblockId, String start) {
        this.uuid = uuid;
        this.sellerUuid = sellerUuid;
        this.skyblockId = skyblockId;
        this.start = start;
    }

    public AuctionData() {
    }
}
