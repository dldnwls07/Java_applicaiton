package auction.gui;

import auction.model.Product;
import auction.service.AuctionException;
import auction.service.AuctionService;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/** 프로그램의 모든 주요 패널과 1초 Swing Timer를 관리한다. */
public class MainFrame extends JFrame {
    private static final String LIST = "list";
    private static final String REGISTER = "register";
    private static final String DETAIL = "detail";
    private final AuctionService service;
    private final String userName;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);
    private final ProductListPanel listPanel;
    private final ProductRegisterPanel registerPanel;
    private final ProductDetailPanel detailPanel;
    private final JLabel connectionLabel = new JLabel("준비 중");

    public MainFrame(AuctionService service, String userName) {
        this.service = service; this.userName = userName;
        setTitle("한국공학대학교 교내 경매");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 650));
        setSize(1120, 720); setLocationRelativeTo(null);

        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        JLabel title = new JLabel("교내 경매 프로그램");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        header.add(title, BorderLayout.WEST);
        header.add(new JLabel("현재 사용자: " + userName), BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        listPanel = new ProductListPanel(this);
        registerPanel = new ProductRegisterPanel(this, service, userName);
        detailPanel = new ProductDetailPanel(this, service, userName);
        cards.add(listPanel, LIST); cards.add(registerPanel, REGISTER); cards.add(detailPanel, DETAIL);
        add(cards, BorderLayout.CENTER);

        JPanel navigation = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton listButton = new JButton("상품 목록");
        JButton registerButton = new JButton("상품 등록");
        JButton historyButton = new JButton("선택 상품 입찰 기록");
        JButton exitButton = new JButton("종료");
        listButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { showProductList(); }
        });
        registerButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { cardLayout.show(cards, REGISTER); }
        });
        historyButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                int id = detailPanel.getProductId() > 0 ? detailPanel.getProductId() : listPanel.getSelectedProductId();
                showBidHistory(id);
            }
        });
        exitButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { dispose(); System.exit(0); }
        });
        navigation.add(listButton); navigation.add(registerButton); navigation.add(historyButton);
        navigation.add(exitButton); navigation.add(Box.createHorizontalStrut(20)); navigation.add(connectionLabel);
        add(navigation, BorderLayout.SOUTH);

        refreshAll(false);
        Timer timer = new Timer(1000, new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { onTimer(); }
        });
        timer.start();
    }

    private void onTimer() {
        try {
            ArrayList<Product> sold = service.closeExpiredAuctions();
            refreshAll(false);
            for (Product product : sold) {
                JOptionPane.showMessageDialog(this, "경매가 종료되었습니다.\n\n낙찰자: "
                        + product.getCurrentBidder() + "\n낙찰 가격: "
                        + String.format("%,d원", product.getCurrentPrice()) + "\n상품명: "
                        + product.getTitle(), "낙찰 완료", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (AuctionException e) { connectionLabel.setText("갱신 오류: " + e.getMessage()); }
    }

    public void refreshAll(boolean showError) {
        try {
            listPanel.setProducts(service.loadProducts());
            detailPanel.refresh();
            connectionLabel.setText("CSV 자동 갱신 중 (1초)");
        } catch (AuctionException e) {
            connectionLabel.setText("갱신 오류");
            if (showError) JOptionPane.showMessageDialog(this, e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void showProductList() { refreshAll(false); cardLayout.show(cards, LIST); }
    public void showProductDetail(int productId) {
        detailPanel.setProductId(productId); listPanel.selectProduct(productId); cardLayout.show(cards, DETAIL);
    }
    public void showBidHistory(int productId) {
        if (productId < 1) {
            JOptionPane.showMessageDialog(this, "상품을 먼저 선택해 주세요.", "안내", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        new BidHistoryFrame(service, productId).setVisible(true);
    }
}
