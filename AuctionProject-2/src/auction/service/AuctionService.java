package auction.service;

import auction.file.DataManager;
import auction.model.Bid;
import auction.model.Product;
import auction.model.Seller;
import auction.model.User;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * 경매 프로그램의 핵심 비즈니스 로직(상품 등록 검증, 입찰 처리, 규칙 검사, 파일 동시성 잠금, 자동 상태 갱신 등)을 수행하는 서비스 클래스다.
 */
public class AuctionService {
    // [상수] TIME_FORMAT: 날짜 및 시각을 "yyyy-MM-dd HH:mm:ss" 포맷으로 변환하고 파싱하기 위한 포맷터 객체
    public static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // [필드] dataManager: CSV 파일 입출력 및 파일 생성을 담당하는 데이터 관리자 객체
    private final DataManager dataManager;

    // [필드] lockFile: 여러 작업이 동시에 CSV 파일을 수정하여 데이터가 꼬이는 것을 방지하기 위한 동시성 잠금 파일 ("data/auction.lock")
    private final File lockFile;

    // [필드] lastErrorMessage: 검증 실패 또는 파일 처리 오류 발생 시 사용자 UI에 보여 줄 최근 오류 메시지
    private String lastErrorMessage;

    /**
     * AuctionService 인스턴스를 생성하고 DataManager 및 잠금 파일 경로를 준비한다.
     */
    public AuctionService() {
        // 데이터 관리자 객체 생성
        dataManager = new DataManager();
        // 잠금 파일 경로 지정 ("data/auction.lock")
        lockFile = new File("data/auction.lock");
        // 에러 메시지 초기화
        lastErrorMessage = "";
    }

    /**
     * DataManager를 통해 필수 폴더(data, images) 및 초기 CSV 파일들을 생성한다.
     * 
     * @return 초기화 성공 시 true, 실패 시 false 반환
     */
    public boolean initializeFiles() {
        // dataManager의 파일 초기화 실행
        boolean success = dataManager.initializeFiles();

        // 초기화 실패 시 DataManager의 최근 오류 메시지를 가져와 저장하고 false 반환
        if (!success) {
            lastErrorMessage = dataManager.getLastErrorMessage();
            return false;
        }

        // 성공 시 오류 메시지 비우고 true 반환
        lastErrorMessage = "";
        return true;
    }

    /**
     * 현재 로그인된 사용자를 판매자로 설정하여 새로운 상품을 검증 후 등록한다.
     * 
     * @param currentUser      현재 앱을 사용 중인 사용자 객체
     * @param title            입력된 상품명
     * @param description      입력된 상품 설명
     * @param priceText        입력된 시작 가격 텍스트 (정수로 파싱됨)
     * @param auctionStartTime 입력된 경매 시작 시각 텍스트
     * @param auctionEndTime   입력된 경매 종료 시각 텍스트
     * @param imageFile        선택한 상품 이미지 파일
     * @return 등록 완료된 Product 객체 (유효성 검사 실패 시 null)
     */
    public Product registerProduct(
            User currentUser,
            String title,
            String description,
            String priceText,
            String auctionStartTime,
            String auctionEndTime,
            File imageFile) {

        // 등록을 수행하는 사용자를 판매자(Seller) 역할 객체로 인스턴스화한다.
        User seller = new Seller(
                currentUser.getUserId(),   // 사용자 식별 ID
                currentUser.getUserName()  // 사용자 이름
        );

        // 입력 텍스트 내의 쉼표, 줄바꿈 문자를 공백으로 정제한다.
        title = cleanText(title);
        description = cleanText(description);
        // 가격 텍스트를 숫자로 변환 (콤마 제거 포함)
        int startPrice = parsePrice(priceText);
        // 시각 문자열을 LocalDateTime 객체로 파싱
        LocalDateTime startTime = parseDateTime(auctionStartTime);
        LocalDateTime endTime = parseDateTime(auctionEndTime);

        // [검증 1] 상품명 필수 입력 여부 확인
        if (title.isEmpty()) {
            lastErrorMessage = "상품명을 입력해 주세요.";
            return null;
        }

        // [검증 2] 상품 설명 필수 입력 여부 확인
        if (description.isEmpty()) {
            lastErrorMessage = "상품 설명을 입력해 주세요.";
            return null;
        }

        // [검증 3] 시작 가격이 0원 이하인지 확인
        if (startPrice <= 0) {
            lastErrorMessage = "시작 가격은 0보다 큰 정수로 입력해 주세요.";
            return null;
        }

        // [검증 4] 이미지 파일 선택 여부 및 올바른 파일인지 확인
        if (imageFile == null || !imageFile.isFile()) {
            lastErrorMessage = "상품 이미지를 선택해 주세요.";
            return null;
        }

        // [검증 5] 경매 시작/종료 시간 파싱 성공 여부 확인
        if (startTime == null || endTime == null) {
            lastErrorMessage = "경매 시작 시간과 종료 시간을 확인해 주세요.";
            return null;
        }

        // [검증 6] 종료 시간이 시작 시간보다 빠른지 확인
        if (!endTime.isAfter(startTime)) {
            lastErrorMessage = "경매 종료 시간은 시작 시간보다 늦어야 합니다.";
            return null;
        }

        // [검증 7] 종료 시간이 현재 시각보다 이미 지난 과거인지 확인
        if (!endTime.isAfter(LocalDateTime.now())) {
            lastErrorMessage = "경매 종료 시간은 현재 시간보다 늦어야 합니다.";
            return null;
        }

        // [동시성 처리] 파일 작성을 위한 잠금(Lock) 파일 생성 시도 (다른 작업 중이면 대기/거부)
        if (!createLock()) {
            return null;
        }

        try {
            // 최신 products.csv 파일 내용을 읽어온다.
            if (!dataManager.loadProducts()) {
                copyDataError();
                return null;
            }

            // 신규 상품에 부여할 고유 ID를 가져온다.
            int productId = dataManager.nextProductId();
            // 이미지 파일을 images/ 폴더에 복사하고 상대 경로를 획득한다.
            String imagePath = dataManager.copyImage(imageFile, productId);

            // 이미지 복사에 실패한 경우
            if (imagePath == null) {
                copyDataError();
                return null;
            }

            // 기본 상품 상태를 "진행 중"으로 설정
            String status = Product.STATUS_OPEN;

            // 만약 시작 시간이 현재 시각보다 미래이면 상태를 "대기 중"으로 설정한다.
            if (startTime.isAfter(LocalDateTime.now())) {
                status = Product.STATUS_WAITING;
            }

            // 신규 Product 객체를 인스턴스화한다.
            Product product = new Product(
                    productId,                  // 생성된 상품 ID
                    seller.getUserName(),       // 판매자 이름
                    title,                      // 상품 제목
                    description,                // 상품 설명
                    startPrice,                 // 시작 가격
                    startPrice,                 // 현재 최고가 (초기값은 시작가와 동일)
                    "",                         // 현재 최고 입찰자 (초기에는 없음)
                    imagePath,                  // 이미지 파일 경로
                    startTime.format(TIME_FORMAT), // 경매 시작 일시 문자열
                    endTime.format(TIME_FORMAT),   // 경매 종료 일시 문자열
                    getCurrentTime(),           // 최근 입찰 시각 (등록 시각)
                    status                      // 결정된 상태 ("대기 중" 또는 "진행 중")
            );

            // 메모리 상의 products 리스트에 추가
            dataManager.addProduct(product);

            // products.csv 파일에 덮어써서 보관한다.
            if (!dataManager.saveProducts()) {
                copyDataError();
                return null;
            }

            // 성공 시 에러 메시지 초기화 후 등록된 상품 객체 반환
            lastErrorMessage = "";
            return product;
        } finally {
            // 작업이 정상 종료되거나 예외가 발생하더라도 반드시 잠금 파일을 삭제하여 잠금을 해제한다.
            deleteLock();
        }
    }

    /**
     * 사용자가 입력한 가격으로 특정 상품에 입찰을 진행한다.
     * 
     * @param productId   입찰 대상 상품의 고유 ID
     * @param currentUser 입찰을 시도하는 사용자 객체
     * @param priceText   입찰자가 제시한 가격 텍스트 (숫자 파싱)
     * @return 입찰이 반영된 갱신된 Product 객체 (입찰 실패 시 null)
     */
    public Product placeBid(
            int productId,
            User currentUser,
            String priceText) {

        // 입찰 금액 텍스트를 숫자로 변환
        int bidPrice = parsePrice(priceText);

        // [검증 1] 입찰 가격 유효성 검사 (0원 이하 금지)
        if (bidPrice <= 0) {
            lastErrorMessage = "입찰 가격은 0보다 큰 정수로 입력해 주세요.";
            return null;
        }

        // [동시성 처리] 파일 잠금 생성 시도
        if (!createLock()) {
            return null;
        }

        try {
            // 최신 products.csv 로드
            if (!dataManager.loadProducts()) {
                copyDataError();
                return null;
            }

            // 최신 bids.csv 로드
            if (!dataManager.loadBids()) {
                copyDataError();
                return null;
            }

            // 상품 ID로 대상 상품 객체 검색
            Product product = dataManager.findProduct(productId);

            // [검증 2] 경매 시간, 최고가 조건, 판매자 자가 입찰 금지 등 규칙 세부 검사
            if (!checkBid(product, currentUser, bidPrice)) {
                return null;
            }

            // 현재 시각 획득
            String now = getCurrentTime();
            // 상품의 현재 최고가 업데이트
            product.setCurrentPrice(bidPrice);
            // 상품의 현재 최고 입찰자 이름 업데이트
            product.setCurrentBidder(currentUser.getUserName());
            // 최근 입찰 시각 업데이트
            product.setLastBidTime(now);

            // 신규 입찰 기록 객체(Bid) 생성
            Bid bid = new Bid(
                    dataManager.nextBidId(),     // 신규 입찰 고유 ID
                    productId,                   // 입찰 상품 ID
                    currentUser.getUserName(),  // 입찰자 이름
                    bidPrice,                    // 제시 금액
                    now                          // 입찰 일시
            );
            // 메모리 상의 bids 리스트에 추가
            dataManager.addBid(bid);

            // 갱신된 상품 목록을 products.csv에 저장
            if (!dataManager.saveProducts()) {
                copyDataError();
                return null;
            }

            // 갱신된 입찰 목록을 bids.csv에 저장
            if (!dataManager.saveBids()) {
                copyDataError();
                return null;
            }

            lastErrorMessage = "";
            return product; // 입찰 성공 상품 반환
        } finally {
            // 잠금 해제
            deleteLock();
        }
    }

    /**
     * 입찰 가능 여부를 상세 규칙별로 검사한다.
     * 
     * @param product     입찰 대상 상품
     * @param currentUser 입찰 시도 사용자
     * @param bidPrice    제시 금액
     * @return 입찰 승인 시 true, 거절 시 false (lastErrorMessage 설정됨)
     */
    private boolean checkBid(
            Product product,
            User currentUser,
            int bidPrice) {

        // 상품이 존재하지 않는 경우
        if (product == null) {
            lastErrorMessage = "상품을 찾을 수 없습니다.";
            return false;
        }

        // 시작/종료 시각 파싱
        LocalDateTime startTime = parseDateTime(product.getAuctionStartTime());
        LocalDateTime endTime = parseDateTime(product.getAuctionEndTime());
        LocalDateTime now = LocalDateTime.now();

        if (startTime == null || endTime == null) {
            lastErrorMessage = "상품의 경매 시간 정보가 올바르지 않습니다.";
            return false;
        }

        // [규칙 1] 경매 시작 시간 이전에는 입찰 불가
        if (now.isBefore(startTime)) {
            lastErrorMessage = "아직 경매 시작 전입니다.";
            return false;
        }

        // [규칙 2] 경매 종료 시간이 지난 경우 자동으로 경매 마감 처리 후 입찰 거부
        if (!now.isBefore(endTime)) {
            finishProduct(product); // 낙찰/유찰 상태 확정
            dataManager.saveProducts(); // 파일 저장
            lastErrorMessage = "경매가 종료되었습니다.";
            return false;
        }

        // [규칙 3] 상품 상태가 "진행 중"이나 "대기 중"이 아닌 이미 종료된("낙찰", "유찰") 경매인 경우
        if (!product.isOpen()
                && !Product.STATUS_WAITING.equals(product.getStatus())) {
            lastErrorMessage = "이미 종료된 경매입니다.";
            return false;
        }

        // [규칙 4] 자기 자신의 상품에는 입찰할 수 없음 (판매자 이름과 현재 사용자 이름 비교)
        if (product.getSellerName().equals(currentUser.getUserName())) {
            lastErrorMessage = "판매자는 자신의 상품에 입찰할 수 없습니다.";
            return false;
        }

        // [규칙 5] 제시한 가격이 현재 최고가 이하인 경우 입찰 불가능
        if (bidPrice <= product.getCurrentPrice()) {
            lastErrorMessage = "현재 최고가 "
                    + String.format("%,d원", product.getCurrentPrice())
                    + "보다 높은 가격을 입력해 주세요.";
            return false;
        }

        // 대기 중이던 상품도 첫 입찰 시 즉시 "진행 중" 상태로 전환
        product.setStatus(Product.STATUS_OPEN);
        return true;
    }

    /**
     * CSV 파일에서 전체 상품 목록을 로드하여 반환한다.
     * 
     * @return Product 목록 리스트
     */
    public ArrayList<Product> loadProducts() {
        if (!dataManager.loadProducts()) {
            copyDataError();
            return new ArrayList<Product>();
        }

        lastErrorMessage = "";
        return dataManager.getProducts();
    }

    /**
     * 특정 ID의 단일 상품 정보를 파일에서 로드하여 반환한다.
     * 
     * @param productId 조회할 상품 ID
     * @return 조회된 Product 객체
     */
    public Product loadProduct(int productId) {
        if (!dataManager.loadProducts()) {
            copyDataError();
            return null;
        }

        Product product = dataManager.findProduct(productId);

        if (product == null) {
            lastErrorMessage = "상품을 찾을 수 없습니다.";
            return null;
        }

        lastErrorMessage = "";
        return product;
    }

    /**
     * 특정 상품의 모든 입찰 내역을 파일에서 읽어와 반환한다.
     * 
     * @param productId 조회할 상품 ID
     * @return 해당 상품의 입찰 목록 리스트
     */
    public ArrayList<Bid> loadBids(int productId) {
        if (!dataManager.loadBids()) {
            copyDataError();
            return new ArrayList<Bid>();
        }

        lastErrorMessage = "";
        return dataManager.findBids(productId);
    }

    /**
     * 메인 타이머(1초 주기)에 의해 호출되어, 시간이 도달한 상품들의 상태를 자동 갱신하고
     * 방금 경매가 새로 마감("낙찰" 또는 "유찰")된 상품들의 목록을 반환한다.
     * 
     * @return 새로 마감된 상품 목록 (알림 팝업 출력용)
     */
    public ArrayList<Product> updateAuctionStatuses() {
        ArrayList<Product> endedProducts = new ArrayList<Product>();

        // 상품 로드
        if (!dataManager.loadProducts()) {
            copyDataError();
            return endedProducts;
        }

        boolean changeNeeded = false; // 상태 변경이 필요한 상품이 존재하는지 체크

        // 각 상품의 현재 시각 기준 예상 상태(calculateStatus)와 저장된 상태 비교
        for (Product product : dataManager.getProducts()) {
            if (!product.getStatus().equals(calculateStatus(product))) {
                changeNeeded = true; // 변경 대상 발견
            }
        }

        // 변경할 대상이 없거나 잠금 파일 생성에 실패한 경우 진행하지 않음
        if (!changeNeeded || !createLockWithoutMessage()) {
            return endedProducts;
        }

        try {
            // 최신 상태 재로드
            if (!dataManager.loadProducts()) {
                copyDataError();
                return endedProducts;
            }

            // 모든 상품의 상태를 최신 시각 기준으로 계산하여 업데이트
            for (Product product : dataManager.getProducts()) {
                String oldStatus = product.getStatus();
                String newStatus = calculateStatus(product);
                product.setStatus(newStatus);

                // 이번 주기에서 새롭게 "낙찰" 또는 "유찰"로 변경된 상품을 결과 리스트에 수집
                if (!oldStatus.equals(newStatus)
                        && (Product.STATUS_SOLD.equals(newStatus)
                        || Product.STATUS_NO_BID.equals(newStatus))) {
                    endedProducts.add(product);
                }
            }

            // 변경된 상품 상태를 CSV 파일에 반영
            if (!dataManager.saveProducts()) {
                copyDataError();
                endedProducts.clear();
                return endedProducts;
            }

            lastErrorMessage = "";
            return endedProducts; // 새로 종료된 상품 리스트 반환
        } finally {
            deleteLock(); // 잠금 해제
        }
    }

    /**
     * 현재 시각 및 입찰 유무에 따라 상품의 올바른 경매 상태를 계산한다.
     * 
     * @param product 상태를 계산할 대상 상품
     * @return 해당 시점에 적용되어야 할 상태 문자열 ("대기 중", "진행 중", "낙찰", "유찰")
     */
    private String calculateStatus(Product product) {
        // 이미 종료된 상태("낙찰", "유찰")인 경우 기존 상태를 그대로 유지
        if (Product.STATUS_SOLD.equals(product.getStatus())
                || Product.STATUS_NO_BID.equals(product.getStatus())) {
            return product.getStatus();
        }

        LocalDateTime startTime = parseDateTime(product.getAuctionStartTime());
        LocalDateTime endTime = parseDateTime(product.getAuctionEndTime());

        if (startTime == null || endTime == null) {
            return product.getStatus();
        }

        LocalDateTime now = LocalDateTime.now();

        // 1. 현재 시간이 시작 시간보다 이전이면 -> "대기 중"
        if (now.isBefore(startTime)) {
            return Product.STATUS_WAITING;
        }

        // 2. 현재 시간이 종료 시간 이전이면 -> "진행 중"
        if (now.isBefore(endTime)) {
            return Product.STATUS_OPEN;
        }

        // 3. 종료 시간이 지났을 때: 최고 입찰자가 비어있으면 "유찰", 입찰자가 있으면 "낙찰"
        if (product.getCurrentBidder().trim().isEmpty()) {
            return Product.STATUS_NO_BID;
        }

        return Product.STATUS_SOLD;
    }

    /**
     * 상품 경매를 마감할 때 입찰자 유무에 따라 "낙찰" 또는 "유찰" 상태로 변경한다.
     * 
     * @param product 마감 처리할 상품 객체
     */
    private void finishProduct(Product product) {
        if (product.getCurrentBidder().trim().isEmpty()) {
            product.setStatus(Product.STATUS_NO_BID); // 입찰자 없음 -> 유찰
        } else {
            product.setStatus(Product.STATUS_SOLD);   // 입찰자 있음 -> 낙찰
        }
    }

    /**
     * GUI 상세 화면에 표시할 경매 남은 시간 / 시작까지 남은 시간 안내 문구를 생성한다.
     * (예: "종료까지 1일 3시간 20분 15초" 또는 "시작까지 0일 2시간 10분 5초")
     * 
     * @param product 시간 메시지를 계산할 대상 상품
     * @return 화면 표시용 남은 시간 문자열
     */
    public String getAuctionTimeMessage(Product product) {
        if (product == null) {
            return "-";
        }

        // 이미 종료된 경매는 "경매 종료" 표시
        if (Product.STATUS_SOLD.equals(product.getStatus())
                || Product.STATUS_NO_BID.equals(product.getStatus())) {
            return "경매 종료";
        }

        LocalDateTime targetTime;

        // "대기 중"이면 목표 시각은 시작 시각, "진행 중"이면 목표 시각은 종료 시각으로 설정
        if (Product.STATUS_WAITING.equals(product.getStatus())) {
            targetTime = parseDateTime(product.getAuctionStartTime());
        } else {
            targetTime = parseDateTime(product.getAuctionEndTime());
        }

        if (targetTime == null) {
            return "시간 정보 오류";
        }

        // 현재 시각부터 목표 시각까지의 남은 초(second) 수 계산
        long totalSeconds = Duration.between(
                LocalDateTime.now(),
                targetTime
        ).getSeconds();

        // 남은 시간이 음수인 경우 0초로 고정
        if (totalSeconds < 0) {
            totalSeconds = 0;
        }

        // 초 단위를 일(86400초), 시간(3600초), 분(60초), 초 단위로 변환
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        String prefix = "종료까지 ";

        // 대기 상태 시 접두어 변경
        if (Product.STATUS_WAITING.equals(product.getStatus())) {
            prefix = "시작까지 ";
        }

        // 최종 조합된 문자열 반환
        return prefix + days + "일 " + hours + "시간 "
                + minutes + "분 " + seconds + "초";
    }

    /**
     * 사용자가 입력한 가격 텍스트에서 콤마(,)를 제거하고 순수 int 정수로 변환한다.
     * 
     * @param text 입력된 가격 텍스트 (예: "10,000")
     * @return 변환된 정수 금액 (실패 시 -1)
     */
    private int parsePrice(String text) {
        try {
            return Integer.parseInt(text.trim().replace(",", ""));
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * "yyyy-MM-dd HH:mm:ss" 포맷 문자열을 LocalDateTime 객체로 파싱한다.
     * 
     * @param text 날짜 시각 문자열
     * @return LocalDateTime 객체 (실패 시 null)
     */
    private LocalDateTime parseDateTime(String text) {
        try {
            return LocalDateTime.parse(text, TIME_FORMAT);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * CSV 저장을 위해 텍스트 내의 쉼표(,) 및 줄바꿈 문자를 공백으로 변환하고 양쪽 여백을 제거한다.
     * 
     * @param text 원본 텍스트
     * @return 정제된 안전 텍스트
     */
    private String cleanText(String text) {
        if (text == null) {
            return "";
        }

        return text.trim()
                .replace(',', ' ')
                .replace('\n', ' ')
                .replace('\r', ' ');
    }

    /**
     * 현재 시각을 "yyyy-MM-dd HH:mm:ss" 문자열로 반환한다.
     * 
     * @return 현재 일시 문자열
     */
    private String getCurrentTime() {
        return LocalDateTime.now().format(TIME_FORMAT);
    }

    /**
     * 동시 접근 방지를 위한 잠금 파일("data/auction.lock")을 생성한다.
     * 
     * @return 생성 성공 시 true, 실패 시 false (오류 메시지 설정됨)
     */
    private boolean createLock() {
        try {
            // createNewFile()은 파일이 존재하지 않아 새로 생성에 성공하면 true를 반환한다.
            if (lockFile.createNewFile()) {
                return true;
            }

            // 파일이 이미 존재하면 다른 프로세스가 작업 중이므로 실패 메시지 설정
            lastErrorMessage = "다른 사용자가 처리 중입니다. 잠시 후 다시 시도해 주세요.";
            return false;
        } catch (Exception e) {
            lastErrorMessage = "잠금 파일을 만들지 못했습니다.";
            return false;
        }
    }

    /**
     * 메시지 설정 없이 단순 잠금 파일 생성을 시도한다. (1초 주기 타이머용)
     * 
     * @return 생성 성공 시 true, 이미 존재하거나 에러 시 false
     */
    private boolean createLockWithoutMessage() {
        try {
            return lockFile.createNewFile();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 작업 완료 후 존재하던 잠금 파일을 삭제한다.
     */
    private void deleteLock() {
        if (lockFile.exists()) {
            lockFile.delete();
        }
    }

    /**
     * DataManager에서 발생한 최근 에러 메시지를 서비스 에러 메시지로 복사한다.
     */
    private void copyDataError() {
        lastErrorMessage = dataManager.getLastErrorMessage();
    }

    /**
     * 서비스 처리 중 발생한 최근 오류 메시지를 반환한다.
     * 
     * @return 에러 메시지 문자열
     */
    public String getLastErrorMessage() {
        return lastErrorMessage;
    }
}
