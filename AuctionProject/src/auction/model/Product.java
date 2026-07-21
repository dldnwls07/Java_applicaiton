package auction.model;

/** CSV 한 행과 대응하는 경매 상품 객체다. */
public class Product {
    private int productId;
    private String sellerName;
    private String title;
    private String description;
    private int startPrice;
    private int currentPrice;
    private String currentBidder;
    private String imagePath;
    private String lastBidTime;
    private String status;

    public Product(int productId, String sellerName, String title, String description,
                   int startPrice, int currentPrice, String currentBidder,
                   String imagePath, String lastBidTime, String status) {
        this.productId = productId;
        this.sellerName = sellerName;
        this.title = title;
        this.description = description;
        this.startPrice = startPrice;
        this.currentPrice = currentPrice;
        this.currentBidder = currentBidder;
        this.imagePath = imagePath;
        this.lastBidTime = lastBidTime;
        this.status = status;
    }

    public int getProductId() { return productId; }
    public String getSellerName() { return sellerName; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getStartPrice() { return startPrice; }
    public int getCurrentPrice() { return currentPrice; }
    public String getCurrentBidder() { return currentBidder; }
    public String getImagePath() { return imagePath; }
    public String getLastBidTime() { return lastBidTime; }
    public String getStatus() { return status; }
    public void setCurrentPrice(int currentPrice) { this.currentPrice = currentPrice; }
    public void setCurrentBidder(String currentBidder) { this.currentBidder = currentBidder; }
    public void setLastBidTime(String lastBidTime) { this.lastBidTime = lastBidTime; }
    public void setStatus(String status) { this.status = status; }

    /** 쉼표와 줄바꿈을 제거해 단순 CSV 형식을 안전하게 유지한다. */
    private static String clean(String value) {
        if (value == null) return "";
        return value.replace(',', ' ').replace('\n', ' ').replace('\r', ' ');
    }

    public String toCsvString() {
        return productId + "," + clean(sellerName) + "," + clean(title) + ","
                + clean(description) + "," + startPrice + "," + currentPrice + ","
                + clean(currentBidder) + "," + clean(imagePath) + ","
                + clean(lastBidTime) + "," + clean(status);
    }

    /** CSV 한 행을 Product 객체로 변환한다. */
    public static Product fromCsvString(String line) {
        String[] values = line.split(",", -1);
        if (values.length != 10) throw new IllegalArgumentException("상품 CSV 열 개수가 올바르지 않습니다.");
        return new Product(Integer.parseInt(values[0]), values[1], values[2], values[3],
                Integer.parseInt(values[4]), Integer.parseInt(values[5]), values[6],
                values[7], values[8], values[9]);
    }

    public boolean isOpen() { return "OPEN".equals(status); }
}
