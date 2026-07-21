package auction.file;

import java.nio.file.Files;

/** 여러 프로그램의 동시 CSV 수정을 막는 간단한 잠금 파일이다. */
public class AuctionLock implements AutoCloseable {
    private boolean acquired;

    public boolean tryAcquire() throws Exception {
        Files.createDirectories(AppPaths.DATA);
        try {
            Files.createFile(AppPaths.LOCK);
            acquired = true;
            return true;
        } catch (java.nio.file.FileAlreadyExistsException e) {
            return false;
        }
    }

    @Override
    public void close() throws Exception {
        if (acquired) {
            Files.deleteIfExists(AppPaths.LOCK);
            acquired = false;
        }
    }
}
