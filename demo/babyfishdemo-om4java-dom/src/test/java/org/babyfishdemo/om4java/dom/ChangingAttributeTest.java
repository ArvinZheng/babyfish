package org.babyfishdemo.om4java.dom;

import java.util.Iterator;

import junit.framework.Assert;

import org.babyfishdemo.om4java.dom.common.XPath;
import org.babyfishdemo.om4java.dom.common.XmlBuilder;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class ChangingAttributeTest {
    
    private static final XmlBuilder XML_BUILDER = new XmlBuilder().readOnly();
    
    private static final XPath XPATH = new XPath().readOnly();

    private Element html;
    
    @Before
    public void create() {
        this.html = new Element(
                "html",
                new Element(
                        "head",
                        new Element(
                                "link",
                                new Attribute("rel", "stylesheet"),
                                new Attribute("type", "text/css"),
                                new Attribute("href", "default_theme.css")
                        ),
                        new Element(
                                "script",
                                new Attribute("type", "text/javascript"),
                                new Attribute("src", "angular.min.js"),
                                new Text("")
                        )
                ),
                new Element(
                        "body",
                        new Attribute("ng-app", ""),
                        new Attribute("ng-controller", "simpleDemoCtrl"),
                        new Element(
                                "p",
                                new Attribute("data-a", "1"),
                                new Attribute("data-b", "4"),
                                new Attribute("data-c", "9"),
                                new Attribute("data-d", "16"),
                                new Attribute("data-e", "25"),
                                new Attribute("data-f", "36"),
                                new Attribute("data-g", "49"),
                                new Element(
                                        "input",
                                        new Attribute("type", "text"),
                                        new Attribute("class", "wide-input"),
                                        new Attribute("ng-model", "keyword")
                                )
                        ),
                        new Element(
                                "div",
                                new Attribute("class", "simple-list"),
                                new Element(
                                        "div", 
                                        new Attribute("ng-repeat", "item in items()"),
                                        new Element(
                                                "span",
                                                new Attribute("ng-bind", "item.name"),
                                                new Text("")
                                        )
                                )
                        )
                )
        );
        Assert.assertEquals(
                "<html>"
                +   "<head>"
                +     "<link rel='stylesheet' type='text/css' href='default_theme.css'/>"
                +     "<script type='text/javascript' src='angular.min.js'></script>"
                +   "</head>"
                +   "<body ng-app='' ng-controller='simpleDemoCtrl'>"
                +     "<p data-a='1' data-b='4' data-c='9' data-d='16' data-e='25' data-f='36' data-g='49'>"
                +       "<input type='text' class='wide-input' ng-model='keyword'/>"
                +     "</p>"
                +     "<div class='simple-list'>"
                +       "<div ng-repeat='item in items()'>"
                +         "<span ng-bind='item.name'></span>"
                +       "</div>"
                +     "</div>"
                +   "</body>"
                + "</html>", 
                XML_BUILDER.build(this.html)
        );
    }
    
    @Test
    public void testRemoveAttributesByMapKeySetViewIterator() {
        
        Element body = XPATH.selectSingleNode(this.html, "body");
        Attribute ngApp = XPATH.selectSingleNode(body, "@ng-app");
        Attribute ngController = XPATH.selectSingleNode(body, "@ng-controller");
        Assert.assertTrue(body.getAttributes().containsValue(ngApp));
        Assert.assertSame(body, ngApp.getParent());
        Assert.assertTrue(body.getAttributes().containsValue(ngController));
        Assert.assertSame(body, ngController.getParent());
        
        Element input = XPATH.selectSingleNode(body, "p/input");
        Attribute ngModel = XPATH.selectSingleNode(input, "@ng-model");
        Assert.assertTrue(input.getAttributes().containsValue(ngModel));
        Assert.assertSame(input, ngModel.getParent());
        
        Element div = XPATH.selectSingleNode(body, "div/div");
        Attribute ngRepeat = XPATH.selectSingleNode(div, "@ng-repeat");
        Assert.assertTrue(div.getAttributes().containsValue(ngRepeat));
        Assert.assertSame(div, ngRepeat.getParent());
        
        Element span = XPATH.selectSingleNode(div, "span");
        Attribute ngBind = XPATH.selectSingleNode(span, "@ng-bind");
        Assert.assertTrue(span.getAttributes().containsValue(ngBind));
        Assert.assertSame(span, ngBind.getParent());
        
        this.html.accept(new Visitor() {

            @Override
            public void visitElement(Element element) {
                /*
                 * If we remove the attribute whose localName start with 
                 * "ng-" in the method "visitAttribute", we will get
                 * "java.util.ConcurrentModificationException"!
                 * 
                 * But, we can remove the attribute safely here :)
                 */
                Iterator<QuanifiedName> itr = element.getAttributes().keySet().iterator();
                while (itr.hasNext()) {
                    QuanifiedName attributeName = itr.next();
                    if (attributeName.getLocalName().startsWith("ng-")) {
                        itr.remove();
                    }
                }
            }

            @Override
            public void visitAttribute(Attribute attribute) {}

            @Override
            public void visitText(Text text) {}

            @Override
            public void visitComment(Comment comment) {}
        });
        
        /*
         * The bidirectional associations between the "ng-" attributes and
         * their parent elements are destroyed automatically and implicitly
         */
        Assert.assertFalse(body.getAttributes().containsValue(ngApp));
        Assert.assertNull(ngApp.getParent()); // Set to be null automatically and implicitly
        Assert.assertFalse(body.getAttributes().containsValue(ngController));
        Assert.assertNull(ngController.getParent());
        
        Assert.assertFalse(input.getAttributes().containsValue(ngModel));
        Assert.assertNull(ngModel.getParent()); // Set to be null automatically and implicitly
        
        Assert.assertFalse(div.getAttributes().containsValue(ngRepeat));
        Assert.assertNull(ngRepeat.getParent()); // Set to be null automatically and implicitly
        
        Assert.assertFalse(span.getAttributes().containsValue(ngBind));
        Assert.assertNull(ngBind.getParent()); // Set to be null automatically and implicitly
        
        /*
         * Now the whole html is(all the attributes start with "ng-" are removed)
         */
        Assert.assertEquals(
                "<html>"
                +   "<head>"
                +     "<link rel='stylesheet' type='text/css' href='default_theme.css'/>"
                +     "<script type='text/javascript' src='angular.min.js'></script>"
                +   "</head>"
                +   "<body>"
                +     "<p data-a='1' data-b='4' data-c='9' data-d='16' data-e='25' data-f='36' data-g='49'>"
                +       "<input type='text' class='wide-input'/>"
                +     "</p>"
                +     "<div class='simple-list'>"
                +       "<div>"
                +         "<span></span>"
                +       "</div>"
                +     "</div>"
                +   "</body>"
                + "</html>", 
                XML_BUILDER.build(this.html)
        );
    }
    
    @Test
    public void testRemoveAttributesByMapValuesViewIterator() {
        
        Element p = XPATH.selectSingleNode(this.html, "body/p");
        Attribute dataB = XPATH.selectSingleNode(p, "@data-b");
        Attribute dataD = XPATH.selectSingleNode(p, "@data-d");
        Attribute dataF = XPATH.selectSingleNode(p, "@data-f");
        Assert.assertTrue(p.getAttributes().containsValue(dataB));
        Assert.assertSame(p, dataB.getParent());
        Assert.assertTrue(p.getAttributes().containsValue(dataD));
        Assert.assertSame(p, dataD.getParent());
        Assert.assertTrue(p.getAttributes().containsValue(dataF));
        Assert.assertSame(p, dataF.getParent());
        
        this.html.accept(new Visitor() {

            @Override
            public void visitElement(Element element) {
                /*
                 * If we remove the attribute whose localName start with 
                 * "ng-" in the method "visitAttribute", we will get
                 * "java.util.ConcurrentModificationException"!
                 * 
                 * But, we can remove the attribute safely here :)
                 */
                Iterator<Attribute> itr = element.getAttributes().values().iterator();
                while (itr.hasNext()) {
                    Attribute attribute = itr.next();
                    if (attribute.getQuanifiedName().getLocalName().startsWith("data-")) {
                        int number;
                        try {
                            number = Integer.parseInt(attribute.getValue());
                        } catch (NumberFormatException ex) {
                            continue;
                        }
                        if (number % 2 == 0) {
                            itr.remove(); //If the value of the current attribute is even number, remove it.
                        }
                    }
                }
            }

            @Override
            public void visitAttribute(Attribute attribute) {}

            @Override
            public void visitText(Text text) {}

            @Override
            public void visitComment(Comment comment) {}
        });
        
        /*
         * The bidirectional associations between the "data-b", "data-d", "data-f"
         * and their parent elements are destroyed automatically and implicitly
         */
        Assert.assertFalse(p.getAttributes().containsValue(dataB));
        Assert.assertNull(dataB.getParent()); // Set to be null automatically and implicitly
        Assert.assertFalse(p.getAttributes().containsValue(dataD));
        Assert.assertNull(dataD.getParent()); // Set to be null automatically and implicitly
        Assert.assertFalse(p.getAttributes().containsValue(dataF));
        Assert.assertNull(dataF.getParent()); // Set to be null automatically and implicitly
        
        /*
         * Now the whole html is(data-b, data-d, data-f are removed)
         */
        Assert.assertEquals(
                "<html>"
                +   "<head>"
                +     "<link rel='stylesheet' type='text/css' href='default_theme.css'/>"
                +     "<script type='text/javascript' src='angular.min.js'></script>"
                +   "</head>"
                +   "<body ng-app='' ng-controller='simpleDemoCtrl'>"
                +     "<p data-a='1' data-c='9' data-e='25' data-g='49'>"
                +       "<input type='text' class='wide-input' ng-model='keyword'/>"
                +     "</p>"
                +     "<div class='simple-list'>"
                +       "<div ng-repeat='item in items()'>"
                +         "<span ng-bind='item.name'></span>"
                +       "</div>"
                +     "</div>"
                +   "</body>"
                + "</html>", 
                XML_BUILDER.build(this.html)
        );
    }
    
    /*
     * Of course, there are many other methods to modify the attributes
     * except using
     * "Element.getAttributes().keySet().iterator()" 
     * and
     * "Element.getAttributes().values().iterator()".
     * 
     * But most of those other methods are shown in 
     * "MovingChildBetweenDifferntParentsTest"
     * and
     * "UnstableMapKeyTest",
     * it is not necessary to show them again in this test class,
     * please view other test classes to know more.
     */
}
