package auction.service;

import auction.file.AppPaths;
import auction.file.AuctionLock;
import auction.file.BidFileManager;
import auction.file.ImageFileManager;
import auction.file.ProductFileManager;
import auction.model.Bid;
import auction.model.Product;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/** 상품 등록, 입찰, 낙찰 판정을 담당하는 핵심 서비스다. */
public class AuctionService {
    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final int AUCTION_SECONDS = 20;

    /** 필요한 폴더와 빈 CSV 파일을 자동 생성한다. */
    public void initializeFiles() throws AuctionException {
        try {
            Files.createDirectories(AppPaths.DATA);
            Files.createDirectories(AppPaths.IMAGES);
            createCsvIfMissing(AppPaths.PRODUCTS, ProductFileManager.HEADER);
            createCsvIfMissing(AppPaths.BIDS, BidFileManager.HEADER);
            createCsvIfMissing(AppPaths.USERS, "userId,userName");
        } catch (Exception e) {
            throw new AuctionException("프로그램 데이터 폴더를 준비하지 못했습니다.", e);
        }
    }

    private void createCsvIfMissing(java.nio.file.Path path, String header) throws Exception {
        if (!Files.exists(path)) {
            BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
            try { writer.write(header); writer.newLine(); } finally { writer.close(); }
        }
    }

    /** 현재 사용자를 users.csv에 중복 없이 등록한다. */
    public void registerUser(String userName) throws AuctionException {
        String cleanName = cleanRequired(userName, "사용자 이름");
        AuctionLock lock = new AuctionLock();
        try {
            if (!lock.tryAcquire()) throw new AuctionException("다른 사용자가 데이터를 처리 중입니다. 잠시 후 다시 실행해 주세요.");
            ArrayList<String> names = new ArrayList<String>();
            BufferedReader reader = Files.newBufferedReader(AppPaths.USERS, StandardCharsets.UTF_8);
            try {
                String line; boolean first = true;
                while ((line = reader.readLine()) != null) {
                    if (first) { first = false; continue; }
                    String[] values = line.split(",", -1);
                    if (values.length >= 2) names.add(values[1]);
                }
            } finally { reader.close(); }
            for (String name : names) if (name.equals(cleanName)) return;
            BufferedWriter writer = Files.newBufferedWriter(AppPaths.USERS, StandardCharsets.UTF_8,
                    java.nio.file.StandardOpenOption.APPEND);
            try { writer.write((names.size() + 1) + "," + cleanName); writer.newLine(); }
            finally { writer.close(); }
        } catch (AuctionException e) { throw e; }
        catch (Exception e) { throw new AuctionException("사용자 정보를 저장하지 못했습니다.", e); }
        finally { try { lock.close(); } catch (Exception ignored) { } }
    }

    /** 입력값을 검사하고 이미지와 상품을 저장한다. */
    public Product registerProduct(String sellerName, String title, String description,
                                   String priceText, File imageFile) throws AuctionException {
        sellerName = cleanRequired(sellerName, "판매자 이름");
        title = cleanRequired(title, "상품명");
        description = cleanRequired(description, "상품 설명");
        if (imageFile == null) throw new AuctionException("상품 이미지를 선택해 주세요.");
        int startPrice = parsePositivePrice(priceText, "시작 가격");

        AuctionLock lock = new AuctionLock();
        try {
            if (!lock.tryAcquire()) throw new AuctionException("다른 사용자가 처리 중입니다. 잠시 후 다시 시도해 주세요.");
            ProductFileManager manager = new ProductFileManager();
            manager.load();
            int id = manager.nextProductId();
            String imagePath = new ImageFileManager().copyImage(imageFile, id);
            String now = LocalDateTime.now().format(TIME_FORMAT);
            Product product = new Product(id, sellerName, title, description, startPrice,
                    startPrice, "", imagePath, now, "OPEN");
            manager.addProduct(product);
            manager.save();
            return product;
        } catch (AuctionException e) { throw e; }
        catch (Exception e) { throw new AuctionException("상품을 저장하지 못했습니다: " + e.getMessage(), e); }
        finally { try { lock.close(); } catch (Exception ignored) { } }
    }

    /** 반드시 최신 CSV를 다시 읽은 뒤 입찰을 처리한다. */
    public Product placeBid(int productId, String bidderName, String priceText) throws AuctionException {
        bidderName = cleanRequired(bidderName, "입찰자 이름");
        int bidPrice = parsePositivePrice(priceText, "입찰 가격");
        AuctionLock lock = new AuctionLock();
        try {
            if (!lock.tryAcquire()) throw new AuctionException("다른 사용자가 입찰을 처리 중입니다. 잠시 후 다시 시도해 주세요.");
            ProductFileManager products = new ProductFileManager();
            BidFileManager bids = new BidFileManager();
            products.load();
            bids.load();
            Product product = products.findById(productId);
            if (product == null) throw new AuctionException("선택한 상품을 찾을 수 없습니다.");
            if (!product.isOpen()) throw new AuctionException("이미 종료된 경매입니다.");
            if (product.getSellerName().equals(bidderName)) throw new AuctionException("판매자는 자신의 상품에 입찰할 수 없습니다.");
            if (bidPrice <= product.getCurrentPrice()) {
                throw new AuctionException("현재 최고가 " + String.format("%,d원", product.getCurrentPrice()) + "보다 높은 가격을 입력해 주세요.");
            }
            String now = LocalDateTime.now().format(TIME_FORMAT);
            product.setCurrentPrice(bidPrice);
            product.setCurrentBidder(bidderName);
            product.setLastBidTime(now);
            bids.addBid(new Bid(bids.nextBidId(), productId, bidderName, bidPrice, now));
            products.save();
            bids.save();
            return product;
        } catch (AuctionException e) { throw e; }
        catch (Exception e) { throw new AuctionException("입찰 정보를 저장하지 못했습니다: " + e.getMessage(), e); }
        finally { try { lock.close(); } catch (Exception ignored) { } }
    }

    public ArrayList<Product> loadProducts() throws AuctionException {
        try {
            ProductFileManager manager = new ProductFileManager();
            manager.load();
            return manager.getProductList();
        } catch (Exception e) { throw new AuctionException("상품 목록을 읽지 못했습니다.", e); }
    }

    public Product loadProduct(int productId) throws AuctionException {
        try {
            ProductFileManager manager = new ProductFileManager();
            manager.load();
            return manager.findById(productId);
        } catch (Exception e) { throw new AuctionException("상품 정보를 읽지 못했습니다.", e); }
    }

    public ArrayList<Bid> loadBids(int productId) throws AuctionException {
        try {
            BidFileManager manager = new BidFileManager();
            manager.load();
            return manager.findByProductId(productId);
        } catch (Exception e) { throw new AuctionException("입찰 기록을 읽지 못했습니다.", e); }
    }

    /** 남은 초를 계산한다. 입찰이 없으면 20을 반환한다. */
    public int calculateRemainingSeconds(Product product) {
        if (product == null || product.getCurrentBidder().trim().isEmpty()) return AUCTION_SECONDS;
        try {
            LocalDateTime lastBid = LocalDateTime.parse(product.getLastBidTime(), TIME_FORMAT);
            long elapsed = Duration.between(lastBid, LocalDateTime.now()).getSeconds();
            long remaining = AUCTION_SECONDS - elapsed;
            return remaining > 0 ? (int) remaining : 0;
        } catch (Exception e) { return AUCTION_SECONDS; }
    }

    /** 만료된 OPEN 상품을 잠금 안에서 재확인하고 SOLD로 바꾼다. */
    public ArrayList<Product> closeExpiredAuctions() throws AuctionException {
        ArrayList<Product> sold = new ArrayList<Product>();
        ArrayList<Product> snapshot = loadProducts();
        boolean candidate = false;
        for (Product product : snapshot) {
            if (product.isOpen() && !product.getCurrentBidder().trim().isEmpty()
                    && calculateRemainingSeconds(product) == 0) candidate = true;
        }
        if (!candidate) return sold;

        AuctionLock lock = new AuctionLock();
        try {
            if (!lock.tryAcquire()) return sold;
            ProductFileManager manager = new ProductFileManager();
            manager.load();
            for (Product product : manager.getProductList()) {
                if (product.isOpen() && !product.getCurrentBidder().trim().isEmpty()
                        && calculateRemainingSeconds(product) == 0) {
                    product.setStatus("SOLD");
                    sold.add(product);
                }
            }
            if (!sold.isEmpty()) manager.save();
            return sold;
        } catch (Exception e) { throw new AuctionException("낙찰 결과를 저장하지 못했습니다.", e); }
        finally { try { lock.close(); } catch (Exception ignored) { } }
    }

    private String cleanRequired(String value, String fieldName) throws AuctionException {
        if (value == null || value.trim().isEmpty()) throw new AuctionException(fieldName + "을(를) 입력해 주세요.");
        return value.trim().replace(',', ' ').replace('\n', ' ').replace('\r', ' ');
    }

    private int parsePositivePrice(String text, String fieldName) throws AuctionException {
        try {
            int value = Integer.parseInt(text.trim().replace(",", ""));
            if (value <= 0) throw new NumberFormatException();
            return value;
        } catch (Exception e) { throw new AuctionException(fieldName + "은(는) 0보다 큰 정수로 입력해 주세요."); }
    }
}
