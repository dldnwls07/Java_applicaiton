package auction.model;

/**
 * 한 번의 경매 입찰 내역(입찰 ID, 대상 상품 ID, 입찰자 이름, 입찰 금액, 입찰 시간)을 표현하는 모델 클래스다.
 * CSV 파일(bids.csv)과의 데이터 직렬화 및 역직렬화를 담당한다.
 */
public class Bid {
    // [필드] bidId: 각 입찰 기록마다 부여되는 고유 입찰 식별 번호 (예: 1, 2, 3...)
    private int bidId;

    // [필드] productId: 입찰을 진행한 경매 대상 상품의 고유 ID 번호 (Product의 productId와 매핑됨)
    private int productId;

    // [필드] bidderName: 입찰을 수행한 사용자의 이름 문자열 (예: "홍길동")
    private String bidderName;

    // [필드] bidPrice: 입찰자가 제시한 입찰 가격 (원 단위 정수)
    private int bidPrice;

    // [필드] bidTime: 입찰이 발생한 시각 문자열 (포맷: "yyyy-MM-dd HH:mm:ss")
    private String bidTime;

    /**
     * Bid 객체의 모든 필드를 초기화하는 생성자다.
     * 
     * @param bidId      고유 입찰 ID (this.bidId 필드에 저장됨)
     * @param productId  대상 상품 ID (this.productId 필드에 저장됨)
     * @param bidderName 입찰자 이름 (this.bidderName 필드에 저장됨)
     * @param bidPrice   입찰 금액 (this.bidPrice 필드에 저장됨)
     * @param bidTime    입찰 일시 문자열 (this.bidTime 필드에 저장됨)
     */
    public Bid(int bidId, int productId, String bidderName, int bidPrice, String bidTime) {
        // 매개변수로 입력받은 입찰 ID(bidId)를 클래스 멤버 변수에 할당한다.
        this.bidId = bidId;
        // 매개변수로 입력받은 상품 ID(productId)를 클래스 멤버 변수에 할당한다.
        this.productId = productId;
        // 매개변수로 입력받은 입찰자 이름(bidderName)을 클래스 멤버 변수에 할당한다.
        this.bidderName = bidderName;
        // 매개변수로 입력받은 입찰 금액(bidPrice)을 클래스 멤버 변수에 할당한다.
        this.bidPrice = bidPrice;
        // 매개변수로 입력받은 입찰 시각(bidTime)을 클래스 멤버 변수에 할당한다.
        this.bidTime = bidTime;
    }

    /** @return int 고유 입찰 ID (bidId) */
    public int getBidId() { 
        // 입찰 ID 번호를 반환한다.
        return bidId; 
    }

    /** @return int 입찰 대상 상품의 고유 ID (productId) */
    public int getProductId() { 
        // 대상 상품 ID 번호를 반환한다.
        return productId; 
    }

    /** @return String 입찰을 수행한 사용자 이름 (bidderName) */
    public String getBidderName() { 
        // 입찰자 이름을 반환한다.
        return bidderName; 
    }

    /** @return int 제시된 입찰 금액 (bidPrice) */
    public int getBidPrice() { 
        // 입찰 금액을 반환한다.
        return bidPrice; 
    }

    /** @return String 입찰 일시 문자열 (bidTime) */
    public String getBidTime() { 
        // 입찰 시각 문자열을 반환한다.
        return bidTime; 
    }

    /** @param bidderName 변경할 입찰자 이름 문자열 */
    public void setBidderName(String bidderName) { 
        // 전달받은 입찰자 이름으로 필드 값을 변경한다.
        this.bidderName = bidderName; 
    }

    /** @param bidPrice 변경할 입찰 금액 (정수) */
    public void setBidPrice(int bidPrice) { 
        // 전달받은 입찰 금액으로 필드 값을 변경한다.
        this.bidPrice = bidPrice; 
    }

    /**
     * CSV 저장 시 구분자인 쉼표(,) 및 줄바꿈 문자로 인해 데이터 파일 구조가 깨지는 것을 방지한다.
     * 
     * @param value 정제할 원본 문자열 (Null 가능)
     * @return 쉼표와 줄바꿈이 공백 문자로 바뀐 안전한 문자열
     */
    private static String clean(String value) {
        // 전달된 문자열이 null인 경우 빈 문자열("")로 처리하여 반환한다.
        if (value == null) return "";
        // 쉼표(,), 개행문자(\n), 복귀문자(\r)를 모두 띄어쓰기 한 칸(' ')으로 치환하여 반환한다.
        return value.replace(',', ' ').replace('\n', ' ').replace('\r', ' ');
    }

    /**
     * 현재 Bid 객체의 정보를 CSV 파일의 한 행 포맷("bidId,productId,bidderName,bidPrice,bidTime")에 맞게 변환한다.
     * 
     * @return CSV 파일에 저장할 쉼표 구분 문자열
     */
    public String toCsvString() {
        // 각 필드를 쉼표로 연결하고 텍스트 필드는 clean() 메서드로 안전하게 정제하여 반환한다.
        return bidId + "," + productId + "," + clean(bidderName) + "," + bidPrice + "," + clean(bidTime);
    }

    /**
     * CSV 파일에서 읽어온 한 줄의 텍스트(line)를 해석하여 Bid 객체 인스턴스로 복원한다.
     * 
     * @param line CSV 파일에서 읽은 데이터 한 줄 (예: "1,101,홍길동,50000,2026-07-22 10:00:00")
     * @return 파싱 성공 시 생성된 Bid 객체, 실패 또는 형식 불일치 시 null 반환
     */
    public static Bid fromCsvString(String line) {
        try {
            // 쉼표(,)를 기준 구분자로 삼아 텍스트를 분할한다. -1 옵션은 빈 값도 유지하도록 한다.
            String[] values = line.split(",", -1);

            // Bid CSV 행은 정확히 5개의 열(bidId, productId, bidderName, bidPrice, bidTime)로 구성되어야 한다.
            if (values.length != 5) {
                // 열 개수가 5개가 아니면 올바르지 않은 데이터이므로 null을 반환한다.
                return null;
            }

            // 0번째 열(bidId) 텍스트를 정수(int) 타입으로 변환한다.
            int bidId = Integer.parseInt(values[0]);
            // 1번째 열(productId) 텍스트를 정수(int) 타입으로 변환한다.
            int productId = Integer.parseInt(values[1]);
            // 3번째 열(bidPrice) 텍스트를 정수(int) 타입으로 변환한다.
            int bidPrice = Integer.parseInt(values[3]);

            // 파싱된 값들을 사용하여 새로운 Bid 객체를 생성하고 반환한다.
            return new Bid(
                    bidId,         // 입찰 고유 ID 번호
                    productId,     // 경매 대상 상품 ID 번호
                    values[2],     // 2번째 열: 입찰자 이름 (String)
                    bidPrice,      // 입찰 금액 (int)
                    values[4]      // 4번째 열: 입찰 일시 (String)
            );
        } catch (Exception e) {
            // 숫자 변환 오류(NumberFormatException) 등 예외 발생 시 파싱 실패로 간주하고 null을 반환한다.
            return null;
        }
    }
}
