package org.clickframes.engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.clickframes.VelocityHelper;
import org.clickframes.engine.manifest.AutoscanUtil;
import org.clickframes.model.Action;
import org.clickframes.model.Appspec;
import org.clickframes.model.Content;
import org.clickframes.model.Email;
import org.clickframes.model.Entity;
import org.clickframes.model.EntityProperty;
import org.clickframes.model.Fact;
import org.clickframes.model.Form;
import org.clickframes.model.Link;
import org.clickframes.model.LinkSet;
import org.clickframes.model.Output;
import org.clickframes.model.OutputList;
import org.clickframes.model.Page;
import org.clickframes.model.SingleUserInput;
import org.clickframes.model.Validation;
import org.clickframes.plugins.ClickframesPlugin;
import org.clickframes.plugins.PluginContext;
import org.clickframes.techspec.AppspecPhase;
import org.clickframes.techspec.Context;
import org.clickframes.techspec.Filter;
import org.clickframes.techspec.FilterType;
import org.clickframes.techspec.Plugin;
import org.clickframes.techspec.Property;
import org.clickframes.techspec.TechspecContext;

/**
 * Base implementation of New Autoscan Engine.
 * 
 * @author Steven Boscarine
 * @author Vineet Manohar
 */
public abstract class AutoscanEngineAPI3 {
	protected TechspecContext techspecContext;
	protected final Appspec appspec;
	protected final InternalVariableResolver variableResolver;
	protected List<TemplateCall> calls = new ArrayList<TemplateCall>();
	protected Map<AppspecPhase, List<String>> byLifeCycle = new HashMap<AppspecPhase, List<String>>();
	protected List<String> listOfTemplates;
	protected Set<String> listOfTemplatesNeverCalled;
	protected String autoscanDirectoryName;
	protected static int NUM_THREADS = 3;
	protected static final Log logger = LogFactory
			.getLog(AutoscanEngineAPI3.class);
	private static final ExecutorService pool = Executors
			.newFixedThreadPool(NUM_THREADS);

	/**
	 * list of all the contexts associated with this autoscan run
	 */
	private Map<Object, Context> contextMap = new HashMap<Object, Context>();
	private List<Context> contextList = new ArrayList<Context>();

	private ClickframesPlugin clickframesPlugin;

	protected AutoscanEngineAPI3(PluginContext pluginContext,
			InternalVariableResolver internalVariableResolver,
			String autoscanDirectoryName, List<String> templates,
			ClickframesPlugin clickframesPlugin) {
		this((TechspecContext) pluginContext, internalVariableResolver,
				autoscanDirectoryName, AutoscanUtil
						.refineAutoscanTemplatesByPlugin(templates,
								pluginContext.getPlugin().getClazz()),
				clickframesPlugin);
	}

	protected AutoscanEngineAPI3(TechspecContext techspecContext,
			InternalVariableResolver internalVariableResolver,
			String autoscanDirectoryName, List<String> templates,
			ClickframesPlugin clickframesPlugin) {
		this.techspecContext = techspecContext;
		this.appspec = techspecContext.getAppspec();
		this.variableResolver = internalVariableResolver;
		this.listOfTemplates = templates;
		this.listOfTemplatesNeverCalled = new HashSet<String>(listOfTemplates);
		this.autoscanDirectoryName = autoscanDirectoryName;
		this.clickframesPlugin = clickframesPlugin;
	}

	/** Override me, if needed. */
	public void process() {
		processAppspec();

		// process all elements

		// this is commented out as this does not process appspec elements like
		// pageBlock outputs which are shared by reference from two locations
		// for (Context context : contextMap.values()) {
		// processTemplatesByContext(context);
		// }

		// use the contextList instead
		for (Context context : contextList) {
			processTemplatesByContext(context);
		}

		if (listOfTemplatesNeverCalled.size() > 0) {
			// logger.info(listOfTemplatesNeverCalled.size() +
			// " templates were never called: " + listOfTemplatesNeverCalled);
		}

		boolean sync = true;
		if (sync) {
			try {
				generateAutoscanArtifactsSync();
			} catch (IOException e) {
				throw new RuntimeException(
						"Error occurred while processing templates", e);
			}
		} else {
			generateAutoscanArtifacts();
		}
	}

	protected void generateAutoscanArtifactsSync() throws IOException {
		for (TemplateCall call : calls) {
			AppspecWorker2 worker = new AppspecWorker2(call.filename,
					call.relativePath, call.templatePath, call.objects);
			worker.callSync();
		}
	}

	/**
	 * Build all your template calls and call me to submit to thread pool.
	 */
	protected void generateAutoscanArtifacts() {
		final List<Future<Boolean>> futureReferences = new ArrayList<Future<Boolean>>();
		// loop through all parameters passed and submit to thread pool.
		for (TemplateCall params : calls) {
			futureReferences.add(submitToThreadPool(params));
		}
		for (Future<Boolean> future : futureReferences) {
			try {
				future.get();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	private Future<Boolean> submitToThreadPool(TemplateCall call) {
		// AppspecWorker worker = new AppspecWorker(call.filename,
		// call.relativePath, call.templatePath, call.objects);
		AppspecWorker2 worker = new AppspecWorker2(call.filename,
				call.relativePath, call.templatePath, call.objects);
		return pool.submit(worker);
	}

	private static final boolean debugMode = false;

	/** Conditionally display found templates by their lifecycle */
	@SuppressWarnings("unused")
	private String debug(Map<AppspecPhase, List<String>> templates) {
		StringBuilder sb = new StringBuilder();
		if (debugMode) {
			for (AppspecPhase k : templates.keySet()) {
				sb.append(k + "\n");
				for (String b : templates.get(k)) {
					sb.append("\t" + b + "\n");
				}
			}
		}
		return sb.toString();
	}

	void processAppspec() {
		Context appspecContext = new Context(null);

		// add all properties before calling any plugins
		{
			// add techspec properties, if they apply
			List<Property> properties = techspecContext.getTechspec()
					.getProperties();
			addProperties(properties, appspecContext);

			// for plugins, add plugin properties, if they apply
			Plugin plugin = (Plugin) appspecContext.get("plugin");
			if (plugin != null) {
				List<Property> listProperties = plugin.getProperties();
				addProperties(listProperties, appspecContext);
			}
		}

		clickframesPlugin.globalContext(appspecContext, techspecContext);
		appspecContext.putAll(variableResolver.getAppspecVariables());

		appspecContext.put("context", contextMap);
		appspecContext.put("techspec", techspecContext.getTechspec());
		appspecContext.put("appspec", techspecContext.getAppspec());
		if (techspecContext instanceof PluginContext) {
			appspecContext.put("plugin", ((PluginContext) techspecContext)
					.getPlugin());
		}

		saveContext(appspec, appspecContext);

		// page
		for (Page page : appspec.getPages()) {
			processPage(appspecContext, page);
		}

		// email
		for (Email email : appspec.getEmails()) {
			processEmail(appspecContext, email);
		}

		// linkSet
		for (LinkSet linkSet : appspec.getLinkSets()) {
			processLinkSet(appspecContext, linkSet);
		}

		// entity
		for (Entity entity : appspec.getEntities()) {
			processEntity(appspecContext, entity);
		}
	}

	private void addProperties(List<Property> properties, Context context) {
		for (Property property : properties) {
			boolean condition = evaluateCondition(context, property
					.getCondition(), true);

			if (condition) {
				context.put(property.getName(), VelocityHelper.resolveText(
						context.getFullMap(), property.getValue()));
			}
		}
	}

	private boolean evaluateCondition(Context context, String condition,
			boolean defaultValue) {
		if (condition == null) {
			return defaultValue;
		}
		Boolean conditionResult = VelocityHelper.evaluateCondition(context
				.getFullMap(), condition);
		if (conditionResult == null) {
			return defaultValue;
		}

		return conditionResult;
	}

	private void createTemplateCalls(Map<String, Object> params,
			List<String> appspecTemplates) {
		createTemplateCallIfIncluded(params, appspecTemplates);
	}

	private void createTemplateCallIfIncluded(Map<String, Object> contextMap,
			List<String> appspecTemplates) {
		// for all templates found at appspec level
		for (String file : appspecTemplates) {
			// if template is in context, see if condition is allowed
			// put the template in context
			Map<String, Object> local = new HashMap<String, Object>(contextMap);
			local.put("template", file);

			if (isPluginIncluded(local)) {
				createTemplateCall(local, file);
			}
		}
	}

	private void createTemplateCall(Map<String, Object> appspecContext,
			String file) {
		String[] components = FilenameUtil.splitFilenameAndFolder(file);
		String templateFolder = components[0];
		String templateFilename = components[1];

		String actualRelativeFolder = replaceVariables(appspecContext,
				templateFolder);
		String generatedArtifactName = replaceVariables(appspecContext,
				templateFilename);

		TemplateCall call = createCall(file, actualRelativeFolder,
				generatedArtifactName, appspecContext);

		calls.add(call);
	}

	void processEntity(Context appspecContext, Entity entity) {
		Context entityContext = new Context(appspecContext);
		clickframesPlugin.entityContext(entityContext, entity);
		entityContext.put("entity", entity);
		saveContext(entity, entityContext);

		// entity properties
		for (EntityProperty entityProperty : entity.getProperties()) {
			Context entityPropertyContext = new Context(entityContext);
			clickframesPlugin.entityPropertyContext(entityPropertyContext,
					entityProperty);
			saveContext(entityProperty, entityPropertyContext);
		}
	}

	void processLinkSet(Context appspecContext, LinkSet linkSet) {
		Context linkSetContext = new Context(appspecContext);
		clickframesPlugin.linkSetContext(linkSetContext, linkSet);
		linkSetContext.put("linkSet", linkSet);
		saveContext(linkSet, linkSetContext);

		// linkSet facts
		for (Fact fact : linkSet.getFacts()) {
			Context linkSetFactContext = new Context(linkSetContext);
			clickframesPlugin.linkSetFactContext(linkSetFactContext, linkSet,
					fact);
			linkSetFactContext.put("fact", fact);
			saveContext(fact, linkSetFactContext);
		}

		// link
		for (Link link : linkSet.getLinks()) {
			Context linkSetLinkContext = new Context(linkSetContext);
			clickframesPlugin.linkSetLinkContext(linkSetLinkContext, linkSet,
					link);
			linkSetLinkContext.put("link", link);
			saveContext(link, linkSetLinkContext);

			// facts
			for (Fact fact : link.getFacts()) {
				Context linkSetLinkFactContext = new Context(linkSetLinkContext);
				clickframesPlugin.linkSetLinkFactContext(
						linkSetLinkFactContext, linkSet, link, fact);
				linkSetLinkFactContext.put("linkSetLinkFact", fact);
				saveContext(fact, linkSetLinkFactContext);
			}
		}
	}

	void processEmail(Context appspecContext, Email email) {
		Context emailContext = new Context(appspecContext);
		clickframesPlugin.emailContext(emailContext, email);
		emailContext.put("email", email);
		saveContext(email, emailContext);

		// email facts
		for (Fact fact : email.getFacts()) {
			Context emailFactContext = new Context(emailContext);
			clickframesPlugin.emailFactContext(emailFactContext, email, fact);
			emailFactContext.put("emailFact", fact);
			saveContext(fact, emailFactContext);
		}
	}

	void processPage(Context appspecContext, Page page) {
		Context pageContext = new Context(appspecContext);

		clickframesPlugin.pageContext(pageContext, page);
		pageContext.putAll(variableResolver.getPageVariables(page));
		pageContext.put("page", page);
		saveContext(page, pageContext);

		// facts - this must occur before other sub facts types, as this is the
		// smallest
		for (Fact fact : page.getFacts()) {
			Context factContext = new Context(pageContext);
			clickframesPlugin.pageFactContext(factContext, page, fact);
			factContext.putAll(variableResolver.getPageFactsVariables(fact));
			factContext.put("fact", fact);
			saveContext(fact, factContext);
		}

		// form
		for (Form form : page.getForms()) {
			processForm(pageContext, page, form);
		}

		// page link
		for (Link link : page.getLinks()) {
			processPageLink(pageContext, link);
		}

		// output
		for (Output output : page.getOutputs()) {
			processOutput(pageContext, page, output);
		}

		// output list
		for (OutputList outputList : page.getOutputLists()) {
			processOutputList(pageContext, page, outputList);
		}

		// page content
		for (Content content : page.getContents()) {
			processPageContent(pageContext, page, content);
		}
	}

	void processOutputList(Context pageContext, Page page, OutputList outputList) {
		Context outputListContext = new Context(pageContext);
		clickframesPlugin.outputListContext(outputListContext, page);
		outputListContext.put("outputList", outputList);
		saveContext(outputList, outputListContext);

		for (Link link : outputList.getLinks()) {
			Context outputListLinkContext = new Context(outputListContext);
			clickframesPlugin.outputListLinkContext(outputListLinkContext,
					page, outputList, link);
			saveContext(link, outputListLinkContext);
		}
		
		for (Action action : outputList.getActions()) {
			for (Link outcome : action.getOutcomes()) {
				Context outcomeContext = new Context(outputListContext);
				outcomeContext.put("action", action);
				clickframesPlugin.outcomeContext(outcomeContext, outcome);
				saveContext(outcome, outcomeContext);
			}
		}
	}

	void processOutput(Context pageContext, Page page, Output output) {
		Context outputContext = new Context(pageContext);
		clickframesPlugin.outputContext(outputContext, page, output);
		outputContext.put("output", output);
		saveContext(output, outputContext);
	}

	void processPageContent(Context pageContext, Page page, Content content) {
		Context contentContext = new Context(pageContext);
		clickframesPlugin.pageContentContext(contentContext, page);
		contentContext.putAll(variableResolver.getContentVariables(page,
				content));
		contentContext.put("content", content);
		saveContext(content, contentContext);
	}

	void processPageAction(Context pageContext, Action action) {
		Context actionContext = new Context(pageContext);
		clickframesPlugin.pageActionContext(actionContext, (Page) pageContext
				.get("page"), action);
		actionContext.putAll(variableResolver.getPageActionVariables(
				(Page) pageContext.get("page"), action));
		actionContext.put("action", action);
		saveContext(action, actionContext);

		// page action outcome
		for (Link outcome : action.getOutcomes()) {
			processPageActionOutcome(actionContext, outcome);
		}
	}

	void processPageLink(Context pageContext, Link link) {
		Context pageLinkContext = new Context(pageContext);
		clickframesPlugin.pageLinkContext(pageLinkContext, (Page) pageContext
				.get("page"), link);
		pageLinkContext.put("link", link);
		saveContext(link, pageLinkContext);

		for (Fact fact : link.getFacts()) {
			Context pageLinkFactContext = new Context(pageLinkContext);
			clickframesPlugin.pageLinkFactContext(pageLinkFactContext,
					(Page) pageContext.get("page"), link, fact);
			pageLinkFactContext.put("fact", fact);
			saveContext(fact, pageLinkFactContext);
		}
	}

	void processForm(Context pageContext, Page page, Form form) {
		Context formContext = new Context(pageContext);
		clickframesPlugin.pageFormContext(formContext, page, form);
		formContext.putAll(variableResolver.getFormVariables(page, form));
		formContext.put("form", form);
		saveContext(form, formContext);

		// form action
		for (Action action : form.getActions()) {
			processFormAction(formContext, action);
		}

		// input
		for (SingleUserInput input : form.getInputs()) {
			Context inputContext = new Context(formContext);
			inputContext.putAll(variableResolver.getInputVariables(page, form,
					input));
			clickframesPlugin.inputContext(inputContext, form, input);
			inputContext.put("input", input);
			saveContext(input, inputContext);

			// input facts
			for (Fact fact : input.getFacts()) {
				Context inputFactContext = new Context(inputContext);
				clickframesPlugin.inputFactContext(inputFactContext, form,
						input, fact);
				inputFactContext.put("fact", fact);
				saveContext(fact, inputFactContext);
			}

			// input validations
			for (Validation validation : input.getValidations()) {
				Context inputValidationContext = new Context(inputContext);
				clickframesPlugin.inputValidationContext(
						inputValidationContext, form, input, validation);
				inputValidationContext.putAll(variableResolver
						.getInputValidationVariables(page, form, input,
								validation));
				inputValidationContext.put("validation", validation);
				saveContext(validation, inputValidationContext);
			}
		}
	}

	void processFormAction(Context formContext, Action action) {
		Context actionContext = new Context(formContext);
		clickframesPlugin.formActionContext(actionContext, (Page) formContext
				.get("page"), (Form) formContext.get("form"), action);
		actionContext.putAll(variableResolver.getFormActionVariables(
				(Page) formContext.get("page"), (Form) formContext.get("form"),
				action));
		actionContext.put("action", action);
		saveContext(action, actionContext);

		// form action outcome
		for (Link outcome : action.getOutcomes()) {
			processFormActionOutcome(actionContext, outcome);
		}
	}

	void processFormActionOutcome(Context actionContext, Link outcome) {
		Context outcomeContext = new Context(actionContext);

		clickframesPlugin.outcomeContext(outcomeContext, outcome);
		outcomeContext.put("outcome", outcome);
		saveContext(outcome, outcomeContext);

		// facts
		for (Fact fact : outcome.getFacts()) {
			Context factContext = new Context(outcomeContext);

			clickframesPlugin.outcomeFactContext(factContext, outcome, fact);
			factContext.put("fact", fact);
			saveContext(fact, factContext);
		}
	}

	void processPageActionOutcome(Context actionContext, Link outcome) {
		Context actionOutcomeContext = new Context(actionContext);
		clickframesPlugin.actionOutcomeContext(actionOutcomeContext,
				(Action) actionContext.get("action"), outcome);
		actionOutcomeContext.put("outcome", outcome);
		saveContext(outcome, actionOutcomeContext);

		// facts
		for (Fact fact : outcome.getFacts()) {
			Context factContext = new Context(actionOutcomeContext);
			clickframesPlugin.actionOutcomeFactContext(factContext,
					(Action) actionContext.get("action"), outcome, fact);
			factContext.put("fact", fact);
			saveContext(fact, factContext);
		}
	}

	private void saveContext(Object key, Context context) {
		if (contextMap.containsKey(key)) {
			boolean stopOnCustomizationConflicts = false;
			RuntimeException ex = new RuntimeException(
					"Developer error: the same appspec element was added twice to code "
							+ "generation queue. There is a bug in code. The element is "
							+ key + ". The original scope was: "
							+ contextMap.get(key) + ". The new scope is "
							+ context);
			if (stopOnCustomizationConflicts) {
				throw ex;
			}

			// logger
			// .warn("This may effect plugin customization for referenenced elements like contentRef, linkSetRef: "
			// + ex.getMessage());
		}
		contextMap.put(key, context);

		// also add context to context list
		contextList.add(context);
	}

	private void processTemplatesByContext(Context context) {
		List<String> templatesInContext = new ArrayList<String>();

		// add techspec properties, if they apply
		List<Property> properties = techspecContext.getTechspec()
				.getProperties();
		addProperties(properties, context);

		// for plugins, add plugin properties, if they apply
		Plugin plugin = (Plugin) context.get("plugin");
		if (plugin != null) {
			List<Property> listProperties = plugin.getProperties();
			addProperties(listProperties, context);
		}

		// consider each template one by one
		for (String templatePath : listOfTemplates) {
			if (isTemplateInContext(templatePath, context.getFullMap())) {
				// is every ancestor required?
				boolean isEveryContextRequired = isEveryContextRequired(
						templatePath, context);

				if (isEveryContextRequired) {
					templatesInContext.add(templatePath);
				}
			}
		}

		createTemplateCalls(context.getFullMap(), templatesInContext);

		listOfTemplatesNeverCalled.removeAll(templatesInContext);
	}

	/**
	 * Remove ancestors from the chain one by one, and see if templates are in
	 * context
	 * 
	 * @param templatePath
	 * @param contextChain
	 * @return
	 * 
	 * @author Vineet Manohar
	 */
	static boolean isEveryContextRequired(String templatePath,
			Map<String, Object>... contextChain) {
		List<Map<String, Object>> ancestors = new ArrayList<Map<String, Object>>();
		// get all ancestors
		for (Map<String, Object> ancestor : contextChain) {
			ancestors.add(ancestor);
		}
		// remove the current context
		// Map<String, Object> currentContext =
		// ancestors.remove(ancestors.size() - 1);
		Map<String, Object> currentContext = ancestors
				.get(ancestors.size() - 1);

		// remove ancestors, one by one, and see if any ancestor is optional
		for (int i = ancestors.size() - 1; i >= 1; i--) {
			Map<String, Object> ancestor = ancestors.get(i);

			// remove all keys in ancestor
			Map<String, Object> hypoteticalCurrentContext = new HashMap<String, Object>(
					currentContext);
			if (ancestor != null) {
				for (String key : ancestor.keySet()) {
					hypoteticalCurrentContext.remove(key);
				}
			}

			// but put all keys in ancestors parent
			if (i - 1 >= 0) {
				Map<String, Object> ancestorsParent = ancestors.get(i - 1);
				if (ancestorsParent != null) {
					for (String key : ancestorsParent.keySet()) {
						hypoteticalCurrentContext.put(key, ancestorsParent
								.get(key));
					}
				}
			}

			// minus the boundary condition, all keys introduced by this
			// ancestor are now removed
			if (isTemplateInContext(templatePath, hypoteticalCurrentContext)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Remove ancestors from the chain one by one, and see if templates are in
	 * context
	 * 
	 * For example, if this context is A -> B -> C -> D
	 * 
	 * Then create remove each context one by one
	 * 
	 * so check 4 cases
	 * 
	 * case 1: abc
	 * 
	 * case 2: abd
	 * 
	 * case 3: acd
	 * 
	 * case 4: bcd
	 * 
	 * if any case satisfies the template, then every context is not required
	 * 
	 * @param templatePath
	 * @param contextChain
	 * @return
	 * 
	 * @author Vineet Manohar
	 */
	@SuppressWarnings( { "cast", "unchecked" })
	static boolean isEveryContextRequired(String templatePath, Context context) {
		List<Map<String, Object>> contextChain = new ArrayList<Map<String, Object>>();

		Context pointer = context;
		do {
			// add to the beginning of the list
			contextChain.add(0, pointer.getFullMap());
			pointer = pointer.getParent();
		} while (pointer != null);

		return isEveryContextRequired(templatePath,
				(Map<String, Object>[]) contextChain.toArray(new Map[] {}));
	}

	/**
	 * 
	 * @param templatePath
	 * @param context
	 * @return true if the context complelely satisfies all ${} variables in the
	 *         template path, false otherwise
	 * 
	 * @author Vineet Manohar
	 */
	static boolean isTemplateInContext(String text, Map<String, Object> context) {
		String resolvedText = replaceVariables(context, text);

		boolean templateInContext = !resolvedText.contains("$");

		if (!templateInContext) {
			return false;
		}

		return templateInContext;
	}

	static boolean isPluginIncluded(Map<String, Object> context) {
		// get plugin context
		Plugin plugin = (Plugin) context.get("plugin");

		if (plugin != null) {
			List<Filter> filters = plugin.getFilters();

			// by default everything is included
			if (filters.size() == 0) {
				return true;
			}

			// iterate through all filters
			for (Filter filter : filters) {
				String condition = filter.getCondition();
				if (!StringUtils.isEmpty(condition)) {
					Boolean conditionResult = VelocityHelper.evaluateCondition(
							context, condition);
					if (conditionResult != null) {
						// logger.info("Filter successfully applied. " +
						// filter + " => " + conditionResult);

						if (filter.getType() == FilterType.INCLUDE) {
							return conditionResult;
						}

						return !conditionResult;
					}
				}
			}

			// if a filter is defined, by default everything is excluded
			// unless
			// included by the filter
			return false;
		}

		// default is included
		return true;
	}

	static String replaceVariables(Map<String, Object> context, String text) {
		return VelocityHelper.resolveText(context, text);
	}

	/** makes paths relative */
	private TemplateCall createCall(String relativePath,
			String actualRelativeFolder, String generatedArtifactName,
			Map<String, Object> objects) {
		final String templatePath = relativePath;
		// replace autoscan directory path in name
		final String destinationDir = actualRelativeFolder.replaceAll("/?"
				+ autoscanDirectoryName + "/?", "");

		validateCall(generatedArtifactName, destinationDir);

		final TemplateCall templateCall = new TemplateCall(templatePath,
				destinationDir, generatedArtifactName, objects);
		return templateCall;
	}

	private void validateCall(final String templatePath,
			final String destinationDir) {
		String destination = destinationDir + templatePath;
		if (destination.contains("${")) {
			throw new IllegalArgumentException(destination
					+ " has unparsed tokens.  Your engine has failed.");
		}
	}
}