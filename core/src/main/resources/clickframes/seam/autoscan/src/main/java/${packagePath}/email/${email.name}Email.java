#parse('/clickframes/seam/includes/getterSetter.vm')

package ${techspec.packageName}.email;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

#foreach ($output in $email.outputs)
import ${techspec.packageName}.entity.${output.entity.name};
#end

/**
 * ${email.description}
 */
@Name("${email.id}Email")
@Scope(ScopeType.CONVERSATION)
public class ${email.name}Email extends AbstractEmail {
#foreach ($output in $email.outputs)
#getterSetter("${output.entity.name}" $output.id $output.name $output.title $output.description)

#end
}