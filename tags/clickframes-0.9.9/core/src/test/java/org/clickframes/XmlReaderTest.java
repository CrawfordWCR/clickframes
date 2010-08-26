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

package org.clickframes;

import java.io.InputStream;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.clickframes.xmlbindings.ActionType;
import org.clickframes.xmlbindings.AppspecType;
import org.clickframes.xmlbindings.OutcomeType;
import org.clickframes.xmlbindings.PageType;
import org.clickframes.AppspecJaxbWrapper;
import org.clickframes.util.ClickframeUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

/**
 * @author Vineet Manohar
 */
public class XmlReaderTest {
    private static AppspecType appspecType;

    @BeforeSuite
    public static void beforeClass() throws JAXBException {
        InputStream is = AppspecJaxbWrapper.class.getResourceAsStream("/appspec_junit.xml");
        Assert.assertNotNull(is, "Sample file not found");
        appspecType = AppspecJaxbWrapper.readAppspecType(is);
        Assert.assertNotNull(appspecType, "Unable to read sample project");
    }

    @Test
    public void testProjectTitle() {
        Assert.assertEquals(appspecType.getTitle(), "Sample Project Title");
    }

    @Test
    public void testProjectPages() {
        List<PageType> pages = appspecType.getPages().getPage();
        Assert.assertEquals(pages.size(), 4, "Wrong number of pages");

        PageType page1 = pages.get(0);
        Assert.assertEquals(page1.getTitle(), "Page 1 Title", "Wrong page title");

        Assert.assertEquals(ClickframeUtils.normalize(page1.getDescription()), "Page 1 Description",
                "Wrong page description");
    }

    @Test
    public void testProjectPageActions() {
        PageType page2 = appspecType.getPages().getPage().get(1);
        List<ActionType> actions = page2.getActions().getAction();
        Assert.assertEquals(actions.size(), 1, "Wrong number of actions");

        ActionType action1 = actions.get(0);
        Assert.assertEquals(action1.getTitle(), "Action 2a title", "Wrong action title");
    }

    @Test
    public void testProjectPageActionOutcomes() {
        List<OutcomeType> outcomes = appspecType.getPages().getPage().get(0).getForms().getForm().get(1).getActions().getAction()
                .get(0).getOutcomes().getOutcome();

        Assert.assertEquals(outcomes.size(), 2, "Wrong number of outcomes");

        OutcomeType outcome1 = outcomes.get(0);
        Assert.assertEquals(outcome1.getTitle(), "Outcome to page1", "Wrong outcome title");
        Assert.assertEquals(outcome1.getPageRef(), "page1", "Wrong pageRef");
    }
}