package auction.gui;

import auction.model.Bid;
import auction.service.AuctionException;
import auction.service.AuctionService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

/** 선택 상품의 입찰 기록을 별도 창에 표시한다. */
public class BidHistoryFrame extends JFrame {
    public BidHistoryFrame(AuctionService service, int productId) {
        setTitle("입찰 기록 - 상품 " + productId);
        setSize(650, 360); setLocationRelativeTo(null); setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        DefaultTableModel model = new DefaultTableModel(new Object[]{"입찰번호", "입찰자", "입찰가격", "입찰시간"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        try {
            ArrayList<Bid> bids = service.loadBids(productId);
            for (Bid bid : bids) model.addRow(new Object[]{bid.getBidId(), bid.getBidderName(),
                    String.format("%,d원", bid.getBidPrice()), bid.getBidTime()});
        } catch (AuctionException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }
        JTable table = new JTable(model); table.setRowHeight(26);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }
}
