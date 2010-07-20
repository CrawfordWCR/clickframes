package ${techspec.packageName}.dao.inmemory;

#if ($entity.referringOutputLists)
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
#end
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import ${techspec.packageName}.dao.${entity.name}Dao;
import ${techspec.packageName}.entity.${entity.name};

@Component("${entity.id}Dao")
public class InMemory${entity.name}Dao implements ${entity.name}Dao {
    private Map<Object, ${entity.name}> primaryIndex = new LinkedHashMap<Object, ${entity.name}>();

    @Override
    public void create(${entity.name} ${entity.id}) {
        Object pk = ${entity.id}.primaryKey();
        if (pk == null) {
            pk = generatePrimaryKey();
            
#if (${entity.primaryKey})
            ${entity.id}.set${entity.primaryKey.name}((${context.get(${entity.primaryKey}).javaType}) pk);
#end
        }

        primaryIndex.put(pk, ${entity.id});
    }

    @PostConstruct
    public void init() {
        // initialize the db with some initial data
    }

    /**
     * The "Create" or "Update" semantics of "CRUD"
     */
    public void createOrUpdate(${entity.name} ${entity.id}) {
        Object pk = ${entity.id}.primaryKey();
        if (pk == null || !primaryIndex.containsKey(pk)) {
            create(${entity.id});
        }
        else {
            update(${entity.id});
        }
    }

#if (${entity.primaryKey})
    /**
     * Find by Primary key
     * 
     * The "read" operation of CRUD
     */
    public $entity.name findByPrimaryKey(String key) {
        return primaryIndex.get(key);
    }
#end

#if (${entity.loginEntity})
    public ${entity.name} findByUsernameAndPassword(String username, String password) {
        for (${entity.name} ${entity.id} : primaryIndex.values()) {
            if (StringUtils.equals(${entity.id}.get${appspec.loginUsernameEntityProperty.name}(), username) 
                    && StringUtils.equals(${entity.id}.get${appspec.loginPasswordEntityProperty.name}(), password)) {
                return ${entity.id};
            }
        }
        
        return null;
    }
#end

    @Override
    public void delete(${entity.name} ${entity.id}) {
        primaryIndex.remove(${entity.id}.primaryKey());
    }

    @Override
    public void update(${entity.name} ${entity.id}) {
        primaryIndex.put(${entity.id}.primaryKey(), ${entity.id});
    }

#foreach ($outputList in $entity.referringOutputLists)

    /**
     * $outputList.title. $outputList.description
     * 
#foreach ($fact in $outputList.facts)
     * $fact.text
     * 
#end
     * The "read" operation of CRUD
     */
    public List<${outputList.entity.name}> find${outputList.name}(Map<String, Object> params) {
        // TODO: implement your code here
        return new ArrayList<${outputList.entity.name}>(primaryIndex.values());
    }
#end
#foreach ($output in $entity.referringOutputs)
    
    /**
     * $output.title. $output.description
     * 
    #foreach ($fact in $output.facts)
     * $fact.text
     * 
    #end
     * The "read" operation of CRUD
     */
    public ${output.entity.name} get${output.name}(Map<String, Object> params) {
        // TODO: implement your code here, by default this returns the first one
        if (primaryIndex.keySet().size() > 0) {
            return primaryIndex.get(primaryIndex.keySet().iterator().next());
        }
        
        return null;
    }
#end

    @Override
    public ${entity.name} newInstance() {
        return new InMemory${entity.name}();
    }
    

    /**
     * A scheme for generating new primary keys
     * 
     * @return
     *
     * @author Vineet Manohar
     */
    public Object generatePrimaryKey() {
        return UUID.randomUUID().toString();   
    }
}