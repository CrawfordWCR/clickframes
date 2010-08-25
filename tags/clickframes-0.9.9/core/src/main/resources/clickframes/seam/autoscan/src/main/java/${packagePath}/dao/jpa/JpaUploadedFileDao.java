package ${techspec.packageName}.dao.jpa;

import java.util.UUID;

public class JpaUploadedFileDao {
    public static ${techspec.packageName}.entity.UploadedFile newInstance() {
        JpaUploadedFile jpaUploadedFile = new JpaUploadedFile();
        jpaUploadedFile.setId(UUID.randomUUID().toString());
        return jpaUploadedFile;
    }
}
