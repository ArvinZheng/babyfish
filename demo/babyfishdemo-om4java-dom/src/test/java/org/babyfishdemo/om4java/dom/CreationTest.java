package org.babyfishdemo.om4java.dom;

import junit.framework.Assert;

import org.babyfish.collection.MACollections;
import org.babyfishdemo.om4java.dom.Attribute;
import org.babyfishdemo.om4java.dom.Comment;
import org.babyfishdemo.om4java.dom.Element;
import org.babyfishdemo.om4java.dom.Node;
import org.babyfishdemo.om4java.dom.QuanifiedName;
import org.babyfishdemo.om4java.dom.common.XmlBuilder;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class CreationTest {
    
    private static final String NS_W3C_XSI = "http://www.w3.org/2001/XMLSchema-instance";
    
    private static final String NS_SPRING_BEANS = "http://www.springframework.org/schema/beans";
    
    private static final String NS_ALIBABA_DUBBO = "http://code.alibabatech.com/schema/dubbo";
    
    private static final XmlBuilder XML_BUILDER =
            new XmlBuilder()
            .addPrefix("", NS_SPRING_BEANS)
            .addPrefix("dubbo", NS_ALIBABA_DUBBO)
            .addPrefix("xsi", NS_W3C_XSI)
            .readOnly();

    @Test
    public void testCreateByAddChildToParent() {
        
        Element 
            beans = new Element(NS_SPRING_BEANS, "beans"),
            application = new Element(NS_ALIBABA_DUBBO, "application"),
            registry = new Element(NS_ALIBABA_DUBBO, "registry"),
            protocol = new Element(NS_ALIBABA_DUBBO, "protocol"),
            service = new Element(NS_ALIBABA_DUBBO, "service"),
            bean = new Element(NS_SPRING_BEANS, "bean");
        
        testPutOnAttributeMap(
                beans, 
                NS_W3C_XSI,
                "schemaLocation", 
                NS_SPRING_BEANS
                + " http://www.springframework.org/schema/beans/spring-beans.xsd "
                + NS_ALIBABA_DUBBO
                + " http://code.alibabatech.com/schema/dubbo/dubbo.xsd"
        );
        testPutOnAttributeMap(application, "name", "hello-world-app");
        testPutOnAttributeMap(registry, "address", "multicast://224.5.6.7:1234");
        testPutOnAttributeMap(protocol, "name", "dubbo");
        testPutOnAttributeMap(protocol, "port", "20880");
        testPutOnAttributeMap(service, "interface", "com.alibaba.dubbo.demo.DemoService");
        testPutOnAttributeMap(service, "ref", "demoService");
        testPutOnAttributeMap(bean, "id", "demoService");
        testPutOnAttributeMap(bean, "class", "com.alibaba.dubbo.demo.provider.DemoServiceImpl");
        
        testAddAllOnChildNodeList(
                beans, 
                new Comment("Provide application information to calculate dependencies"),
                application,
                new Comment("Use Multicast broadcast registry center to expose service address"),
                registry,
                new Comment("Apply dubbo protocol on port 20880"),
                protocol,
                new Comment("Declare the interface of service"),
                service,
                new Comment("Implement the service as local bean"),
                bean
        );
        
        Assert.assertEquals(
                "<beans "
                + "xmlns='http://www.springframework.org/schema/beans' "
                + "xmlns:dubbo='http://code.alibabatech.com/schema/dubbo' "
                + "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' "
                + "xsi:schemaLocation='http://www.springframework.org/schema/beans "
                + "http://www.springframework.org/schema/beans/spring-beans.xsd "
                + "http://code.alibabatech.com/schema/dubbo "
                + "http://code.alibabatech.com/schema/dubbo/dubbo.xsd'>"
                
                +     "<!--Provide application information to calculate dependencies-->"
                +     "<dubbo:application name='hello-world-app'/>"
                
                +     "<!--Use Multicast broadcast registry center to expose service address-->"
                +     "<dubbo:registry address='multicast://224.5.6.7:1234'/>"
                
                +     "<!--Apply dubbo protocol on port 20880-->"
                +     "<dubbo:protocol name='dubbo' port='20880'/>"
                
                +     "<!--Declare the interface of service-->"
                +     "<dubbo:service interface='com.alibaba.dubbo.demo.DemoService' ref='demoService'/>"
                
                +     "<!--Implement the service as local bean-->"
                +     "<bean id='demoService' class='com.alibaba.dubbo.demo.provider.DemoServiceImpl'/>"
                + "</beans>",
                XML_BUILDER.build(beans)
        );
    }
    
    public void testCreateBySetParentToChild() {
        
        Element 
            beans = new Element(NS_SPRING_BEANS, "beans"),
            application = new Element(NS_ALIBABA_DUBBO, "application"),
            registry = new Element(NS_ALIBABA_DUBBO, "registry"),
            protocol = new Element(NS_ALIBABA_DUBBO, "protocol"),
            service = new Element(NS_ALIBABA_DUBBO, "service"),
            bean = new Element(NS_SPRING_BEANS, "bean");
        
        testSetParentOnAttribute(
                NS_W3C_XSI, 
                "schemaLocation",
                NS_SPRING_BEANS
                + " http://www.springframework.org/schema/beans/spring-beans.xsd "
                + NS_ALIBABA_DUBBO
                + " http://code.alibabatech.com/schema/dubbo/dubbo.xsd", 
                beans
        );
        testSetParentOnAttribute("name", "hello-world-app", application);
        testSetParentOnAttribute("address", "multicast://224.5.6.7:1234", registry);
        testSetParentOnAttribute("name", "dubbo", protocol);
        testSetParentOnAttribute("port", "20880", protocol);
        testSetParentOnAttribute("interface", "com.alibaba.dubbo.demo.DemoService", service);
        testSetParentOnAttribute("ref", "demoService", service);
        testSetParentOnAttribute("id", "demoService", bean);
        testSetParentOnAttribute("class", "com.alibaba.dubbo.demo.provider.DemoServiceImpl", bean);
        
        testSetParentOnChildNode(new Comment("Provide application information to calculate dependencies"), 0, beans);
        testSetParentOnChildNode(application, 1, beans);
        testSetParentOnChildNode(new Comment("Use Multicast broadcast registry center to expose service address"), 2, beans);
        testSetParentOnChildNode(registry, 3, beans);
        testSetParentOnChildNode(new Comment("Apply dubbo protocol on port 20880"), 4, beans);
        testSetParentOnChildNode(protocol, 5, beans);
        testSetParentOnChildNode(new Comment("Declare the interface of service"), 6, beans);
        testSetParentOnChildNode(service, 7, beans);
        testSetParentOnChildNode(new Comment("Implement the service as local bean"), 8, beans);
        testSetParentOnChildNode(bean, 9, beans);
        
        Assert.assertEquals(
                "<beans "
                + "xmlns='http://www.springframework.org/schema/beans' "
                + "xmlns:dubbo='http://code.alibabatech.com/schema/dubbo' "
                + "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' "
                + "xsi:schemaLocation='http://www.springframework.org/schema/beans "
                + "http://www.springframework.org/schema/beans/spring-beans.xsd "
                + "http://code.alibabatech.com/schema/dubbo "
                + "http://code.alibabatech.com/schema/dubbo/dubbo.xsd'>"
                
                +     "<!--Provide application information to calculate dependencies-->"
                +     "<dubbo:application name='hello-world-app'/>"
                
                +     "<!--Use Multicast broadcast registry center to expose service address-->"
                +     "<dubbo:registry address='multicast://224.5.6.7:1234'/>"
                
                +     "<!--Apply dubbo protocol on port 20880-->"
                +     "<dubbo:protocol name='dubbo' port='20880'/>"
                
                +     "<!--Declare the interface of service-->"
                +     "<dubbo:service interface='com.alibaba.dubbo.demo.DemoService' ref='demoService'/>"
                
                +     "<!--Implement the service as local bean-->"
                +     "<bean id='demoService' class='com.alibaba.dubbo.demo.provider.DemoServiceImpl'/>"
                + "</beans>",
                XML_BUILDER.build(beans)
        );
    }
    
    private static void testPutOnAttributeMap(Element element, String localName, String value) {
        testPutOnAttributeMap(element, null, localName, value);
    }
    
    private static void testPutOnAttributeMap(Element element, String namespaceURI, String localName, String value) {
        Attribute attribute = new Attribute(value);
        
        /*
         * Before call "element.getAttributes().put(?, ?)",
         * both the "parent" and "quanifiedName" of "attribute" should be null
         */
        Assert.assertNull(attribute.getParent());
        Assert.assertNull(attribute.getQuanifiedName());
        
        /*
         * This explicit modification will cause two implicit modifications automatically
         * (1) "attribute.parent" will be assigned automatically
         * (2) "attribute.quanifiedName" will be assinged automatically
         */
        element.getAttributes().put(new QuanifiedName(namespaceURI, localName), attribute);
        
        /*
         * Before call "element.getAttributes().put(?, ?)"
         * both the "parent" and "quanifiedName" of "attribute" are assigned automatically and implicitly
         */
        Assert.assertSame(element, attribute.getParent());
        Assert.assertEquals(namespaceURI, attribute.getQuanifiedName().getNamespaceURI());
        Assert.assertEquals(localName, attribute.getQuanifiedName().getLocalName());
    }
    
    private static void testAddAllOnChildNodeList(Element element, Node ... childNodes) {
        /*
         * Before call "element.getAttributes().addAll(?),
         * (1) The "parent" of each node must be null
         * (2) The "index" of each node must be -1
         */
        for (Node childNode : childNodes) {
            Assert.assertEquals(null, childNode.getParent());
            Assert.assertEquals(-1, childNode.getIndex());
        }
        
        /*
         * This explicit modification will cause two implicit modifications automatically
         * (1) "childNode.index" will be assigned automatically
         * (2) "childNode.parent" will be assinged automatically
         */
        element.getChildNodes().addAll(MACollections.wrap(childNodes));
        
        /*
         * Before call "element.getAttributes().addAll(?),
         * (1) "childNode.parent" is assigned automatically and implicitly
         * (2) "childNode.index" is assigned automatically and implicitly
         */
        int index = 0;
        for (Node childNode : childNodes) {
            Assert.assertSame(element, childNode.getParent());
            Assert.assertEquals(index++, childNode.getIndex());
        }
    }
    
    private static void testSetParentOnAttribute(
            String localName, 
            String value, 
            Element element) {
        testSetParentOnAttribute(null, localName, value, element);
    }
    
    private static void testSetParentOnAttribute(
            String namespaceURI, 
            String localName, 
            String value, 
            Element element) {
        
        Attribute attribute = new Attribute(value);
        
        /*
         * Before call "attribute.setParent(?)" and "attribute.setQuanifiedName(?)",
         * element does not contain this attribute.
         */
        Assert.assertFalse(element.getAttributes().containsKey(new QuanifiedName(namespaceURI, localName)));
        Assert.assertFalse(element.getAttributes().containsValue(attribute));
        
        /*
         * call "attribute.setParent(?)", but "attribute.setQuanifiedName(?)" is not called
         * so that element still does not contain this attribute
         */
        attribute.setParent(element);
        Assert.assertFalse(element.getAttributes().containsKey(new QuanifiedName(namespaceURI, localName)));
        Assert.assertFalse(element.getAttributes().containsValue(attribute));
        
        /*
         * call "attribute.setQuanifiedName(?)", now both the "parent" and "quanifiedName"
         * of attribute have beean set, so a key-value pair will be inserted into 
         * "element.getAttributes()" automatically and implicitly.
         */
        attribute.setQuanifiedName(new QuanifiedName(namespaceURI, localName));
        Assert.assertTrue(element.getAttributes().containsKey(new QuanifiedName(namespaceURI, localName)));
        Assert.assertTrue(element.getAttributes().containsValue(attribute));
    }
    
    private static void testSetParentOnChildNode(Node childNode, int index, Element parentElement) {
        
        /*
         * Before call "childNode.setParent(?)" and "childNode.setIndex(?)",
         * parentElement does not contain this childNode
         */
        Assert.assertFalse(parentElement.getChildNodes().contains(childNode));
        
        /*
         * call "childNode.setParent(?)", but "childNode.setIndex(?)" is not called
         * so that parentElement still does not contain this childNode
         */
        childNode.setParent(parentElement);
        Assert.assertFalse(parentElement.getChildNodes().contains(childNode));
        
        /*
         * call "childNode.setIndex(?)", now both the "parent" and "index"
         * of childNode have beean set, so the childNode will be inserted into 
         * "parentElement.getChildNodes()" automatically and implicitly.
         */
        childNode.setIndex(index);
        Assert.assertTrue(parentElement.getChildNodes().contains(childNode));
    }
}
