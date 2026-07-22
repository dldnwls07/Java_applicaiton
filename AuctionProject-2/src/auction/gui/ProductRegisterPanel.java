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

/**
 * 판매자가 상품명, 상품 설명, 시작 가격, 경매 시작/종료 일시 및 이미지를 입력하여 새로운 경매 상품을 등록하는 Swing 패널 컴포넌트다.
 */
public class ProductRegisterPanel extends JPanel {
    // [필드] mainFrame: 상품 등록 완료 시 탭 전환 및 화면 갱신을 위해 메인 프레임을 참조하는 변수
    private final MainFrame mainFrame;

    // [필드] service: 상품 등록 비즈니스 로직 및 파일 처리를 수행하는 서비스 객체
    private final AuctionService service;

    // [필드] currentUser: 현재 앱에 로그인하여 상품을 등록하는 사용자 객체 (판매자로 설정됨)
    private final User currentUser;

    // [필드] titleField: 상품 제목을 입력받는 텍스트 입력 상자 컴포넌트 (JTextField)
    private final JTextField titleField;

    // [필드] descriptionArea: 상품 상세 설명을 여러 줄로 입력받는 텍스트 영역 컴포넌트 (JTextArea)
    private final JTextArea descriptionArea;

    // [필드] priceField: 경매 시작 가격을 입력받는 텍스트 입력 상자 컴포넌트 (JTextField)
    private final JTextField priceField;

    // [필드] startTimeSpinner: 경매 시작 날짜 및 시간을 연/월/일/시/분 단위로 선택하는 스피너 컴포넌트 (JSpinner)
    private final JSpinner startTimeSpinner;

    // [필드] endTimeSpinner: 경매 종료 날짜 및 시간을 연/월/일/시/분 단위로 선택하는 스피너 컴포넌트 (JSpinner)
    private final JSpinner endTimeSpinner;

    // [필드] imagePreview: 사용자가 선택한 이미지의 썸네일 축소판을 화면에 보여 주는 라벨 컴포넌트 (JLabel)
    private final JLabel imagePreview;

    // [필드] selectedImage: 사용자가 파일 탐색기에서 선택한 이미지 파일 객체 (미선택 시 null)
    private File selectedImage;

    /**
     * ProductRegisterPanel 패널의 레이아웃, 입력 폼 컴포넌트 및 이벤트 리스너를 초기화한다.
     * 
     * @param mainFrame   메인 창 인스턴스
     * @param service     경매 서비스 인스턴스
     * @param currentUser 현재 로그인한 사용자 인스턴스
     */
    public ProductRegisterPanel(
            MainFrame mainFrame,
            AuctionService service,
            User currentUser) {

        // 전달받은 메인 프레임 참조 대입
        this.mainFrame = mainFrame;
        // 전달받은 서비스 참조 대입
        this.service = service;
        // 현재 사용자 참조 대입
        this.currentUser = currentUser;

        // 패널 레이아웃을 BorderLayout(여백 15px)으로 설정
        setLayout(new BorderLayout(15, 15));
        // 패널 외곽선 여백 설정 (상, 좌, 하, 우 18px)
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        // 입력 폼 컴포넌트 생성 (제목: 20컬럼 너비)
        titleField = new JTextField(20);
        // 설명 입력 텍스트 영역 생성 (5행 20열 기본 크기)
        descriptionArea = new JTextArea(5, 20);
        // 가격 입력 컴포넌트 생성 (20컬럼 너비)
        priceField = new JTextField(20);
        // 텍스트 영역 자동 줄바꿈 활성화
        descriptionArea.setLineWrap(true);
        // 단어 단위 줄바꿈 설정
        descriptionArea.setWrapStyleWord(true);

        // 현재 날짜 시각 객체 생성
        Date now = new Date();
        // 기본 종료 시각으로 사용할 24시간(1일) 뒤의 Date 객체 계산 (24 * 60 * 60 * 1000ms)
        Date tomorrow = new Date(now.getTime() + 24L * 60L * 60L * 1000L);

        // 시작 시간 날짜 스피너 생성 (분 단위 변경)
        startTimeSpinner = new JSpinner(
                new SpinnerDateModel(now, null, null, Calendar.MINUTE)
        );
        // 종료 시간 날짜 스피너 생성 (분 단위 변경)
        endTimeSpinner = new JSpinner(
                new SpinnerDateModel(tomorrow, null, null, Calendar.MINUTE)
        );
        // 시작 시간 스피너의 날짜 표시 형식 지정 ("yyyy-MM-dd HH:mm")
        startTimeSpinner.setEditor(
                new JSpinner.DateEditor(startTimeSpinner, "yyyy-MM-dd HH:mm")
        );
        // 종료 시간 스피너의 날짜 표시 형식 지정 ("yyyy-MM-dd HH:mm")
        endTimeSpinner.setEditor(
                new JSpinner.DateEditor(endTimeSpinner, "yyyy-MM-dd HH:mm")
        );

        // 입력 항목들을 배치한 GridBagLayout 폼 패널을 생성하여 중앙(CENTER)에 배치
        JPanel formPanel = createFormPanel();
        add(formPanel, BorderLayout.CENTER);

        // 이미지 미리보기 영역 라벨 생성 및 초기 안내 텍스트 설정
        imagePreview = new JLabel("이미지를 선택해 주세요", SwingConstants.CENTER);
        // 이미지 미리보기 고정 권장 크기 지정 (너비 300px, 높이 230px)
        imagePreview.setPreferredSize(new Dimension(300, 230));
        // 미리보기 외곽선 테두리 설정
        imagePreview.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        // 패널 우측(EAST)에 이미지 미리보기 배치
        add(imagePreview, BorderLayout.EAST);

        // 하단 버튼들을 정렬할 흐름 레이아웃 패널 생성 (우측 정렬)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton("취소");
        JButton registerButton = new JButton("상품 등록");

        // [취소 버튼 이벤트] 입력 양식을 비우고 경매 목록 탭으로 이동한다.
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearForm(); // 입력 양식 초기화
                mainFrame.showAuctionTab(); // 첫 번째 경매 탭으로 이동
            }
        });

        // [상품 등록 버튼 이벤트] 입력값 유효성을 검사하고 등록 서비스를 호출한다.
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registerProduct(); // 상품 등록 함수 실행
            }
        });

        // 버튼 패널에 버튼 추가
        buttonPanel.add(cancelButton);
        buttonPanel.add(registerButton);
        // 하단(SOUTH)에 버튼 패널 배치
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * 입력 레이블과 컨트롤 컴포넌트를 GridBagLayout으로 정돈하여 배치한 패널을 생성한다.
     * 
     * @return 폼 입력 패널 (JPanel)
     */
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5); // 여백 설정 (상좌하우 5px)
        c.anchor = GridBagConstraints.WEST;  // 서쪽(좌측) 정렬
        c.fill = GridBagConstraints.HORIZONTAL; // 가로 방향 채우기

        // 0행: 판매자 표시 (수정 불가 라벨)
        addField(panel, c, 0, "판매자", new JLabel(currentUser.getUserName()));
        // 1행: 상품명 입력 필드
        addField(panel, c, 1, "상품명", titleField);

        // 2행: 상품 설명 (스크롤 가능한 JTextArea)
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 0;
        c.anchor = GridBagConstraints.NORTHWEST; // 북서쪽 정렬
        panel.add(new JLabel("상품 설명"), c);
        c.gridx = 1;
        c.weightx = 1; // 텍스트 영역이 늘어나도록 가중치 1 부여
        panel.add(new JScrollPane(descriptionArea), c);

        // 3행: 시작 가격 입력 필드
        addField(panel, c, 3, "시작 가격", priceField);
        // 4행: 경매 시작 일시 스피너
        addField(panel, c, 4, "경매 시작", startTimeSpinner);
        // 5행: 경매 종료 일시 스피너
        addField(panel, c, 5, "경매 종료", endTimeSpinner);

        // 6행: 이미지 선택 버튼
        JButton imageButton = new JButton("이미지 선택");
        imageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooseImage(); // 파일 탐색기 열기
            }
        });
        addField(panel, c, 6, "상품 이미지", imageButton);

        return panel;
    }

    /**
     * GridBagLayout 패널에 라벨과 컴포넌트 한 쌍을 행 단위로 추가하는 헬퍼 메서드다.
     * 
     * @param panel     대상 패널
     * @param c         GridBagConstraints 배치 제약조건 객체
     * @param row       행 번호 (0, 1, 2...)
     * @param name      좌측 라벨에 표시할 텍스트
     * @param component 우측에 배치할 Swing 입력 컴포넌트
     */
    private void addField(
            JPanel panel,
            GridBagConstraints c,
            int row,
            String name,
            JComponent component) {

        // 열 0: 항목 이름 라벨
        c.gridx = 0;
        c.gridy = row;
        c.weightx = 0;
        c.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel(name), c);

        // 열 1: 입력 컴포넌트
        c.gridx = 1;
        c.weightx = 1;
        panel.add(component, c);
    }

    /**
     * JFileChooser 팝업을 띄워 사용자가 컴퓨터의 이미지 파일(jpg, jpeg, png, gif)을 선택하도록 처리한다.
     * 선택된 이미지는 리사이징되어 imagePreview 라벨에 축소 표시된다.
     */
    private void chooseImage() {
        JFileChooser chooser = new JFileChooser();
        // 확장자 필터 설정 (jpg, jpeg, png, gif만 허용)
        chooser.setFileFilter(new FileNameExtensionFilter(
                "이미지 파일 (jpg, jpeg, png, gif)",
                "jpg", "jpeg", "png", "gif"
        ));

        // 사용자가 파일 선택 창에서 "열기"를 누르지 않고 취소한 경우 종료
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        // 선택된 이미지 파일 획득
        File file = chooser.getSelectedFile();

        try {
            // ImageIO를 통해 파일을 이미지 객체로 메모리에 읽어온다.
            BufferedImage image = ImageIO.read(file);

            // 이미지 읽기 실패 시 경고
            if (image == null) {
                showMessage("지원하지 않는 이미지입니다.");
                return;
            }

            // 미리보기 크기(290x220)에 맞추어 부드러운 스케일링(SCALE_SMOOTH) 적용
            Image scaledImage = image.getScaledInstance(
                    290,
                    220,
                    Image.SCALE_SMOOTH
            );
            // 라벨 텍스트 제거 및 축소 이미지 아이콘 설정
            imagePreview.setText("");
            imagePreview.setIcon(new ImageIcon(scaledImage));
            // 선택된 이미지 필드에 파일 저장
            selectedImage = file;
        } catch (Exception e) {
            selectedImage = null;
            showMessage("이미지를 읽을 수 없습니다.");
        }
    }

    /**
     * 입력한 정보들을 모아 AuctionService의 registerProduct 메서드를 호출하고 상품 등록을 마무리한다.
     */
    private void registerProduct() {
        // 날짜 스피너의 Date 객체를 "yyyy-MM-dd HH:mm:00" 형식 문자열로 포맷팅한다.
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:00");
        String startTime = format.format((Date) startTimeSpinner.getValue());
        String endTime = format.format((Date) endTimeSpinner.getValue());

        // 서비스 객체에 등록 요청 전송
        Product product = service.registerProduct(
                currentUser,               // 등록자
                titleField.getText(),      // 제목
                descriptionArea.getText(), // 설명
                priceField.getText(),      // 시작가
                startTime,                 // 시작 시간
                endTime,                   // 종료 시간
                selectedImage              // 선택한 이미지 파일
        );

        // 등록 실패 시 오류 메시지 팝업을 띄우고 종료
        if (product == null) {
            showMessage(service.getLastErrorMessage());
            return;
        }

        // 성공 안내 팝업 출력
        JOptionPane.showMessageDialog(this, "상품이 등록되었습니다.");
        // 양식 초기화
        clearForm();
        // 등록된 상품의 상세 보기 화면(경매 탭)으로 즉시 이동
        mainFrame.showAuctionProduct(product.getProductId());
    }

    /**
     * 입력 폼의 모든 텍스트 필드, 스피너, 선택된 이미지를 초기화 상태로 되돌린다.
     */
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

    /**
     * 경고 안내 메세지 팝업창을 띄운다.
     * 
     * @param message 안내할 메세지 내용
     */
    private void showMessage(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "안내",
                JOptionPane.WARNING_MESSAGE
        );
    }
}
