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

import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xml.sax.helpers.AttributesImpl;

import com.gargoylesoftware.htmlunit.BrowserRunner;
import com.gargoylesoftware.htmlunit.BrowserRunner.Alerts;
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.SimpleWebTestCase;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode.DescendantElementsIterator;
import com.gargoylesoftware.htmlunit.xml.XmlPage;

/**
 * Tests for {@link DomNode}.
 *
 * @version $Revision: 7974 $
 * @author Chris Erskine
 * @author Ahmed Ashour
 */
@RunWith(BrowserRunner.class)
public class DomNodeTest extends SimpleWebTestCase {

    /**
     * Test hasAttributes() on an element with attributes.
     * @throws Exception if the test fails
     */
    @Test
    public void testElementHasAttributesWith() throws Exception {
        final String content = "<html><head></head><body id='tag'>text</body></html>";
        final HtmlPage page = loadPage(content);

        final DomNode node = page.getElementById("tag");
        Assert.assertTrue("Element should have attribute", node.hasAttributes());
    }

    /**
     * Test hasAttributes() on an element with no attributes.
     * @throws Exception if the test fails
     */
    @Test
    public void testElementHasAttributesNone() throws Exception {
        final String content = "<html><head></head><body id='tag'>text</body></html>";
        final HtmlPage page = loadPage(content);

        final DomNode node = page.getElementById("tag");
        final DomNode parent = node.getParentNode();
        Assert.assertFalse("Element should not have attribute", parent.hasAttributes());
    }

    /**
     * Test hasAttributes on a node that is not defined to have attributes.
     * @throws Exception if the test fails
     */
    @Test
    public void testNonElementHasAttributes() throws Exception {
        final String content = "<html><head></head><body id='tag'>text</body></html>";
        final HtmlPage page = loadPage(content);

        final DomNode node = page.getElementById("tag");
        final DomNode child = node.getFirstChild();
        Assert.assertFalse("Text should not have attribute", child.hasAttributes());
    }

    /**
     * Test getPrefix on a node that is not defined to have a prefix.
     * @throws Exception if the test fails
     */
    @Test
    public void testNonElementGetPrefix() throws Exception {
        final String content = "<html><head></head><body id='tag'>text</body></html>";
        final HtmlPage page = loadPage(content);

        final DomNode node = page.getElementById("tag");
        final DomNode child = node.getFirstChild();
        Assert.assertEquals("Text should not have a prefix", null, child.getPrefix());
    }

    /**
     * Test getNamespaceURI on a node that is not defined to have a namespace.
     * @throws Exception if the test fails
     */
    @Test
    public void testNonElementGetNamespaceURI() throws Exception {
        final String content = "<html><head></head><body id='tag'>text</body></html>";
        final HtmlPage page = loadPage(content);

        final DomNode node = page.getElementById("tag");
        final DomNode child = node.getFirstChild();
        Assert.assertEquals("Text should not have a prefix", null, child.getNamespaceURI());
    }

    /**
     * Test getLocalName on a node that is not defined to have a local name.
     * @throws Exception if the test fails
     */
    @Test
    public void testNonElementGetLocalName() throws Exception {
        final String content = "<html><head></head><body id='tag'>text</body></html>";
        final HtmlPage page = loadPage(content);

        final DomNode node = page.getElementById("tag");
        final DomNode child = node.getFirstChild();
        Assert.assertEquals("Text should not have a prefix", null, child.getLocalName());
    }

    /**
     * Test setPrefix on a node that is not defined to have a prefix.
     * @throws Exception if the test fails
     */
    @Test
    public void testNonElementSetPrefix() throws Exception {
        final String content = "<html><head></head><body id='tag'>text</body></html>";
        final HtmlPage page = loadPage(content);

        final DomNode node = page.getElementById("tag");
        final DomNode child = node.getFirstChild();
        child.setPrefix("bar"); // This does nothing.
        Assert.assertEquals("Text should not have a prefix", null, child.getPrefix());
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void testRemoveAllChildren() throws Exception {
        final String content
            = "<html><head></head><body>\n"
            + "<p id='tag'><table>\n"
            + "<tr><td>row 1</td></tr>\n"
            + "<tr><td>row 2</td></tr>\n"
            + "</table></p></body></html>";
        final HtmlPage page = loadPage(content);

        final DomNode node = page.getElementById("tag");
        node.removeAllChildren();
        Assert.assertEquals("Did not remove all nodes", null, node.getFirstChild());
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void testReplace() throws Exception {
        final String content
            = "<html><head></head><body>\n"
            + "<br><div id='tag'></div><br><div id='tag2'/></body></html>";
        final HtmlPage page = loadPage(content);

        final DomNode node = page.getElementById("tag");

        final DomNode previousSibling = node.getPreviousSibling();
        final DomNode nextSibling = node.getNextSibling();
        final DomNode parent = node.getParentNode();

        // position among parent's children
        final int position = readPositionAmongParentChildren(node);

        final DomNode newNode = new DomText(page, "test");
        node.replace(newNode);
        assertSame("previous sibling", previousSibling, newNode.getPreviousSibling());
        assertSame("next sibling", nextSibling, newNode.getNextSibling());
        assertSame("parent", parent, newNode.getParentNode());
        assertSame(newNode, previousSibling.getNextSibling());
        assertSame(newNode, nextSibling.getPreviousSibling());
        assertEquals(position, readPositionAmongParentChildren(newNode));

        final AttributesImpl attributes = new AttributesImpl();
        attributes.addAttribute(null, "id", "id", null, "tag2"); // with the same id as the node to replace
        final DomNode node2 = page.getHtmlElementById("tag2");
        assertEquals("div", node2.getNodeName());

        final DomNode node3 = HTMLParser.getFactory(HtmlSpan.TAG_NAME).createElement(
                page, HtmlSpan.TAG_NAME, attributes);
        node2.replace(node3);
        assertEquals("span", page.getHtmlElementById("tag2").getTagName());
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void testGetNewNodeById() throws Exception {
        final String content
            = "<html><head></head><body>\n"
            + "<br><div id='tag'/></body></html>";
        final HtmlPage page = loadPage(content);

        final DomNode node = page.getElementById("tag");

        final AttributesImpl attributes = new AttributesImpl();
        attributes.addAttribute(null, "id", "id", null, "newElt");
        final DomNode newNode = HTMLParser.getFactory(HtmlDivision.TAG_NAME).createElement(
                page, HtmlDivision.TAG_NAME, attributes);
        try {
            page.getHtmlElementById("newElt");
            fail("Element should not exist yet");
        }
        catch (final ElementNotFoundException e) {
            // nothing to do, it's ok
        }

        node.replace(newNode);

        page.getHtmlElementById("newElt");
        try {
            page.getHtmlElementById("tag");
            fail("Element should not exist anymore");
        }
        catch (final ElementNotFoundException e) {
            // nothing to do, it's ok
        }

        newNode.insertBefore(node);
        page.getHtmlElementById("tag");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void appendChild() throws Exception {
        final String content
            = "<html><head></head><body>\n"
            + "<br><div><div id='tag'></div></div><br></body></html>";
        final HtmlPage page = loadPage(content);

        final DomNode node = page.getElementById("tag");

        final DomNode parent = node.getParentNode();

        // position among parent's children
        final int position = readPositionAmongParentChildren(node);

        final DomNode newNode = new DomText(page, "test");
        parent.appendChild(newNode);
        assertSame("new node previous sibling", node, newNode.getPreviousSibling());
        assertSame("new node next sibling", null, newNode.getNextSibling());
        assertSame("next sibling", newNode, node.getNextSibling());
        assertSame("parent", parent, newNode.getParentNode());
        assertEquals(position + 1, readPositionAmongParentChildren(newNode));

        final DomNode newNode2 = new DomText(page, "test2");
        parent.appendChild(newNode2);
        page.getHtmlElementById("tag");
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void testInsertBefore() throws Exception {
        final String content
            = "<html><head></head><body>\n"
            + "<br><div id='tag'></div><br></body></html>";
        final HtmlPage page = loadPage(content);

        final DomNode node = page.getElementById("tag");

        final DomNode previousSibling = node.getPreviousSibling();
        final DomNode nextSibling = node.getNextSibling();
        final DomNode parent = node.getParentNode();

        // position among parent's children
        final int position = readPositionAmongParentChildren(node);

        final DomNode newNode = new DomText(page, "test");
        node.insertBefore(newNode);
        assertSame("new node previous sibling", previousSibling, newNode.getPreviousSibling());
        assertSame("previous sibling", newNode, node.getPreviousSibling());
        assertSame("new node next sibling", node, newNode.getNextSibling());
        assertSame("next sibling", nextSibling, node.getNextSibling());
        assertSame("parent", parent, newNode.getParentNode());
        assertSame(newNode, previousSibling.getNextSibling());
        assertSame(node, nextSibling.getPreviousSibling());
        assertEquals(position, readPositionAmongParentChildren(newNode));
    }

    /**
     * Reads the position of the node among the children of its parent
     * @param node the node to look at
     * @return the position
     */
    private int readPositionAmongParentChildren(final DomNode node) {
        int i = 0;
        for (final DomNode child : node.getParentNode().getChildren()) {
            if (child == node) {
                return i;
            }
            i++;
        }

        return -1;
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void testGetByXPath() throws Exception {
        final String htmlContent
            = "<html><head><title>my title</title></head><body>\n"
            + "<div id='d1'><ul><li>foo 1</li><li>foo 2</li></ul></div>\n"
            + "<div><span>bla</span></div>\n"
            + "</body></html>";
        final HtmlPage page = loadPage(htmlContent);

        final List<?> results = page.getByXPath("//title");
        assertEquals(1, results.size());
        final HtmlTitle title = (HtmlTitle) results.get(0);
        assertEquals("my title", title.asText());

        final HtmlHead head = (HtmlHead) title.getParentNode();
        assertEquals(results, head.getByXPath("//title"));
        assertEquals(results, head.getByXPath("./title"));
        assertEquals(0, head.getByXPath("/title").size());
        assertEquals(results, head.getByXPath("title"));

        final HtmlElement div = page.getFirstByXPath("//div");
        assertSame(div, page.getHtmlElementById("d1"));
        final List<?> lis = div.getByXPath("ul/li");
        assertEquals(2, lis.size());
        assertEquals(lis, page.getByXPath("//ul/li"));

        assertEquals(2, div.<Number>getFirstByXPath("count(//li)").intValue());
    }

    /**
     * Regression test for bug 3035213: xmlns value has to be trimmed.
     * @throws Exception if the test fails
     */
    @Test
    public void getByXPath_trim_namespace() throws Exception {
        final String html = "<html xmlns=' http://www.w3.org/1999/xhtml'>\n"
            + "<body>\n"
            + "<div><span>bla</span></div>\n"
            + "</body></html>";
        final HtmlPage page = loadPage(html);

        final List<?> results = page.getByXPath("//div");
        assertEquals(1, results.size());
    }

    /**
     * Test that element.selectNodes("/tagName") searches from root of the tree, not from that specific element.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(FF = { "book", "exception" } , IE = { "book", "0", "1" })
    public void selectNodes() throws Exception {
        final String html = "<html><head><title>foo</title><script>\n"
            + "  function test() {\n"
            + "    var doc = createXmlDocument();\n"
            + "    doc.async = false;\n"
            + "    doc.load('foo.xml');\n"
            + "    var child = doc.documentElement.firstChild;\n"
            + "    alert(child.tagName);\n"
            + "    try {\n"
            + "      alert(child.selectNodes('/title').length);\n"
            + "      alert(child.selectNodes('title').length);\n"
            + "    } catch (e) { alert('exception') }\n"
            + "  }\n"
            + "  function createXmlDocument() {\n"
            + "    if (document.implementation && document.implementation.createDocument)\n"
            + "      return document.implementation.createDocument('', '', null);\n"
            + "    else if (window.ActiveXObject)\n"
            + "      return new ActiveXObject('Microsoft.XMLDOM');\n"
            + "  }\n"
            + "</script></head><body onload='test()'>\n"
            + "</body></html>";

        final String xml = "<books><book><title>Immortality</title><author>John Smith</author></book></books>";

        getMockWebConnection().setDefaultResponse(xml, "text/xml");

        loadPageWithAlerts(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void testGetFirstByXPath() throws Exception {
        final String htmlContent
            = "<html><head><title>my title</title></head><body>\n"
            + "<div id='d1'><ul><li>foo 1</li><li>foo 2</li></ul></div>\n"
            + "<div><span>bla</span></div>\n"
            + "</body></html>";
        final HtmlPage page = loadPage(htmlContent);

        final HtmlTitle title = page.getFirstByXPath("//title");
        assertEquals("my title", title.asText());

        final HtmlHead head = (HtmlHead) title.getParentNode();
        assertSame(title, head.getFirstByXPath("//title"));
        assertSame(title, head.getFirstByXPath("./title"));
        assertNull(head.getFirstByXPath("/title"));
        assertSame(title, head.getFirstByXPath("title"));

        final HtmlElement div = page.getFirstByXPath("//div");
        assertSame(div, page.getHtmlElementById("d1"));
        final HtmlListItem listItem = (HtmlListItem) div.getFirstByXPath("ul/li");
        assertSame(listItem, page.getFirstByXPath("//ul/li"));

        assertEquals(2, ((Number) div.getFirstByXPath("count(//li)")).intValue());
    }

    /**
     * Verifies that {@link DomNode#getHtmlElementDescendants()} returns descendant elements in the correct order.
     * @throws Exception if an error occurs
     */
    @Test
    public void testGetHtmlElementDescendantsOrder() throws Exception {
        final String html = "<html><body id='0'>\n"
            + "<span id='I'><span id='I.1'><span id='I.1.a'/><span id='I.1.b'/><span id='I.1.c'/></span>\n"
            + "<span id='I.2'><span id='I.2.a'/></span></span>\n"
            + "<span id='II'/>\n"
            + "<span id='III'><span id='III.1'><span id='III.1.a'/></span></span>\n"
            + "</body></html>";
        final HtmlPage page = loadPage(html);
        final DescendantElementsIterator<HtmlElement> iterator = (DescendantElementsIterator<HtmlElement>)
            page.getDocumentElement().getHtmlElementDescendants().iterator();
        assertEquals("", iterator.nextNode().getId());
        assertEquals("0", iterator.nextNode().getId());
        assertEquals("I", iterator.nextNode().getId());
        assertEquals("I.1", iterator.nextNode().getId());
        assertEquals("I.1.a", iterator.nextNode().getId());
        assertEquals("I.1.b", iterator.nextNode().getId());
        assertEquals("I.1.c", iterator.nextNode().getId());
        assertEquals("I.2", iterator.nextNode().getId());
        assertEquals("I.2.a", iterator.nextNode().getId());
        assertEquals("II", iterator.nextNode().getId());
        assertEquals("III", iterator.nextNode().getId());
        assertEquals("III.1", iterator.nextNode().getId());
        assertEquals("III.1.a", iterator.nextNode().getId());
        assertFalse(iterator.hasNext());
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    public void testGetDescendants_remove() throws Exception {
        final String html =
              "<html><body id='body'>\n"
            + "<div id='a'>a<div id='b'>b</div>a<div id='c'>c</div>a</div><div id='d'>d</div>\n"
            + "</body></html>";
        final HtmlPage page = loadPage(html);
        assertEquals("abacad", page.asText().replaceAll("\\s", ""));
        final DescendantElementsIterator<HtmlElement> iterator = (DescendantElementsIterator<HtmlElement>)
            page.getDocumentElement().getHtmlElementDescendants().iterator();
        assertEquals("", iterator.nextNode().getId());
        assertEquals("body", iterator.nextNode().getId());
        assertEquals("a", iterator.nextNode().getId());
        iterator.remove();
        assertEquals("d", iterator.nextNode().getId());
        assertFalse(iterator.hasNext());
        assertEquals("d", page.asText().replaceAll("\\s", ""));
    }

    static class DomChangeListenerTestImpl implements DomChangeListener {
        private final List<String> collectedValues_ = new ArrayList<String>();
        public void nodeAdded(final DomChangeEvent event) {
            collectedValues_.add("nodeAdded: " + event.getParentNode().getNodeName() + ','
                    + event.getChangedNode().getNodeName());
        }
        public void nodeDeleted(final DomChangeEvent event) {
            collectedValues_.add("nodeDeleted: " + event.getParentNode().getNodeName() + ','
                    + event.getChangedNode().getNodeName());
        }
        List<String> getCollectedValues() {
            return collectedValues_;
        }
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void testDomChangeListenerTestImpl_insertBefore() throws Exception {
        final String htmlContent
            = "<html><head><title>foo</title>\n"
            + "<script>\n"
            + "  function clickMe() {\n"
            + "    var p1 = document.getElementById('p1');\n"
            + "    var div = document.createElement('DIV');\n"
            + "    p1.insertBefore(div, null);\n"
            + "  }\n"
            + "</script>\n"
            + "</head>\n"
            + "<body>\n"
            + "<p id='p1' title='myTitle'></p>\n"
            + "<input id='myButton' type='button' onclick='clickMe()'>\n"
            + "</body></html>";

        final String[] expectedValues = {"nodeAdded: p,div", "nodeAdded: p,div"};
        final HtmlPage page = loadPage(htmlContent);

        final HtmlElement p1 = page.getHtmlElementById("p1");
        final DomChangeListenerTestImpl listenerImpl = new DomChangeListenerTestImpl();
        p1.addDomChangeListener(listenerImpl);
        page.addDomChangeListener(listenerImpl);
        final HtmlButtonInput myButton = page.getHtmlElementById("myButton");

        myButton.click();
        assertEquals(expectedValues, listenerImpl.getCollectedValues());
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void testDomChangeListenerTestImpl_appendChild() throws Exception {
        final String htmlContent
            = "<html><head><title>foo</title>\n"
            + "<script>\n"
            + "  function clickMe() {\n"
            + "    var p1 = document.getElementById('p1');\n"
            + "    var div = document.createElement('DIV');\n"
            + "    p1.appendChild(div);\n"
            + "  }\n"
            + "</script>\n"
            + "</head>\n"
            + "<body>\n"
            + "<p id='p1' title='myTitle'></p>\n"
            + "<input id='myButton' type='button' onclick='clickMe()'>\n"
            + "</body></html>";

        final String[] expectedValues = {"nodeAdded: p,div", "nodeAdded: p,div"};
        final HtmlPage page = loadPage(htmlContent);
        final HtmlElement p1 = page.getHtmlElementById("p1");
        final DomChangeListenerTestImpl listenerImpl = new DomChangeListenerTestImpl();
        p1.addDomChangeListener(listenerImpl);
        page.addDomChangeListener(listenerImpl);
        final HtmlButtonInput myButton = page.getHtmlElementById("myButton");

        myButton.click();
        assertEquals(expectedValues, listenerImpl.getCollectedValues());
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void testDomChangeListenerTestImpl_removeChild() throws Exception {
        final String htmlContent
            = "<html><head><title>foo</title>\n"
            + "<script>\n"
            + "  function clickMe() {\n"
            + "    var p1 = document.getElementById('p1');\n"
            + "    var div = document.getElementById('myDiv');\n"
            + "    div.removeChild(p1);\n"
            + "  }\n"
            + "</script>\n"
            + "</head>\n"
            + "<body>\n"
            + "<div id='myDiv'><p id='p1' title='myTitle'></p></div>\n"
            + "<input id='myButton' type='button' onclick='clickMe()'>\n"
            + "</body></html>";

        final String[] expectedValues = {"nodeDeleted: div,p", "nodeDeleted: div,p"};
        final HtmlPage page = loadPage(htmlContent);
        final HtmlElement p1 = page.getHtmlElementById("p1");
        final DomChangeListenerTestImpl listenerImpl = new DomChangeListenerTestImpl();
        p1.addDomChangeListener(listenerImpl);
        page.addDomChangeListener(listenerImpl);
        final HtmlButtonInput myButton = page.getHtmlElementById("myButton");

        myButton.click();
        assertEquals(expectedValues, listenerImpl.getCollectedValues());
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void testDomChangeListenerRegisterNewListener() throws Exception {
        final String htmlContent
            = "<html><head><title>foo</title>\n"
            + "<script>\n"
            + "  function clickMe() {\n"
            + "    var p1 = document.getElementById('p1');\n"
            + "    var div = document.createElement('DIV');\n"
            + "    p1.appendChild(div);\n"
            + "  }\n"
            + "</script>\n"
            + "</head>\n"
            + "<body>\n"
            + "<p id='p1' title='myTitle'></p>\n"
            + "<input id='myButton' type='button' onclick='clickMe()'>\n"
            + "</body></html>";

        final HtmlPage page = loadPage(htmlContent);

        final List<String> l = new ArrayList<String>();
        final DomChangeListener listener2 = new DomChangeListenerTestImpl() {
            @Override
            public void nodeAdded(final DomChangeEvent event) {
                l.add("in listener 2");
            }
        };
        final DomChangeListener listener1 = new DomChangeListenerTestImpl() {
            @Override
            public void nodeAdded(final DomChangeEvent event) {
                l.add("in listener 1");
                page.addDomChangeListener(listener2);
            }
        };

        page.addDomChangeListener(listener1);

        final HtmlButtonInput myButton = page.getHtmlElementById("myButton");
        myButton.click();

        final String[] expectedValues = {"in listener 1"};
        assertEquals(expectedValues, l);
        l.clear();

        myButton.click();
        final String[] expectedValues2 = {"in listener 1", "in listener 2"};
        assertEquals(expectedValues2, l);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void testGetByXPath_XML() throws Exception {
        final String xml
            = "<books>\n"
            + "  <book>\n"
            + "    <title>Immortality</title>\n"
            + "    <author>John Smith</author>\n"
            + "  </book>\n"
            + "</books>";

        getMockWebConnection().setResponse(URL_FIRST, xml, "text/xml");
        final WebClient client = getWebClientWithMockWebConnection();
        final XmlPage page = (XmlPage) client.getPage(URL_FIRST);

        final List<?> results = page.getByXPath("//title");
        assertEquals(1, results.size());
    }

    /**
     * @throws Exception on test failure
     */
    @Test
    public void testOwnerDocument() throws Exception {
        final String content = "<html>\n"
            + "<head>\n"
            + "    <title>test</title>\n"
            + "<script>\n"
            + "function test() {\n"
            + "    alert(document == document.body.ownerDocument);\n"
            + "    alert(document == document.getElementById('foo').ownerDocument);\n"
            + "    alert(document == document.body.firstChild.ownerDocument);\n"
            + "}\n"
            + "</script>\n"
            + "</head>\n"
            + "<body onload='test()'>bla\n"
            + "<div id='foo'>bla</div>\n"
            + "</body>\n"
            + "</html>";

        final String[] expectedAlerts = {"true", "true", "true"};
        createTestPageForRealBrowserIfNeeded(content, expectedAlerts);
        final List<String> collectedAlerts = new ArrayList<String>();
        loadPage(content, collectedAlerts);
        assertEquals(expectedAlerts, collectedAlerts);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void getCanonicalXPath() throws Exception {
        final String content = "<html><head></head><body><div id='div1'/><div id='div2'/></body></html>";
        final HtmlPage page = loadPage(content);
        for (final HtmlElement element : page.getHtmlElementDescendants()) {
            final List<? extends Object> foundElements = page.getByXPath(element.getCanonicalXPath());
            assertEquals(1, foundElements.size());
            assertSame(element, foundElements.get(0));
        }
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void getChildNodes_remove() throws Exception {
        final String content = "<html><body id='b'><div id='d1'></div><div id='d2'></div></body></html>";
        final HtmlPage page = loadPage(content);
        final DomNodeList<DomNode> children = page.getElementById("b").getChildNodes();
        assertEquals(2, children.getLength());
        page.getElementById("d1").remove();
        assertEquals(1, children.getLength());
        page.getElementById("d2").remove();
        assertEquals(0, children.getLength());
        assertNull(children.get(0));
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    public void serialization() throws Exception {
        final String html = "<html><head></head><body></body></html>";
        final DomChangeListenerTestImpl listener = new DomChangeListenerTestImpl();
        HtmlPage page = loadPage(html);
        page.addDomChangeListener(listener);
        page = clone(page);
        page.removeDomChangeListener(listener);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void isDisplayed() throws Exception {
        final String html = "<html><head>\n"
            + "<style>\n"
            + "#d2 { display: none; }\n"
            + "#d3 { visibility: hidden; }\n"
            + "#d4 { display: block !important; }\n"
            + "</style>\n"
            + "<div id='d1'>hello</div>\n"
            + "<div id='d2'>world</div>\n"
            + "<div id='d3'>again</div>\n"
            + "<div id='d4' style='display: none' >important</div>\n"
            + "</body></html>";

        final HtmlPage page = loadPage(html);
        assertTrue(page.getElementById("d1").isDisplayed());
        assertFalse(page.getElementById("d2").isDisplayed());
        assertFalse(page.getElementById("d3").isDisplayed());
        assertTrue(page.getElementById("d4").isDisplayed());

        getWebClient().getOptions().setCssEnabled(false);
        assertTrue(page.getElementById("d1").isDisplayed());
        assertTrue(page.getElementById("d2").isDisplayed());
        assertTrue(page.getElementById("d3").isDisplayed());
    }

    /**
     * Test for Bug #1253.
     *
     * @throws Exception on test failure
     */
    @Test
    @Alerts(FF = { "exception", "0" }, IE = {"true", "0" })
    public void appendChild_recursive() throws Exception {
        final String html = "<html><head><title>foo</title>\n"
                + "<script>\n"
                + "function test(){\n"
                + "  var e = document.createElement('div');\n"
                + "  try {\n"
                + "    alert(e.appendChild(e) === e);\n"
                + "  } catch(e) {alert('exception');}\n"
                + "  alert(e.childNodes.length);"
                + "}\n"
                + "</script>\n"
                + "</head><body onload='test()'>\n"
                + "</body></html>";

        loadPageWithAlerts(html);
    }

    /**
     * Test for Bug #1253.
     *
     * @throws Exception on test failure
     */
    @Test
    @Alerts({ "true", "exception", "1", "0" })
    public void appendChild_recursive_parent() throws Exception {
        final String html = "<html><head><title>foo</title>\n"
                + "<script>\n"
                + "function test(){\n"
                + "  var e1 = document.createElement('div');\n"
                + "  var e2 = document.createElement('div');\n"
                + "  try {\n"
                + "    alert(e1.appendChild(e2) === e2);\n"
                + "    alert(e2.appendChild(e1) === e1);\n"
                + "  } catch(e) {alert('exception');}\n"
                + "  alert(e1.childNodes.length);"
                + "  alert(e2.childNodes.length);"
                + "}\n"
                + "</script>\n"
                + "</head><body onload='test()'>\n"
                + "</body></html>";

        loadPageWithAlerts(html);
    }

}
