/**
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.task.service.test;

import static org.jbpm.task.service.test.impl.TestServerUtil.createTestTaskClientConnector;
import static org.jbpm.task.service.test.impl.TestServerUtil.startServer;

import org.jbpm.task.service.TaskClient;
import org.jbpm.task.service.persistence.TaskServiceEscalationBaseTest;
import org.jbpm.task.service.test.impl.TestTaskServer;

public class TaskServiceEscalationTest extends TaskServiceEscalationBaseTest {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
        
        server = startServer(taskService);

        client = new TaskClient(createTestTaskClientConnector("client 1", (TestTaskServer) server));
        client.connect();
	}

}
