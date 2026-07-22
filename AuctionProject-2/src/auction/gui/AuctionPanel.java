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

/**
 * 경매 상품 목록 조회(상태별 필터ing), 선택된 상품의 상세 정보 확인, 입찰 진행 및 입찰 기록 조회를 수행하는 메인 경매 화면 패널이다.
 */
public class AuctionPanel extends JPanel {
    // [필드] service: 경매 데이터 조회 및 입찰 처리를 담당하는 비즈니스 로직 서비스 객체
    private final AuctionService service;

    // [필드] currentUser: 현재 앱에 로그인한 사용자 (입찰 시 입찰자로 기록됨)
    private final User currentUser;

    // [필드] tableModel: 좌측 상품 목록 JTable의 행(row) 및 열(column) 데이터를 관리하는 모델 객체
    private final DefaultTableModel tableModel;

    // [필드] productTable: 상품 목록 데이터를 화면에 시각적 테이블 형태로 보여 주는 JTable 컴포넌트
    private final JTable productTable;

    // [필드] statusFilter: "전체", "대기 중", "진행 중", "낙찰", "유찰" 등 상태별로 목록을 필터링하는 Drop-down 콤보박스
    private final JComboBox<String> statusFilter;

    // [필드] imageLabel: 우측 상세 영역 상단에 선택한 상품의 이미지를 보여 주는 라벨 컴포넌트
    private final JLabel imageLabel;

    // [필드] titleLabel: 선택한 상품의 제목을 큰 글씨로 표시하는 라벨 컴포넌트
    private final JLabel titleLabel;

    // [필드] descriptionArea: 선택한 상품의 설명 텍스트를 보여 주는 읽기 전용 텍스트 영역 컴포넌트
    private final JTextArea descriptionArea;

    // [필드] sellerLabel: 선택한 상품의 판매자 이름을 표시하는 라벨 컴포넌트
    private final JLabel sellerLabel;

    // [필드] priceLabel: 선택한 상품의 현재 최고가(또는 시작가)를 원화 포맷으로 표시하는 라벨 컴포넌트
    private final JLabel priceLabel;

    // [필드] bidderLabel: 선택한 상품의 현재 최고 입찰자 이름을 표시하는 라벨 컴포넌트
    private final JLabel bidderLabel;

    // [필드] periodLabel: 선택한 상품의 경매 시작 시각 ~ 종료 시각을 표시하는 라벨 컴포넌트
    private final JLabel periodLabel;

    // [필드] remainingLabel: 선택한 상품의 경매 시작까지/종료까지 남은 시간을 실시간 카운트다운 형태로 표시하는 라벨
    private final JLabel remainingLabel;

    // [필드] statusLabel: 선택한 상품의 현재 경매 진행 상태("대기 중", "진행 중", "낙찰", "유찰") 라벨
    private final JLabel statusLabel;

    // [필드] bidPriceField: 입찰하고자 하는 금액을 입력하는 텍스트 입력창 (JTextField)
    private final JTextField bidPriceField;

    // [필드] bidButton: 입찰을 실행하는 버튼 컴포넌트 (종료/대기 상품인 경우 비활성화됨)
    private final JButton bidButton;

    // [필드] products: 파일에서 로드한 전체 상품 객체 리스트
    private ArrayList<Product> products;

    // [필드] selectedProductId: 현재 테이블에서 선택된 상품의 고유 ID (선택 항목 없을 시 -1)
    private int selectedProductId;

    /**
     * AuctionPanel 패널의 모든 UI 컴포넌트를 배치하고 이벤트 리스너를 바인딩한다.
     * 
     * @param service     경매 서비스 인스턴스
     * @param currentUser 현재 로그인 사용자 객체
     */
    public AuctionPanel(AuctionService service, User currentUser) {
        // 서비스 및 사용자 필드 초기화
        this.service = service;
        // 사용자 객체 설정
        this.currentUser = currentUser;
        // 상품 메모리 리스트 생성
        products = new ArrayList<Product>();
        // 선택 상품 ID 초기값 -1로 설정 (선택 안 됨)
        selectedProductId = -1;

        // 동서남북 8px 간격의 BorderLayout 설정
        setLayout(new BorderLayout(8, 8));
        // 패널 여백 설정 (10px)
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 상단 필터 패널 구성 (좌측 정렬 FlowLayout)
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
        // 패널 북쪽(NORTH)에 상단 필터 바 배치
        add(filterPanel, BorderLayout.NORTH);

        // 테이블 모델 정의 (셀 내용 직접 수정 불가 처리 overrides isCellEditable)
        tableModel = new DefaultTableModel(
                new Object[]{"번호", "상품명", "현재가", "상태"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 셀 수정 불가 설정
            }
        };
        // 테이블 생성 및 단일 행 선택 모드 설정
        productTable = new JTable(tableModel);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productTable.setRowHeight(27); // 행 높이 27px

        // 우측 상세 정보 패널 생성
        JPanel detailPanel = new JPanel(new BorderLayout(8, 8));
        detailPanel.setBorder(BorderFactory.createTitledBorder("상품 상세 및 입찰"));
        
        // 상품 이미지 표시 라벨 초기화
        imageLabel = new JLabel("상품을 선택해 주세요", SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(300, 220));
        imageLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        detailPanel.add(imageLabel, BorderLayout.NORTH);

        // 상세 정보 텍스트 컴포넌트들 초기화
        titleLabel = new JLabel("-");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 20f));
        descriptionArea = new JTextArea(4, 25);
        descriptionArea.setEditable(false); // 수정 불가
        descriptionArea.setLineWrap(true);   // 줄바꿈
        descriptionArea.setWrapStyleWord(true);
        sellerLabel = new JLabel("-");
        priceLabel = new JLabel("-");
        bidderLabel = new JLabel("-");
        periodLabel = new JLabel("-");
        remainingLabel = new JLabel("-");
        statusLabel = new JLabel("-");

        // 상세 텍스트 패널을 중앙에 배치
        detailPanel.add(createInformationPanel(), BorderLayout.CENTER);

        // 입찰 전용 하단 패널 생성 (입찰 가격 입력창, 입찰 버튼, 입찰 기록 버튼)
        bidPriceField = new JTextField(12);
        bidButton = new JButton("입찰하기");
        JButton historyButton = new JButton("입찰 기록");
        JPanel bidPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bidPanel.add(new JLabel("입찰 가격"));
        bidPanel.add(bidPriceField);
        bidPanel.add(bidButton);
        bidPanel.add(historyButton);
        detailPanel.add(bidPanel, BorderLayout.SOUTH);

        // 좌측(테이블 목록)과 우측(상세정보)을 나누는 JSplitPane 분할 생성
        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(productTable), // 좌측 테이블 스크롤
                detailPanel                   // 우측 상세 패널
        );
        splitPane.setResizeWeight(0.45);
        splitPane.setDividerLocation(460); // 경계선 위치
        add(splitPane, BorderLayout.CENTER);

        // [상태 필터 콤보박스 이벤트 리스너]
        statusFilter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rebuildTable(); // 필터 조건에 맞게 테이블 다시 그리기
            }
        });

        // [새로고침 버튼 이벤트 리스너]
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refresh(); // 데이터 재로드 및 화면 갱신
            }
        });

        // [테이블 선택 이벤트 리스너] 사용자가 테이블의 한 행을 누르면 해당 상품 선택
        productTable.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {
                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        // 드래그 중인 임시 이벤트를 제외하고 선택 확정 시에만 실행
                        if (!e.getValueIsAdjusting()) {
                            selectTableProduct();
                        }
                    }
                }
        );

        // [입찰하기 버튼 이벤트 리스너]
        bidButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placeBid(); // 입찰 처리 실행
            }
        });

        // [입찰 기록 버튼 이벤트 리스너]
        historyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showBidHistory(); // 입찰 내역 팝업 띄우기
            }
        });

        // 화면 초기 로딩 시 데이터 조회
        refresh();
    }

    /**
     * 우측 항목에 표시될 라벨과 값 텍스트 컴포넌트들을 GridBagLayout 패널로 묶어 생성한다.
     * 
     * @return 정보 패널 (JPanel)
     */
    private JPanel createInformationPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 5, 3, 5);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;

        // 상품 제목 표시
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.weightx = 1;
        panel.add(titleLabel, c);
        c.gridwidth = 1;

        // 세부 항목 행 추가
        addInformation(panel, c, 1, "설명", new JScrollPane(descriptionArea));
        addInformation(panel, c, 2, "판매자", sellerLabel);
        addInformation(panel, c, 3, "현재 가격", priceLabel);
        addInformation(panel, c, 4, "최고 입찰자", bidderLabel);
        addInformation(panel, c, 5, "경매 기간", periodLabel);
        addInformation(panel, c, 6, "남은 시간", remainingLabel);
        addInformation(panel, c, 7, "상태", statusLabel);

        return panel;
    }

    /**
     * 정보 패널에 라벨 텍스트와 컴포넌트를 줄 단위로 추가한다.
     */
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

    /**
     * CSV 파일에서 상품 목록을 재로드하고, 테이블 및 선택된 상품의 상세 정보를 갱신한다.
     */
    public void refresh() {
        // 서비스로부터 최신 상품 리스트 로드
        products = service.loadProducts();
        // 테이블 행 다시 구성
        rebuildTable();

        // 이전에 선택된 상품이 존재하는 경우 상세 보기 재갱신
        if (selectedProductId > 0) {
            selectProduct(selectedProductId);
            refreshDetail();
        }
    }

    /**
     * 현재 선택된 필터 조건("전체" 또는 특정 상태)에 따라 테이블 모델의 데이터 행을 재생성한다.
     */
    private void rebuildTable() {
        int oldSelectedId = selectedProductId;
        // 기존 테이블 행 삭제
        tableModel.setRowCount(0);
        // 선택된 필터 항목 텍스트 획득
        String filter = String.valueOf(statusFilter.getSelectedItem());

        // 로드된 상품들을 순회하며 필터 조건에 부합하는 경우에만 테이블에 행 추가
        for (Product product : products) {
            if (!"전체".equals(filter)
                    && !filter.equals(product.getStatus())) {
                continue; // 필터와 일치하지 않으면 건너뜀
            }

            // 테이블 행 데이터 추가 (번호, 상품명, 원화 포맷 현재가, 상태)
            tableModel.addRow(new Object[]{
                    product.getProductId(),
                    product.getTitle(),
                    String.format("%,d원", product.getCurrentPrice()),
                    product.getStatus()
            });
        }

        // 기존 선택 항목 유지 시도
        if (oldSelectedId > 0) {
            selectProduct(oldSelectedId);
        }
    }

    /**
     * 사용자가 JTable 상에서 특정 행(Row)을 클릭했을 때 선택된 상품 ID를 획득하고 상세 정보를 업데이트한다.
     */
    private void selectTableProduct() {
        int row = productTable.getSelectedRow();

        // 선택된 행이 없는 경우 종료
        if (row < 0) {
            return;
        }

        // 0번째 컬럼(상품 ID) 값 추출하여 정수로 변환
        selectedProductId = Integer.parseInt(
                String.valueOf(tableModel.getValueAt(row, 0))
        );
        // 우측 상세 정보 갱신
        refreshDetail();
    }

    /**
     * 특정 상품 ID에 해당하는 행을 테이블에서 찾아 선택 상태로 만들고 상세 정보를 표시한다.
     * 
     * @param productId 선택할 상품의 고유 ID
     */
    public void selectProduct(int productId) {
        selectedProductId = productId;

        // 테이블의 행들을 탐색하여 productId가 일치하는 행을 찾음
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            int rowProductId = Integer.parseInt(
                    String.valueOf(tableModel.getValueAt(row, 0))
            );

            if (rowProductId == productId) {
                // 해당 테이블 행에 포커스/선택 효과 적용
                productTable.setRowSelectionInterval(row, row);
                // 상세 영역 갱신
                refreshDetail();
                return;
            }
        }
    }

    /**
     * 선택된 상품(selectedProductId)의 최신 데이터를 서비스에서 조회하여 우측 라벨 및 이미지 컴포넌트에 반영한다.
     */
    private void refreshDetail() {
        if (selectedProductId < 1) {
            return;
        }

        // 최신 상품 단건 로드
        Product product = service.loadProduct(selectedProductId);

        if (product == null) {
            return;
        }

        // 라벨 값 갱신
        titleLabel.setText(product.getTitle());
        descriptionArea.setText(product.getDescription());
        sellerLabel.setText(product.getSellerName());
        priceLabel.setText(String.format("%,d원", product.getCurrentPrice()));

        // 최고 입찰자 유무에 따른 텍스트 설정
        if (product.getCurrentBidder().isEmpty()) {
            bidderLabel.setText("아직 입찰 없음");
        } else {
            bidderLabel.setText(product.getCurrentBidder());
        }

        // 경매 기간 텍스트
        periodLabel.setText(
                product.getAuctionStartTime()
                        + " ~ "
                        + product.getAuctionEndTime()
        );
        // 남은 시간 텍스트 (서비스 계산 함수 호출)
        remainingLabel.setText(service.getAuctionTimeMessage(product));
        // 상태 텍스트
        statusLabel.setText(product.getStatus());

        // 경매가 "진행 중" 상태일 때만 입찰 버튼 및 입력창 활성화
        bidButton.setEnabled(product.isOpen());
        bidPriceField.setEnabled(product.isOpen());

        // 상품 이미지 불러오기
        showImage(product.getImagePath());
    }

    /**
     * 상대 경로에 위치한 이미지 파일(imagePath)을 불러와 축소 스케일링 후 imageLabel에 보여 준다.
     * 
     * @param imagePath 이미지 파일 상대 경로 (예: "images/product_1.jpg")
     */
    private void showImage(String imagePath) {
        File imageFile = new File(imagePath);
        ImageIcon icon = new ImageIcon(imageFile.getPath());

        // 이미지를 정상적으로 읽지 못한 경우 (너비가 0 이하)
        if (icon.getIconWidth() <= 0) {
            imageLabel.setIcon(null);
            imageLabel.setText("이미지를 읽을 수 없습니다");
            return;
        }

        // 290x210px 크기로 부드럽게(SCALE_SMOOTH) 조절
        Image resizedImage = icon.getImage().getScaledInstance(
                290,
                210,
                Image.SCALE_SMOOTH
        );
        imageLabel.setText("");
        imageLabel.setIcon(new ImageIcon(resizedImage));
    }

    /**
     * 입력된 입찰 가격으로 입찰을 진행한다.
     */
    private void placeBid() {
        if (selectedProductId < 1) {
            showMessage("상품을 먼저 선택해 주세요.");
            return;
        }

        // 서비스에 입찰 요청
        Product product = service.placeBid(
                selectedProductId,
                currentUser,
                bidPriceField.getText()
        );

        // 입찰 실패 시 오류 안내 팝업 출력 후 화면 새로고침
        if (product == null) {
            showMessage(service.getLastErrorMessage());
            refresh();
            return;
        }

        // 입찰 금액 입력창 초기화
        bidPriceField.setText("");
        // 화면 재로드
        refresh();

        // 입찰 완료 안내 메시지 팝업
        JOptionPane.showMessageDialog(
                this,
                "입찰이 완료되었습니다.\n현재 최고가: "
                        + String.format("%,d원", product.getCurrentPrice()),
                "입찰 완료",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    /**
     * 선택된 상품의 지금까지 쌓인 모든 입찰 기록(Bid History)을 모달 JDialog/JOptionPane 팝업으로 보여 준다.
     */
    private void showBidHistory() {
        if (selectedProductId < 1) {
            showMessage("상품을 먼저 선택해 주세요.");
            return;
        }

        // 해당 상품의 모든 입찰 기록 로드
        ArrayList<Bid> bids = service.loadBids(selectedProductId);

        // 팝업 테이블용 모델 정의
        DefaultTableModel bidModel = new DefaultTableModel(
                new Object[]{"입찰번호", "입찰자", "입찰가격", "입찰시간"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 셀 수정 불가
            }
        };

        // 입찰 기록을 순회하며 테이블 행 추가
        for (Bid bid : bids) {
            bidModel.addRow(new Object[]{
                    bid.getBidId(),
                    bid.getBidderName(),
                    String.format("%,d원", bid.getBidPrice()),
                    bid.getBidTime()
            });
        }

        // JTable 생성 및 스크롤 패널로 감싸기
        JTable bidTable = new JTable(bidModel);
        bidTable.setRowHeight(26);
        JScrollPane scrollPane = new JScrollPane(bidTable);
        scrollPane.setPreferredSize(new Dimension(600, 300));

        // 팝업 띄우기
        JOptionPane.showMessageDialog(
                this,
                scrollPane,
                "입찰 기록",
                JOptionPane.PLAIN_MESSAGE
        );
    }

    /**
     * 경고 안내 팝업창을 출력한다.
     * 
     * @param message 팝업 메세지 텍스트
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
