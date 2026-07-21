package auction.model;

/** 교내 경매 프로그램 사용자의 공통 정보를 표현한다. */
public abstract class User {
    protected int userId;
    protected String userName;

    public User(int userId, String userName) {
        this.userId = userId;
        this.userName = userName;
    }

    public int getUserId() { return userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public abstract void showRole();
}
