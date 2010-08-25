package ${techspec.packageName}.servlet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ${techspec.packageName}.entity.UploadedFile;

public class DownloadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private final Log logger = LogFactory.getLog(getClass());

    protected void streamUploadedFile(HttpServletResponse response, UploadedFile file) throws IOException {
        if (file == null) {
            return;
        }

        String fileName = file.getFileName();
        InputStream inputStream;
        inputStream = file.getInputStream();
        streamFile(fileName, inputStream, file.getLength(), file.getContentType(), response);
    }

    /**
     * Streams files. TODO: Change me to stream byte arrays.
     * 
     * @param contentType
     */
    protected void streamFile(String fileName, InputStream inputStream, Long length, String contentType,
            HttpServletResponse response) throws IOException {
        if (inputStream == null) {
            return;
        }

        // Get the absolute path of the image
        ServletContext sc = getServletContext();

        // Get the MIME type of the image
        String mimeType = contentType;

        if (mimeType == null) {
            mimeType = sc.getMimeType(fileName);
        }

        if (mimeType == null) {
            mimeType = "application/octet-stream";
            // response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            // "Could not get MIME type of " + fileName);
            // response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            // return;
        }
        //
        // Set content type
        response.setContentType(mimeType);
        if (length != null) {
            response.setContentLength((int) (long) length);
        }
        if (mimeType == null) {
            response.setContentType("application/x-download");
        }
        // http://kb.mozillazine.org/Filenames_with_spaces_are_truncated_upon_download
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

        // Open the file and output streams
        // FileInputStream in = new FileInputStream(file);
        OutputStream out = response.getOutputStream();

        // Copy the contents of the file to the output stream
        byte[] buf = new byte[1024];
        int count = 0;
        while ((count = inputStream.read(buf)) >= 0) {
            out.write(buf, 0, count);
        }
        inputStream.close();
        out.close();
    }
}