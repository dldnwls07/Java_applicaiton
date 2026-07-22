package auction.model;

/**
 * 교내 경매 프로그램 사용자의 공통 정보 및 동작을 정의하는 추상 클래스다.
 * 입찰자(Bidder) 및 판매자(Seller) 클래스의 부모 클래스로 사용된다.
 */
public abstract class User {
    // [필드] userId: 사용자를 유일하게 식별하는 고유 ID 번호 (예: 1, 2, 3...)
    protected int userId;

    // [필드] userName: 화면 및 경매 기록에 표시될 사용자의 이름 (예: "홍길동")
    protected String userName;

    /**
     * User 객체를 생성하는 생성자 함수다.
     * 
     * @param userId   사용자 고유 식별 번호 (this.userId 필드에 저장됨)
     * @param userName 사용자 이름 문자열 (this.userName 필드에 저장됨)
     */
    public User(int userId, String userName) {
        // 전달받은 userId 매개변수 값을 클래스의 protected userId 필드에 할당한다.
        this.userId = userId;
        // 전달받은 userName 매개변수 값을 클래스의 protected userName 필드에 할당한다.
        this.userName = userName;
    }

    /**
     * 사용자의 고유 ID 번호를 반환한다.
     * 
     * @return int 타입의 사용자 고유 식별 ID (userId)
     */
    public int getUserId() { 
        // 저장된 사용자 식별 번호를 호출자에게 반환한다.
        return userId; 
    }

    /**
     * 사용자의 이름을 반환한다.
     * 
     * @return String 타입의 사용자 이름 (userName)
     */
    public String getUserName() { 
        // 저장된 사용자 이름을 호출자에게 반환한다.
        return userName; 
    }

    /**
     * 사용자의 이름을 새로운 값으로 변경한다.
     * 
     * @param userName 새롭게 변경할 사용자 이름 문자열
     */
    public void setUserName(String userName) { 
        // 전달받은 새로운 사용자 이름(userName)을 클래스의 userName 필드에 덮어쓴다.
        this.userName = userName; 
    }

    /**
     * 사용자의 역할(입찰자/판매자 등)을 콘솔 또는 화면에 출력하는 추상 메서드다.
     * 자식 클래스(Bidder, Seller)에서 각 역할에 맞게 반드시 재정의(Override)해야 한다.
     */
    public abstract void showRole();
}
