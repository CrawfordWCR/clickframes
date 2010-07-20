package ${techspec.packageName}.email;

import java.util.Map;

import org.apache.commons.beanutils.BeanMap;
import org.jboss.seam.annotations.In;
import org.jboss.seam.faces.Renderer;

import ${techspec.packageName}.controller.EmailController;

/**
 * Base class for all emails
 */
public abstract class AbstractEmail {
    @In(create = true)
    protected Renderer renderer;

    @In
    protected EmailController emailController;

    public void sendMail() {
        String className = this.getClass().getSimpleName();
        String templateName = className.replace(className.charAt(0), Character.toLowerCase(className.charAt(0)))
                .replaceAll("Email$", "");

        Map<String, Object> params = params();

        emailController.sendMessage(500, renderer, params, "/WEB-INF/facelets/email/" + templateName + ".xhtml");
    }

    /**
     * By default, this will be computed by using bean reflection. Child classes
     * simply need to define properties with getters and setters. They may
     * optionally override this method for further customization
     * 
     * @return all params needed by email
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> params() {
        return new BeanMap(this);
    }
}