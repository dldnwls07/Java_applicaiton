한국공학대학교 교내 경매 프로그램
==================================

1. 개발 환경
- Java 17 이상
- 순수 Java Swing 사용
- 데이터베이스와 외부 라이브러리 사용 안 함
- 상품과 입찰 데이터는 UTF-8 CSV 파일로 저장

2. 실행 방법
- Windows에서는 run.bat을 더블 클릭합니다.
- Eclipse에서는 File > Import > Existing Projects into Workspace로 AuctionProject를 가져옵니다.
- 가져온 뒤 AuctionProject.launch를 우클릭하여 Run As > AuctionProject를 실행합니다.
- 예전 auction.Main 실행 설정은 삭제하고 auction.MainFrame을 사용해야 합니다.
- 명령 프롬프트에서는 다음 명령을 사용합니다.
  compile.bat
  java -cp out auction.MainFrame

3. 주요 기능
- 사용자 이름 입력
- 상품명, 설명, 시작 가격, 이미지 등록
- 판매자가 경매 시작·종료 날짜와 시간 설정
- 상품 목록과 상세 정보를 한 화면에서 확인
- 시작 전 입찰 차단
- 본인 상품 입찰 차단
- 현재 최고가 이하 입찰 차단
- 입찰 기록 확인
- 1초마다 CSV 자동 갱신
- 종료 시 입찰자가 있으면 낙찰, 없으면 유찰 처리
- auction.lock 파일을 이용한 여러 프로그램의 동시 수정 방지

4. 화면 구성
- 경매 참여 탭: 왼쪽 상품 목록, 오른쪽 상세 정보와 입찰 영역
- 상품 등록 탭: 상품 정보, 경매 기간, 이미지 입력

5. 클래스 구조
- MainFrame: 프로그램 시작, 탭 화면, 1초 Timer
- AuctionPanel: 상품 목록, 상세 정보, 입찰, 입찰 기록
- ProductRegisterPanel: 상품 등록과 이미지 선택
- AuctionService: 입찰 검증, 시간 상태 변경, 잠금 처리
- DataManager: products.csv, bids.csv, 이미지 파일 관리
- Product, Bid: 상품과 입찰 데이터
- User, Seller, Bidder: 추상 클래스, 상속, 오버라이딩

6. 폴더 구조
- src/auction: MainFrame
- src/auction/model: User, Seller, Bidder, Product, Bid
- src/auction/file: DataManager
- src/auction/service: AuctionService
- src/auction/gui: AuctionPanel, ProductRegisterPanel
- data: products.csv, bids.csv
- images: 등록한 상품 이미지
- out: 컴파일 결과

7. 초급자용 코드 구성
- 인터페이스는 사용하지 않습니다.
- throw와 throws는 사용하지 않습니다.
- 별도의 사용자 정의 예외 클래스는 사용하지 않습니다.
- 실패 시 false 또는 null을 반환하고 getLastErrorMessage()로 이유를 확인합니다.
- CardLayout 대신 JTabbedPane을 사용합니다.
- 상품 파일과 입찰 파일은 DataManager 하나에서 관리합니다.

8. 주의사항
- run.bat을 사용하면 현재 실행 폴더가 자동으로 설정됩니다.
- 프로그램 실행 중 CSV 파일을 직접 수정하지 마세요.
- 같은 AuctionProject 폴더에서 여러 번 실행해야 같은 CSV를 공유합니다.
