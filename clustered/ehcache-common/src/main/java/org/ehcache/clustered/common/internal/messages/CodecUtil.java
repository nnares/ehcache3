/*
 * Copyright Terracotta, Inc.
 * Copyright IBM Corp. 2024, 2025
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ehcache.clustered.common.internal.messages;

import java.nio.ByteBuffer;

public final class CodecUtil {

  private CodecUtil() {
  }


  public static void putStringAsCharArray(ByteBuffer byteBuffer, String s) {
    byteBuffer.asCharBuffer().put(s);
    byteBuffer.position(byteBuffer.position() + (s.length() * 2));
  }

  public static String getStringFromBuffer(ByteBuffer buffer, int length) {
    char[] arr = new char[length];
    for (int i = 0; i < length; i++) {
      arr[i] = buffer.getChar();
    }
    return new String(arr);
  }

}
