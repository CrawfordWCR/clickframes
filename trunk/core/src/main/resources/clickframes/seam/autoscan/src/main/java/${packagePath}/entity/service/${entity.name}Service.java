package ${techspec.packageName}.entity.service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import ${techspec.packageName}.dao.${entity.name}Dao;
import ${techspec.packageName}.entity.${entity.name};

@Component
public class ${entity.name}Service {
    private Log log = LogFactory.getLog(this.getClass());

    @Resource(name = "${entity.id}Dao")
    private ${entity.name}Dao ${entity.id}Dao;

    private static ${entity.name}Service instance;

    @PostConstruct
    public void init() {
        // initialize with some initial data
        // ${entity.name} ${entity.id} = ${entity.id}Dao.newInstance();
        
#foreach ($property in $entity.properties)
        // ${entity.id}.set${property.name}("$!property.positiveValue");
#end
        // ${entity.id}Dao.create(${entity.id});

        instance = this;
    }

    public ${entity.name}Dao get${entity.name}Dao() {
        return this.${entity.id}Dao;
    }
    
    public static ${entity.name}Service getInstance() {
        return instance;
    }
}