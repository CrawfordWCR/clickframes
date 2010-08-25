package ${techspec.packageName}.entity;

import java.io.InputStream;

public interface UploadedFile {
    public abstract String getContentType();

    public abstract String getFileName();

    public abstract String getId();

    public abstract InputStream getInputStream();

    public abstract Long getLength();

    public abstract void setContentType(String contentType);

    public abstract void setFileName(String fileName);

    public abstract void setId(String id);

    public abstract void update(String fileName, String contentType, InputStream inputStream);
    
    public boolean isValid();
}