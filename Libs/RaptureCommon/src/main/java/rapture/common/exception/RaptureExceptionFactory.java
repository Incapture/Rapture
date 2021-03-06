/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2011-2016 Incapture Technologies LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package rapture.common.exception;

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;

import rapture.common.Formattable;
import rapture.util.IDGenerator;

public class RaptureExceptionFactory {

    private static final Logger log = Logger.getLogger(RaptureExceptionFactory.class);

    public static RaptureException create(Integer status, Formattable message, Throwable cause) {
        String id = IDGenerator.getUUID();
        RaptureException e = new RaptureException(id, status, message.format(), cause);
        if (log.isTraceEnabled()) {
            log.trace(ExceptionToString.format(e));
        }
        return e;
    }

    public static RaptureException create(Integer status, Formattable message) {
        return create(status, message, null);
    }

    public static RaptureException create(Formattable message, Throwable cause) {
        return create(HttpStatus.SC_INTERNAL_SERVER_ERROR, message, cause);
    }

    public static RaptureException create(Formattable message) {
        return create(HttpStatus.SC_INTERNAL_SERVER_ERROR, message, null);
    }

    /**
     * Do not use hard codes strings. Use rapture.kernel.Messages.getMessage
     * and put the message in the property file. This ensures consistent error messages 
     * across the product and will ease localization efforts if the need ever arises.
     */
    public static RaptureException create(Integer status, String message, Throwable cause) {
        String id = IDGenerator.getUUID();
        RaptureException e = new RaptureException(id, status, message, cause);
        if (log.isTraceEnabled()) {
            log.trace(ExceptionToString.format(e));
        }
        return e;
    }

    /**
     * Do not use hard codes strings. Use rapture.kernel.Messages.getMessage
     * and put the message in the property file. This ensures consistent error messages 
     * across the product and will ease localization efforts if the need ever arises.
     */
    public static RaptureException create(Integer status, String message) {
        return create(status, message, null);
    }

    /**
     *  Do not use hard codes strings. Use rapture.kernel.Messages.getMessage
     * and put the message in the property file. This ensures consistent error messages 
     * across the product and will ease localization efforts if the need ever arises.
     */
    public static RaptureException create(String message) {
        return create(HttpStatus.SC_INTERNAL_SERVER_ERROR, message, null);
    }

    /**
     *  Do not use hard codes strings. Use rapture.kernel.Messages.getMessage
     * and put the message in the property file. This ensures consistent error messages 
     * across the product and will ease localization efforts if the need ever arises.
     */
    public static RaptureException create(String message, Throwable cause) {
        return create(HttpStatus.SC_INTERNAL_SERVER_ERROR, message, cause);
    }
}
