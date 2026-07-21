package auction.gui;

import auction.model.Product;
import auction.service.AuctionException;
import auction.service.AuctionService;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;

/** 상품 입력, 이미지 미리보기, 등록 기능을 제공한다. */
public class ProductRegisterPanel extends JPanel {
    private final MainFrame mainFrame;
    private final AuctionService service;
    private final JTextField sellerField = new JTextField(20);
    private final JTextField titleField = new JTextField(20);
    private final JTextArea descriptionArea = new JTextArea(5, 20);
    private final JTextField priceField = new JTextField(20);
    private final JLabel imagePreview = new JLabel("이미지를 선택해 주세요", SwingConstants.CENTER);
    private File selectedImage;

    public ProductRegisterPanel(MainFrame mainFrame, AuctionService service, String userName) {
        this.mainFrame = mainFrame;
        this.service = service;
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        sellerField.setText(userName);
        sellerField.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5); c.anchor = GridBagConstraints.WEST; c.fill = GridBagConstraints.HORIZONTAL;
        addField(form, c, 0, "판매자", sellerField);
        addField(form, c, 1, "상품명", titleField);
        c.gridx = 0; c.gridy = 2; c.weightx = 0; c.anchor = GridBagConstraints.NORTHWEST;
        form.add(new JLabel("상품 설명"), c);
        c.gridx = 1; c.weightx = 1;
        form.add(new JScrollPane(descriptionArea), c);
        addField(form, c, 3, "시작 가격", priceField);

        JButton chooseImage = new JButton("이미지 선택");
        chooseImage.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { chooseImage(); }
        });
        c.gridx = 0; c.gridy = 4; c.weightx = 0; form.add(new JLabel("상품 이미지"), c);
        c.gridx = 1; c.weightx = 1; form.add(chooseImage, c);
        add(form, BorderLayout.CENTER);

        imagePreview.setPreferredSize(new Dimension(300, 230));
        imagePreview.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        add(imagePreview, BorderLayout.EAST);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancel = new JButton("취소");
        JButton register = new JButton("상품 등록");
        cancel.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { clearForm(); mainFrame.showProductList(); }
        });
        register.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { registerProduct(); }
        });
        buttons.add(cancel); buttons.add(register);
        add(buttons, BorderLayout.SOUTH);
    }

    private void addField(JPanel form, GridBagConstraints c, int row, String label, JComponent component) {
        c.gridx = 0; c.gridy = row; c.weightx = 0; c.anchor = GridBagConstraints.WEST;
        form.add(new JLabel(label), c);
        c.gridx = 1; c.weightx = 1; form.add(component, c);
    }

    private void chooseImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("이미지 파일 (jpg, jpeg, png, gif)", "jpg", "jpeg", "png", "gif"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();
        try {
            BufferedImage image = ImageIO.read(file);
            if (image == null) throw new Exception("지원하지 않는 이미지입니다.");
            Image scaled = image.getScaledInstance(290, 220, Image.SCALE_SMOOTH);
            imagePreview.setText(""); imagePreview.setIcon(new ImageIcon(scaled));
            selectedImage = file;
        } catch (Exception e) {
            selectedImage = null;
            JOptionPane.showMessageDialog(this, "이미지를 읽을 수 없습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void registerProduct() {
        try {
            Product product = service.registerProduct(sellerField.getText(), titleField.getText(),
                    descriptionArea.getText(), priceField.getText(), selectedImage);
            JOptionPane.showMessageDialog(this, "상품이 등록되었습니다.", "등록 완료", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            mainFrame.refreshAll(false);
            mainFrame.showProductDetail(product.getProductId());
        } catch (AuctionException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "등록 실패", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void clearForm() {
        titleField.setText(""); descriptionArea.setText(""); priceField.setText("");
        selectedImage = null; imagePreview.setIcon(null); imagePreview.setText("이미지를 선택해 주세요");
    }
}
