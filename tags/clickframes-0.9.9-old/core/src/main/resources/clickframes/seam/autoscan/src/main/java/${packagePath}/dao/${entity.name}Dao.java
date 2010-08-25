package ${techspec.packageName}.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ${techspec.packageName}.entity.${entity.name};

public interface ${entity.name}Dao {
    /**
     * The "Create" semantics of "CRUD"
     */
    public void create(${entity.name} ${entity.id});

    /**
     * Create a new instance of this object
     */
    public ${entity.name} newInstance();

    /**
     * The "Update" semantics of "CRUD"
     */
    public void update(${entity.name} ${entity.id});
    
#if (${entity.primaryKey})

    /**
     * Find by Primary key
     * 
     * The "read" operation of CRUD
     */
    public $entity.name findByPrimaryKey(String key);
#end

#if (${entity.loginEntity})
    public ${entity.name} findByUsernameAndPassword(String username, String password);
#end

    /**
     * The "Create" or "Update" semantics of "CRUD"
     */
    public void createOrUpdate(${entity.name} ${entity.id});

    /**
     * The "delete" semantics of "CRUD"
     */
    public void delete(${entity.name} ${entity.id});

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
    public List<${outputList.entity.name}> find${outputList.name}(Map<String, Object> params);
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
    public ${output.entity.name} get${output.name}(Map<String, Object> params);
#end

    /**
     * Generic method to search by any property
     * 
     * @param <K>
     * @param name
     * @param value
     * @return
     *
     * @author Vineet Manohar
     */
    public <K> List<${entity.name}> findBy(String name, K value);
}