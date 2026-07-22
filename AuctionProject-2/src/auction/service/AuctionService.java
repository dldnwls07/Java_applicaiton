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

/** 경매 규칙을 검사하고 상품 등록, 입찰, 자동 종료를 처리한다. */
public class AuctionService {
    public static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final DataManager dataManager;
    private final File lockFile;
    private String lastErrorMessage;

    public AuctionService() {
        dataManager = new DataManager();
        lockFile = new File("data/auction.lock");
        lastErrorMessage = "";
    }

    public boolean initializeFiles() {
        boolean success = dataManager.initializeFiles();

        if (!success) {
            lastErrorMessage = dataManager.getLastErrorMessage();
            return false;
        }

        lastErrorMessage = "";
        return true;
    }

    /** 현재 사용자를 판매자 역할로 사용해 상품을 등록한다. */
    public Product registerProduct(
            User currentUser,
            String title,
            String description,
            String priceText,
            String auctionStartTime,
            String auctionEndTime,
            File imageFile) {

        User seller = new Seller(
                currentUser.getUserId(),
                currentUser.getUserName()
        );

        title = cleanText(title);
        description = cleanText(description);
        int startPrice = parsePrice(priceText);
        LocalDateTime startTime = parseDateTime(auctionStartTime);
        LocalDateTime endTime = parseDateTime(auctionEndTime);

        if (title.isEmpty()) {
            lastErrorMessage = "상품명을 입력해 주세요.";
            return null;
        }

        if (description.isEmpty()) {
            lastErrorMessage = "상품 설명을 입력해 주세요.";
            return null;
        }

        if (startPrice <= 0) {
            lastErrorMessage = "시작 가격은 0보다 큰 정수로 입력해 주세요.";
            return null;
        }

        if (imageFile == null || !imageFile.isFile()) {
            lastErrorMessage = "상품 이미지를 선택해 주세요.";
            return null;
        }

        if (startTime == null || endTime == null) {
            lastErrorMessage = "경매 시작 시간과 종료 시간을 확인해 주세요.";
            return null;
        }

        if (!endTime.isAfter(startTime)) {
            lastErrorMessage = "경매 종료 시간은 시작 시간보다 늦어야 합니다.";
            return null;
        }

        if (!endTime.isAfter(LocalDateTime.now())) {
            lastErrorMessage = "경매 종료 시간은 현재 시간보다 늦어야 합니다.";
            return null;
        }

        if (!createLock()) {
            return null;
        }

        try {
            if (!dataManager.loadProducts()) {
                copyDataError();
                return null;
            }

            int productId = dataManager.nextProductId();
            String imagePath = dataManager.copyImage(imageFile, productId);

            if (imagePath == null) {
                copyDataError();
                return null;
            }

            String status = Product.STATUS_OPEN;

            if (startTime.isAfter(LocalDateTime.now())) {
                status = Product.STATUS_WAITING;
            }

            Product product = new Product(
                    productId,
                    seller.getUserName(),
                    title,
                    description,
                    startPrice,
                    startPrice,
                    "",
                    imagePath,
                    startTime.format(TIME_FORMAT),
                    endTime.format(TIME_FORMAT),
                    getCurrentTime(),
                    status
            );

            dataManager.addProduct(product);

            if (!dataManager.saveProducts()) {
                copyDataError();
                return null;
            }

            lastErrorMessage = "";
            return product;
        } finally {
            deleteLock();
        }
    }

    /** 최신 CSV를 읽고 현재 사용자의 입찰을 처리한다. */
    public Product placeBid(
            int productId,
            User currentUser,
            String priceText) {

        int bidPrice = parsePrice(priceText);

        if (bidPrice <= 0) {
            lastErrorMessage = "입찰 가격은 0보다 큰 정수로 입력해 주세요.";
            return null;
        }

        if (!createLock()) {
            return null;
        }

        try {
            if (!dataManager.loadProducts()) {
                copyDataError();
                return null;
            }

            if (!dataManager.loadBids()) {
                copyDataError();
                return null;
            }

            Product product = dataManager.findProduct(productId);

            if (!checkBid(product, currentUser, bidPrice)) {
                return null;
            }

            String now = getCurrentTime();
            product.setCurrentPrice(bidPrice);
            product.setCurrentBidder(currentUser.getUserName());
            product.setLastBidTime(now);

            Bid bid = new Bid(
                    dataManager.nextBidId(),
                    productId,
                    currentUser.getUserName(),
                    bidPrice,
                    now
            );
            dataManager.addBid(bid);

            if (!dataManager.saveProducts()) {
                copyDataError();
                return null;
            }

            if (!dataManager.saveBids()) {
                copyDataError();
                return null;
            }

            lastErrorMessage = "";
            return product;
        } finally {
            deleteLock();
        }
    }

    /** 입찰할 수 있는 상품과 가격인지 순서대로 검사한다. */
    private boolean checkBid(
            Product product,
            User currentUser,
            int bidPrice) {

        if (product == null) {
            lastErrorMessage = "상품을 찾을 수 없습니다.";
            return false;
        }

        LocalDateTime startTime = parseDateTime(product.getAuctionStartTime());
        LocalDateTime endTime = parseDateTime(product.getAuctionEndTime());
        LocalDateTime now = LocalDateTime.now();

        if (startTime == null || endTime == null) {
            lastErrorMessage = "상품의 경매 시간 정보가 올바르지 않습니다.";
            return false;
        }

        if (now.isBefore(startTime)) {
            lastErrorMessage = "아직 경매 시작 전입니다.";
            return false;
        }

        if (!now.isBefore(endTime)) {
            finishProduct(product);
            dataManager.saveProducts();
            lastErrorMessage = "경매가 종료되었습니다.";
            return false;
        }

        if (!product.isOpen()
                && !Product.STATUS_WAITING.equals(product.getStatus())) {
            lastErrorMessage = "이미 종료된 경매입니다.";
            return false;
        }

        if (product.getSellerName().equals(currentUser.getUserName())) {
            lastErrorMessage = "판매자는 자신의 상품에 입찰할 수 없습니다.";
            return false;
        }

        if (bidPrice <= product.getCurrentPrice()) {
            lastErrorMessage = "현재 최고가 "
                    + String.format("%,d원", product.getCurrentPrice())
                    + "보다 높은 가격을 입력해 주세요.";
            return false;
        }

        product.setStatus(Product.STATUS_OPEN);
        return true;
    }

    public ArrayList<Product> loadProducts() {
        if (!dataManager.loadProducts()) {
            copyDataError();
            return new ArrayList<Product>();
        }

        lastErrorMessage = "";
        return dataManager.getProducts();
    }

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

    public ArrayList<Bid> loadBids(int productId) {
        if (!dataManager.loadBids()) {
            copyDataError();
            return new ArrayList<Bid>();
        }

        lastErrorMessage = "";
        return dataManager.findBids(productId);
    }

    /** 시작 및 종료 시간이 된 상품의 상태를 자동 변경한다. */
    public ArrayList<Product> updateAuctionStatuses() {
        ArrayList<Product> endedProducts = new ArrayList<Product>();

        if (!dataManager.loadProducts()) {
            copyDataError();
            return endedProducts;
        }

        boolean changeNeeded = false;

        for (Product product : dataManager.getProducts()) {
            if (!product.getStatus().equals(calculateStatus(product))) {
                changeNeeded = true;
            }
        }

        if (!changeNeeded || !createLockWithoutMessage()) {
            return endedProducts;
        }

        try {
            if (!dataManager.loadProducts()) {
                copyDataError();
                return endedProducts;
            }

            for (Product product : dataManager.getProducts()) {
                String oldStatus = product.getStatus();
                String newStatus = calculateStatus(product);
                product.setStatus(newStatus);

                if (!oldStatus.equals(newStatus)
                        && (Product.STATUS_SOLD.equals(newStatus)
                        || Product.STATUS_NO_BID.equals(newStatus))) {
                    endedProducts.add(product);
                }
            }

            if (!dataManager.saveProducts()) {
                copyDataError();
                endedProducts.clear();
                return endedProducts;
            }

            lastErrorMessage = "";
            return endedProducts;
        } finally {
            deleteLock();
        }
    }

    private String calculateStatus(Product product) {
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

        if (now.isBefore(startTime)) {
            return Product.STATUS_WAITING;
        }

        if (now.isBefore(endTime)) {
            return Product.STATUS_OPEN;
        }

        if (product.getCurrentBidder().trim().isEmpty()) {
            return Product.STATUS_NO_BID;
        }

        return Product.STATUS_SOLD;
    }

    private void finishProduct(Product product) {
        if (product.getCurrentBidder().trim().isEmpty()) {
            product.setStatus(Product.STATUS_NO_BID);
        } else {
            product.setStatus(Product.STATUS_SOLD);
        }
    }

    /** 화면에 표시할 시작 또는 종료까지의 시간을 반환한다. */
    public String getAuctionTimeMessage(Product product) {
        if (product == null) {
            return "-";
        }

        if (Product.STATUS_SOLD.equals(product.getStatus())
                || Product.STATUS_NO_BID.equals(product.getStatus())) {
            return "경매 종료";
        }

        LocalDateTime targetTime;

        if (Product.STATUS_WAITING.equals(product.getStatus())) {
            targetTime = parseDateTime(product.getAuctionStartTime());
        } else {
            targetTime = parseDateTime(product.getAuctionEndTime());
        }

        if (targetTime == null) {
            return "시간 정보 오류";
        }

        long totalSeconds = Duration.between(
                LocalDateTime.now(),
                targetTime
        ).getSeconds();

        if (totalSeconds < 0) {
            totalSeconds = 0;
        }

        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        String prefix = "종료까지 ";

        if (Product.STATUS_WAITING.equals(product.getStatus())) {
            prefix = "시작까지 ";
        }

        return prefix + days + "일 " + hours + "시간 "
                + minutes + "분 " + seconds + "초";
    }

    private int parsePrice(String text) {
        try {
            return Integer.parseInt(text.trim().replace(",", ""));
        } catch (Exception e) {
            return -1;
        }
    }

    private LocalDateTime parseDateTime(String text) {
        try {
            return LocalDateTime.parse(text, TIME_FORMAT);
        } catch (Exception e) {
            return null;
        }
    }

    private String cleanText(String text) {
        if (text == null) {
            return "";
        }

        return text.trim()
                .replace(',', ' ')
                .replace('\n', ' ')
                .replace('\r', ' ');
    }

    private String getCurrentTime() {
        return LocalDateTime.now().format(TIME_FORMAT);
    }

    private boolean createLock() {
        try {
            if (lockFile.createNewFile()) {
                return true;
            }

            lastErrorMessage = "다른 사용자가 처리 중입니다. 잠시 후 다시 시도해 주세요.";
            return false;
        } catch (Exception e) {
            lastErrorMessage = "잠금 파일을 만들지 못했습니다.";
            return false;
        }
    }

    private boolean createLockWithoutMessage() {
        try {
            return lockFile.createNewFile();
        } catch (Exception e) {
            return false;
        }
    }

    private void deleteLock() {
        if (lockFile.exists()) {
            lockFile.delete();
        }
    }

    private void copyDataError() {
        lastErrorMessage = dataManager.getLastErrorMessage();
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }
}
