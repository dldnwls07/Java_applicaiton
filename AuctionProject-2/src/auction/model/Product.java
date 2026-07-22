package auction.model;

/**
 * CSV 파일(products.csv)의 한 행 데이터와 1:1 매핑되는 경매 상품 개체 모델 클래스다.
 * 경매 물품의 상태, 가격 정보, 판매자/입찰자 정보, 경매 기간 및 이미지 경로 등을 관리한다.
 */
public class Product {
    // [상수] STATUS_WAITING: 아직 경매 시작 시간이 되지 않아 시작을 기다리는 상태 ("대기 중")
    public static final String STATUS_WAITING = "대기 중";

    // [상수] STATUS_OPEN: 경매가 시작되어 입찰을 정상적으로 받고 있는 상태 ("진행 중")
    public static final String STATUS_OPEN = "진행 중";

    // [상수] STATUS_SOLD: 경매 종료 시 입찰자가 있어 최종 낙찰된 상태 ("낙찰")
    public static final String STATUS_SOLD = "낙찰";

    // [상수] STATUS_NO_BID: 경매 종료 시 아무도 입찰하지 않아 유찰된 상태 ("유찰")
    public static final String STATUS_NO_BID = "유찰";

    // [필드] productId: 상품에 부여된 고유 식별 번호 (예: 1, 2, 3...)
    private int productId;

    // [필드] sellerName: 상품을 등록한 판매자의 사용자 이름 (예: "김철수")
    private String sellerName;

    // [필드] title: 화면에 표시될 경매 상품의 제목 (예: "아이패드 프로 11인치")
    private String title;

    // [필드] description: 상품의 상태나 특징을 작성한 상세 설명 텍스트
    private String description;

    // [필드] startPrice: 경매를 시작할 당시 등록된 최소 시작 가격 (원 단위 정수)
    private int startPrice;

    // [필드] currentPrice: 현재 시점까지 제출된 최고 입찰가 (입찰이 없을 경우 startPrice와 동일)
    private int currentPrice;

    // [필드] currentBidder: 현재 최고 입찰 금액을 제시한 입찰자의 이름 (입찰 없을 시 빈 문자열 "")
    private String currentBidder;

    // [필드] imagePath: 저장된 상품 이미지 파일의 relative 경로 (예: "images/product_1.jpg")
    private String imagePath;

    // [필드] auctionStartTime: 경매가 시작되는 시각 문자열 (포맷: "yyyy-MM-dd HH:mm:ss")
    private String auctionStartTime;

    // [필드] auctionEndTime: 경매가 종료되는 시각 문자열 (포맷: "yyyy-MM-dd HH:mm:ss")
    private String auctionEndTime;

    // [필드] lastBidTime: 가장 최근 입찰이 등록된 시각 (포맷: "yyyy-MM-dd HH:mm:ss")
    private String lastBidTime;

    // [필드] status: 경매의 현재 진행 상태 문자열 ("대기 중", "진행 중", "낙찰", "유찰")
    private String status;

    /**
     * Product 객체를 생성하는 전체 필드 초기화 생성자다.
     * 
     * @param productId        상품 고유 ID
     * @param sellerName       판매자 이름
     * @param title            상품 제목
     * @param description      상품 설명
     * @param startPrice       시작 가격
     * @param currentPrice     현재 최고가
     * @param currentBidder    현재 최고 입찰자
     * @param imagePath        이미지 저장 경로
     * @param auctionStartTime 경매 시작 일시
     * @param auctionEndTime   경매 종료 일시
     * @param lastBidTime      최근 입찰 일시
     * @param status           경매 상태 문자열
     */
    public Product(int productId, String sellerName, String title, String description,
                   int startPrice, int currentPrice, String currentBidder,
                   String imagePath, String auctionStartTime, String auctionEndTime,
                   String lastBidTime, String status) {
        // 상품 식별 ID 필드를 초기화한다.
        this.productId = productId;
        // 판매자 이름 필드를 초기화한다.
        this.sellerName = sellerName;
        // 상품 제목 필드를 초기화한다.
        this.title = title;
        // 상품 상세 설명 필드를 초기화한다.
        this.description = description;
        // 경매 시작 가격 필드를 초기화한다.
        this.startPrice = startPrice;
        // 현재 최고 입찰가 필드를 초기화한다.
        this.currentPrice = currentPrice;
        // 현재 최고 입찰자 이름 필드를 초기화한다.
        this.currentBidder = currentBidder;
        // 이미지 경로 필드를 초기화한다.
        this.imagePath = imagePath;
        // 경매 시작 시각 필드를 초기화한다.
        this.auctionStartTime = auctionStartTime;
        // 경매 종료 시각 필드를 초기화한다.
        this.auctionEndTime = auctionEndTime;
        // 최근 입찰 시각 필드를 초기화한다.
        this.lastBidTime = lastBidTime;
        // 입력된 영어/기타 상태 문자열을 안전하게 한국어 표기로 전환하여 status 필드에 할당한다.
        this.status = changeToKoreanStatus(status);
    }

    /** @return int 상품 고유 식별 번호 (productId) */
    public int getProductId() { return productId; }

    /** @return String 판매자 이름 (sellerName) */
    public String getSellerName() { return sellerName; }

    /** @return String 상품 제목 (title) */
    public String getTitle() { return title; }

    /** @return String 상품 상세 설명 (description) */
    public String getDescription() { return description; }

    /** @return int 경매 시작 가격 (startPrice) */
    public int getStartPrice() { return startPrice; }

    /** @return int 현재 최고 입찰 가격 (currentPrice) */
    public int getCurrentPrice() { return currentPrice; }

    /** @return String 현재 최고가 입찰자 이름 (currentBidder) */
    public String getCurrentBidder() { return currentBidder; }

    /** @return String 상품 이미지 상대 경로 (imagePath) */
    public String getImagePath() { return imagePath; }

    /** @return String 경매 시작 일시 문자열 (auctionStartTime) */
    public String getAuctionStartTime() { return auctionStartTime; }

    /** @return String 경매 종료 일시 문자열 (auctionEndTime) */
    public String getAuctionEndTime() { return auctionEndTime; }

    /** @return String 최근 입찰 일시 문자열 (lastBidTime) */
    public String getLastBidTime() { return lastBidTime; }

    /** @return String 현재 경매 상태 문자열 (status) */
    public String getStatus() { return status; }

    /** @param currentPrice 변경할 현재 최고가 */
    public void setCurrentPrice(int currentPrice) { this.currentPrice = currentPrice; }

    /** @param currentBidder 변경할 현재 최고가 입찰자 이름 */
    public void setCurrentBidder(String currentBidder) { this.currentBidder = currentBidder; }

    /** @param auctionStartTime 변경할 경매 시작 일시 */
    public void setAuctionStartTime(String auctionStartTime) { this.auctionStartTime = auctionStartTime; }

    /** @param auctionEndTime 변경할 경매 종료 일시 */
    public void setAuctionEndTime(String auctionEndTime) { this.auctionEndTime = auctionEndTime; }

    /** @param lastBidTime 변경할 최근 입찰 일시 */
    public void setLastBidTime(String lastBidTime) { this.lastBidTime = lastBidTime; }

    /** @param status 변경할 경매 상태 문자열 (한국어로 변환되어 저장됨) */
    public void setStatus(String status) { this.status = changeToKoreanStatus(status); }

    /**
     * CSV 저장 시 데이터 구분을 깨뜨리는 쉼표(,)와 줄바꿈(\n, \r) 문자를 공백 띄어쓰기로 정제한다.
     * 
     * @param value 검사할 문자열 (null 허용)
     * @return 정제된 안심 문자열
     */
    private static String clean(String value) {
        // null인 경우 빈 문자열("")로 치환한다.
        if (value == null) return "";
        // 쉼표 및 엔터(줄바꿈) 문자를 띄어쓰기로 교체하여 반환한다.
        return value.replace(',', ' ').replace('\n', ' ').replace('\r', ' ');
    }

    /**
     * Product 객체의 모든 필드를 CSV 12개 열(productId, sellerName, title, description, startPrice, currentPrice,
     * currentBidder, imagePath, auctionStartTime, auctionEndTime, lastBidTime, status) 규칙에 따라 한 줄 문자열로 조합한다.
     * 
     * @return products.csv에 저장될 쉼표 구분 문자열
     */
    public String toCsvString() {
        // 모든 속성 필드를 쉼표로 구획하고 텍스트 항목은 clean()으로 다듬어 연결한다.
        return productId + "," + clean(sellerName) + "," + clean(title) + ","
                + clean(description) + "," + startPrice + "," + currentPrice + ","
                + clean(currentBidder) + "," + clean(imagePath) + ","
                + clean(auctionStartTime) + "," + clean(auctionEndTime) + ","
                + clean(lastBidTime) + "," + clean(status);
    }

    /**
     * CSV 파일에서 읽은 한 줄 텍스트(line)를 해석하여 Product 객체 인스턴스를 복원한다.
     * 최신 12개 열 포맷과 이전 버전의 10개 열 포맷을 모두 지원한다.
     * 
     * @param line CSV 파일 내 한 줄 데이터
     * @return 성공 시 생성된 Product 객체, 실패 시 null
     */
    public static Product fromCsvString(String line) {
        try {
            // 쉼표(,) 기준으로 분할한다.
            String[] values = line.split(",", -1);

            // [최신 포맷] 열 개수가 12개인 경우
            if (values.length == 12) {
                // 0번째 열: productId (int)
                int productId = Integer.parseInt(values[0]);
                // 4번째 열: startPrice (int)
                int startPrice = Integer.parseInt(values[4]);
                // 5번째 열: currentPrice (int)
                int currentPrice = Integer.parseInt(values[5]);

                // 12개 필드를 인자로 전달해 Product 객체 생성 후 반환
                return new Product(
                        productId,   // 0: 상품 ID
                        values[1],   // 1: 판매자 이름
                        values[2],   // 2: 상품 제목
                        values[3],   // 3: 상품 설명
                        startPrice,  // 4: 시작 가격
                        currentPrice,// 5: 현재 가격
                        values[6],   // 6: 최고 입찰자
                        values[7],   // 7: 이미지 경로
                        values[8],   // 8: 경매 시작 시각
                        values[9],   // 9: 경매 종료 시각
                        values[10],  // 10: 최근 입찰 시각
                        values[11]   // 11: 상태 문자열
                );
            }

            // [구버전 호환] 이전 버전에서 데이터 파일에 10개 열로 작성된 경우 처리
            if (values.length == 10) {
                // 0번째 열: productId (int)
                int productId = Integer.parseInt(values[0]);
                // 4번째 열: startPrice (int)
                int startPrice = Integer.parseInt(values[4]);
                // 5번째 열: currentPrice (int)
                int currentPrice = Integer.parseInt(values[5]);
                
                // 8번째 열(시작 시각)을 LocalDateTime 객체로 파싱한다.
                java.time.LocalDateTime oldTime = java.time.LocalDateTime.parse(
                        values[8],
                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                );
                // 구버전 데이터는 종료 시간이 별도로 없었으므로 시작 시각으로부터 1일(24시간) 뒤를 종료 시간으로 임시 설정한다.
                String endTime = oldTime.plusDays(1).format(
                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                );

                // 부족한 2개 열(종료 시각, 최근 입찰 시각)을 보완하여 Product 객체를 구성한다.
                return new Product(
                        productId,
                        values[1],
                        values[2],
                        values[3],
                        startPrice,
                        currentPrice,
                        values[6],
                        values[7],
                        values[8],  // 시작 시각
                        endTime,    // 기본 1일 후로 설정된 종료 시각
                        values[8],  // 최근 입찰 시각
                        values[9]   // 상태 문자열
                );
            }

            // 열 개수가 12개나 10개가 아니면 정상 규격이 아니므로 null 반환
            return null;
        } catch (Exception e) {
            // 정수 파싱이나 날짜 파싱 오류가 발생한 경우 null 반환
            return null;
        }
    }

    /**
     * 현재 경매 상태가 "진행 중"인지 여부를 확인한다.
     * 
     * @return 진행 중인 경매이면 true, 그 외에는 false 반환
     */
    public boolean isOpen() { 
        // status 필드 값이 STATUS_OPEN ("진행 중")과 일치하는지 비교한다.
        return STATUS_OPEN.equals(status); 
    }

    /**
     * 영어로 기재된 과거 경매 상태 문자열을 한국어 표준 표기로 변환한다.
     * 
     * @param status 변환할 상태 문자열 (예: "WAITING", "OPEN", "SOLD", "NO_BID")
     * @return 한국어로 변환된 경매 상태 문자열
     */
    private static String changeToKoreanStatus(String status) {
        // "WAITING" -> "대기 중"
        if ("WAITING".equals(status)) {
            return STATUS_WAITING;
        }

        // "OPEN" -> "진행 중"
        if ("OPEN".equals(status)) {
            return STATUS_OPEN;
        }

        // "SOLD" -> "낙찰"
        if ("SOLD".equals(status)) {
            return STATUS_SOLD;
        }

        // "NO_BID" -> "유찰"
        if ("NO_BID".equals(status)) {
            return STATUS_NO_BID;
        }

        // 이미 한국어이거나 기타 문자열인 경우 그대로 반환한다.
        return status;
    }
}
