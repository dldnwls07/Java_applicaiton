package auction.gui;

import auction.model.Bid;
import auction.model.Product;
import auction.model.User;
import auction.service.AuctionService;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

/** 상품 목록, 상세 정보, 입찰 기능을 한 화면에 보여 준다. */
public class AuctionPanel extends JPanel {
    private final AuctionService service;
    private final User currentUser;
    private final DefaultTableModel tableModel;
    private final JTable productTable;
    private final JComboBox<String> statusFilter;
    private final JLabel imageLabel;
    private final JLabel titleLabel;
    private final JTextArea descriptionArea;
    private final JLabel sellerLabel;
    private final JLabel priceLabel;
    private final JLabel bidderLabel;
    private final JLabel periodLabel;
    private final JLabel remainingLabel;
    private final JLabel statusLabel;
    private final JTextField bidPriceField;
    private final JButton bidButton;
    private ArrayList<Product> products;
    private int selectedProductId;

    public AuctionPanel(AuctionService service, User currentUser) {
        this.service = service;
        this.currentUser = currentUser;
        products = new ArrayList<Product>();
        selectedProductId = -1;

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusFilter = new JComboBox<String>(
                new String[]{
                        "전체",
                        Product.STATUS_WAITING,
                        Product.STATUS_OPEN,
                        Product.STATUS_SOLD,
                        Product.STATUS_NO_BID
                }
        );
        JButton refreshButton = new JButton("새로고침");
        filterPanel.add(new JLabel("상태:"));
        filterPanel.add(statusFilter);
        filterPanel.add(refreshButton);
        add(filterPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new Object[]{"번호", "상품명", "현재가", "상태"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        productTable = new JTable(tableModel);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productTable.setRowHeight(27);

        JPanel detailPanel = new JPanel(new BorderLayout(8, 8));
        detailPanel.setBorder(BorderFactory.createTitledBorder("상품 상세 및 입찰"));
        imageLabel = new JLabel("상품을 선택해 주세요", SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(300, 220));
        imageLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        detailPanel.add(imageLabel, BorderLayout.NORTH);

        titleLabel = new JLabel("-");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 20f));
        descriptionArea = new JTextArea(4, 25);
        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        sellerLabel = new JLabel("-");
        priceLabel = new JLabel("-");
        bidderLabel = new JLabel("-");
        periodLabel = new JLabel("-");
        remainingLabel = new JLabel("-");
        statusLabel = new JLabel("-");

        detailPanel.add(createInformationPanel(), BorderLayout.CENTER);

        bidPriceField = new JTextField(12);
        bidButton = new JButton("입찰하기");
        JButton historyButton = new JButton("입찰 기록");
        JPanel bidPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bidPanel.add(new JLabel("입찰 가격"));
        bidPanel.add(bidPriceField);
        bidPanel.add(bidButton);
        bidPanel.add(historyButton);
        detailPanel.add(bidPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(productTable),
                detailPanel
        );
        splitPane.setResizeWeight(0.45);
        splitPane.setDividerLocation(460);
        add(splitPane, BorderLayout.CENTER);

        statusFilter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rebuildTable();
            }
        });

        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refresh();
            }
        });

        productTable.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {
                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        if (!e.getValueIsAdjusting()) {
                            selectTableProduct();
                        }
                    }
                }
        );

        bidButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placeBid();
            }
        });

        historyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showBidHistory();
            }
        });

        refresh();
    }

    private JPanel createInformationPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 5, 3, 5);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.weightx = 1;
        panel.add(titleLabel, c);
        c.gridwidth = 1;

        addInformation(panel, c, 1, "설명", new JScrollPane(descriptionArea));
        addInformation(panel, c, 2, "판매자", sellerLabel);
        addInformation(panel, c, 3, "현재 가격", priceLabel);
        addInformation(panel, c, 4, "최고 입찰자", bidderLabel);
        addInformation(panel, c, 5, "경매 기간", periodLabel);
        addInformation(panel, c, 6, "남은 시간", remainingLabel);
        addInformation(panel, c, 7, "상태", statusLabel);

        return panel;
    }

    private void addInformation(
            JPanel panel,
            GridBagConstraints c,
            int row,
            String name,
            JComponent value) {

        c.gridx = 0;
        c.gridy = row;
        c.weightx = 0;
        panel.add(new JLabel(name), c);
        c.gridx = 1;
        c.weightx = 1;
        panel.add(value, c);
    }

    /** CSV를 다시 읽고 목록과 선택 상품을 갱신한다. */
    public void refresh() {
        products = service.loadProducts();
        rebuildTable();

        if (selectedProductId > 0) {
            selectProduct(selectedProductId);
            refreshDetail();
        }
    }

    private void rebuildTable() {
        int oldSelectedId = selectedProductId;
        tableModel.setRowCount(0);
        String filter = String.valueOf(statusFilter.getSelectedItem());

        for (Product product : products) {
            if (!"전체".equals(filter)
                    && !filter.equals(product.getStatus())) {
                continue;
            }

            tableModel.addRow(new Object[]{
                    product.getProductId(),
                    product.getTitle(),
                    String.format("%,d원", product.getCurrentPrice()),
                    product.getStatus()
            });
        }

        if (oldSelectedId > 0) {
            selectProduct(oldSelectedId);
        }
    }

    private void selectTableProduct() {
        int row = productTable.getSelectedRow();

        if (row < 0) {
            return;
        }

        selectedProductId = Integer.parseInt(
                String.valueOf(tableModel.getValueAt(row, 0))
        );
        refreshDetail();
    }

    /** 지정 상품을 목록에서 선택하고 상세 정보를 표시한다. */
    public void selectProduct(int productId) {
        selectedProductId = productId;

        for (int row = 0; row < tableModel.getRowCount(); row++) {
            int rowProductId = Integer.parseInt(
                    String.valueOf(tableModel.getValueAt(row, 0))
            );

            if (rowProductId == productId) {
                productTable.setRowSelectionInterval(row, row);
                refreshDetail();
                return;
            }
        }
    }

    private void refreshDetail() {
        if (selectedProductId < 1) {
            return;
        }

        Product product = service.loadProduct(selectedProductId);

        if (product == null) {
            return;
        }

        titleLabel.setText(product.getTitle());
        descriptionArea.setText(product.getDescription());
        sellerLabel.setText(product.getSellerName());
        priceLabel.setText(String.format("%,d원", product.getCurrentPrice()));

        if (product.getCurrentBidder().isEmpty()) {
            bidderLabel.setText("아직 입찰 없음");
        } else {
            bidderLabel.setText(product.getCurrentBidder());
        }

        periodLabel.setText(
                product.getAuctionStartTime()
                        + " ~ "
                        + product.getAuctionEndTime()
        );
        remainingLabel.setText(service.getAuctionTimeMessage(product));
        statusLabel.setText(product.getStatus());
        bidButton.setEnabled(product.isOpen());
        bidPriceField.setEnabled(product.isOpen());
        showImage(product.getImagePath());
    }

    private void showImage(String imagePath) {
        File imageFile = new File(imagePath);
        ImageIcon icon = new ImageIcon(imageFile.getPath());

        if (icon.getIconWidth() <= 0) {
            imageLabel.setIcon(null);
            imageLabel.setText("이미지를 읽을 수 없습니다");
            return;
        }

        Image resizedImage = icon.getImage().getScaledInstance(
                290,
                210,
                Image.SCALE_SMOOTH
        );
        imageLabel.setText("");
        imageLabel.setIcon(new ImageIcon(resizedImage));
    }

    private void placeBid() {
        if (selectedProductId < 1) {
            showMessage("상품을 먼저 선택해 주세요.");
            return;
        }

        Product product = service.placeBid(
                selectedProductId,
                currentUser,
                bidPriceField.getText()
        );

        if (product == null) {
            showMessage(service.getLastErrorMessage());
            refresh();
            return;
        }

        bidPriceField.setText("");
        refresh();
        JOptionPane.showMessageDialog(
                this,
                "입찰이 완료되었습니다.\n현재 최고가: "
                        + String.format("%,d원", product.getCurrentPrice()),
                "입찰 완료",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showBidHistory() {
        if (selectedProductId < 1) {
            showMessage("상품을 먼저 선택해 주세요.");
            return;
        }

        ArrayList<Bid> bids = service.loadBids(selectedProductId);
        DefaultTableModel bidModel = new DefaultTableModel(
                new Object[]{"입찰번호", "입찰자", "입찰가격", "입찰시간"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (Bid bid : bids) {
            bidModel.addRow(new Object[]{
                    bid.getBidId(),
                    bid.getBidderName(),
                    String.format("%,d원", bid.getBidPrice()),
                    bid.getBidTime()
            });
        }

        JTable bidTable = new JTable(bidModel);
        bidTable.setRowHeight(26);
        JScrollPane scrollPane = new JScrollPane(bidTable);
        scrollPane.setPreferredSize(new Dimension(600, 300));
        JOptionPane.showMessageDialog(
                this,
                scrollPane,
                "입찰 기록",
                JOptionPane.PLAIN_MESSAGE
        );
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "안내",
                JOptionPane.WARNING_MESSAGE
        );
    }
}
