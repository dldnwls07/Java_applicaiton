package auction.gui;

import auction.MainFrame;
import auction.model.Product;
import auction.model.User;
import auction.service.AuctionService;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/** 상품 정보와 경매 기간을 입력해 등록하는 화면이다. */
public class ProductRegisterPanel extends JPanel {
    private final MainFrame mainFrame;
    private final AuctionService service;
    private final User currentUser;
    private final JTextField titleField;
    private final JTextArea descriptionArea;
    private final JTextField priceField;
    private final JSpinner startTimeSpinner;
    private final JSpinner endTimeSpinner;
    private final JLabel imagePreview;
    private File selectedImage;

    public ProductRegisterPanel(
            MainFrame mainFrame,
            AuctionService service,
            User currentUser) {

        this.mainFrame = mainFrame;
        this.service = service;
        this.currentUser = currentUser;

        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        titleField = new JTextField(20);
        descriptionArea = new JTextArea(5, 20);
        priceField = new JTextField(20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        Date now = new Date();
        Date tomorrow = new Date(now.getTime() + 24L * 60L * 60L * 1000L);
        startTimeSpinner = new JSpinner(
                new SpinnerDateModel(now, null, null, Calendar.MINUTE)
        );
        endTimeSpinner = new JSpinner(
                new SpinnerDateModel(tomorrow, null, null, Calendar.MINUTE)
        );
        startTimeSpinner.setEditor(
                new JSpinner.DateEditor(startTimeSpinner, "yyyy-MM-dd HH:mm")
        );
        endTimeSpinner.setEditor(
                new JSpinner.DateEditor(endTimeSpinner, "yyyy-MM-dd HH:mm")
        );

        JPanel formPanel = createFormPanel();
        add(formPanel, BorderLayout.CENTER);

        imagePreview = new JLabel("이미지를 선택해 주세요", SwingConstants.CENTER);
        imagePreview.setPreferredSize(new Dimension(300, 230));
        imagePreview.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        add(imagePreview, BorderLayout.EAST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton("취소");
        JButton registerButton = new JButton("상품 등록");

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearForm();
                mainFrame.showAuctionTab();
            }
        });

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registerProduct();
            }
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(registerButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        addField(panel, c, 0, "판매자", new JLabel(currentUser.getUserName()));
        addField(panel, c, 1, "상품명", titleField);

        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 0;
        c.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel("상품 설명"), c);
        c.gridx = 1;
        c.weightx = 1;
        panel.add(new JScrollPane(descriptionArea), c);

        addField(panel, c, 3, "시작 가격", priceField);
        addField(panel, c, 4, "경매 시작", startTimeSpinner);
        addField(panel, c, 5, "경매 종료", endTimeSpinner);

        JButton imageButton = new JButton("이미지 선택");
        imageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooseImage();
            }
        });
        addField(panel, c, 6, "상품 이미지", imageButton);

        return panel;
    }

    private void addField(
            JPanel panel,
            GridBagConstraints c,
            int row,
            String name,
            JComponent component) {

        c.gridx = 0;
        c.gridy = row;
        c.weightx = 0;
        c.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel(name), c);

        c.gridx = 1;
        c.weightx = 1;
        panel.add(component, c);
    }

    private void chooseImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter(
                "이미지 파일 (jpg, jpeg, png, gif)",
                "jpg", "jpeg", "png", "gif"
        ));

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();

        try {
            BufferedImage image = ImageIO.read(file);

            if (image == null) {
                showMessage("지원하지 않는 이미지입니다.");
                return;
            }

            Image scaledImage = image.getScaledInstance(
                    290,
                    220,
                    Image.SCALE_SMOOTH
            );
            imagePreview.setText("");
            imagePreview.setIcon(new ImageIcon(scaledImage));
            selectedImage = file;
        } catch (Exception e) {
            selectedImage = null;
            showMessage("이미지를 읽을 수 없습니다.");
        }
    }

    private void registerProduct() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:00");
        String startTime = format.format((Date) startTimeSpinner.getValue());
        String endTime = format.format((Date) endTimeSpinner.getValue());

        Product product = service.registerProduct(
                currentUser,
                titleField.getText(),
                descriptionArea.getText(),
                priceField.getText(),
                startTime,
                endTime,
                selectedImage
        );

        if (product == null) {
            showMessage(service.getLastErrorMessage());
            return;
        }

        JOptionPane.showMessageDialog(this, "상품이 등록되었습니다.");
        clearForm();
        mainFrame.showAuctionProduct(product.getProductId());
    }

    private void clearForm() {
        titleField.setText("");
        descriptionArea.setText("");
        priceField.setText("");
        selectedImage = null;
        imagePreview.setIcon(null);
        imagePreview.setText("이미지를 선택해 주세요");

        Date now = new Date();
        startTimeSpinner.setValue(now);
        endTimeSpinner.setValue(
                new Date(now.getTime() + 24L * 60L * 60L * 1000L)
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
