package auction.file;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/** 선택한 상품 이미지를 images 폴더로 복사한다. */
public class ImageFileManager {
    public String copyImage(File source, int productId) throws Exception {
        if (source == null || !source.isFile()) throw new IllegalArgumentException("이미지 파일을 찾을 수 없습니다.");
        String name = source.getName();
        int dot = name.lastIndexOf('.');
        String extension = dot >= 0 ? name.substring(dot).toLowerCase() : ".img";
        String fileName = "product_" + productId + extension;
        Files.createDirectories(AppPaths.IMAGES);
        Files.copy(source.toPath(), AppPaths.IMAGES.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        return "images/" + fileName;
    }
}
