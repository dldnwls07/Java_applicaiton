package auction.file;

import auction.model.Bid;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

/** bids.csv의 모든 입찰 기록을 관리한다. */
public class BidFileManager implements FileManager {
    public static final String HEADER = "bidId,productId,bidderName,bidPrice,bidTime";
    private final ArrayList<Bid> bidList = new ArrayList<Bid>();

    @Override
    public void load() throws Exception {
        bidList.clear();
        if (!Files.exists(AppPaths.BIDS)) return;
        BufferedReader reader = Files.newBufferedReader(AppPaths.BIDS, StandardCharsets.UTF_8);
        try {
            String line; boolean first = true;
            while ((line = reader.readLine()) != null) {
                if (first) { first = false; continue; }
                if (line.trim().isEmpty()) continue;
                try { bidList.add(Bid.fromCsvString(line)); }
                catch (RuntimeException e) { System.err.println("잘못된 입찰 행을 건너뜁니다: " + line); }
            }
        } finally { reader.close(); }
    }

    @Override
    public void save() throws Exception {
        Files.createDirectories(AppPaths.DATA);
        java.nio.file.Path temp = AppPaths.DATA.resolve("bids.tmp");
        BufferedWriter writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8);
        try {
            writer.write(HEADER); writer.newLine();
            for (Bid bid : bidList) { writer.write(bid.toCsvString()); writer.newLine(); }
        } finally { writer.close(); }
        try {
            Files.move(temp, AppPaths.BIDS, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (java.nio.file.AtomicMoveNotSupportedException e) {
            Files.move(temp, AppPaths.BIDS, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public void addBid(Bid bid) { bidList.add(bid); }
    public int nextBidId() {
        int max = 0;
        for (Bid bid : bidList) if (bid.getBidId() > max) max = bid.getBidId();
        return max + 1;
    }
    public ArrayList<Bid> findByProductId(int productId) {
        ArrayList<Bid> result = new ArrayList<Bid>();
        for (Bid bid : bidList) if (bid.getProductId() == productId) result.add(bid);
        return result;
    }
}
