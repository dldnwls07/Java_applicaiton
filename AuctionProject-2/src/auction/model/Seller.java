package auction.model;

/**
 * 경매에 물품을 등록하고 판매하는 판매자(Seller) 역할을 나타내는 클래스다.
 * 추상 클래스인 User를 상속받아 사용자의 기본 정보(userId, userName)를 보유한다.
 */
public class Seller extends User {

    /**
     * Seller 객체를 생성하는 생성자다.
     * 부모 클래스인 User의 생성자 super(userId, userName)를 호출하여 기본 사용자 정보를 초기화한다.
     * 
     * @param userId   판매자의 고유 식별 번호 (부모 클래스 User의 userId에 전달됨)
     * @param userName 판매자의 이름 문자열 (부모 클래스 User의 userName에 전달됨)
     */
    public Seller(int userId, String userName) { 
        // 부모 클래스(User)의 생성자를 호출하여 userId와 userName 필드를 설정한다.
        super(userId, userName); 
    }

    /**
     * 부모 클래스 User의 추상 메서드 showRole()을 재정의한다.
     * 현재 사용자의 역할이 "판매자"임을 표준 콘솔 출력으로 표시한다.
     */
    @Override
    public void showRole() { 
        // 콘솔 창에 "판매자입니다." 문구를 출력한다.
        System.out.println("판매자입니다."); 
    }
}
