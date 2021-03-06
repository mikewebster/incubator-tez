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

package org.apache.tez.runtime.library.api;

import java.io.IOException;

import org.apache.tez.runtime.api.Writer;

/**
 * A key/value(s) pair based {@link Writer}
 */
public interface KeyValueWriter extends Writer {
  /**
   * Writes a key/value pair.
   * 
   * @param key
   *          the key to write
   * @param value
   *          the value to write
   * @throws IOException
   *           if an error occurs
   */
  public void write(Object key, Object value) throws IOException;
}
