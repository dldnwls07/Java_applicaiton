한국공학대학교 교내 경매 프로그램
==================================

1. 개발 환경
- Java 17 이상 (Java 21 권장)
- 순수 Java Swing만 사용
- 데이터베이스와 외부 라이브러리 사용 안 함

2. 실행 방법 (Windows)
- run.bat을 더블 클릭합니다.
- 또는 명령 프롬프트에서 다음을 실행합니다.
  compile.bat
  java -cp out auction.Main

3. 여러 사용자 동시 실행
- 같은 AuctionProject 폴더에서 run.bat을 2~3번 실행합니다.
- 모든 창은 data 폴더의 같은 CSV 파일을 사용합니다.
- 각 창은 1초마다 CSV를 다시 읽습니다.
- 입찰과 낙찰 저장 시 data/auction.lock 파일로 동시 수정을 막습니다.

4. 사용 방법
- 시작할 때 사용자 이름을 입력합니다.
- 상품 등록에서 상품명, 설명, 시작 가격, 이미지를 선택합니다.
- 상품 목록의 행을 더블 클릭하면 상세 화면이 열립니다.
- 상세 화면에서 현재 최고가보다 높은 가격으로 입찰합니다.
- 본인이 등록한 상품에는 입찰할 수 없습니다.
- 첫 입찰 후 20초 동안 추가 입찰이 없으면 자동 낙찰됩니다.
- 새 입찰이 생길 때마다 20초가 다시 시작됩니다.

5. 폴더 구조
- src/auction/model : Product, Bid, User 등 모델 클래스
- src/auction/file : CSV, 이미지, 잠금 파일 관리
- src/auction/service : 경매 규칙과 입찰/낙찰 처리
- src/auction/gui : Swing 화면
- data : products.csv, bids.csv, users.csv
- images : 등록할 때 복사된 상품 이미지
- out : 컴파일 결과 (compile.bat 실행 시 생성)

6. 주의 사항
- 프로그램은 반드시 AuctionProject 폴더를 현재 폴더로 하여 실행해야 합니다.
  run.bat을 사용하면 자동으로 처리됩니다.
- CSV는 UTF-8 형식입니다. 실행 중 직접 수정하지 마세요.
- 비정상 종료로 30초 이상 된 auction.lock이 남으면 다음 실행 때 삭제 여부를 묻습니다.
- 입찰이 한 번도 없는 상품은 기획서 조건에 따라 계속 OPEN 상태를 유지합니다.
