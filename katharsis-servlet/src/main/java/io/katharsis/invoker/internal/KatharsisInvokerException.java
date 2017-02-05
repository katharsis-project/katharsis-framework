/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.katharsis.invoker.internal;


/**
 * The <CODE>KatharsisInvokerException</CODE> class defines a general exception
 * that <CODE>KatharsisInvoker</CODE> can throw when it is unable to perform its operation
 * successfully.
 */
public class KatharsisInvokerException extends RuntimeException {

    private final int statusCode;

    /**
     * Constructs a new KatharsisInvokerException exception.
     *
     * @param   statusCode
     *          the status code
     */
    public KatharsisInvokerException(final int statusCode) {
        super();
        this.statusCode = statusCode;
    }

    /**
     * Constructs a new KatharsisInvokerException exception with the given message.
     *
     * @param   statusCode
     *          the status code
     * @param   message
     *          the exception message
     */
    public KatharsisInvokerException(final int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    /**
     * Constructs a new KatharsisInvokerException exception with the nested exception.
     *
     * @param   statusCode
     *          the status code
     * @param   nested
     *          the nested exception
     */
    public KatharsisInvokerException(final int statusCode, Throwable nested) {
        super(nested);
        this.statusCode = statusCode;
    }

    /**
     * Constructs a new KatharsisInvokerException exception when the container needs to do
     * the following:
     * <ul>
     * <li>throw an exception 
     * <li>include the "nested" exception
     * <li>include a description message
     * </ul>
     *
     * @param   statusCode
     *          the status code
     * @param   msg
     *          the exception message
     * @param   nested
     *          the nested exception
     */
    public KatharsisInvokerException(final int statusCode, String msg, Throwable nested) {
        super(msg, nested);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
