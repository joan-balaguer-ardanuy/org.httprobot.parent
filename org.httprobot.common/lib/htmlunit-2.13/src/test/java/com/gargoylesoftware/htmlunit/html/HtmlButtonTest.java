/*
 * Copyright (c) 2002-2013 Gargoyle Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gargoylesoftware.htmlunit.html;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.BrowserRunner;
import com.gargoylesoftware.htmlunit.MockWebConnection;
import com.gargoylesoftware.htmlunit.SimpleWebTestCase;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.util.NameValuePair;

/**
 * Tests for {@link HtmlButton}.
 *
 * @version $Revision: 8254 $
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * @author David K. Taylor
 * @author Brad Clarke
 * @author Marc Guillemot
 * @author Ahmed Ashour
 */
@RunWith(BrowserRunner.class)
public class HtmlButtonTest extends SimpleWebTestCase {

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void buttonClick_onClick() throws Exception {
        final String htmlContent
            = "<html><head><title>foo</title></head><body>\n"
            + "<form id='form1' onSubmit='alert(\"bar\")' onReset='alert(\"reset\")'>\n"
            + "    <button type='button' name='button' id='button' "
            + "onClick='alert(\"foo\")'>Push me</button>\n"
            + "</form></body></html>";

        final List<String> collectedAlerts = new ArrayList<String>();
        final HtmlPage page = loadPage(htmlContent, collectedAlerts);
        final HtmlButton button = page.getHtmlElementById("button");

        final HtmlPage secondPage = (HtmlPage) button.click();

        final String[] expectedAlerts = {"foo"};
        assertEquals(expectedAlerts, collectedAlerts);

        assertSame(page, secondPage);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void submitClick_onClick() throws Exception {
        final String htmlContent
            = "<html><head><title>foo</title></head><body>\n"
            + "<form id='form1' onSubmit='alert(\"bar\")' onReset='alert(\"reset\")'>\n"
            + "    <button type='submit' name='button' id='button' "
            + "onClick='alert(\"foo\")'>Push me</button>\n"
            + "</form></body></html>";
        final List<String> collectedAlerts = new ArrayList<String>();
        final HtmlPage page = loadPage(htmlContent, collectedAlerts);
        final HtmlButton button = page.getHtmlElementById("button");

        final HtmlPage secondPage = button.click();

        final String[] expectedAlerts = {"foo", "bar"};
        assertEquals(expectedAlerts, collectedAlerts);

        assertNotSame(page, secondPage);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void buttonTypeSubmit() throws Exception {
        final String htmlContent
            = "<html><head><title>foo</title></head><body>\n"
            + "<form id='form1' method='post' onSubmit='alert(\"bar\")' onReset='alert(\"reset\")'>\n"
            + "    <button type='submit' name='button' id='button' value='foo'"
            + "    >Push me</button>\n"
            + "</form></body></html>";
        final List<String> collectedAlerts = new ArrayList<String>();
        final HtmlPage page = loadPage(htmlContent, collectedAlerts);
        final HtmlButton button = page.getHtmlElementById("button");

        final HtmlPage secondPage = button.click();

        final String[] expectedAlerts = {"bar"};
        assertEquals(expectedAlerts, collectedAlerts);

        assertNotSame(page, secondPage);
        final List<NameValuePair> expectedParameters = Arrays.asList(new NameValuePair[]{
            new NameValuePair("button", "foo")
        });
        final List<NameValuePair> collectedParameters = getMockConnection(secondPage).getLastParameters();

        assertEquals(expectedParameters, collectedParameters);
    }

    /**
     * According to the HTML spec, the default type for a button is "submit".
     * IE is different than the HTML spec and has a default type of "button".
     * @throws Exception if the test fails
     */
    @Test
    public void defaultButtonType_StandardsCompliantBrowser() throws Exception {
        final String expectedType = getBrowserVersion().isFirefox() ? "submit" : "button";
        final String firstContent
            = "<html><head><title>First</title></head><body>\n"
            + "<form id='form1' action='" + URL_SECOND + "' method='post'>\n"
            + "    <button name='button' id='button' value='pushme'>PushMe</button>\n"
            + "</form></body></html>";
        final String secondContent
            = "<html><head><title>Second</title></head><body'></body></html>";
        final WebClient client = getWebClient();

        final MockWebConnection webConnection = new MockWebConnection();
        webConnection.setResponse(URL_FIRST, firstContent);
        webConnection.setResponse(URL_SECOND, secondContent);
        client.setWebConnection(webConnection);

        final HtmlPage page = client.getPage(URL_FIRST);
        final HtmlButton button = page.getHtmlElementById("button");
        assertEquals(expectedType, button.getTypeAttribute());

        final HtmlPage page2 = button.click();
        final List<NameValuePair> expectedParameters;
        final String expectedSecondPageTitle;
        if ("submit".equals(expectedType)) {
            expectedParameters = Collections.singletonList(new NameValuePair("button", "pushme"));
            expectedSecondPageTitle = "Second";
        }
        else {
            expectedParameters = Collections.emptyList();
            expectedSecondPageTitle = "First";
        }
        assertEquals(expectedParameters, webConnection.getLastParameters());
        assertEquals(expectedSecondPageTitle, page2.getTitleText());
    }

}
