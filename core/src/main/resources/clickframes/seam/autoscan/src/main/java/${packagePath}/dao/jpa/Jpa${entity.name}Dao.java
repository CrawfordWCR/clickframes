package ${techspec.packageName}.dao.jpa;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.persistence.Query;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import ${techspec.packageName}.dao.${entity.name}Dao;
import ${techspec.packageName}.entity.${entity.name};

@Component("${entity.id}Dao")
@Repository
@Transactional
public class Jpa${entity.name}Dao extends AbstractJpaDao<${entity.name}> implements ${entity.name}Dao {
    @Override
    public void create(${entity.name} ${entity.id}) {
        Object pk = ${entity.id}.primaryKey();
        if (pk == null) {
            pk = generatePrimaryKey();
            
#if (${entity.primaryKey})
            ${entity.id}.set${entity.primaryKey.name}((${context.get(${entity.primaryKey}).javaType}) pk);
#end
        }

        em.persist(${entity.id});
    }

    @PostConstruct
    public void init() {
        // initialize the db with some initial data
    }

    /**
     * The "Create" or "Update" semantics of "CRUD"
     */
    public void createOrUpdate(${entity.name} ${entity.id}) {
        if (${entity.id}.primaryKey() == null) {
            create(${entity.id});
            return;
        }

        em.merge(${entity.id});
    }

#if (${entity.primaryKey})
    /**
     * Find by Primary key
     * 
     * The "read" operation of CRUD
     */
    public $entity.name findByPrimaryKey(String key) {
        if (key == null) {
            return null;
        }
        return em.find(Jpa${entity.name}.class, key);
    }
#end

#if (${entity.loginEntity})
    public ${entity.name} findByUsernameAndPassword(String username, String password) {
        Query qry = em.createQuery("from Jpa${entity.name} user where user.${appspec.loginUsernameEntityProperty.id}=:username and user.${appspec.loginPasswordEntityProperty.id}=:password");
        qry.setParameter("username", username);
        qry.setParameter("password", Jpa${entity.name}.encrypt(password));
        @SuppressWarnings("unchecked")
        List<Jpa${entity.name}> userList = qry.getResultList();

        if (userList.size() == 0) {
            return null;
        }

        if (userList.size() == 1) {
            return userList.get(0);
        }

        throw new RuntimeException(
                "More than one user found for this username and password. Please contact the administrator. (username="
                        + username + ")");    
    }
#end

    @Override
    public void delete(${entity.name} ${entity.id}) {
        em.remove(${entity.id});
    }

    @Override
    public void update(${entity.name} ${entity.id}) {
        em.merge(${entity.id});
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
        Query qry = em.createQuery("from Jpa${entity.name} ${entity.id} " /* where ${entity.id}.field1=:param1 and ${entity.id}.field2=:param2 */);
        // qry.setParameter("param1", param1);
        // qry.setParameter("param2", param2);
        @SuppressWarnings("unchecked")
        List<Jpa${entity.name}> ${entity.id}List = qry.getResultList();

        return new ArrayList<${entity.name}>(${entity.id}List);
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
        Query qry = em.createQuery("from Jpa${entity.name} ${entity.id} " /* where ${entity.id}.field1=:param1 and ${entity.id}.field2=:param2 */);
        // qry.setParameter("param1", param1);
        // qry.setParameter("param2", param2);
        @SuppressWarnings("unchecked")
        List<Jpa${entity.name}> ${entity.id}List = qry.getResultList();

        if (${entity.id}List.size() == 0) {
            return null;
        }

        if (${entity.id}List.size() == 1) {
            return ${entity.id}List.get(0);
        }

        throw new RuntimeException(
                "More than one ${entity.name} found. Please contact the administrator.");    
    }
#end

    @Override
    public ${entity.name} newInstance() {
        ${entity.name} ${entity.id} = new Jpa${entity.name}();
        
#foreach ($property in $entity.fileProperties)
        ${entity.id}.set${property.name}(JpaUploadedFileDao.newInstance());
#end
        return ${entity.id};
    }

    private Object generatePrimaryKey() {
        return UUID.randomUUID().toString();
    }
    
    public <K> List<${entity.name}> findBy(String name, K value) {
        return queryBySingleArgument("from Jpa${entity.name} ${entity.id} where ${entity.id}." + name + " = :" + name, name, value);
    }
}