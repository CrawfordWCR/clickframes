package ${techspec.packageName}.dao.jpa;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.Length;

import ${techspec.packageName}.entity.UploadedFile;

/**
 * Represents an uploaded file
 */
@Entity
@Table(name="uploadedFile")
public class JpaUploadedFile implements Cloneable, UploadedFile {
    private String contentType;
    private String fileName;

    /**
     * Unique identifier of this activity.
     */
    @Id
    @Column(name = "uploadedFile_id")
    private String id;

    @Column(name = "data", length=100000000)
    private byte[] data;

    @Override
    public UploadedFile clone() {
        try {
            return (UploadedFile) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Developer exception", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see demo.issuetracker.dao.jpa.UploadedFile#getContentType()
     */
    public String getContentType() {
        return contentType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see demo.issuetracker.dao.jpa.UploadedFile#getFileName()
     */
    public String getFileName() {
        return fileName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see demo.issuetracker.dao.jpa.UploadedFile#getId()
     */
    public String getId() {
        return id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * demo.issuetracker.dao.jpa.UploadedFile#setContentType(java.lang.String)
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setData(InputStream input) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            IOUtils.copy(input, bos);
            this.data = bos.toByteArray();
            // this.data = Hibernate.createBlob(stream);
        } catch (IOException ex) {
            throw new RuntimeException("Error while converting uploaded file to blob", ex);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see demo.issuetracker.dao.jpa.UploadedFile#setFileName(java.lang.String)
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see demo.issuetracker.dao.jpa.UploadedFile#setId(java.lang.String)
     */
    public void setId(String id) {
        this.id = id;
    }

    public static UploadedFile newInstance() {
        UploadedFile uploadedFile = new JpaUploadedFile();
        uploadedFile.setId(UUID.randomUUID().toString());
        return uploadedFile;
    }

    /*
     * (non-Javadoc)
     * 
     * @see demo.issuetracker.dao.jpa.UploadedFile#update(java.lang.String,
     * java.lang.String, java.io.InputStream)
     */
    public void update(String fileName, String contentType, InputStream inputStream) {
        if (StringUtils.isNotBlank(fileName)) {
            setFileName(fileName);
            setContentType(contentType);
            setData(inputStream);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see demo.issuetracker.dao.jpa.UploadedFile#getInputStream()
     */
    @Transient
    public InputStream getInputStream() {
        if (this.data == null) {
            return null;
        }

        // try {
        // return this.blob.getBinaryStream();
        return new ByteArrayInputStream(data);
        // } catch (Exception e) {
        // throw new
        // RuntimeException("Error while reading data from UploadedFile: " +
        // this.getId(), e);
        // }
    }

    /*
     * (non-Javadoc)
     * 
     * @see demo.issuetracker.dao.jpa.UploadedFile#getLength()
     */
    // @Transient
    // public Long getLength() {
    // if (this.blob == null) {
    // return null;
    // }
    //
    // try {
    // return this.blob.length();
    // } catch (SQLException e) {
    // throw new
    // RuntimeException("Error while reading data length from UploadedFile: " +
    // this.getId(), e);
    // }
    // }
    @Transient
    public Long getLength() {
        if (this.data == null) {
            return null;
        }

        // try {
        return new Long(this.data.length);
        // } catch (SQLException e) {
        // throw new
        // RuntimeException("Error while reading data length from UploadedFile: "
        // + this.getId(), e);
        // }
    }

    @Override
    public boolean isValid() {
        return !StringUtils.isBlank(this.fileName);
    }
}