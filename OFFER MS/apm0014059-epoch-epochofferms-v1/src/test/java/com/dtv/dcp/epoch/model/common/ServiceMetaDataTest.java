package com.dtv.dcp.epoch.model.common;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.jupiter.api.Test;


public class ServiceMetaDataTest {
    @Test
    public void testServiceMetaData() throws Exception {
        // JavaBeanTester.test(ServiceMetaData.class);
        ServiceMetaData beanServiceMetaData = new ServiceMetaData();
        beanServiceMetaData.description(null);
        beanServiceMetaData.processingTime(0);
        beanServiceMetaData.timestamp(null);
        beanServiceMetaData.title(null);
        beanServiceMetaData.version(null);
        assertNull(beanServiceMetaData.version());
        assertNull(beanServiceMetaData.description());
        assertNull(beanServiceMetaData.timestamp());
        assertEquals(0, beanServiceMetaData.processingTime());
        assertNull(beanServiceMetaData.title());
        assertNotNull(beanServiceMetaData.toString());
    }
}
