package auction.service;

/** 사용자에게 그대로 안내할 수 있는 경매 처리 오류다. */
public class AuctionException extends Exception {
    public AuctionException(String message) { super(message); }
    public AuctionException(String message, Throwable cause) { super(message, cause); }
}
