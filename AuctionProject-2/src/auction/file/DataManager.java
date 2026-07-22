package auction.file;

import auction.model.Bid;
import auction.model.Product;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

/**
 * 경매 데이터 파일(data/products.csv, data/bids.csv) 및 상품 이미지 파일(images/) 입출력을 관장하는 파일 데이터 관리 클래스다.
 * 메모리 내의 products, bids 리스트와 디스크 저장소 파일 사이의 읽기/쓰기를 수행한다.
 */
public class DataManager {
    // [상수] PRODUCT_HEADER: products.csv 파일의 첫 번째 줄(컬럼 헤더)에 작성되는 표준 컬럼명 문자열
    public static final String PRODUCT_HEADER =
            "productId,sellerName,title,description,startPrice,currentPrice,currentBidder,imagePath,auctionStartTime,auctionEndTime,lastBidTime,status";

    // [상수] BID_HEADER: bids.csv 파일의 첫 번째 줄(컬럼 헤더)에 작성되는 표준 컬럼명 문자열
    public static final String BID_HEADER =
            "bidId,productId,bidderName,bidPrice,bidTime";

    // [필드] productFile: "data/products.csv" 경로를 가리키는 자바 File 객체
    private final File productFile;

    // [필드] bidFile: "data/bids.csv" 경로를 가리키는 자바 File 객체
    private final File bidFile;

    // [필드] imageFolder: 상품 이미지가 복사되어 보관될 "images" 폴더 경로를 가리키는 자바 File 객체
    private final File imageFolder;

    // [필드] products: 데이터 파일에서 읽어온 경매 상품(Product) 객체들을 메모리에 보관하는 리스트
    private final ArrayList<Product> products;

    // [필드] bids: 데이터 파일에서 읽어온 입찰 내역(Bid) 객체들을 메모리에 보관하는 리스트
    private final ArrayList<Bid> bids;

    // [필드] lastErrorMessage: 파일 입출력 중 오류 발생 시 사용자에게 보여 줄 에러 메시지 텍스트
    private String lastErrorMessage;

    /**
     * DataManager 인스턴스를 생성하고 내부 저장소 필드들을 기본 경로로 초기화한다.
     */
    public DataManager() {
        // 상품 정보 CSV 파일 경로 지정 ("data/products.csv")
        productFile = new File("data/products.csv");
        // 입찰 기록 CSV 파일 경로 지정 ("data/bids.csv")
        bidFile = new File("data/bids.csv");
        // 이미지 저장용 폴더 경로 지정 ("images")
        imageFolder = new File("images");
        // 메모리 상의 상품 리스트 객체 생성
        products = new ArrayList<Product>();
        // 메모리 상의 입찰 기록 리스트 객체 생성
        bids = new ArrayList<Bid>();
        // 오류 메시지 초기화
        lastErrorMessage = "";
    }

    /**
     * 데이터 폴더(data), 이미지 폴더(images)의 존재 여부를 검사하여 생성하고, 
     * 빈 products.csv 및 bids.csv 파일(헤더 포함)을 준비한다.
     * 
     * @return 파일 및 폴더 생성 완료 시 true, 실패 시 false 반환
     */
    public boolean initializeFiles() {
        // "data" 폴더를 가리키는 File 객체 생성
        File dataFolder = new File("data");

        // data 폴더가 존재하지 않는 경우 폴더 생성
        if (!dataFolder.exists()) {
            dataFolder.mkdirs(); // 디렉터리(및 하위 디렉터리) 생성
        }

        // images 폴더가 존재하지 않는 경우 폴더 생성
        if (!imageFolder.exists()) {
            imageFolder.mkdirs();
        }

        // products.csv 파일이 없으면 헤더를 포함하여 새로 만든다.
        if (!createCsvFile(productFile, PRODUCT_HEADER)) {
            return false; // 파일 생성 실패 시 false 반환
        }

        // bids.csv 파일이 없으면 헤더를 포함하여 새로 만든다.
        if (!createCsvFile(bidFile, BID_HEADER)) {
            return false; // 파일 생성 실패 시 false 반환
        }

        // 정상적으로 초기화되었으므로 에러 메시지를 비운다.
        lastErrorMessage = "";
        return true;
    }

    /**
     * 파일이 존재하지 않는 경우 신규 CSV 파일을 만들고 지정된 헤더(header) 줄을 기록한다.
     * UTF-8 인코딩을 사용하여 한글 깨짐을 방지한다.
     * 
     * @param file   생성할 파일 객체
     * @param header 첫 줄에 기록할 헤더 문자열
     * @return 성공 여부 (true/false)
     */
    private boolean createCsvFile(File file, String header) {
        try {
            // 파일이 물리적으로 존재하지 않을 경우에만 생성 작업을 진행한다.
            if (!file.exists()) {
                // UTF-8 인코딩으로 파일 쓰기 스트림을 개설한다.
                BufferedWriter writer = new BufferedWriter(
                        new FileWriter(file, StandardCharsets.UTF_8)
                );
                // 첫 행에 컬럼 헤더 텍스트를 작성한다.
                writer.write(header);
                // 줄바꿈 문자를 추가한다.
                writer.newLine();
                // 쓰기 스트림을 닫아 변경 사항을 저장한다.
                writer.close();
            }

            return true;
        } catch (Exception e) {
            // 파일 작성 도중 에러가 발생한 경우 에러 메시지 설정 후 false 반환
            lastErrorMessage = "CSV 파일을 만들지 못했습니다.";
            return false;
        }
    }

    /**
     * products.csv 파일에서 데이터를 한 줄씩 읽어와 Product 객체로 변환한 뒤 products 리스트를 채운다.
     * 
     * @return 로드 성공 시 true, 에러 발생 시 false
     */
    public boolean loadProducts() {
        // 기존 메모리 상의 상품 리스트를 깨끗이 비운다.
        products.clear();

        try {
            // UTF-8 인코딩으로 products.csv 읽기 스트림을 개설한다.
            BufferedReader reader = new BufferedReader(
                    new FileReader(productFile, StandardCharsets.UTF_8)
            );
            String line;
            boolean firstLine = true; // 첫 줄(헤더)인지 구분하기 위한 플래그 변수

            // 파일 끝까지 한 줄씩 반복해서 읽는다.
            while ((line = reader.readLine()) != null) {
                // 첫 번째 줄은 컬럼 헤더이므로 건너뛴다.
                if (firstLine) {
                    firstLine = false; // 플래그 변경
                    continue;
                }

                // 빈 줄(공백 포함)인 경우 다음 줄로 건너뛴다.
                if (line.trim().isEmpty()) {
                    continue;
                }

                // 읽어온 CSV 한 줄 문자열을 Product 객체로 복원한다.
                Product product = Product.fromCsvString(line);

                // 파싱에 성공하여 Product 객체가 제대로 생성되었으면 리스트에 추가한다.
                if (product != null) {
                    products.add(product);
                }
            }

            // 파일 읽기 스트림 자원을 해제한다.
            reader.close();
            // 성공 시 에러 메시지 초기화
            lastErrorMessage = "";
            return true;
        } catch (Exception e) {
            // 입출력 오류 발생 시 에러 메시지 작성 후 false 반환
            lastErrorMessage = "상품 파일을 읽지 못했습니다.";
            return false;
        }
    }

    /**
     * 메모리의 products 리스트에 보관된 모든 상품 정보를 products.csv 파일에 덮어써서 저장한다.
     * 
     * @return 저장 성공 시 true, 실패 시 false
     */
    public boolean saveProducts() {
        try {
            // UTF-8 인코딩으로 products.csv 쓰기 스트림을 개설한다. (덮어쓰기)
            BufferedWriter writer = new BufferedWriter(
                    new FileWriter(productFile, StandardCharsets.UTF_8)
            );
            // 첫 번째 줄에 표준 컬럼 헤더를 작성한다.
            writer.write(PRODUCT_HEADER);
            writer.newLine();

            // products 리스트에 저장된 모든 상품 객체를 순회하며 CSV 행으로 변환해 저장한다.
            for (Product product : products) {
                writer.write(product.toCsvString()); // CSV 형식 문자열 출력
                writer.newLine(); // 행 구분 줄바꿈
            }

            // 스트림 닫기
            writer.close();
            lastErrorMessage = "";
            return true;
        } catch (Exception e) {
            lastErrorMessage = "상품 파일을 저장하지 못했습니다.";
            return false;
        }
    }

    /**
     * bids.csv 파일에서 데이터를 한 줄씩 읽어와 Bid 객체로 파싱 후 bids 리스트에 채운다.
     * 
     * @return 읽기 성공 시 true, 에러 발생 시 false
     */
    public boolean loadBids() {
        // 메모리의 기존 입찰 목록을 비운다.
        bids.clear();

        try {
            // UTF-8 읽기 스트림 개설
            BufferedReader reader = new BufferedReader(
                    new FileReader(bidFile, StandardCharsets.UTF_8)
            );
            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                // 첫 줄(헤더) 건너뛰기
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                // 공백 라인 건너뛰기
                if (line.trim().isEmpty()) {
                    continue;
                }

                // 읽어온 줄을 Bid 객체로 변환
                Bid bid = Bid.fromCsvString(line);

                // 파싱 성공 시 리스트에 추가
                if (bid != null) {
                    bids.add(bid);
                }
            }

            reader.close();
            lastErrorMessage = "";
            return true;
        } catch (Exception e) {
            lastErrorMessage = "입찰 파일을 읽지 못했습니다.";
            return false;
        }
    }

    /**
     * 메모리의 bids 리스트 전체 내역을 bids.csv 파일에 덮어써서 최신 상태로 저장한다.
     * 
     * @return 저장 성공 시 true, 실패 시 false
     */
    public boolean saveBids() {
        try {
            // UTF-8 쓰기 스트림 개설
            BufferedWriter writer = new BufferedWriter(
                    new FileWriter(bidFile, StandardCharsets.UTF_8)
            );
            // 헤더 작성
            writer.write(BID_HEADER);
            writer.newLine();

            // 입찰 목록 전체를 순회하여 파일에 기록
            for (Bid bid : bids) {
                writer.write(bid.toCsvString());
                writer.newLine();
            }

            writer.close();
            lastErrorMessage = "";
            return true;
        } catch (Exception e) {
            lastErrorMessage = "입찰 파일을 저장하지 못했습니다.";
            return false;
        }
    }

    /**
     * 사용자가 선택한 원본 이미지 파일(source)을 "images" 폴더에 "product_{productId}.확장자" 규칙으로 복사하고
     * 저장된 상대 경로 문자열을 반환한다.
     * 
     * @param source    사용자가 컴퓨터에서 선택한 원본 파일 객체
     * @param productId 복사될 파일 이름에 붙일 상품 고유 ID
     * @return 복사 성공 시 "images/product_X.png" 형태 경로 반환, 실패 시 null
     */
    public String copyImage(File source, int productId) {
        try {
            // 원본 파일의 이름 추출 (예: "my_photo.PNG")
            String originalName = source.getName();
            // 확장자 구분을 위해 마지막 마침표(.) 위치를 찾는다.
            int dotPosition = originalName.lastIndexOf('.');
            String extension = ".img"; // 기본 확장자 설정

            // 마침표가 존재하는 경우 해당 확장자 추출 (소문자로 정렬)
            if (dotPosition >= 0) {
                extension = originalName.substring(dotPosition).toLowerCase();
            }

            // 고유 상품 ID를 포함한 저장용 신규 파일명 생성 (예: "product_3.jpg")
            String fileName = "product_" + productId + extension;
            // images 폴더 내의 최종 목적지 파일 객체 지정
            File destination = new File(imageFolder, fileName);

            // 파일 복사 실행 (동일한 파일명이 있을 경우 덮어쓰기 지정)
            Files.copy(
                    source.toPath(),
                    destination.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
            );

            lastErrorMessage = "";
            // 프로젝트 상대 경로 문자열 반환
            return "images/" + fileName;
        } catch (Exception e) {
            lastErrorMessage = "이미지를 복사하지 못했습니다.";
            return null;
        }
    }

    /**
     * 새 상품 등록 시 부여할 다음 번 상품 ID 번호를 계산한다. (기존 최고 ID + 1)
     * 
     * @return 생성될 신규 상품 ID 정수값
     */
    public int nextProductId() {
        int maxId = 0; // 최댓값 저장 변수

        // 현재 메모리에 로드된 상품들을 순회하며 가장 큰 productId를 찾는다.
        for (Product product : products) {
            if (product.getProductId() > maxId) {
                maxId = product.getProductId();
            }
        }

        // 가장 큰 ID보다 1 큰 값을 다음 ID로 반환
        return maxId + 1;
    }

    /**
     * 새 입찰 등록 시 부여할 다음 번 입찰 ID 번호를 계산한다. (기존 최고 입찰 ID + 1)
     * 
     * @return 생성될 신규 입찰 ID 정수값
     */
    public int nextBidId() {
        int maxId = 0;

        // 입찰 목록을 탐색하며 가장 큰 bidId를 찾는다.
        for (Bid bid : bids) {
            if (bid.getBidId() > maxId) {
                maxId = bid.getBidId();
            }
        }

        // 가장 큰 ID보다 1 큰 값을 다음 입찰 ID로 반환
        return maxId + 1;
    }

    /**
     * 지정한 상품 ID(productId)에 해당하는 Product 객체를 메모리 리스트에서 검색하여 반환한다.
     * 
     * @param productId 찾으려는 상품 ID
     * @return 찾은 Product 객체 (없을 경우 null)
     */
    public Product findProduct(int productId) {
        for (Product product : products) {
            if (product.getProductId() == productId) {
                return product; // 해당 상품 반환
            }
        }

        return null; // 찾지 못한 경우 null
    }

    /**
     * 특정 상품 ID(productId)에 등록된 모든 입찰 기록 목록을 검색하여 리스트로 반환한다.
     * 
     * @param productId 입찰 기록을 조회할 대상 상품 ID
     * @return 해당 상품의 Bid 객체들을 담은 ArrayList
     */
    public ArrayList<Bid> findBids(int productId) {
        ArrayList<Bid> result = new ArrayList<Bid>();

        // 입찰 목록 중 대상 productId와 일치하는 기록만 골라 결과 리스트에 담는다.
        for (Bid bid : bids) {
            if (bid.getProductId() == productId) {
                result.add(bid);
            }
        }

        return result;
    }

    /**
     * 신규 등록할 Product 객체를 메모리 내 products 리스트에 추가한다.
     * 
     * @param product 추가할 상품 객체
     */
    public void addProduct(Product product) {
        products.add(product);
    }

    /**
     * 신규 입찰 Bid 객체를 메모리 내 bids 리스트에 추가한다.
     * 
     * @param bid 추가할 입찰 객체
     */
    public void addBid(Bid bid) {
        bids.add(bid);
    }

    /**
     * 메모리에 저장된 상품 목록 전체의 얕은 사본(새 ArrayList)을 반환한다.
     * 외부에서 원본 리스트 훼손을 방지하기 위함이다.
     * 
     * @return Product 객체들이 담긴 ArrayList
     */
    public ArrayList<Product> getProducts() {
        return new ArrayList<Product>(products);
    }

    /**
     * 파일 입출력 및 처리 중 가장 최근에 발생한 에러 메시지 텍스트를 반환한다.
     * 
     * @return 에러 메시지 문자열 (에러가 없으면 빈 문자열 "")
     */
    public String getLastErrorMessage() {
        return lastErrorMessage;
    }
}
