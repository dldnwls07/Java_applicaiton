package auction.model;

/** 한 번의 입찰 기록을 표현한다. */
public class Bid {
    private int bidId;
    private int productId;
    private String bidderName;
    private int bidPrice;
    private String bidTime;

    public Bid(int bidId, int productId, String bidderName, int bidPrice, String bidTime) {
        this.bidId = bidId;
        this.productId = productId;
        this.bidderName = bidderName;
        this.bidPrice = bidPrice;
        this.bidTime = bidTime;
    }

    public int getBidId() { return bidId; }
    public int getProductId() { return productId; }
    public String getBidderName() { return bidderName; }
    public int getBidPrice() { return bidPrice; }
    public String getBidTime() { return bidTime; }
    public void setBidderName(String bidderName) { this.bidderName = bidderName; }
    public void setBidPrice(int bidPrice) { this.bidPrice = bidPrice; }

    private static String clean(String value) {
        if (value == null) return "";
        return value.replace(',', ' ').replace('\n', ' ').replace('\r', ' ');
    }

    public String toCsvString() {
        return bidId + "," + productId + "," + clean(bidderName) + "," + bidPrice + "," + clean(bidTime);
    }

    public static Bid fromCsvString(String line) {
        try {
            String[] values = line.split(",", -1);

            if (values.length != 5) {
                return null;
            }

            int bidId = Integer.parseInt(values[0]);
            int productId = Integer.parseInt(values[1]);
            int bidPrice = Integer.parseInt(values[3]);

            return new Bid(
                    bidId,
                    productId,
                    values[2],
                    bidPrice,
                    values[4]
            );
        } catch (Exception e) {
            return null;
        }
    }
}
