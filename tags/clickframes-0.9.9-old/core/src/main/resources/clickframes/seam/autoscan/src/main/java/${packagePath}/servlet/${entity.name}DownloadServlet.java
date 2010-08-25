package ${techspec.packageName}.servlet;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import ${techspec.packageName}.entity.${entity.name};
import ${techspec.packageName}.entity.UploadedFile;
import ${techspec.packageName}.entity.service.${entity.name}Service;

/**
 * Servlet to download Uploaded from ${entity.name}
 */
@Component("${entity.id}DownloadServlet")
public class ${entity.name}DownloadServlet extends DownloadServlet {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private final Log logger = LogFactory.getLog(getClass());

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String ${entity.primaryKey.id} = request.getParameter("${entity.primaryKey.id}");

        final ${entity.name} ${entity.id} = ${entity.name}Service.getInstance().get${entity.name}Dao().findByPrimaryKey(${entity.primaryKey.id});

#if ($entity.fileProperties.size() == 0)
        // TODO: implement retrieval of uploaded file
        UploadedFile uploadedFile = null;
#elseif (${entity.fileProperties.size()} > 1)
        String type = request.getParameter("type");
        UploadedFile uploadedFile = getUploadedFileForType(${entity.id}, type);
#else
        UploadedFile uploadedFile = ${entity.id}.get${entity.firstFileProperty.name}();
#end
        streamUploadedFile(response, uploadedFile);
    }

#if (${entity.fileProperties.size()} > 1)
    protected UploadedFile getUploadedFileForType(${entity.name} ${entity.id}, String type) {
        if (type != null) {
#foreach ($property in $entity.fileProperties)
            if (type.equals("${property.id}")) {
                return ${entity.id}.get${property.name}();
            }
#end
        }

#if ($entity.firstFileProperty)
        return ${entity.id}.get${entity.firstFileProperty.name}();
#else
        throw new RuntimeException("No file property available on this entity");
#end
    }
#end ## if more than file properties persent
}