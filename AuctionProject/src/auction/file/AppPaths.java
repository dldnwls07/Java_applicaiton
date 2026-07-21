package auction.file;

import java.nio.file.Path;
import java.nio.file.Paths;

/** 프로그램이 사용하는 폴더와 파일 경로를 한곳에서 관리한다. */
public final class AppPaths {
    public static final Path ROOT = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
    public static final Path DATA = ROOT.resolve("data");
    public static final Path IMAGES = ROOT.resolve("images");
    public static final Path PRODUCTS = DATA.resolve("products.csv");
    public static final Path BIDS = DATA.resolve("bids.csv");
    public static final Path USERS = DATA.resolve("users.csv");
    public static final Path LOCK = DATA.resolve("auction.lock");

    private AppPaths() { }
}
