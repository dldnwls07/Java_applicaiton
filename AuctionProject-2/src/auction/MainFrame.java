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

/**
 * 교내 경매 프로그램의 진입점(main)이자 메인 GUI 윈도우(JFrame) 클래스다.
 * 상단 헤더, 탭 관리(경매 참여 / 상품 등록), 하단 상태바 및 1초 주기 타이머 자동 갱신(백그라운드 동기화)을 관장한다.
 */
public class MainFrame extends JFrame {
    // [필드] service: 데이터 파일 및 경매 관련 비즈니스 로직을 처리하는 핵심 서비스 객체
    private final AuctionService service;

    // [필드] currentUser: 앱 시작 시 입력받은 사용자 이름으로 생성된 현재 로그인 사용자 인스턴스 (Bidder 객체)
    private final User currentUser;

    // [필드] auctionPanel: "경매 참여" 탭 화면 패널 객체
    private final AuctionPanel auctionPanel;

    // [필드] tabs: "경매 참여" 탭과 "상품 등록" 탭을 관리하는 Swing 탭 컨테이너 (JTabbedPane)
    private final JTabbedPane tabs;

    // [필드] statusLabel: 화면 하단에 1초 주기 자동 갱신 및 에러 상태를 표시해 주는 안내 라벨 (JLabel)
    private final JLabel statusLabel;

    /**
     * 메인 프레임 창을 구성하고 탭, 레이아웃, 1초 자동 갱신 타이머를 초기화하는 생성자다.
     * 
     * @param service     경매 비즈니스 로직 서비스 객체
     * @param currentUser 앱 접속 사용자 인스턴스
     */
    public MainFrame(AuctionService service, User currentUser) {
        // 서비스 필드 초기화
        this.service = service;
        // 사용자 필드 초기화
        this.currentUser = currentUser;

        // 메인 프레임 창 제목 설정
        setTitle("한국공학대학교 교내 경매");
        // 창 닫기 버튼 클릭 시 프로그램 전체 종료 설정
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // 창 기본 크기 지정 (가로 1150px, 세로 760px)
        setSize(1150, 760);
        // 창 최소 크기 제한 지정 (가로 1000px, 세로 680px)
        setMinimumSize(new Dimension(1000, 680));
        // 화면 중앙에 창 띄우기
        setLocationRelativeTo(null);

        // 상단 헤더 패널 생성 및 여백 설정 (상하 10px, 좌우 14px)
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        
        // 메인 제목 라벨 생성 및 굵은 24pt 폰트 적용
        JLabel titleLabel = new JLabel("교내 경매 프로그램");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 24f));
        header.add(titleLabel, BorderLayout.WEST);
        
        // 우측 사용자 이름 표시 라벨 추가
        header.add(
                new JLabel("현재 사용자: " + currentUser.getUserName()),
                BorderLayout.EAST
        );
        // 북쪽(NORTH) 영역에 헤더 패널 추가
        add(header, BorderLayout.NORTH);

        // 경매 목록 화면 패널 생성
        auctionPanel = new AuctionPanel(service, currentUser);
        // 상품 등록 화면 패널 생성
        ProductRegisterPanel registerPanel = new ProductRegisterPanel(
                this,
                service,
                currentUser
        );

        // 탭 컨테이너 초기화 및 탭 2개 추가
        tabs = new JTabbedPane();
        tabs.addTab("경매 참여", auctionPanel);   // 0번 탭
        tabs.addTab("상품 등록", registerPanel); // 1번 탭
        add(tabs, BorderLayout.CENTER);

        // 하단 상태바 라벨 초기화
        statusLabel = new JLabel("CSV 자동 갱신 중 (1초)");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 12, 8, 12));
        add(statusLabel, BorderLayout.SOUTH);

        // [1초 주기 타이머 설정] javax.swing.Timer를 이용해 1000ms(1초) 간격으로 refreshAuction() 메서드 자동 실행
        Timer timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshAuction(); // 1초마다 자동 갱신 및 상태 체킹 수행
            }
        });
        // 타이머 동작 시작
        timer.start();
    }

    /**
     * 상품 등록 완료 직후 첫 번째(경매 참여) 탭으로 이동하며, 해당 상품을 선택한 상태로 화면에 보여 준다.
     * 
     * @param productId 경매 탭에서 즉시 선택할 상품 ID
     */
    public void showAuctionProduct(int productId) {
        tabs.setSelectedIndex(0);              // 0번(경매 참여) 탭으로 전환
        auctionPanel.refresh();                 // 목록 새로고침
        auctionPanel.selectProduct(productId);  // 해당 상품 포커스 지정
    }

    /**
     * 취소 버튼 클릭 시 첫 번째(경매 참여) 탭으로 화면을 전환한다.
     */
    public void showAuctionTab() {
        tabs.setSelectedIndex(0); // 0번 탭 선택
        auctionPanel.refresh();    // 데이터 새로고침
    }

    /**
     * 타이머에 의해 1초마다 호출되며, 경매 상태(대기 중 -> 진행 중 -> 낙찰/유찰)를 자동 갱신하고
     * 새롭게 종료된 경매가 발견되면 결과 알림 팝업을 출력한다.
     */
    private void refreshAuction() {
        // 경매 상태 자동 업데이트 및 새로 종료된 상품 리스트 반환 받기
        ArrayList<Product> endedProducts = service.updateAuctionStatuses();
        // 경매 패널 화면 새로고침
        auctionPanel.refresh();

        // 서비스 에러 메시지 유무에 따른 하단 상태바 텍스트 변경
        if (service.getLastErrorMessage().isEmpty()) {
            statusLabel.setText("CSV 자동 갱신 중 (1초)");
        } else {
            statusLabel.setText("갱신 오류: " + service.getLastErrorMessage());
        }

        // 방금 새로 종료 처리된 상품들에 대해 낙찰/유찰 알림 팝업 띄우기
        for (Product product : endedProducts) {
            showAuctionResult(product);
        }
    }

    /**
     * 경매 마감 시 결과(낙찰된 경우 낙찰자와 낙찰가, 유찰된 경우 유찰 안내)를 알림 팝업으로 출력한다.
     * 
     * @param product 경매가 종료된 Product 객체
     */
    private void showAuctionResult(Product product) {
        if (Product.STATUS_SOLD.equals(product.getStatus())) {
            // [낙찰] 낙찰자와 낙찰 가격 안내 팝업
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
            // [유찰] 입찰자 없이 종료된 유찰 안내 팝업
            JOptionPane.showMessageDialog(
                    this,
                    "입찰 없이 경매가 종료되었습니다.\n상품명: "
                            + product.getTitle(),
                    "경매 종료",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    /**
     * 자바 애플리케이션 시작 메인 진입점이다.
     * Swing 이벤트 스레드(EDT) 상에서 안전하게 startProgram()을 호출한다.
     * 
     * @param args 커맨드라인 매개변수 배열
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                startProgram(); // 프로그램 초기화 및 시작
            }
        });
    }

    /**
     * 시스템 OS 룩앤필(UI 디자인) 적용, 필수 파일 검사, 사용자 이름 입력 및 메인 프레임 생성을 담당한다.
     */
    private static void startProgram() {
        try {
            // 현재 사용 중인 운영체제(macOS, Windows 등)의 기본 UI 룩앤필 적용
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("기본 화면 모양을 사용합니다.");
        }

        // 경매 서비스 객체 생성
        AuctionService service = new AuctionService();

        // 초기 데이터 파일 및 디렉터리 검사/생성 실패 시 프로그램 종료
        if (!service.initializeFiles()) {
            JOptionPane.showMessageDialog(null, service.getLastErrorMessage());
            return;
        }

        // 비정상 종료 등으로 남아있는 구버전 잠금 파일(auction.lock) 정리
        clearOldLockFile();
        
        // 사용자 이름 입력 대화상자 띄우기
        String userName = askUserName();

        // 사용자가 이름을 입력하지 않고 취소한 경우 프로그램 종료
        if (userName == null) {
            return;
        }

        // 입력된 사용자 이름으로 기본 입찰자(Bidder) 객체 생성 (ID=1)
        User currentUser = new Bidder(1, userName);
        
        // 메인 창 생성 및 화면에 시각화
        MainFrame frame = new MainFrame(service, currentUser);
        frame.setVisible(true); // 창 보이기
    }

    /**
     * 앱 시작 시 사용자의 이름을 입력받는 대화상자를 띄운다. (공백 또는 취소 처리 포함)
     * 
     * @return 입력된 유효한 사용자 이름 문자열 (취소 시 null)
     */
    private static String askUserName() {
        while (true) {
            // 사용자 이름 입력 input dialog 팝업
            String name = JOptionPane.showInputDialog(
                    null,
                    "사용자 이름을 입력해 주세요.",
                    "교내 경매 로그인",
                    JOptionPane.QUESTION_MESSAGE
            );

            // 취소 버튼 클릭 시 null 반환
            if (name == null) {
                return null;
            }

            // 앞뒤 공백 제거 및 쉼표 정제
            name = name.trim().replace(',', ' ');

            // 이름을 올바르게 입력한 경우 입력된 이름 반환
            if (!name.isEmpty()) {
                return name;
            }

            // 빈 이름 입력 시 경고 팝업 후 다시 입력 대기
            JOptionPane.showMessageDialog(null, "사용자 이름을 입력해 주세요.");
        }
    }

    /**
     * 프로그램 비정상 종료 등으로 잔여 잠금 파일("data/auction.lock")이 여전히 존재하는지 검사하고 삭제 여부를 확인한다.
     */
    private static void clearOldLockFile() {
        File lockFile = new File("data/auction.lock");

        // 잠금 파일이 없으면 정상
        if (!lockFile.exists()) {
            return;
        }

        // 잠금 파일 발견 시 확인 대화상자 출력
        int answer = JOptionPane.showConfirmDialog(
                null,
                "이전 잠금 파일이 남아 있습니다. 삭제할까요?",
                "잠금 파일 확인",
                JOptionPane.YES_NO_OPTION
        );

        // 예(YES) 선택 시 잠금 파일 삭제
        if (answer == JOptionPane.YES_OPTION) {
            lockFile.delete();
        }
    }
}
