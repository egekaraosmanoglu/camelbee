/*
 * Copyright 2023 Rahmi Ege Karaosmanoglu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camelbee.constants;

/**
 * CamelBee Constants.
 */
public final class CamelBeeConstants {

  private CamelBeeConstants() {
    throw new IllegalStateException("Utility class");
  }

  public static final String INITIAL_MESSAGE = "camelbee-initial-message-received";

  public static final String CURRENT_ROUTE_NAME = "camelbee-current-route-name";

  public static final String CURRENT_ROUTE_TRACE_STACK = "camelbee-routeTraceStack";

  public static final String SEND_ENDPOINT = "camelbee-send-endpoint";

  public static final String FRAMEWORK = "SpringBoot";

  public static final String SYSTEM_JVM_VENDOR = "java.vendor";

  public static final String SYSTEM_JVM_VENDOR_URL = "java.vendor.url";

  public static final String SYSTEM_JVM_VERSION = "java.version";

  public static final String CURRENT_INTERCEPTOR_TYPE = "camelbee-current-interceptor-type";

  public static final String CAMEL_HTTP_PATH_HEADER = "camelbee-camelhttppath";

  public static final String CAMEL_FAILED_EVENT_IDENTITIY_HASHCODE = "camelbee-failedevent-identitiy";

}
