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

package org.apache.tez.mapreduce.input;

import java.io.IOException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.classification.InterfaceAudience.LimitedPrivate;
import org.apache.hadoop.classification.InterfaceAudience.Private;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.tez.runtime.api.events.RootInputDataInformationEvent;

@LimitedPrivate("Hive")
public class MRInputLegacy extends MRInput {

  private static final Log LOG = LogFactory.getLog(MRInputLegacy.class);
  
  private RootInputDataInformationEvent initEvent;
  private volatile boolean inited = false;
  private ReentrantLock eventLock = new ReentrantLock();
  private Condition eventCondition = eventLock.newCondition();

  @Private
  protected void initializeInternal() throws IOException {
    LOG.info("MRInputLegacy deferring initialization");
  }
  
  @Private
  public org.apache.hadoop.mapreduce.InputSplit getNewInputSplit() {
    return this.newInputSplit;
  }  
  
  @SuppressWarnings("rawtypes")
  @Private
  public RecordReader getOldRecordReader() {
    return this.oldRecordReader;
  }
  
  @LimitedPrivate("hive")
  public void init() throws IOException {
    super.initializeInternal();
    checkAndAwaitRecordReaderInitialization();
  }
  
  @Override
  void processSplitEvent(RootInputDataInformationEvent event) {
    eventLock.lock();
    try {
      initEvent = event;
      // Don't process event, but signal in case init is waiting on the event.
      eventCondition.signal();
    } finally {
      eventLock.unlock();
    }
  }

  @Override
  void checkAndAwaitRecordReaderInitialization() throws IOException {
    eventLock.lock();
    if (inited) {
      return;
    }
    try {
      if (splitInfoViaEvents && !inited) {
        if (initEvent == null) {
          LOG.info("Awaiting init event before initializing record reader");
          try {
            eventCondition.await();
          } catch (InterruptedException e) {
            throw new IOException("Interrupted while awaiting init event", e);
          }
        }
        initFromEvent(initEvent);
        inited = true;
      } else {
        // Already inited
        return;
      }
    } finally {
      eventLock.unlock();
    }
  }
}
