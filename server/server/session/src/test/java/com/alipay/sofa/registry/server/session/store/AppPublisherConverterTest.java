/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.registry.server.session.store;

import com.alipay.sofa.registry.common.model.AppRegisterServerDataBox;
import com.alipay.sofa.registry.server.session.converter.AppRegisterConstant;
import com.alipay.sofa.registry.util.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author xiaojian.xj
 * @version $Id: AppPublisherConverterTest.java, v 0.1 2020年12月11日 17:34 xiaojian.xj Exp $
 */
public class AppPublisherConverterTest {

    @Test
    public void testConvert() throws Exception {

        String box = "{\"url\":\"127.0.0.1:8080\",\"revision\":\"faf447f9a7990b4be937f0e06664ee41\",\"baseParams\":{\"a\":[\"2\"]},"
                     + "\"interfaceParams\":{\"com.alipay.test.Simple4#@#DEFAULT_INSTANCE_ID#@#DEFAULT_GROUP\":{},\"com.alipay.test"
                     + ".Simple5#@#DEFAULT_INSTANCE_ID#@#DEFAULT_GROUP\":{\"b\":[\"3\",\"4\"]}}}";

        ObjectMapper mapper = JsonUtils.getJacksonObjectMapper();
        Map<String, Object> jsonObject = mapper.readValue(box, HashMap.class);
        AppRegisterServerDataBox serverDataBox = new AppRegisterServerDataBox();
        serverDataBox.setUrl((String) jsonObject.get(AppRegisterConstant.URL_KEY));
        serverDataBox.setRevision((String) jsonObject.get(AppRegisterConstant.REVISION_KEY));
        serverDataBox.setBaseParams((Map) jsonObject.get(AppRegisterConstant.BASE_PARAMS_KEY));
        serverDataBox.setInterfaceParams((Map) jsonObject
            .get(AppRegisterConstant.INTERFACE_PARAMS_KEY));
        Assert.assertEquals(serverDataBox.getBaseParams().get("a").size(), 1);
        Assert.assertEquals(
            serverDataBox.getInterfaceParams()
                .get("com.alipay.test.Simple5#@#DEFAULT_INSTANCE_ID#@#DEFAULT_GROUP").get("b")
                .size(), 2);

        AppRegisterServerDataBox dataBox = mapper.readValue(box, AppRegisterServerDataBox.class);
        Assert.assertEquals(
            dataBox.getInterfaceParams()
                .get("com.alipay.test.Simple5#@#DEFAULT_INSTANCE_ID#@#DEFAULT_GROUP").get("b")
                .size(), 2);
    }
}