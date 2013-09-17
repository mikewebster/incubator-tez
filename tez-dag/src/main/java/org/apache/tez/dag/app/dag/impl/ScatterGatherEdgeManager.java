/**
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.apache.tez.dag.app.dag.impl;

import java.util.List;

import org.apache.tez.dag.app.dag.EdgeManager;
import org.apache.tez.dag.app.dag.Vertex;
import org.apache.tez.engine.newapi.events.DataMovementEvent;
import org.apache.tez.engine.newapi.events.InputReadErrorEvent;
import org.apache.tez.engine.newapi.events.InputFailedEvent;

public class ScatterGatherEdgeManager extends EdgeManager {

  private int initialDestinationTaskNumber = -1;

  @Override
  public int getNumDestinationTaskInputs(Vertex sourceVertex,
      int destinationTaskIndex) {
    return sourceVertex.getTotalTasks();
  }
  
  @Override
  public int getNumSourceTaskOutputs(Vertex destinationVertex,
      int sourceTaskIndex) {
    if(initialDestinationTaskNumber == -1) {
      // the downstream vertex may not have started and so its number of tasks
      // may change. So save this initial count and provide a consistent view 
      // to all source tasks, including late starters and retries.
      // When the number of destination tasks change then the routing will have 
      // to be updated too.
      // This value may be obtained from config too if destination task initial 
      // parallelism is not specified.
      initialDestinationTaskNumber = destinationVertex.getTotalTasks();
    }
    return initialDestinationTaskNumber;
  }

  @Override
  public void routeEventToDestinationTasks(DataMovementEvent event,
      int sourceTaskIndex, int numDestinationTasks, List<Integer> taskIndices) {
    int destinationTaskIndex = event.getSourceIndex();
    event.setTargetIndex(sourceTaskIndex);
    taskIndices.add(new Integer(destinationTaskIndex));
  }

  @Override
  public void routeEventToDestinationTasks(InputFailedEvent event,
      int sourceTaskIndex, int numDestinationTasks, List<Integer> taskIndices) {
    int destinationTaskIndex = event.getSourceIndex();
    event.setTargetIndex(sourceTaskIndex);
    taskIndices.add(new Integer(destinationTaskIndex));
  }

  @Override
  public int routeEventToSourceTasks(int destinationTaskIndex,
      InputReadErrorEvent event) {
    return event.getIndex();
  }
  
}