/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */
package org.apache.logging.log4j.mongodb2;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.categories.Appenders;
import org.apache.logging.log4j.junit.LoggerContextRule;
import org.apache.logging.log4j.message.MapMessage;
import org.apache.logging.log4j.mongodb2.MongoDbTestRule.LoggingTarget;
import org.apache.logging.log4j.test.AvailablePortSystemPropertyTestRule;
import org.apache.logging.log4j.test.RuleChainFactory;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.RuleChain;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

/**
 * This class name does NOT end in "Test" in order to only be picked up by {@link Java8Test}.
 */
@Category(Appenders.MongoDb.class)
public class MongoDbMapMessageTestJava8 {

    private static LoggerContextRule loggerContextTestRule = new LoggerContextRule("log4j2-mongodb-map-message.xml");

    private static final AvailablePortSystemPropertyTestRule mongoDbPortTestRule = AvailablePortSystemPropertyTestRule
            .create(TestConstants.SYS_PROP_NAME_PORT);

    private static final MongoDbTestRule mongoDbTestRule = new MongoDbTestRule(mongoDbPortTestRule.getName(), LoggingTarget.NULL);

    @ClassRule
    public static RuleChain ruleChain = RuleChainFactory.create(mongoDbPortTestRule, mongoDbTestRule,
            loggerContextTestRule);

    @Test
    public void test() {
        final Logger logger = LogManager.getLogger();
        final MapMessage mapMessage = new MapMessage();
        mapMessage.with("SomeName", "SomeValue");
        mapMessage.with("SomeInt", 1);
        logger.info(mapMessage);
        //
        final MongoClient mongoClient = mongoDbTestRule.getMongoClient();
        try {
            final DB database = mongoClient.getDB("test");
            Assert.assertNotNull(database);
            final DBCollection collection = database.getCollection("applog");
            Assert.assertNotNull(collection);
            final DBObject first = collection.find().next();
            Assert.assertNotNull(first);
            final String firstMapString = first.toMap().toString();
            Assert.assertEquals(firstMapString, "SomeValue", first.get("SomeName"));
            Assert.assertEquals(firstMapString, Integer.valueOf(1), first.get("SomeInt"));
        } finally {
            mongoClient.close();
        }
    }
}
