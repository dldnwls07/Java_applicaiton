package auction.model;

/** 판매자 역할을 나타내는 User의 하위 클래스다. */
public class Seller extends User {
    public Seller(int userId, String userName) { super(userId, userName); }

    @Override
    public void showRole() { System.out.println("판매자입니다."); }
}
