package auction.model;

/** CSV 한 행과 대응하는 경매 상품 객체다. */
public class Product {
    public static final String STATUS_WAITING = "대기 중";
    public static final String STATUS_OPEN = "진행 중";
    public static final String STATUS_SOLD = "낙찰";
    public static final String STATUS_NO_BID = "유찰";

    private int productId;
    private String sellerName;
    private String title;
    private String description;
    private int startPrice;
    private int currentPrice;
    private String currentBidder;
    private String imagePath;
    private String auctionStartTime;
    private String auctionEndTime;
    private String lastBidTime;
    private String status;

    public Product(int productId, String sellerName, String title, String description,
                   int startPrice, int currentPrice, String currentBidder,
                   String imagePath, String auctionStartTime, String auctionEndTime,
                   String lastBidTime, String status) {
        this.productId = productId;
        this.sellerName = sellerName;
        this.title = title;
        this.description = description;
        this.startPrice = startPrice;
        this.currentPrice = currentPrice;
        this.currentBidder = currentBidder;
        this.imagePath = imagePath;
        this.auctionStartTime = auctionStartTime;
        this.auctionEndTime = auctionEndTime;
        this.lastBidTime = lastBidTime;
        this.status = changeToKoreanStatus(status);
    }

    public int getProductId() { return productId; }
    public String getSellerName() { return sellerName; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getStartPrice() { return startPrice; }
    public int getCurrentPrice() { return currentPrice; }
    public String getCurrentBidder() { return currentBidder; }
    public String getImagePath() { return imagePath; }
    public String getAuctionStartTime() { return auctionStartTime; }
    public String getAuctionEndTime() { return auctionEndTime; }
    public String getLastBidTime() { return lastBidTime; }
    public String getStatus() { return status; }
    public void setCurrentPrice(int currentPrice) { this.currentPrice = currentPrice; }
    public void setCurrentBidder(String currentBidder) { this.currentBidder = currentBidder; }
    public void setAuctionStartTime(String auctionStartTime) { this.auctionStartTime = auctionStartTime; }
    public void setAuctionEndTime(String auctionEndTime) { this.auctionEndTime = auctionEndTime; }
    public void setLastBidTime(String lastBidTime) { this.lastBidTime = lastBidTime; }
    public void setStatus(String status) { this.status = changeToKoreanStatus(status); }

    /** 쉼표와 줄바꿈을 제거해 단순 CSV 형식을 안전하게 유지한다. */
    private static String clean(String value) {
        if (value == null) return "";
        return value.replace(',', ' ').replace('\n', ' ').replace('\r', ' ');
    }

    public String toCsvString() {
        return productId + "," + clean(sellerName) + "," + clean(title) + ","
                + clean(description) + "," + startPrice + "," + currentPrice + ","
                + clean(currentBidder) + "," + clean(imagePath) + ","
                + clean(auctionStartTime) + "," + clean(auctionEndTime) + ","
                + clean(lastBidTime) + "," + clean(status);
    }

    /** CSV 한 행을 Product 객체로 변환한다. */
    public static Product fromCsvString(String line) {
        try {
            String[] values = line.split(",", -1);

            if (values.length == 12) {
                int productId = Integer.parseInt(values[0]);
                int startPrice = Integer.parseInt(values[4]);
                int currentPrice = Integer.parseInt(values[5]);

                return new Product(
                        productId,
                        values[1],
                        values[2],
                        values[3],
                        startPrice,
                        currentPrice,
                        values[6],
                        values[7],
                        values[8],
                        values[9],
                        values[10],
                        values[11]
                );
            }

            // 이전 버전에서 저장한 10열 상품도 계속 읽는다.
            if (values.length == 10) {
                int productId = Integer.parseInt(values[0]);
                int startPrice = Integer.parseInt(values[4]);
                int currentPrice = Integer.parseInt(values[5]);
                java.time.LocalDateTime oldTime = java.time.LocalDateTime.parse(
                        values[8],
                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                );
                String endTime = oldTime.plusDays(1).format(
                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                );

                return new Product(
                        productId,
                        values[1],
                        values[2],
                        values[3],
                        startPrice,
                        currentPrice,
                        values[6],
                        values[7],
                        values[8],
                        endTime,
                        values[8],
                        values[9]
                );
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isOpen() { return STATUS_OPEN.equals(status); }

    /** 이전 CSV의 영어 상태를 새로운 한국어 상태로 바꾼다. */
    private static String changeToKoreanStatus(String status) {
        if ("WAITING".equals(status)) {
            return STATUS_WAITING;
        }

        if ("OPEN".equals(status)) {
            return STATUS_OPEN;
        }

        if ("SOLD".equals(status)) {
            return STATUS_SOLD;
        }

        if ("NO_BID".equals(status)) {
            return STATUS_NO_BID;
        }

        return status;
    }
}
