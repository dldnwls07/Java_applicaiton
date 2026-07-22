package auction;

import auction.gui.AuctionPanel;
import auction.gui.ProductRegisterPanel;
import auction.model.Bidder;
import auction.model.Product;
import auction.model.User;
import auction.service.AuctionService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

/** 프로그램 시작과 두 개의 주요 화면, 1초 자동 갱신을 관리한다. */
public class MainFrame extends JFrame {
    private final AuctionService service;
    private final User currentUser;
    private final AuctionPanel auctionPanel;
    private final JTabbedPane tabs;
    private final JLabel statusLabel;

    public MainFrame(AuctionService service, User currentUser) {
        this.service = service;
        this.currentUser = currentUser;

        setTitle("한국공학대학교 교내 경매");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1150, 760);
        setMinimumSize(new Dimension(1000, 680));
        setLocationRelativeTo(null);

        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        JLabel titleLabel = new JLabel("교내 경매 프로그램");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 24f));
        header.add(titleLabel, BorderLayout.WEST);
        header.add(
                new JLabel("현재 사용자: " + currentUser.getUserName()),
                BorderLayout.EAST
        );
        add(header, BorderLayout.NORTH);

        auctionPanel = new AuctionPanel(service, currentUser);
        ProductRegisterPanel registerPanel = new ProductRegisterPanel(
                this,
                service,
                currentUser
        );

        tabs = new JTabbedPane();
        tabs.addTab("경매 참여", auctionPanel);
        tabs.addTab("상품 등록", registerPanel);
        add(tabs, BorderLayout.CENTER);

        statusLabel = new JLabel("CSV 자동 갱신 중 (1초)");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 12, 8, 12));
        add(statusLabel, BorderLayout.SOUTH);

        Timer timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshAuction();
            }
        });
        timer.start();
    }

    /** 등록한 상품을 경매 탭에서 바로 선택해 보여 준다. */
    public void showAuctionProduct(int productId) {
        tabs.setSelectedIndex(0);
        auctionPanel.refresh();
        auctionPanel.selectProduct(productId);
    }

    public void showAuctionTab() {
        tabs.setSelectedIndex(0);
        auctionPanel.refresh();
    }

    private void refreshAuction() {
        ArrayList<Product> endedProducts = service.updateAuctionStatuses();
        auctionPanel.refresh();

        if (service.getLastErrorMessage().isEmpty()) {
            statusLabel.setText("CSV 자동 갱신 중 (1초)");
        } else {
            statusLabel.setText("갱신 오류: " + service.getLastErrorMessage());
        }

        for (Product product : endedProducts) {
            showAuctionResult(product);
        }
    }

    private void showAuctionResult(Product product) {
        if (Product.STATUS_SOLD.equals(product.getStatus())) {
            JOptionPane.showMessageDialog(
                    this,
                    "경매가 종료되었습니다.\n\n낙찰자: "
                            + product.getCurrentBidder()
                            + "\n낙찰 가격: "
                            + String.format("%,d원", product.getCurrentPrice())
                            + "\n상품명: "
                            + product.getTitle(),
                    "낙찰 완료",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "입찰 없이 경매가 종료되었습니다.\n상품명: "
                            + product.getTitle(),
                    "경매 종료",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                startProgram();
            }
        });
    }

    private static void startProgram() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("기본 화면 모양을 사용합니다.");
        }

        AuctionService service = new AuctionService();

        if (!service.initializeFiles()) {
            JOptionPane.showMessageDialog(null, service.getLastErrorMessage());
            return;
        }

        clearOldLockFile();
        String userName = askUserName();

        if (userName == null) {
            return;
        }

        User currentUser = new Bidder(1, userName);
        MainFrame frame = new MainFrame(service, currentUser);
        frame.setVisible(true);
    }

    private static String askUserName() {
        while (true) {
            String name = JOptionPane.showInputDialog(
                    null,
                    "사용자 이름을 입력해 주세요.",
                    "교내 경매 로그인",
                    JOptionPane.QUESTION_MESSAGE
            );

            if (name == null) {
                return null;
            }

            name = name.trim().replace(',', ' ');

            if (!name.isEmpty()) {
                return name;
            }

            JOptionPane.showMessageDialog(null, "사용자 이름을 입력해 주세요.");
        }
    }

    /** 비정상 종료 후 남은 잠금 파일을 확인하고 삭제한다. */
    private static void clearOldLockFile() {
        File lockFile = new File("data/auction.lock");

        if (!lockFile.exists()) {
            return;
        }

        int answer = JOptionPane.showConfirmDialog(
                null,
                "이전 잠금 파일이 남아 있습니다. 삭제할까요?",
                "잠금 파일 확인",
                JOptionPane.YES_NO_OPTION
        );

        if (answer == JOptionPane.YES_OPTION) {
            lockFile.delete();
        }
    }
}
