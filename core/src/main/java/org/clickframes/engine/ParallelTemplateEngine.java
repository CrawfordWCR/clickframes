/*
 * Clickframes: Full lifecycle software development automation.
 * Copyright (C)  2009 Children's Hospital Boston
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.clickframes.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.clickframes.model.Appspec;

/**
 * I create calls to CodeGenerator via a threadpool. I also declare the methods
 * overridden by the Spring Web Flow and PHP plugins.
 *
 * @author Steven Boscarine
 * @deprecated
 */
@Deprecated
public abstract class ParallelTemplateEngine {
    protected final Log logger = LogFactory.getLog(getClass());
    // TODO: Make me configurable
    // set to 2 because 3 was causing transient IO errors on my box (quad core
    // Xeon 2.66GHz)
    private static final ExecutorService pool = Executors.newFixedThreadPool(2);

    protected final Appspec appspec;

    public ParallelTemplateEngine(Appspec appspec) {
        super();
        this.appspec = appspec;
    }

    /** Override me, if needed. */
    public void generateAll() {
        generateAll(buildTemplateCalls());
        generateArtifactsManually();
    }

    /**
     * Override me to introduce non-velocity calls. For example, properties
     * files are set using the Java API to allow merging and to eliminate
     * whitespace.
     */
    public void generateArtifactsManually() {
        // I am blank intentionally.
    }

    /**
     * Build all your template calls and call me to submit to thread pool.
     */
    protected void generateAll(Collection<TemplateCall> allParams) {
        final List<Future<Boolean>> futureReferences = new ArrayList<Future<Boolean>>();
        // loop through all parameters passed and submit to thread pool.
        for (TemplateCall params : allParams) {
            futureReferences.add(submitToThreadPool(params));
        }
        for (Future<Boolean> future : futureReferences) {
            try {
                future.get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /** Main logic method. */
    protected abstract Collection<TemplateCall> buildTemplateCalls();

    private Future<Boolean> submitToThreadPool(TemplateCall call) {
        AppspecWorker worker = new AppspecWorker(call.filename, call.relativePath, call.templatePath, call.objects);
        return pool.submit(worker);
    }
}