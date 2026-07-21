package auction.file;

import auction.model.Product;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

/** products.csv의 읽기와 전체 저장을 담당한다. */
public class ProductFileManager implements FileManager {
    public static final String HEADER = "productId,sellerName,title,description,startPrice,currentPrice,currentBidder,imagePath,lastBidTime,status";
    private final ArrayList<Product> productList = new ArrayList<Product>();

    @Override
    public void load() throws Exception {
        productList.clear();
        if (!Files.exists(AppPaths.PRODUCTS)) return;
        BufferedReader reader = Files.newBufferedReader(AppPaths.PRODUCTS, StandardCharsets.UTF_8);
        try {
            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                if (first) { first = false; continue; }
                if (line.trim().isEmpty()) continue;
                try { productList.add(Product.fromCsvString(line)); }
                catch (RuntimeException e) { System.err.println("잘못된 상품 행을 건너뜁니다: " + line); }
            }
        } finally { reader.close(); }
    }

    @Override
    public void save() throws Exception {
        Files.createDirectories(AppPaths.DATA);
        java.nio.file.Path temp = AppPaths.DATA.resolve("products.tmp");
        BufferedWriter writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8);
        try {
            writer.write(HEADER); writer.newLine();
            for (Product product : productList) { writer.write(product.toCsvString()); writer.newLine(); }
        } finally { writer.close(); }
        try {
            Files.move(temp, AppPaths.PRODUCTS, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (java.nio.file.AtomicMoveNotSupportedException e) {
            Files.move(temp, AppPaths.PRODUCTS, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public ArrayList<Product> getProductList() { return new ArrayList<Product>(productList); }
    public void addProduct(Product product) { productList.add(product); }
    public int nextProductId() {
        int max = 0;
        for (Product product : productList) if (product.getProductId() > max) max = product.getProductId();
        return max + 1;
    }
    public Product findById(int id) {
        for (Product product : productList) if (product.getProductId() == id) return product;
        return null;
    }
}
