package auction.gui;

import auction.file.AppPaths;
import auction.model.Product;
import auction.service.AuctionException;
import auction.service.AuctionService;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/** 선택 상품의 상세 내용, 남은 시간, 입찰 입력을 표시한다. */
public class ProductDetailPanel extends JPanel {
    private final MainFrame mainFrame;
    private final AuctionService service;
    private int productId = -1;
    private Product product;
    private final JLabel imageLabel = new JLabel("이미지 없음", SwingConstants.CENTER);
    private final JLabel titleLabel = new JLabel("-");
    private final JTextArea descriptionArea = new JTextArea();
    private final JLabel sellerLabel = new JLabel("-");
    private final JLabel startPriceLabel = new JLabel("-");
    private final JLabel currentPriceLabel = new JLabel("-");
    private final JLabel bidderLabel = new JLabel("-");
    private final JLabel remainingLabel = new JLabel("-");
    private final JLabel statusLabel = new JLabel("-");
    private final JTextField bidderField = new JTextField(16);
    private final JTextField bidPriceField = new JTextField(16);
    private final JButton bidButton = new JButton("입찰하기");

    public ProductDetailPanel(MainFrame mainFrame, AuctionService service, String userName) {
        this.mainFrame = mainFrame; this.service = service;
        setLayout(new BorderLayout(16, 16));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        imageLabel.setPreferredSize(new Dimension(330, 250));
        imageLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        add(imageLabel, BorderLayout.WEST);

        JPanel info = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 5, 4, 5); c.anchor = GridBagConstraints.WEST; c.fill = GridBagConstraints.HORIZONTAL;
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 22f));
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2; c.weightx = 1; info.add(titleLabel, c); c.gridwidth = 1;
        descriptionArea.setLineWrap(true); descriptionArea.setWrapStyleWord(true); descriptionArea.setEditable(false);
        descriptionArea.setBackground(UIManager.getColor("Panel.background"));
        addInfo(info, c, 1, "설명", new JScrollPane(descriptionArea));
        addInfo(info, c, 2, "판매자", sellerLabel);
        addInfo(info, c, 3, "시작 가격", startPriceLabel);
        addInfo(info, c, 4, "현재 최고가", currentPriceLabel);
        addInfo(info, c, 5, "최고 입찰자", bidderLabel);
        addInfo(info, c, 6, "남은 시간", remainingLabel);
        addInfo(info, c, 7, "경매 상태", statusLabel);
        add(info, BorderLayout.CENTER);

        JPanel bidPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bidderField.setText(userName); bidderField.setEditable(false);
        bidPanel.add(new JLabel("입찰자")); bidPanel.add(bidderField);
        bidPanel.add(new JLabel("입찰 가격")); bidPanel.add(bidPriceField); bidPanel.add(bidButton);
        JButton history = new JButton("입찰 기록");
        JButton back = new JButton("목록으로");
        bidPanel.add(history); bidPanel.add(back);
        bidButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { placeBid(); }
        });
        history.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { mainFrame.showBidHistory(productId); }
        });
        back.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { mainFrame.showProductList(); }
        });
        add(bidPanel, BorderLayout.SOUTH);
    }

    private void addInfo(JPanel panel, GridBagConstraints c, int row, String name, JComponent value) {
        c.gridy = row; c.gridx = 0; c.weightx = 0; c.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel(name), c);
        c.gridx = 1; c.weightx = 1; panel.add(value, c);
    }

    public void setProductId(int productId) { this.productId = productId; refresh(); }
    public int getProductId() { return productId; }

    public void refresh() {
        if (productId < 0) return;
        try {
            Product latest = service.loadProduct(productId);
            if (latest == null) return;
            boolean imageChanged = product == null || !latest.getImagePath().equals(product.getImagePath());
            product = latest;
            titleLabel.setText(product.getTitle()); descriptionArea.setText(product.getDescription());
            sellerLabel.setText(product.getSellerName()); startPriceLabel.setText(String.format("%,d원", product.getStartPrice()));
            currentPriceLabel.setText(String.format("%,d원", product.getCurrentPrice()));
            bidderLabel.setText(product.getCurrentBidder().isEmpty() ? "아직 입찰 없음" : product.getCurrentBidder());
            statusLabel.setText(product.getStatus());
            if (!product.isOpen()) remainingLabel.setText("경매 종료");
            else if (product.getCurrentBidder().isEmpty()) remainingLabel.setText("첫 입찰 대기 중");
            else remainingLabel.setText(service.calculateRemainingSeconds(product) + "초");
            bidButton.setEnabled(product.isOpen()); bidPriceField.setEnabled(product.isOpen());
            if (imageChanged) loadImage(product.getImagePath());
        } catch (AuctionException e) { statusLabel.setText("불러오기 실패"); }
    }

    private void loadImage(String relativePath) {
        java.nio.file.Path path = AppPaths.ROOT.resolve(relativePath).normalize();
        ImageIcon icon = new ImageIcon(path.toString());
        if (icon.getIconWidth() <= 0) { imageLabel.setIcon(null); imageLabel.setText("이미지를 읽을 수 없습니다"); return; }
        Image scaled = icon.getImage().getScaledInstance(320, 240, Image.SCALE_SMOOTH);
        imageLabel.setText(""); imageLabel.setIcon(new ImageIcon(scaled));
    }

    private void placeBid() {
        if (productId < 0) return;
        try {
            Product result = service.placeBid(productId, bidderField.getText(), bidPriceField.getText());
            bidPriceField.setText(""); refresh(); mainFrame.refreshAll(false);
            JOptionPane.showMessageDialog(this, "입찰이 완료되었습니다.\n현재 최고가: "
                    + String.format("%,d원", result.getCurrentPrice()) + "\n현재 최고 입찰자: "
                    + result.getCurrentBidder(), "입찰 완료", JOptionPane.INFORMATION_MESSAGE);
        } catch (AuctionException e) {
            refresh();
            JOptionPane.showMessageDialog(this, e.getMessage(), "입찰 실패", JOptionPane.WARNING_MESSAGE);
        }
    }
}
