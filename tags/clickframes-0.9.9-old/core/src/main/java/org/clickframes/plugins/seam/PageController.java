package org.clickframes.plugins.seam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.clickframes.model.Entity;
import org.clickframes.model.Form;
import org.clickframes.model.Page;
import org.clickframes.model.SingleUserInput;
import org.clickframes.plugins.seam.PageEntity.Type;

/**
 * @author Vineet Manohar
 */
public class PageController {
    private static final Log log = LogFactory.getLog(PageController.class);

    public List<PageEntity> pageEntities = new ArrayList<PageEntity>();

    public List<PageEntityList> pageEntityLists = new ArrayList<PageEntityList>();

    private Page page;

    public PageController(Page page) {
        this.page = page;
    }

    public List<PageEntity> getPageEntities() {
        return pageEntities;
    }

    public void setPageEntities(List<PageEntity> pageEntities) {
        this.pageEntities = pageEntities;
    }

    public String getPageLoadActionId() {
        return "processPageParameters";
    }

    /**
     * This methods set the variable names for all page entities in this
     * controller. This contains core business logic determining how the
     * variable will be bound, including heuristics and best practices.
     *
     * @author Vineet Manohar
     */
    public void analyzeAndSetPageEntityVariableNames() {
        // ////// assign ids
        for (PageEntity pageEntity : pageEntities) {
            switch (pageEntity.getType()) {
                case PARAM_PRIMARY_KEY:
                case PARAM_OTHER:
                    // Rule 1) Type.PARAM_PRIMARY_KEY:
                    // 1a) should create an entity with var name ${entity.id}.
                    // Example:
                    // ?projectId=foo
                    pageEntity.setId(pageEntity.getEntity().getId());

                    // 1b) if there are 2 params which map to the same entity,
                    // e.g.
                    // compare.seam?user1Id=foo1&user2Id=foo2
                    // remove the "Id" part and use that as the entity name

                    // TODO: implement case of multiple params, not very common

                    // Rule 2) Type.PARAM_OTHER: same as param_primary_key
                    break;

                case INPUT:
                    // Rule 3) Type.INPUT
                    // Always create entity with var name ${entity.id}.
                    pageEntity.setId(pageEntity.getEntity().getId());
                    break;

                case OUTPUT:
                    // Type.OUTPUT
                    // Rule 4) Create entity name with var ${output.id}
                    pageEntity.setId(pageEntity.getOutput().getId());
                    break;
                default:
                    throw new RuntimeException("Case was added but not implemented, contact developer immediately: "
                            + pageEntity.getType());
            }
        }

        // ////// remove duplicates

        // Rule 1: PARAM supersedes OUTPUT (as PARAM has more info than
        // OUTPUT, namely link to entity primary key)
        {
            List<PageEntity> paramPageEntities = getParamPageEntities();
            // if there is a output page entity with same var name, remove
            // it
            Set<PageEntity> duplicates = new HashSet<PageEntity>();
            for (PageEntity paramPageEntity : paramPageEntities) {
                for (PageEntity duplicateContender : pageEntities) {
                    if (duplicateContender.getType() == Type.OUTPUT
                            && duplicateContender.getId().equals(paramPageEntity.getId())
                            && duplicateContender.getEntity().equals(paramPageEntity.getEntity())) {
                        duplicates.add(duplicateContender);

                        // save info that there is also an associated output
                        paramPageEntity.setOutput(duplicateContender.getOutput());
                    }
                }
            }

            // remove all duplicates
            for (PageEntity duplicate : duplicates) {
                pageEntities.remove(duplicate);
                // log.info("Duplicate removed: " + duplicate.getType() + ", " +
                // duplicate.getId());
            }
        }

        // ///////// Rule 2: INPUT vs PARAM: both are mapped to ${entity.id},
        // however, PARAM should supersede input, as PARAM looks up whereas
        // input only edits/updates it
        {
            List<PageEntity> paramPageEntities = getParamPageEntities();
            // if there is a output page entity with same var name, remove
            // it
            Set<PageEntity> duplicates = new HashSet<PageEntity>();
            for (PageEntity paramPageEntity : paramPageEntities) {
                for (PageEntity duplicateContender : pageEntities) {
                    if (duplicateContender.getType() == Type.INPUT
                            && duplicateContender.getId().equals(paramPageEntity.getId())
                            && duplicateContender.getEntity().equals(paramPageEntity.getEntity())) {
                        duplicates.add(duplicateContender);

                        {
                            // save info that there is also an linked inputs
                            Map<Form, List<SingleUserInput>> inputs = new HashMap<Form, List<SingleUserInput>>();

                            for (Form form : page.getForms()) {
                                Map<Entity, List<SingleUserInput>> entityInputs = form.getEntityInputs();

                                if (entityInputs.containsKey(paramPageEntity.getEntity())) {
                                    for (SingleUserInput linkedInput : form.getEntityInputs().get(
                                            paramPageEntity.getEntity())) {
                                        if (!inputs.containsKey(form)) {
                                            inputs.put(form, new ArrayList<SingleUserInput>());
                                        }
                                        inputs.get(form).add(linkedInput);
                                    }
                                }
                            }
                            paramPageEntity.setInputs(inputs);
                        }
                    }
                }
            }

            // remove all duplicates
            for (PageEntity duplicate : duplicates) {
                pageEntities.remove(duplicate);
                // log.info("Duplicate removed: " + duplicate.getType() + ", " +
                // duplicate.getId());
            }

        }
        // ///////// Rule 3: more rules as we find them

        // check for duplicates
        List<String> varIds = new ArrayList<String>();
        for (PageEntity pageEntity : pageEntities) {
            if (varIds.contains(pageEntity.getId())) {
                log.error("Duplicate variable in controller: " + pageEntity.getId());
            } else {
                varIds.add(pageEntity.getId());
            }
        }
    }

    /**
     * used to generate imports
     *
     * @return
     *
     * @author Vineet Manohar
     */
    public Set<Entity> getUniqueEntities() {
        Set<Entity> retVal = new TreeSet<Entity>();
        for (PageEntity pageEntity : pageEntities) {
            retVal.add(pageEntity.getEntity());
        }

        for (PageEntityList pageEntityList : pageEntityLists) {
            retVal.add(pageEntityList.getOutputList().getEntity());
        }

        return retVal;
    }

    public List<PageEntity> getParamPageEntities() {
        List<PageEntity> retVal = new ArrayList<PageEntity>();

        for (PageEntity pageEntity : pageEntities) {
            if (pageEntity.getType() == Type.PARAM_PRIMARY_KEY || pageEntity.getType() == Type.PARAM_OTHER) {
                retVal.add(pageEntity);
            }
        }

        return retVal;
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    public List<PageEntityList> getPageEntityLists() {
        return pageEntityLists;
    }

    public void setPageEntityLists(List<PageEntityList> pageEntityLists) {
        this.pageEntityLists = pageEntityLists;
    }
}