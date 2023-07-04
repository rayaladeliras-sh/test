/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cxf.interceptor;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.cxf.common.injection.NoJSR250Annotations;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;

import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitor;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitorFactory;

/**
 * A simple logging handler which outputs the bytes of the message to the
 * Logger.
 */
@NoJSR250Annotations
public class LoggingInInterceptor extends AbstractLoggingInterceptor {
    private static final Logger LOG = LogUtils.getLogger(LoggingInInterceptor.class);
    
    public LoggingInInterceptor() {
        super(Phase.RECEIVE);
    }
    
    public LoggingInInterceptor(String phase) {
        super(phase);
    }

    public LoggingInInterceptor(String id, String phase) {
        super(id, phase);
    }

    public LoggingInInterceptor(int lim) {
        this();
        limit = lim;
    }
    public LoggingInInterceptor(String id, int lim) {
        this(id, Phase.RECEIVE);
        limit = lim;
    }

    public LoggingInInterceptor(PrintWriter w) {
        this();
        this.writer = w;
    }
    public LoggingInInterceptor(String id, PrintWriter w) {
        this(id, Phase.RECEIVE);
        this.writer = w;
    }
    
    public void handleMessage(Message message) throws Fault {
        Logger logger = getMessageLogger(message);
        if (writer != null || logger.isLoggable(Level.INFO)) {
            logging(logger, message);
        }
    }

	@Override
	protected String transform(String originalLogString) {
		/* If the request has fulfillmentArtifact and deliveryoption is not pdf */
		if (Pattern.compile("fulfillmentArtifact", Pattern.CASE_INSENSITIVE).matcher(originalLogString).find()
				&& !Pattern.compile("\"deliveryOption\":\"pdf\"", Pattern.CASE_INSENSITIVE).matcher(originalLogString)
						.find()) {
			try {
				originalLogString = maskField(originalLogString, "fulfillmentArtifact");
			} catch (Exception e) {
				LOG.info("Exception while masking the payload");
			}
		}
		return originalLogString;
	}

    private String maskField(String str, String fieldName) {
        String regExp = "(.+?)\"" + fieldName + "\":\\s*\"[-\\w]+([-\\w]{4})\"(.+?)";
        String replacement = "$1\"" + fieldName + "\":\"****$2\"$3";
        String result = "";

        if (str != null)
          result = str.replaceAll(regExp, replacement);

        return result;
      }

    protected void logging(Logger logger, Message message) throws Fault {
        if (message.containsKey(LoggingMessage.ID_KEY)) {
            return;
        }
        String id = (String)message.getExchange().get(LoggingMessage.ID_KEY);
        if (id == null) {
            id = LoggingMessage.nextId();
            message.getExchange().put(LoggingMessage.ID_KEY, id);
        }
        message.put(LoggingMessage.ID_KEY, id);
        final LoggingMessage buffer 
            = new LoggingMessage("Custom Inbound Message\n----------------------------", id);

        Integer responseCode = (Integer)message.get(Message.RESPONSE_CODE);
        if (responseCode != null) {
            buffer.getResponseCode().append(responseCode);
        }

        String encoding = (String)message.get(Message.ENCODING);

        if (encoding != null) {
            buffer.getEncoding().append(encoding);
        }
        String httpMethod = (String)message.get(Message.HTTP_REQUEST_METHOD);
        if (httpMethod != null) {
            buffer.getHttpMethod().append(httpMethod);
        }
        String ct = (String)message.get(Message.CONTENT_TYPE);
        if (ct != null) {
            buffer.getContentType().append(ct);
        }
        // Do not log Headers
        /* Object headers = message.get(Message.PROTOCOL_HEADERS);

        if (headers != null) {
            buffer.getHeader().append(headers);
        }*/
        String uri = (String)message.get(Message.REQUEST_URL);
        if (uri != null) {
            buffer.getAddress().append(uri);
            String query = (String)message.get(Message.QUERY_STRING);
            if (query != null) {
                buffer.getAddress().append("?").append(query);
            }
        }
        
        if (!isShowBinaryContent() && isBinaryContent(ct)) {
            buffer.getMessage().append(BINARY_CONTENT_MESSAGE).append('\n');
            log(logger, buffer.toString());
            return;
        }
        
        InputStream is = message.getContent(InputStream.class);
        if (is != null) {
            CachedOutputStream bos = new CachedOutputStream();
            if (threshold > 0) {
                bos.setThreshold(threshold);
            }
            try {
                IOUtils.copy(is, bos);

                bos.flush();
                is.close();

                message.setContent(InputStream.class, bos.getInputStream());
                if (bos.getTempFile() != null) {
                    //large thing on disk...
                    buffer.getMessage().append("\nMessage (saved to tmp file):\n");
                    buffer.getMessage().append("Filename: " + bos.getTempFile().getAbsolutePath() + "\n");
                }
                if (bos.size() > limit) {
                    buffer.getMessage().append("(message truncated to " + limit + " bytes)\n");
                }
                writePayload(buffer.getPayload(), bos, encoding, ct); 
                    
                bos.close();
            } catch (Exception e) {
                throw new Fault(e);
            }
        } else {
            Reader reader = message.getContent(Reader.class);
            if (reader != null) {
                try {
                    BufferedReader r = new BufferedReader(reader, limit);
                    r.mark(limit);
                    char b[] = new char[limit];
                    int i = r.read(b);
                    buffer.getPayload().append(b, 0, i);
                    r.reset();
                    message.setContent(Reader.class, r);
                } catch (Exception e) {
                    throw new Fault(e);
                }
                
            }
        }
        log(logger, buffer.toString());
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
