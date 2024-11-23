import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.servlet.http.Part;
import java.util.UUID;

public class FilePathHandler {
    private static final String BASE_UPLOAD_DIR = "assets/img";
    
    public static String saveImage(Part filePart, String applicationPath) throws IOException {
        if (filePart == null || filePart.getSize() <= 0) {
            return null;
        }

        // Tạo tên file ngẫu nhiên để tránh trùng lặp
        String originalFileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String randomFileName = UUID.randomUUID().toString() + fileExtension;

        // Tạo đường dẫn tương đối cho việc lưu vào database
        String relativePathForDB = BASE_UPLOAD_DIR + "/" + randomFileName;

        // Tạo đường dẫn tuyệt đối cho việc lưu file
        String absolutePath = applicationPath + File.separator + BASE_UPLOAD_DIR;
        File uploadDir = new File(absolutePath);
        
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // Chuẩn hóa đường dẫn cho mọi hệ điều hành
        Path fullPath = Paths.get(absolutePath, randomFileName);
        
        // Lưu file
        filePart.write(fullPath.toString());

        // Trả về đường dẫn tương đối để lưu vào database
        return relativePathForDB;
    }

    public static String getAbsoluteImagePath(String relativeImagePath, String applicationPath) {
        if (relativeImagePath == null || relativeImagePath.isEmpty()) {
            return null;
        }
        return Paths.get(applicationPath, relativeImagePath).toString();
    }
}