package auction.file;

import auction.model.Bid;
import auction.model.Product;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

/** 상품, 입찰, 이미지 파일을 한곳에서 관리한다. */
public class DataManager {
    public static final String PRODUCT_HEADER =
            "productId,sellerName,title,description,startPrice,currentPrice,currentBidder,imagePath,auctionStartTime,auctionEndTime,lastBidTime,status";
    public static final String BID_HEADER =
            "bidId,productId,bidderName,bidPrice,bidTime";

    private final File productFile;
    private final File bidFile;
    private final File imageFolder;
    private final ArrayList<Product> products;
    private final ArrayList<Bid> bids;
    private String lastErrorMessage;

    public DataManager() {
        productFile = new File("data/products.csv");
        bidFile = new File("data/bids.csv");
        imageFolder = new File("images");
        products = new ArrayList<Product>();
        bids = new ArrayList<Bid>();
        lastErrorMessage = "";
    }

    /** 필요한 폴더와 빈 CSV 파일을 만든다. */
    public boolean initializeFiles() {
        File dataFolder = new File("data");

        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        if (!imageFolder.exists()) {
            imageFolder.mkdirs();
        }

        if (!createCsvFile(productFile, PRODUCT_HEADER)) {
            return false;
        }

        if (!createCsvFile(bidFile, BID_HEADER)) {
            return false;
        }

        lastErrorMessage = "";
        return true;
    }

    private boolean createCsvFile(File file, String header) {
        try {
            if (!file.exists()) {
                BufferedWriter writer = new BufferedWriter(
                        new FileWriter(file, StandardCharsets.UTF_8)
                );
                writer.write(header);
                writer.newLine();
                writer.close();
            }

            return true;
        } catch (Exception e) {
            lastErrorMessage = "CSV 파일을 만들지 못했습니다.";
            return false;
        }
    }

    /** products.csv를 읽어 상품 목록에 저장한다. */
    public boolean loadProducts() {
        products.clear();

        try {
            BufferedReader reader = new BufferedReader(
                    new FileReader(productFile, StandardCharsets.UTF_8)
            );
            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                if (line.trim().isEmpty()) {
                    continue;
                }

                Product product = Product.fromCsvString(line);

                if (product != null) {
                    products.add(product);
                }
            }

            reader.close();
            lastErrorMessage = "";
            return true;
        } catch (Exception e) {
            lastErrorMessage = "상품 파일을 읽지 못했습니다.";
            return false;
        }
    }

    /** 현재 상품 목록 전체를 products.csv에 저장한다. */
    public boolean saveProducts() {
        try {
            BufferedWriter writer = new BufferedWriter(
                    new FileWriter(productFile, StandardCharsets.UTF_8)
            );
            writer.write(PRODUCT_HEADER);
            writer.newLine();

            for (Product product : products) {
                writer.write(product.toCsvString());
                writer.newLine();
            }

            writer.close();
            lastErrorMessage = "";
            return true;
        } catch (Exception e) {
            lastErrorMessage = "상품 파일을 저장하지 못했습니다.";
            return false;
        }
    }

    /** bids.csv를 읽어 입찰 목록에 저장한다. */
    public boolean loadBids() {
        bids.clear();

        try {
            BufferedReader reader = new BufferedReader(
                    new FileReader(bidFile, StandardCharsets.UTF_8)
            );
            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                if (line.trim().isEmpty()) {
                    continue;
                }

                Bid bid = Bid.fromCsvString(line);

                if (bid != null) {
                    bids.add(bid);
                }
            }

            reader.close();
            lastErrorMessage = "";
            return true;
        } catch (Exception e) {
            lastErrorMessage = "입찰 파일을 읽지 못했습니다.";
            return false;
        }
    }

    /** 현재 입찰 목록 전체를 bids.csv에 저장한다. */
    public boolean saveBids() {
        try {
            BufferedWriter writer = new BufferedWriter(
                    new FileWriter(bidFile, StandardCharsets.UTF_8)
            );
            writer.write(BID_HEADER);
            writer.newLine();

            for (Bid bid : bids) {
                writer.write(bid.toCsvString());
                writer.newLine();
            }

            writer.close();
            lastErrorMessage = "";
            return true;
        } catch (Exception e) {
            lastErrorMessage = "입찰 파일을 저장하지 못했습니다.";
            return false;
        }
    }

    /** 선택한 이미지를 images 폴더에 복사하고 상대 경로를 반환한다. */
    public String copyImage(File source, int productId) {
        try {
            String originalName = source.getName();
            int dotPosition = originalName.lastIndexOf('.');
            String extension = ".img";

            if (dotPosition >= 0) {
                extension = originalName.substring(dotPosition).toLowerCase();
            }

            String fileName = "product_" + productId + extension;
            File destination = new File(imageFolder, fileName);

            Files.copy(
                    source.toPath(),
                    destination.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
            );

            lastErrorMessage = "";
            return "images/" + fileName;
        } catch (Exception e) {
            lastErrorMessage = "이미지를 복사하지 못했습니다.";
            return null;
        }
    }

    public int nextProductId() {
        int maxId = 0;

        for (Product product : products) {
            if (product.getProductId() > maxId) {
                maxId = product.getProductId();
            }
        }

        return maxId + 1;
    }

    public int nextBidId() {
        int maxId = 0;

        for (Bid bid : bids) {
            if (bid.getBidId() > maxId) {
                maxId = bid.getBidId();
            }
        }

        return maxId + 1;
    }

    public Product findProduct(int productId) {
        for (Product product : products) {
            if (product.getProductId() == productId) {
                return product;
            }
        }

        return null;
    }

    public ArrayList<Bid> findBids(int productId) {
        ArrayList<Bid> result = new ArrayList<Bid>();

        for (Bid bid : bids) {
            if (bid.getProductId() == productId) {
                result.add(bid);
            }
        }

        return result;
    }

    public void addProduct(Product product) {
        products.add(product);
    }

    public void addBid(Bid bid) {
        bids.add(bid);
    }

    public ArrayList<Product> getProducts() {
        return new ArrayList<Product>(products);
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }
}
