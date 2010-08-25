##Spring MVC Service Template
package ${servicePackage};
#foreach ($import in $formService.imports)
import ${import};
#end
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
@Service
public class DynamicFormService{
	@SuppressWarnings("unused")
	private final Log logger = LogFactory.getLog(getClass());
#foreach ($field in $formService.fields)
	${field.annotation}
	private ${field.type} ${field.name};
#end
#foreach($method in $formService.methods)
	${method.visibility} ${method.returnType} ${method.name}(${method.argsAsString}){
#foreach($line in $method.lines)
		${line}
#end
	}
#end
}
