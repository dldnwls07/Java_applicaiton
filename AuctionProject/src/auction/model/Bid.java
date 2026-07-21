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
        String[] values = line.split(",", -1);
        if (values.length != 5) throw new IllegalArgumentException("입찰 CSV 열 개수가 올바르지 않습니다.");
        return new Bid(Integer.parseInt(values[0]), Integer.parseInt(values[1]), values[2],
                Integer.parseInt(values[3]), values[4]);
    }
}
