package org.babyfishdemo.om4java.dom;

import junit.framework.Assert;

import org.babyfishdemo.om4java.dom.common.XPath;
import org.babyfishdemo.om4java.dom.common.XmlBuilder;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class MovingChildBetweenDifferntParentsTest {
    
    private static final XmlBuilder XML_BUILDER = new XmlBuilder().readOnly();
    
    private static final XPath XPATH = new XPath().readOnly();

    private Element html;
    
    private Element head;
    
    private Element body;
    
    private Element script;
    
    private Element div;
    
    private Attribute ngInit;
    
    @Before
    public void create() {
        this.html = new Element(
                "html",
                new Element("head"),
                new Element(
                        "body",
                        new Attribute("ng-app", ""),
                        new Attribute("style", "background-color: {{bgColor}}"),
                        new Element(
                                "script",
                                new Attribute("type", "text/javascript"),
                                new Attribute("src", "angular.min.js"),
                                new Text("")
                        ),
                        new Element(
                                "div",
                                new Attribute("ng-init", "bgColor = 'blue';"),
                                new Text("Please input the background color: "),
                                new Element(
                                        "input",
                                        new Attribute("type", "text"),
                                        new Attribute("ng-model", "bgColor")
                                )
                        )
                )
        );
        this.head = XPATH.selectSingleNode(this.html, "head");
        this.body = XPATH.selectSingleNode(this.html, "body");
        this.script = XPATH.selectSingleNode(this.html, "body/script");
        this.div = XPATH.selectSingleNode(this.html, "body/div");
        this.ngInit = XPATH.selectSingleNode(this.html, "body/div/@ng-init");
        
        Assert.assertEquals(
                "<html>"
                +   "<head/>"
                +   "<body ng-app='' style='background-color: {{bgColor}}'>"
                
                // The "<script></script>" is declaring in body, not in head.
                // this is bad style, let our test code change it
                +     "<script type='text/javascript' src='angular.min.js'></script>"
                
                // The "ng-init" attribute is declared on div, not on body.
                // this bad style, let our test code change it
                +     "<div ng-init='bgColor = &apos;blue&apos;;'>"
                +       "Please input the background color: "
                +      "<input type='text' ng-model='bgColor'/>"
                +     "</div>"
                
                +   "</body>"
                + "</html>",
                XML_BUILDER.build(this.html)
        );
        
        Assert.assertFalse(this.head.getChildNodes().contains(this.script));
        Assert.assertTrue(this.body.getChildNodes().contains(script));
        Assert.assertSame(this.body, this.script.getParent());
        
        Assert.assertEquals(0, this.script.getIndex());
        Assert.assertEquals(1, this.div.getIndex());
        
        Assert.assertSame(this.div, this.ngInit.getParent());
    }
    
    @Test
    public void testMoveScriptByChangingParent() {
        
        /*
         * Before moving,
         * (1) The parent of <script> is <body>
         * (2) The <body> contains the <script>
         * (3) The index of next element <div> of the <script> is 1.
         */
        Assert.assertSame(this.body, this.script.getParent());
        Assert.assertTrue(this.body.getChildNodes().contains(this.script));
        Assert.assertEquals(1, this.div.getIndex());
        
        /*
         * Let <script> be the child of <head>
         */
        this.head.getChildNodes().add(script);
        
        /*
         * After "this.head.getChildNodes().add(script)"
         * (1) The "parent" of <script> will be set to be <head> automatically and implicitly.
         * (2) The <script> will be removed from <body> automatically and implicitly.
         * (3) The index of <div> is changed from 1 to 0 automatically and implicitly.
         */
        Assert.assertSame(this.head, script.getParent());
        Assert.assertFalse(this.body.getChildNodes().contains(this.script));
        Assert.assertEquals(0, this.div.getIndex());
        
        /*
         * Now, the whole html is
         */
        Assert.assertEquals(
                "<html>"
                +   "<head>"
                        
                // The "<script>" is move to here.
                +     "<script type='text/javascript' src='angular.min.js'></script>"
                
                +   "</head>"
                +   "<body ng-app='' style='background-color: {{bgColor}}'>"
                
                +     "<div ng-init='bgColor = &apos;blue&apos;;'>"
                +       "Please input the background color: "
                +      "<input type='text' ng-model='bgColor'/>"
                +     "</div>"
                
                +   "</body>"
                + "</html>",
                XML_BUILDER.build(this.html)
        );
    }
    
    @Test
    public void testMoveScriptByChaningItself() {
        
        /*
         * Before moving,
         * (1) The <body> contains the <script>
         * (2) The <head> does not contain the <script>
         * (3) The index the next element <div> of the <script> is 1.
         */
        Assert.assertTrue(this.body.getChildNodes().contains(this.script));
        Assert.assertFalse(this.head.getChildNodes().contains(this.script));
        Assert.assertEquals(1, this.div.getIndex());
        
        /*
         * Let <head> be the parent of <script>
         * 
         * Notes: Before moving, the index of <script> is 0, so this action can 
         * add the <script> into <head>. If the original index of <script> is
         * greater then the original children count of <head>, you must change its
         * index to a number that is small enough before you change its parent; 
         * otherwise, you will get an IndexOutBoundsException.
         */
        this.script.setParent(this.head);
        
        /*
         * After "this.script.setParent(this.head);
         * (1) The <script> is removed from the <body> automatically and implicitly.
         * (2) The <script> is added into the <head> automatically and implicitly.
         * (3) The index of <div> of the <script> is 1 automatically and implicitly.
         */
        Assert.assertFalse(this.body.getChildNodes().contains(this.script));
        Assert.assertTrue(this.head.getChildNodes().contains(this.script));
        Assert.assertEquals(0, this.div.getIndex());
        
        /*
         * Now, the whole html is
         */
        Assert.assertEquals(
                "<html>"
                +   "<head>"
                        
                // The "<script>" is move to here.
                +     "<script type='text/javascript' src='angular.min.js'></script>"
                
                +   "</head>"
                +   "<body ng-app='' style='background-color: {{bgColor}}'>"
                
                +     "<div ng-init='bgColor = &apos;blue&apos;;'>"
                +       "Please input the background color: "
                +      "<input type='text' ng-model='bgColor'/>"
                +     "</div>"
                
                +   "</body>"
                + "</html>",
                XML_BUILDER.build(this.html)
        );
    }
    
    @Test
    public void testMoveNgInitByChangingParent() {
        
        /*
         * Before moving
         * (1) The parent of attribute "ng-init" is <div>
         * (2) The <div> contains the attribute "ng-init"
         */
        Assert.assertSame(this.div, this.ngInit.getParent());
        Assert.assertTrue(this.div.getAttributes().containsValue(this.ngInit));
        
        /*
         * Let attribute "ng-init" to be child of body.
         */
        this.body.addAttribute(this.ngInit);
        
        /*
         * After "this.body.addAttribute(this.ngInit)"
         * (1) The parent of attribute "ng-init" is changed to be <body> automatically and implicitly.
         * (2) The attribute "ng-init" is removed from <div> automatically and implicitly.
         */
        Assert.assertSame(this.body, this.ngInit.getParent());
        Assert.assertFalse(this.div.getAttributes().containsValue(this.ngInit));
        
        /*
         * Now, the whole html is
         */
        Assert.assertEquals(
                "<html>"
                +   "<head/>"
                        
                // attribute "ng-init" is moved into <body>
                +   "<body ng-app='' style='background-color: {{bgColor}}' ng-init='bgColor = &apos;blue&apos;;'>"
                +     "<script type='text/javascript' src='angular.min.js'></script>"
                +     "<div>"
                +       "Please input the background color: "
                +      "<input type='text' ng-model='bgColor'/>"
                +     "</div>"
                +   "</body>"
                
                + "</html>",
                XML_BUILDER.build(this.html)
        );
    }
    
    @Test
    public void testMoveNgInitByChangingItself() {
        
        /*
         * Before moving
         * (1) The <body> does not contain the attribute "ng-init"
         * (2) The <div> contains the attribute "ng-init"
         */
        Assert.assertFalse(this.body.getAttributes().containsValue(this.ngInit));
        Assert.assertTrue(this.div.getAttributes().containsValue(this.ngInit));
        
        /*
         * Let <body> to be parent of attribute "ng-init"
         */
        this.ngInit.setParent(this.body);
        
        /*
         * After "this.ngInit.setParent(this.body)"
         * (1) The attribute "ng-init" is added into <body> automatically and implicitly.
         * (1) The attribute "ng-init" is removed from <div> automatically and implicitly.
         */
        Assert.assertTrue(this.body.getAttributes().containsValue(this.ngInit));
        Assert.assertFalse(this.div.getAttributes().containsValue(this.ngInit));
        
        /*
         * Now, the whole html is
         */
        Assert.assertEquals(
                "<html>"
                +   "<head/>"
                        
                // attribute "ng-init" is moved into <body>
                +   "<body ng-app='' style='background-color: {{bgColor}}' ng-init='bgColor = &apos;blue&apos;;'>"
                +     "<script type='text/javascript' src='angular.min.js'></script>"
                +     "<div>"
                +       "Please input the background color: "
                +      "<input type='text' ng-model='bgColor'/>"
                +     "</div>"
                +   "</body>"
                
                + "</html>",
                XML_BUILDER.build(this.html)
        );
    }
}
