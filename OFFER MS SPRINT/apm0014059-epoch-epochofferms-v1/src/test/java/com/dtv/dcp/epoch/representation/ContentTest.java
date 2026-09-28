package com.dtv.dcp.epoch.representation;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.dtv.dcp.epoch.model.common.ServiceMetaData;


public class ContentTest {

    /**
     * Test feature beans.
     *
     * @throws Exception the exception
     */
    @Test
    public void testToString() throws Exception {
        Content<String> content = new Content<>("");
        assertNotNull(content.toString());
    }

    /**
     * Test bean.
     *
     * @throws IntrospectionException the introspection exception
     */
//    @Test
//    public void testBean() throws IntrospectionException {
//        JavaBeanTester.test(Content.class, "data");
//    }

    /**
     * Test constructor.
     */
    @Test
    public void testConstructor() {
        ServiceMetaData metaData = new ServiceMetaData();
        Content<String> content = new Content<>(metaData);
        assertNotNull(content.metaData());
    }

    /**
     * Test constructor 2.
     */
    @Test
    public void testConstructor2() {
        ServiceMetaData metaData = new ServiceMetaData();
        Content<String> content = new Content<>("", metaData);
        assertNotNull(content.metaData());
        assertNotNull(content.getData());
    }

    /**
     * Test constructor 3.
     */
    @Test
    public void testConstructor3() {
        ServiceMetaData metaData = new ServiceMetaData();
        Content<String> content = new Content<>("");
        content.setData("dummy");
        content.metaData(metaData);
        assertNotNull(content.metaData());
        assertNotNull(content.getData());
    }

    /**
     * Test constructor is private.
     *
     * @throws NoSuchMethodException the no such method exception
     * @throws IllegalAccessException the illegal access exception
     * @throws InvocationTargetException the invocation target exception
     * @throws InstantiationException the instantiation exception
     */
    @Test
    public void testConstructorIsPrivate() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<Content> constructor = Content.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }
    
    @Test
    public void testCollectionLinks() {
    	CollectionLinks collectionLinks = new CollectionLinks("test");
    	collectionLinks.setPrev("Prev");
    	assertNotNull(collectionLinks.getPrev());
    	
    	
    	collectionLinks.setNext("Next");
    	assertNotNull(collectionLinks.getNext());
    	
    	collectionLinks.setFirst("First");
    	assertNotNull(collectionLinks.getFirst());
    	
    	collectionLinks.setLast("Last");
    	assertNotNull(collectionLinks.getLast());
    	
    	Pagination pagination = new Pagination(1, 1, 1);
    	pagination.getTotalCount();
    	pagination.getPageNumber();
    	pagination.getPageSize();
    	pagination.getTotalCount();
    	assertNotNull(pagination.getTotalCount() + pagination.getTotalCount() + pagination.getTotalCount());    	
    	
    	Links links = new Links("linkset");    	
    	assertNotNull(links.getSelf().toString());
        assertNotNull(collectionLinks.toString());
        
        Resource resource = new Resource("linkset");  
       
        resource.getContent();        
        resource.setLinks(links);
        assertNotNull(resource.getLinks().toString());
        
        List<Integer> listCollection = new ArrayList<Integer>(); 
        listCollection.add(1); 
        listCollection.add(3); 
        
        ResourceCollection resourceCollection = new ResourceCollection(listCollection);  
        resourceCollection.setCollection(listCollection);
       // resourceCollection.setLinks(links);
        resourceCollection.setPagination(pagination);
    }
}