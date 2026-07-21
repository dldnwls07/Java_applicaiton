package auction.file;

/** 파일을 읽고 저장하는 클래스가 공통으로 구현할 기능이다. */
public interface FileManager {
    void load() throws Exception;
    void save() throws Exception;
}
