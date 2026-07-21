package auction.gui;

import auction.model.Product;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/** 상품 목록과 상태 필터를 보여 주는 화면이다. */
public class ProductListPanel extends JPanel {
    private final MainFrame mainFrame;
    private final DefaultTableModel model;
    private final JTable table;
    private final JComboBox<String> statusFilter;
    private ArrayList<Product> products = new ArrayList<Product>();

    public ProductListPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("경매 상태:"));
        statusFilter = new JComboBox<String>(new String[]{"전체", "OPEN", "SOLD", "NO_BID"});
        statusFilter.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { rebuildRows(); }
        });
        JButton refreshButton = new JButton("새로고침");
        refreshButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { mainFrame.refreshAll(true); }
        });
        top.add(statusFilter);
        top.add(refreshButton);
        add(top, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[]{"상품번호", "상품명", "시작가", "현재가", "최고 입찰자", "상태"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(28);
        table.getColumnModel().getColumn(0).setPreferredWidth(65);
        table.getColumnModel().getColumn(1).setPreferredWidth(210);
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) openSelectedProduct();
            }
        });
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton detailButton = new JButton("상세 보기");
        detailButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { openSelectedProduct(); }
        });
        bottom.add(detailButton);
        add(bottom, BorderLayout.SOUTH);
    }

    public void setProducts(ArrayList<Product> products) {
        int selectedId = getSelectedProductId();
        this.products = products;
        rebuildRows();
        if (selectedId > 0) selectProduct(selectedId);
    }

    private void rebuildRows() {
        model.setRowCount(0);
        String filter = String.valueOf(statusFilter.getSelectedItem());
        for (Product product : products) {
            if (!"전체".equals(filter) && !filter.equals(product.getStatus())) continue;
            model.addRow(new Object[]{product.getProductId(), product.getTitle(),
                    String.format("%,d원", product.getStartPrice()),
                    String.format("%,d원", product.getCurrentPrice()),
                    product.getCurrentBidder().isEmpty() ? "-" : product.getCurrentBidder(), product.getStatus()});
        }
    }

    public int getSelectedProductId() {
        int row = table.getSelectedRow();
        if (row < 0) return -1;
        return Integer.parseInt(String.valueOf(model.getValueAt(row, 0)));
    }

    public void selectProduct(int productId) {
        for (int row = 0; row < model.getRowCount(); row++) {
            if (Integer.parseInt(String.valueOf(model.getValueAt(row, 0))) == productId) {
                table.setRowSelectionInterval(row, row);
                return;
            }
        }
    }

    private void openSelectedProduct() {
        int id = getSelectedProductId();
        if (id < 0) {
            JOptionPane.showMessageDialog(this, "상품을 먼저 선택해 주세요.", "안내", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        mainFrame.showProductDetail(id);
    }
}
