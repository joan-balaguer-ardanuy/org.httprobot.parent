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
package com.gargoylesoftware.htmlunit.javascript.host.css;

import static com.gargoylesoftware.htmlunit.BrowserRunner.Browser.FF3_6;
import static com.gargoylesoftware.htmlunit.BrowserRunner.Browser.IE;
import static com.gargoylesoftware.htmlunit.BrowserRunner.Browser.IE6;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.BrowserRunner;
import com.gargoylesoftware.htmlunit.BrowserRunner.Alerts;
import com.gargoylesoftware.htmlunit.BrowserRunner.Browser;
import com.gargoylesoftware.htmlunit.BrowserRunner.BuggyWebDriver;
import com.gargoylesoftware.htmlunit.BrowserRunner.NotYetImplemented;
import com.gargoylesoftware.htmlunit.WebDriverTestCase;
import com.gargoylesoftware.htmlunit.html.HtmlPageTest;
import com.gargoylesoftware.htmlunit.util.UrlUtils;

/**
 * Tests for CSS selectors.
 *
 * @version $Revision: 8609 $
 * @author Ronald Brill
 * @author Marc Guillemot
 * @author Ahmed Ashour
 * @author Frank Danek
 */
@RunWith(BrowserRunner.class)
public class CSSSelectorTest extends WebDriverTestCase {

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(DEFAULT = { "0", "0" },
            FF3_6 = "exception")
    @NotYetImplemented({ FF3_6, IE6 })
    public void querySelectorAll_nullUndefined() throws Exception {
        final String html = "<html><head><title>First</title>\n"
            + "<meta http-equiv='X-UA-Compatible' content='IE=edge'>\n"
            + "<script>\n"
            + "function test() {\n"
            + "  if (document.querySelectorAll) {\n"
            + "    try {\n"
            + "      alert(document.querySelectorAll(null).length);\n"
            + "      alert(document.querySelectorAll(undefined).length);\n"
            + "    } catch(e) {alert('exception')}\n"
            + "  }\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='test()'>\n"
            + "<ul>\n"
            + "  <li id='li1'></li>\n"
            + "  <li id='li2'></li>\n"
            + "  <li id='li3'></li>\n"
            + "</ul>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts({ "exception", "exception" })
    public void querySelectorAll_emptyString() throws Exception {
        final String html = "<html><head><title>First</title>\n"
            + "<meta http-equiv='X-UA-Compatible' content='IE=edge'>\n"
            + "<script>\n"
            + "function test() {\n"
            + "  if (document.querySelectorAll) {\n"
            + "    try {\n"
            + "      alert(document.querySelectorAll(''));\n"
            + "    } catch(e) {alert('exception')}\n"
            + "    try {\n"
            + "      alert(document.querySelectorAll('  '));\n"
            + "    } catch(e) {alert('exception')}\n"
            + "  }\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='test()'>\n"
            + "<ul>\n"
            + "  <li id='li1'></li>\n"
            + "  <li id='li2'></li>\n"
            + "  <li id='li3'></li>\n"
            + "</ul>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * Test for bug 1287.
     *
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(DEFAULT = { "li2", "li1", "li2", "li1", "li3", "li1", "2", "li1", "li2" },
            IE8 = "exception")
    public void nth_child() throws Exception {
        final String html = "<html><head><title>First</title>\n"
            + "<meta http-equiv='X-UA-Compatible' content='IE=edge'>\n"
            + "<script>\n"
            + "function test() {\n"
            + "  if (document.querySelectorAll) {\n"
            + "    try {\n"
            + "      alert(document.querySelectorAll('li:nth-child(2)')[0].id);\n"
            + "      alert(document.querySelectorAll('li:nth-child(n)')[0].id);\n"
            + "      alert(document.querySelectorAll('li:nth-child(2n)')[0].id);\n"
            + "      alert(document.querySelectorAll('li:nth-child(2n+1)')[0].id);\n"
            + "      alert(document.querySelectorAll('li:nth-child(2n+1)')[1].id);\n"
            + "      alert(document.querySelectorAll('li:nth-child(2n-1)')[0].id);\n"

            + "      alert(document.querySelectorAll('li:nth-child(-n+2)').length);\n"
            + "      alert(document.querySelectorAll('li:nth-child(-n+2)')[0].id);\n"
            + "      alert(document.querySelectorAll('li:nth-child(-n+2)')[1].id);\n"
            + "    } catch(e) {alert('exception')}\n"
            + "  }\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='test()'>\n"
            + "<ul>\n"
            + "  <li id='li1'></li>\n"
            + "  <li id='li2'></li>\n"
            + "  <li id='li3'></li>\n"
            + "</ul>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(DEFAULT = { "1", "li2", "2", "li1", "li3" },
            IE8 = "exception")
    public void nth_child_even_odd() throws Exception {
        final String html = "<html><head><title>First</title>\n"
            + "<meta http-equiv='X-UA-Compatible' content='IE=edge'>\n"
            + "<script>\n"
            + "function test() {\n"
            + "  if (document.querySelectorAll) {\n"
            + "    try {\n"
            + "      alert(document.querySelectorAll('li:nth-child(even)').length);\n"
            + "      alert(document.querySelectorAll('li:nth-child(eVen)')[0].id);\n"
            + "      alert(document.querySelectorAll('li:nth-child(odd)').length);\n"
            + "      alert(document.querySelectorAll('li:nth-child(OdD)')[0].id);\n"
            + "      alert(document.querySelectorAll('li:nth-child(ODD)')[1].id);\n"
            + "    } catch(e) {alert('exception')}\n"
            + "  }\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='test()'>\n"
            + "<ul>\n"
            + "  <li id='li1'></li>\n"
            + "  <li id='li2'></li>\n"
            + "  <li id='li3'></li>\n"
            + "</ul>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts({ "1", "[object HTMLBodyElement]", "1", "0" })
    public void childSelector_html_body() throws Exception {
        final String html = "<html><head><title>First</title>\n"
            + "<meta http-equiv='X-UA-Compatible' content='IE=edge'>\n"
            + "<script>\n"
            + "function test() {\n"
            + "  if (document.querySelectorAll) {\n"
            + "    try {\n"
            + "      alert(document.querySelectorAll('html > body').length);\n"
            + "      alert(document.querySelectorAll('html > body')[0]);\n"
            + "      alert(document.querySelectorAll('  \\t\\r\\n  html > body  \\t\\r\\n  ').length);\n"

            + "      elem = document.getElementById('root');\n"
            + "      alert(elem.querySelectorAll('html > body').length);\n"
            + "    } catch(e) {alert('exception')}\n"
            + "  }\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='test()'>\n"
            + "<div id='root'>\n"
            + "</div>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts("exception")
    public void nth_child_no_argument() throws Exception {
        final String html
            = HtmlPageTest.STANDARDS_MODE_PREFIX_ + "<html><head><title>First</title><script>\n"
            + "function test() {\n"
            + "  try {\n"
            + "    alert(document.querySelectorAll('li:nth-child()'));\n"
            + "  } catch (e) {alert('exception')}\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='test()'>\n"
            + "<ul>\n"
            + "  <li id='li1'></li>\n"
            + "  <li id='li2'></li>\n"
            + "  <li id='li3'></li>\n"
            + "</ul>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(DEFAULT = { "li1", "li4", "li7", "li10" },
            IE8 = { "exception" })
    public void nth_child_equation() throws Exception {
        final String html
            = HtmlPageTest.STANDARDS_MODE_PREFIX_ + "<html><head><title>First</title><script>\n"
            + "function test() {\n"
            + "  if (document.querySelectorAll) {\n"
            + "    try {\n"
            + "      var list = document.querySelectorAll('li:nth-child(3n+1)');\n"
            + "      for (var i = 0 ; i < list.length; i++) {\n"
            + "        alert(list[i].id);\n"
            + "      }\n"
            + "    } catch (e) {alert('exception')}\n"
            + "  }\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='test()'>\n"
            + "<ul>\n"
            + "  <li id='li1'></li>\n"
            + "  <li id='li2'></li>\n"
            + "  <li id='li3'></li>\n"
            + "  <li id='li4'></li>\n"
            + "  <li id='li5'></li>\n"
            + "  <li id='li6'></li>\n"
            + "  <li id='li7'></li>\n"
            + "  <li id='li8'></li>\n"
            + "  <li id='li9'></li>\n"
            + "  <li id='li10'></li>\n"
            + "</ul>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * Exception should be thrown for an invalid selector.
     *
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts("exception")
    public void invalid() throws Exception {
        final String html
            = "<html><head><title>First</title><script>\n"
            + "function test() {\n"
            + "  try {\n"
            + "    alert(document.querySelectorAll('td:gt(4)').length);\n"
            + "  } catch(e) {alert('exception')}\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='test()'>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts({ "1", "ul2" })
    public void directAdjacentSelector() throws Exception {
        final String html = "<html><head><title>First</title>\n"
            + "<meta http-equiv='X-UA-Compatible' content='IE=edge'>\n"
            + "<script>\n"
            + "function test() {\n"
            + "  if (document.querySelectorAll) {\n"
            + "    var list = document.querySelectorAll('p+ul');\n"
            + "    alert(list.length);\n"
            + "    alert(list[0].id);\n"
            + "  }\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='test()'>\n"
            + "  <div></div>\n"
            + "  <ul id='ul1'></ul>\n"
            + "  <p></p>\n"
            + "  <ul id='ul2'></ul>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts({ "1", "thing1" })
    public void prefixAttribute() throws Exception {
        final String html = "<html><head><title>First</title>\n"
            + "<meta http-equiv='X-UA-Compatible' content='IE=edge'>\n"
            + "<script>\n"
            + "function test() {\n"
            + "  if (document.querySelectorAll) {\n"
            + "    var list = document.querySelectorAll('[id^=\"thing\"]');\n"
            + "    alert(list.length);\n"
            + "    alert(list[0].id);\n"
            + "  }\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='test()'>\n"
            + "  <div></div>\n"
            + "  <ul id='something'></ul>\n"
            + "  <p></p>\n"
            + "  <ul id='thing1'></ul>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(DEFAULT = "0")
    public void prefixAttributeEmpty() throws Exception {
        final String html = "<html><head><title>First</title>\n"
            + "<meta http-equiv='X-UA-Compatible' content='IE=edge'>\n"
            + "<script>\n"
            + "function test() {\n"
            + "  if (document.querySelectorAll) {\n"
            + "    var list = document.querySelectorAll('[id^=\"\"]');\n"
            + "    alert(list.length);\n"
            + "  }\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='test()'>\n"
            + "  <div></div>\n"
            + "  <ul id='something'></ul>\n"
            + "  <p></p>\n"
            + "  <ul id='thing1'></ul>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts({ "1", "something" })
    public void suffixAttribute() throws Exception {
        final String html = "<html><head><title>First</title>\n"
            + "<meta http-equiv='X-UA-Compatible' content='IE=edge'>\n"
            + "<script>\n"
            + "function test() {\n"
            + "  if (document.querySelectorAll) {\n"
            + "    var list = document.querySelectorAll('[id$=\"thing\"]');\n"
            + "    alert(list.length);\n"
            + "    alert(list[0].id);\n"
            + "  }\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='test()'>\n"
            + "  <div></div>\n"
            + "  <ul id='something'></ul>\n"
            + "  <p></p>\n"
            + "  <ul id='thing2'></ul>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(DEFAULT = "0")
    public void suffixAttributeEmpty() throws Exception {
        final String html = "<html><head><title>First</title>\n"
            + "<meta http-equiv='X-UA-Compatible' content='IE=edge'>\n"
            + "<script>\n"
            + "function test() {\n"
            + "  if (document.querySelectorAll) {\n"
            + "    var list = document.querySelectorAll('[id$=\"\"]');\n"
            + "    alert(list.length);\n"
            + "  }\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='test()'>\n"
            + "  <div></div>\n"
            + "  <ul id='something'></ul>\n"
            + "  <p></p>\n"
            + "  <ul id='thing2'></ul>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts({ "2", "something", "thing2" })
    public void substringAttribute() throws Exception {
        final String html = "<html><head><title>First</title>\n"
            + "<meta http-equiv='X-UA-Compatible' content='IE=edge'>\n"
            + "<script>\n"
            + "function test() {\n"
            + "  if (document.querySelectorAll) {\n"
            + "    var list = document.querySelectorAll('[id*=\"thing\"]');\n"
            + "    alert(list.length);\n"
            + "    alert(list[0].id);\n"
            + "    alert(list[1].id);\n"
            + "  }\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='test()'>\n"
            + "  <div></div>\n"
            + "  <ul id='something'></ul>\n"
            + "  <p></p>\n"
            + "  <ul id='thing2'></ul>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(DEFAULT = "0")
    public void substringAttributeEmpty() throws Exception {
        final String html = "<html><head><title>First</title>\n"
            + "<meta http-equiv='X-UA-Compatible' content='IE=edge'>\n"
            + "<script>\n"
            + "function test() {\n"
            + "  if (document.querySelectorAll) {\n"
            + "    var list = document.querySelectorAll('[id*=\"\"]');\n"
            + "    alert(list.length);\n"
            + "  }\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='test()'>\n"
            + "  <div></div>\n"
            + "  <ul id='something'></ul>\n"
            + "  <p></p>\n"
            + "  <ul id='thing2'></ul>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts({ "2", "id1", "id2" })
    public void oneOfAttribute() throws Exception {
        final String html = "<html><head><title>First</title>\n"
            + "<meta http-equiv='X-UA-Compatible' content='IE=edge'>\n"
            + "<script>\n"
            + "function test() {\n"
            + "  if (document.querySelectorAll) {\n"
            + "    var list = document.querySelectorAll('[title~=\"w2\"]');\n"
            + "    alert(list.length);\n"
            + "    alert(list[0].id);\n"
            + "    alert(list[1].id);\n"
            + "  }\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='test()'>\n"
            + "  <div></div>\n"
            + "  <ul id='id1' title='w1 w2 w3'></ul>\n"
            + "  <p id='id2' title='w2'></p>\n"
            + "  <ul id='id3' title='w1w2 w3'></ul>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts("0")
    public void oneOfAttributeEmpty() throws Exception {
        final String html = "<html><head><title>First</title>\n"
            + "<meta http-equiv='X-UA-Compatible' content='IE=edge'>\n"
            + "<script>\n"
            + "function test() {\n"
            + "  if (document.querySelectorAll) {\n"
            + "    var list = document.querySelectorAll('[title~=\"\"]');\n"
            + "    alert(list.length);\n"
            + "  }\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='test()'>\n"
            + "  <div></div>\n"
            + "  <ul id='id1' title='w1 w2 w3'></ul>\n"
            + "  <p id='id2' title='w2'></p>\n"
            + "  <ul id='id3' title='w1w2 w3'></ul>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts({ "2", "ul2", "ul3" })
    public void generalAdjacentSelector() throws Exception {
        final String html = "<html><head><title>First</title>\n"
            + "<meta http-equiv='X-UA-Compatible' content='IE=edge'>\n"
            + "<script>\n"
            + "function test() {\n"
            + "  if (document.querySelectorAll) {\n"
            + "    var list = document.querySelectorAll('div~ul');\n"
            + "    alert(list.length);\n"
            + "    for (var i = 0 ; i < list.length; i++) {\n"
            + "      alert(list[i].id);\n"
            + "    }\n"
            + "  }\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='test()'>\n"
            + "  <div></div>\n"
            + "  <p></p>\n"
            + "  <ul id='ul2'></ul>\n"
            + "  <ul id='ul3'></ul>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(DEFAULT = { "li3", "2", "li1", "li3" },
            IE8 = "exception")
    public void nth_last_child() throws Exception {
        final String html = "<html><head><title>First</title>\n"
            + "<meta http-equiv='X-UA-Compatible' content='IE=edge'>\n"
            + "<script>\n"
            + "function test() {\n"
            + "  if (document.querySelectorAll) {\n"
            + "    try {\n"
            + "      alert(document.querySelectorAll('li:nth-last-child(1)')[0].id);\n"

            + "      alert(document.querySelectorAll('li:nth-last-child(odd)').length);\n"
            + "      alert(document.querySelectorAll('li:nth-last-child(odd)')[0].id);\n"
            + "      alert(document.querySelectorAll('li:nth-last-child(odd)')[1].id);\n"
            + "    } catch(e) {alert('exception')}\n"
            + "  }\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='test()'>\n"
            + "<ul>\n"
            + "  <li id='li1'></li>\n"
            + "  <li id='li2'></li>\n"
            + "  <li id='li3'></li>\n"
            + "</ul>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(DEFAULT = { "2", "div1", "div3", "2", "div1", "div3", "2", "div1", "div3", "0" },
            IE8 = "exception")
    public void nth_last_child2() throws Exception {
        final String html = "<html><head><title>First</title>\n"
            + "<meta http-equiv='X-UA-Compatible' content='IE=edge'>\n"
            + "<script>\n"
            + "function test() {\n"
            + "  if (document.querySelectorAll) {\n"
            + "    try {\n"
            + "      alert(document.querySelectorAll('.nthchild1 > :nth-last-child(odd)').length);\n"
            + "      alert(document.querySelectorAll('.nthchild1 > :nth-last-child(odd)')[0].id);\n"
            + "      alert(document.querySelectorAll('.nthchild1 > :nth-last-child(odd)')[1].id);\n"

            + "      elem = document.getElementById('root');\n"
            + "      alert(elem.querySelectorAll('.nthchild1 > :nth-last-child(odd)').length);\n"
            + "      alert(elem.querySelectorAll('.nthchild1 > :nth-last-child(odd)')[0].id);\n"
            + "      alert(elem.querySelectorAll('.nthchild1 > :nth-last-child(odd)')[1].id);\n"

            + "      elem = document.getElementById('parent');\n"
            + "      alert(elem.querySelectorAll('.nthchild1 > :nth-last-child(odd)').length);\n"
            + "      alert(elem.querySelectorAll('.nthchild1 > :nth-last-child(odd)')[0].id);\n"
            + "      alert(elem.querySelectorAll('.nthchild1 > :nth-last-child(odd)')[1].id);\n"

            + "      elem = document.getElementById('div1');\n"
            + "      alert(elem.querySelectorAll('.nthchild1 > :nth-last-child(odd)').length);\n"
            + "    } catch(e) {alert('exception')}\n"
            + "  }\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='test()'>\n"
            + "<div id='root'>\n"
            + " <div id='parent' class='nthchild1'>\n"
            + "  <div id='div1'>1</div>\n"
            + "  <div id='div2'>2</div>\n"
            + "  <div id='div3'>3</div>\n"
            + " </div>\n"
            + "</div>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(DEFAULT = "id3",
            IE8 = "exception")
    public void nth_of_type() throws Exception {
        final String html = "<html><head><title>First</title>\n"
            + "<meta http-equiv='X-UA-Compatible' content='IE=edge'>\n"
            + "<script>\n"
            + "function test() {\n"
            + "  if (document.querySelectorAll) {\n"
            + "    try {\n"
            + "      alert(document.querySelectorAll('p:nth-of-type(2)')[0].id);\n"
            + "    } catch(e) {alert('exception')}\n"
            + "  }\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='test()'>\n"
            + "<section>\n"
            + "  <h1 id='id11'></h1>\n"
            + "  <p id='id2'></p>\n"
            + "  <p id='id3'></p>\n"
            + "</section>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(DEFAULT = "id3",
            IE8 = "exception")
    public void nth_last_of_type() throws Exception {
        final String html = "<html><head><title>First</title>\n"
            + "<meta http-equiv='X-UA-Compatible' content='IE=edge'>\n"
            + "<script>\n"
            + "function test() {\n"
            + "  if (document.querySelectorAll) {\n"
            + "    try {\n"
            + "      alert(document.querySelectorAll('p:nth-last-of-type(1)')[0].id);\n"
            + "    } catch(e) {alert('exception')}\n"
            + "  }\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='test()'>\n"
            + "<section>\n"
            + "  <h1 id='id1'></h1>\n"
            + "  <p  id='id2'></p>\n"
            + "  <p  id='id3'></p>\n"
            + "</section>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(DEFAULT = { },
            IE6 = { "li1" },
            IE8 = { "li1" })
    @NotYetImplemented(IE)
    public void pseudoAfter() throws Exception {
        final String html
            = HtmlPageTest.STANDARDS_MODE_PREFIX_ + "<html><head><title>Pseudo-After</title><script>\n"
            + "function test() {\n"
            + "  if (document.querySelectorAll) {\n"
            + "    try {\n"
            + "      var list = document.querySelectorAll('#li1:after');\n"
            + "      for (var i = 0 ; i < list.length; i++) {\n"
            + "        alert(list[i].id);\n"
            + "      }\n"
            + "    } catch (e) {alert('exception')}\n"
            + "  }\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='test()'>\n"
            + "<ul>\n"
            + "  <li id='li1'></li>\n"
            + "  <li id='li2'></li>\n"
            + "</ul>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts("li1")
    public void first_child() throws Exception {
        final String html = "<html><head><title>First</title>\n"
            + "<meta http-equiv='X-UA-Compatible' content='IE=edge'>\n"
            + "<script>\n"
            + "function test() {\n"
            + "  if (document.querySelectorAll) {\n"
            + "   alert(document.querySelectorAll('li:first-child')[0].id);\n"
            + "  }\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='test()'>\n"
            + "<ul>\n"
            + "  <li id='li1'></li>\n"
            + "  <li id='li2'></li>\n"
            + "  <li id='li3'></li>\n"
            + "</ul>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(DEFAULT = "li3",
            IE8 = "exception")
    public void last_child() throws Exception {
        final String html = "<html><head><title>First</title>\n"
            + "<meta http-equiv='X-UA-Compatible' content='IE=edge'>\n"
            + "<script>\n"
            + "function test() {\n"
            + "  if (document.querySelectorAll) {\n"
            + "    try {\n"
            + "      alert(document.querySelectorAll('li:last-child')[0].id);\n"
            + "    } catch (e) {alert('exception')}\n"
            + "  }\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='test()'>\n"
            + "<ul>\n"
            + "  <li id='li1'></li>\n"
            + "  <li id='li2'></li>\n"
            + "  <li id='li3'></li>\n"
            + "</ul>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(DEFAULT = "id2",
            IE8 = "exception")
    public void first_of_type() throws Exception {
        final String html = "<html><head><title>First</title>\n"
            + "<meta http-equiv='X-UA-Compatible' content='IE=edge'>\n"
            + "<script>\n"
            + "function test() {\n"
            + "  if (document.querySelectorAll) {\n"
            + "    try {\n"
            + "      alert(document.querySelectorAll('p:first-of-type')[0].id);\n"
            + "    } catch(e) {alert('exception')}\n"
            + "  }\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='test()'>\n"
            + "<section>\n"
            + "  <h1 id='id1'></h1>\n"
            + "  <p  id='id2'></p>\n"
            + "  <h1 id='id3'></h1>\n"
            + "  <p  id='id4'></p>\n"
            + "  <h1 id='id5'></h1>\n"
            + "</section>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(DEFAULT = "id4",
            IE8 = "exception")
    public void last_of_type() throws Exception {
        final String html = "<html><head><title>First</title>\n"
            + "<meta http-equiv='X-UA-Compatible' content='IE=edge'>\n"
            + "<script>\n"
            + "function test() {\n"
            + "  if (document.querySelectorAll) {\n"
            + "    try {\n"
            + "      alert(document.querySelectorAll('p:last-of-type')[0].id);\n"
            + "    } catch(e) {alert('exception')}\n"
            + "  }\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='test()'>\n"
            + "<section>\n"
            + "  <h1 id='id1'></h1>\n"
            + "  <p  id='id2'></p>\n"
            + "  <h1 id='id3'></h1>\n"
            + "  <p  id='id4'></p>\n"
            + "  <h1 id='id5'></h1>\n"
            + "</section>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(DEFAULT = "id3",
            IE8 = "exception")
    public void only_child() throws Exception {
        final String html = "<html><head><title>First</title>\n"
            + "<meta http-equiv='X-UA-Compatible' content='IE=edge'>\n"
            + "<script>\n"
            + "function test() {\n"
            + "  if (document.querySelectorAll) {\n"
            + "    try {\n"
            + "      alert(document.querySelectorAll('h1:only-child')[0].id);\n"
            + "    } catch(e) {alert('exception')}\n"
            + "  }\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='test()'>\n"
            + "<section>\n"
            + "  <h1 id='id1'></h1>\n"
            + "  <p  id='id2'></p>\n"
            + "</section>\n"
            + "<section>\n"
            + "  <h1 id='id3'></h1>\n"
            + "</section>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(DEFAULT = "id3",
            IE8 = "exception")
    public void only_of_type() throws Exception {
        final String html = "<html><head><title>First</title>\n"
            + "<meta http-equiv='X-UA-Compatible' content='IE=edge'>\n"
            + "<script>\n"
            + "function test() {\n"
            + "  if (document.querySelectorAll) {\n"
            + "    try {\n"
            + "      alert(document.querySelectorAll('p:only-of-type')[0].id);\n"
            + "    } catch(e) {alert('exception')}\n"
            + "  }\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='test()'>\n"
            + "<section>\n"
            + "  <p  id='id1'></p>\n"
            + "  <p  id='id2'></p>\n"
            + "</section>\n"
            + "<section>\n"
            + "  <p  id='id3'></p>\n"
            + "</section>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(DEFAULT = { "id2", "span1" },
            IE8 = "exception")
    public void empty() throws Exception {
        final String html = "<html><head><title>First</title>\n"
            + "<meta http-equiv='X-UA-Compatible' content='IE=edge'>\n"
            + "<script>\n"
            + "function test() {\n"
            + "  if (document.querySelectorAll) {\n"
            + "    try {\n"
            + "      alert(document.querySelectorAll('p:empty')[0].id);\n"
            + "      alert(document.querySelectorAll('span:empty')[0].id);\n"
            + "    } catch(e) {alert('exception')}\n"
            + "  }\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='test()'>\n"
            + "  <p id='id1'>Hello, World!</p>\n"
            + "  <p id='id2'></p>\n"
            + "  <span id='span1'><!-- a comment --></span>\n"
            + "  <span id='span2'>a text</span>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(DEFAULT = "id2",
            IE8 = "exception")
    public void not() throws Exception {
        final String html = "<html><head><title>First</title>\n"
            + "<meta http-equiv='X-UA-Compatible' content='IE=edge'>\n"
            + "<script>\n"
            + "function test() {\n"
            + "  if (document.querySelectorAll) {\n"
            + "    try {\n"
            + "      alert(document.querySelectorAll('input:not([type=\"file\"])')[0].id);\n"
            + "    } catch(e) {alert('exception')}\n"
            + "  }\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='test()'>\n"
            + "  <input id='id1' type='file'>\n"
            + "  <input id='id2'>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(DEFAULT = { "2", "item_2", "item_3" },
            IE8 = "exception")
    public void childNot() throws Exception {
        final String html = "<html><head><title>First</title>\n"
            + "<meta http-equiv='X-UA-Compatible' content='IE=9'>\n"
            + "<script>\n"
            + "function test() {\n"
            + "  if (document.querySelectorAll) {\n"
            + "    try {\n"
            + "      var res = document.querySelectorAll('#list li:not(#item_1)');\n"
            + "      alert(res.length);\n"
            + "      alert(res[0].id);\n"
            + "      alert(res[1].id);\n"
            + "    } catch(e) {alert('exception')}\n"
            + "  }\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='test()'>\n"
            + "  <ul id='list'>\n"
            + "    <li id='item_1'>1</li>\n"
            + "    <li id='item_2'>2</li>\n"
            + "    <li id='item_3'>3</li>\n"
            + "  </ul>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(DEFAULT = { "1", "item_2" },
            IE8 = "exception")
    public void childNotNot() throws Exception {
        final String html = "<html><head><title>First</title>\n"
            + "<meta http-equiv='X-UA-Compatible' content='IE=9'>\n"
            + "<script>\n"
            + "function test() {\n"
            + "  if (document.querySelectorAll) {\n"
            + "    try {\n"
            + "      var res = document.querySelectorAll('#list li:not(#item_1):not(#item_3)');\n"
            + "      alert(res.length);\n"
            + "      alert(res[0].id);\n"
            + "    } catch(e) {alert('exception')}\n"
            + "  }\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='test()'>\n"
            + "  <ul id='list'>\n"
            + "    <li id='item_1'>1</li>\n"
            + "    <li id='item_2'>2</li>\n"
            + "    <li id='item_3'>3</li>\n"
            + "  </ul>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(DEFAULT = { "1", "[object HTMLHtmlElement]", "1", "[object HTMLInputElement]", "id2" },
            FF = { "0", "undefined", "1", "[object HTMLInputElement]", "id2" },
            IE10 = { "1", "[object HTMLBodyElement]", "1", "[object HTMLInputElement]", "id2" })
    @BuggyWebDriver({ Browser.FF10, Browser.FF17 })
    public void focus() throws Exception {
        final String html = "<html><head><title>First</title>\n"
            + "<meta http-equiv='X-UA-Compatible' content='IE=edge'>\n"
            + "<script>\n"
            + "function test() {\n"
            + "  if (document.querySelectorAll) {\n"
            + "    try {\n"
            + "      found = document.querySelectorAll(':focus');\n"
            + "      alert(found.length);\n"
            + "      alert(found[0]);\n"
            + "    } catch(e) {alert('exception')}\n"
            + "  }\n"
            + "\n"
            + "  document.getElementById('id2').focus();\n"
            + "\n"
            + "  if (document.querySelectorAll) {\n"
            + "    try {\n"
            + "      found = document.querySelectorAll(':focus');\n"
            + "      alert(found.length);\n"
            + "      alert(found[0]);\n"
            + "      alert(found[0].id);\n"
            + "    } catch(e) {alert('exception')}\n"
            + "  }\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='test()'>\n"
            + "  <input id='id1'>\n"
            + "  <input id='id2'>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(DEFAULT = { "1", "id1", },
            IE6 = "exception",
            IE8 = "exception")
    public void enabled() throws Exception {
        final String html = "<html><head><title>First</title>\n"
            + "<meta http-equiv='X-UA-Compatible' content='IE=edge'>\n"
            + "<script>\n"
            + "function test() {\n"
            + "  if (document.querySelectorAll) {\n"
            + "    try {\n"
            + "      found = document.querySelectorAll('input:enabled');\n"
            + "      alert(found.length);\n"
            + "      alert(found[0].id);\n"
            + "    } catch(e) {alert('exception')}\n"
            + "  }\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='test()'>\n"
            + "  <input id='id1' >\n"
            + "  <input id='id2' disabled='disabled'>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(DEFAULT = { "1", "id2" },
            IE6 = "exception",
            IE8 = "exception")
    public void disabled() throws Exception {
        final String html = "<html><head><title>First</title>\n"
            + "<meta http-equiv='X-UA-Compatible' content='IE=edge'>\n"
            + "<script>\n"
            + "function test() {\n"
            + "  if (document.querySelectorAll) {\n"
            + "    try {\n"
            + "      found = document.querySelectorAll('input:disabled');\n"
            + "      alert(found.length);\n"
            + "      alert(found[0].id);\n"
            + "    } catch(e) {alert('exception')}\n"
            + "  }\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='test()'>\n"
            + "  <input id='id1' >\n"
            + "  <input id='id2' disabled='disabled'>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(DEFAULT = { "1", "id2" },
            IE6 = "exception",
            IE8 = "exception")
    public void target() throws Exception {
        final String html = "<html><head><title>First</title>\n"
            + "<meta http-equiv='X-UA-Compatible' content='IE=edge'>\n"
            + "<script>\n"
            + "function test() {\n"
            + "  if (document.querySelectorAll) {\n"
            + "    try {\n"
            + "      found = document.querySelectorAll(':target');\n"
            + "      alert(found.length);\n"
            + "      alert(found[0].id);\n"
            + "    } catch(e) {alert('exception')}\n"
            + "  }\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='test()'>\n"
            + "  <input id='id1' >\n"
            + "  <input id='id2'>\n"
            + "</body></html>";

        getMockWebConnection().setDefaultResponse(html);
        loadPageWithAlerts2(UrlUtils.getUrlWithNewRef(URL_FIRST, "id2"));
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(DEFAULT = { "0" },
            IE6 = "exception",
            IE8 = "exception")
    public void targetNoHash() throws Exception {
        final String html = "<html><head><title>First</title>\n"
            + "<meta http-equiv='X-UA-Compatible' content='IE=edge'>\n"
            + "<script>\n"
            + "function test() {\n"
            + "  if (document.querySelectorAll) {\n"
            + "    try {\n"
            + "      found = document.querySelectorAll(':target');\n"
            + "      alert(found.length);\n"
            + "    } catch(e) {alert('exception')}\n"
            + "  }\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='test()'>\n"
            + "  <input id='id1' >\n"
            + "  <input id='id2'>\n"
            + "</body></html>";

        getMockWebConnection().setDefaultResponse(html);
        loadPageWithAlerts2(URL_FIRST);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(DEFAULT = { "0" },
            IE6 = "exception",
            IE8 = "exception")
    public void targetUnknown() throws Exception {
        final String html = "<html><head><title>First</title>\n"
            + "<meta http-equiv='X-UA-Compatible' content='IE=edge'>\n"
            + "<script>\n"
            + "function test() {\n"
            + "  if (document.querySelectorAll) {\n"
            + "    try {\n"
            + "      found = document.querySelectorAll(':target');\n"
            + "      alert(found.length);\n"
            + "    } catch(e) {alert('exception')}\n"
            + "  }\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='test()'>\n"
            + "  <input id='id1' >\n"
            + "  <input id='id2'>\n"
            + "</body></html>";

        getMockWebConnection().setDefaultResponse(html);
        loadPageWithAlerts2(UrlUtils.getUrlWithNewRef(URL_FIRST, "id3"));
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts(DEFAULT = { "first", "second" },
            IE8 = { "exception", "exception" })
    public void escapedAttributeValue() throws Exception {
        final String html = "<html><head>\n"
            + "<meta http-equiv='X-UA-Compatible' content='IE=edge'>\n"
            + "</head><body>\n"
            + "  <input id='first' name='foo[bar]'>\n"
            + "  <input id='second' name='foo.bar'>\n"
            + "<script>\n"
            + "try {\n"
            + "  alert(document.querySelectorAll('input[name=foo\\\\[bar\\\\]]')[0].id);\n"
            + "} catch(e) {alert('exception')}\n"
            + "try {\n"
            + "  alert(document.querySelectorAll('input[name=foo\\\\.bar]')[0].id);\n"
            + "} catch(e) {alert('exception')}\n"
            + "</script></body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts({ "first", "second", "third" })
    public void escapedClassName() throws Exception {
        final String html = "<html><head>\n"
            + "<meta http-equiv='X-UA-Compatible' content='IE=edge'>\n"
            + "</head><body>\n"
            + "  <input id='first' class='foo[bar]'>\n"
            + "  <input id='second' class='foo.bar'>\n"
            + "  <input id='third' class='foo:bar'>\n"
            + "<script>\n"
            + "try {\n"
            + "  alert(document.querySelectorAll('.foo\\\\[bar\\\\]')[0].id);\n"
            + "  alert(document.querySelectorAll('.foo\\\\.bar')[0].id);\n"
            + "  alert(document.querySelectorAll('.foo\\\\:bar')[0].id);\n"
            + "} catch(e) {alert('exception')}\n"
            + "</script></body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts("exception")
    public void invalidSelectors() throws Exception {
        final String html
            = HtmlPageTest.STANDARDS_MODE_PREFIX_ + "<html><head><title>Invalid Selectors</title><script>\n"
            + "function test() {\n"
            + "  if (document.querySelectorAll) {\n"
            + "    try {\n"
            + "      var list = document.querySelectorAll('li:foo() ~ li');\n"
            + "      alert(list.length);\n"
            + "    } catch (e) {alert('exception')}\n"
            + "  }\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='test()'>\n"
            + "<ul id='ul'>\n"
            + "  <li id='li1'></li>\n"
            + "  <li id='li2'></li>\n"
            + "</ul>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }
}
