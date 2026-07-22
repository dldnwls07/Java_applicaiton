package auction.model;

/** 입찰자 역할을 나타내는 User의 하위 클래스다. */
public class Bidder extends User {
    public Bidder(int userId, String userName) { super(userId, userName); }

    @Override
    public void showRole() { System.out.println("입찰자입니다."); }
}
