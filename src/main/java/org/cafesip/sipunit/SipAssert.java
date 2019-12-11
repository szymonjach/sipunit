/*
 * Created on Sep 20, 2009
 * 
 * Copyright 2005 CafeSip.org
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.cafesip.sipunit;

import com.jayway.awaitility.core.ConditionTimeoutException;
import lombok.experimental.UtilityClass;

import javax.sip.header.CSeqHeader;
import javax.sip.header.Header;
import javax.sip.message.Request;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This class is the static equivalent of SipTestCase. It is intended for use with JUnit 4 or for
 * when the test class must extend something other than SipTestCase.
 * 
 * <p>
 * These methods can be used directly: <code>SipAssert.assertAnswered(...)</code>, or they can be
 * referenced through static import:
 * 
 * <pre>
 * import static org.cafesip.sipunit.SipAssert.assertAnswered;
 *    ...
 *    assertAnswered(...);
 * </pre>
 * 
 * <p>
 * See SipTestCase for further details on writing a SipUnit test class.
 * 
 * @author Becky McElroy
 * 
 */
@UtilityClass
public class SipAssert {

  /**
   * Asserts that the last SIP operation performed by the given object was successful.
   * 
   * @param op the SipUnit object that executed an operation.
   */
  public void assertLastOperationSuccess(SipActionObject op) {
    assertNotNull(op);
    assertEquals(0, op.getErrorMessage().length());
  }

  /**
   * Asserts that the last SIP operation performed by the given object failed.
   * 
   * @param op the SipUnit object that executed an operation.
   */
  public void assertLastOperationFail(SipActionObject op) {
    assertNotNull(op);
    assertTrue(op.getErrorMessage().length() > 0);
  }

  /**
   * Asserts that the given SIP message contains at least one occurrence of the specified header.
   * 
   * @param sipMessage the SIP message.
   * @param header the string identifying the header, as specified in RFC-3261.
   * 
   */
  public void assertHeaderPresent(SipMessage sipMessage, String header) {
    assertNotNull(sipMessage);
    assertTrue(sipMessage.getHeaders(header).hasNext());
  }

  /**
   * Asserts that the given SIP message contains no occurrence of the specified header.
   * 
   * @param sipMessage the SIP message.
   * @param header the string identifying the header as specified in RFC-3261.
   */
  public void assertHeaderNotPresent(SipMessage sipMessage, String header) {
    assertNotNull(sipMessage);
    assertFalse(sipMessage.getHeaders(header).hasNext());
  }

  /**
   * Asserts that the given SIP message contains at least one occurrence of the specified header and
   * that at least one occurrence of this header contains the given value. The assertion fails if no
   * occurrence of the header contains the value or if the header is not present in the mesage.
   * Assertion failure output includes the given message text.
   * 
   * @param sipMessage the SIP message.
   * @param header the string identifying the header as specified in RFC-3261.
   * @param value the string value within the header to look for. An exact string match is done
   *        against the entire contents of the header. The assertion will pass if any part of the
   *        header matches the value given.
   */
  public void assertHeaderContains(SipMessage sipMessage, String header,
      String value) {
    assertNotNull(sipMessage);
    ListIterator<Header> l = sipMessage.getHeaders(header);
    while (l.hasNext()) {
      String h = l.next().toString();

      if (h.contains(value)) {
        assertTrue(true);
        return;
      }
    }

    fail();
  }

  /**
   * Asserts that the given SIP message contains no occurrence of the specified header with the
   * value given, or that there is no occurrence of the header in the message. The assertion fails
   * if any occurrence of the header contains the value. Assertion failure output includes the given
   * message text.
   * 
   * @param sipMessage the SIP message.
   * @param header the string identifying the header as specified in RFC-3261.
   * @param value the string value within the header to look for. An exact string match is done
   *        against the entire contents of the header. The assertion will fail if any part of the
   *        header matches the value given.
   */
  public void assertHeaderNotContains(SipMessage sipMessage, String header,
      String value) {
    assertNotNull(sipMessage);
    ListIterator<Header> l = sipMessage.getHeaders(header);
    while (l.hasNext()) {
      String h = l.next().toString();

      if (h.contains(value)) {
        fail();
      }
    }

    assertTrue(true);
  }

  /**
   * Asserts that the given message listener object received a response with the indicated status
   * code. Assertion failure output includes the given message text.
   * 
   * @param statusCode The response status code to check for (eg, SipResponse.RINGING)
   * @param obj The MessageListener object (ie, SipCall, Subscription, etc.).
   */
  public void assertResponseReceived(int statusCode, MessageListener obj) {
    assertNotNull(obj);
    assertTrue(responseReceived(statusCode, obj));
  }

  /**
   * Await until a the size of {@link SipCall#getAllReceivedResponses()} is equal to count.
   * 
   * @param call the {@link SipCall} under test
   * @param count the expected amount of responses
   * @throws ConditionTimeoutException If condition was not fulfilled within the default time
   *         period.
   */
  public void awaitReceivedResponses(final SipCall call, final int count) {
    await().until(() -> call.getAllReceivedResponses().size(), is(count));
  }

  /**
   * Check the given message listener object received a response with the indicated status
   * code.
   *
   * @param statusCode the code we want to find
   * @param messageListener the {@link MessageListener} we want to check
   * @return true if a received response matches the given statusCode
   */
  public boolean responseReceived(int statusCode, MessageListener messageListener) {
    ArrayList<SipResponse> responses = messageListener.getAllReceivedResponses();

    for (SipResponse r : responses) {
      if (statusCode == r.getStatusCode()) {
        return true;
      }
    }

    return false;
  }

  /**
   * Asserts that the given message listener object received a response with the indicated status
   * code, CSeq method and CSeq sequence number. Assertion failure output includes the given message
   * text.
   * 
   * @param statusCode The response status code to check for (eg, SipResponse.RINGING)
   * @param method The CSeq method to look for (SipRequest.INVITE, etc.)
   * @param sequenceNumber The CSeq sequence number to look for
   * @param obj The MessageListener object (ie, SipCall, Subscription, etc.).
   */
  public void assertResponseReceived(int statusCode, String method,
      long sequenceNumber, MessageListener obj) {
    assertNotNull(obj);
    assertTrue(responseReceived(statusCode, method, sequenceNumber, obj));
  }

  private boolean responseReceived(int statusCode, String method, long sequenceNumber,
      MessageListener obj) {
    List<SipResponse> responses = obj.getAllReceivedResponses();

    for (SipResponse resp : responses) {
      if (resp.getStatusCode() == statusCode) {
        CSeqHeader hdr = (CSeqHeader) resp.getMessage().getHeader(CSeqHeader.NAME);
        if (hdr != null) {
          if (hdr.getMethod().equals(method)) {
            if (hdr.getSeqNumber() == sequenceNumber) {
              return true;
            }
          }
        }
      }
    }

    return false;
  }

  /**
   * Asserts that the given message listener object has not received a response with the indicated
   * status code. Assertion failure output includes the given message text.
   * 
   * @param statusCode The response status code to verify absent (eg, SipResponse.RINGING)
   * @param obj The MessageListener object (ie, SipCall, Subscription, etc.).
   */
  public void assertResponseNotReceived(int statusCode, MessageListener obj) {
    assertNotNull(obj);
    assertFalse(responseReceived(statusCode, obj));
  }

  /**
   * Asserts that the given message listener object has not received a response with the indicated
   * status code, CSeq method and sequence number. Assertion failure output includes the given
   * message text.
   * 
   * @param statusCode The response status code to verify absent (eg, SipResponse.RINGING)
   * @param method The CSeq method to verify absent (SipRequest.INVITE, etc.)
   * @param sequenceNumber The CSeq sequence number to verify absent
   * @param obj The MessageListener object (ie, SipCall, Subscription, etc.).
   */
  public void assertResponseNotReceived(int statusCode, String method,
      long sequenceNumber, MessageListener obj) {
    assertNotNull(obj);
    assertFalse(responseReceived(statusCode, method, sequenceNumber, obj));
  }

  /**
   * Asserts that the given message listener object received a request with the indicated request
   * method. Assertion failure output includes the given message text.
   * 
   * @param method The request method to check for (eg, SipRequest.INVITE)
   * @param obj The MessageListener object (ie, SipCall, Subscription, etc.).
   */
  public void assertRequestReceived(String method, MessageListener obj) {
    assertNotNull(obj);
    assertTrue(requestReceived(method, obj));
  }

  private boolean requestReceived(String method, MessageListener obj) {
    List<SipRequest> requests = obj.getAllReceivedRequests();

    for (SipRequest request : requests) {
      Request req = (Request) request.getMessage();
      if (req != null) {
        if (req.getMethod().equals(method)) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Asserts that the given message listener object received a request with the indicated CSeq
   * method and CSeq sequence number. Assertion failure output includes the given message text.
   * 
   * @param method The CSeq method to look for (SipRequest.INVITE, etc.)
   * @param sequenceNumber The CSeq sequence number to look for
   * @param obj The MessageListener object (ie, SipCall, Subscription, etc.).
   */
  public void assertRequestReceived(String method, long sequenceNumber,
      MessageListener obj) {
    assertNotNull(obj);
    assertTrue(requestReceived(method, sequenceNumber, obj));
  }

  private boolean requestReceived(String method, long sequenceNumber, MessageListener obj) {
    List<SipRequest> requests = obj.getAllReceivedRequests();

    for (SipRequest request : requests) {
      Request req = (Request) request.getMessage();
      if (req != null) {
        CSeqHeader hdr = (CSeqHeader) req.getHeader(CSeqHeader.NAME);
        if (hdr != null) {
          if (hdr.getMethod().equals(method)) {
            if (hdr.getSeqNumber() == sequenceNumber) {
              return true;
            }
          }
        }
      }
    }

    return false;
  }

  /**
   * Asserts that the given message listener object has not received a request with the indicated
   * request method. Assertion failure output includes the given message text.
   * 
   * @param method The request method to verify absent (eg, SipRequest.BYE)
   * @param obj The MessageListener object (ie, SipCall, Subscription, etc.).
   */
  public void assertRequestNotReceived(String method, MessageListener obj) {
    assertNotNull(obj);
    assertFalse(requestReceived(method, obj));
  }

  /**
   * Asserts that the given message listener object has not received a request with the indicated
   * CSeq method and sequence number. Assertion failure output includes the given message text.
   * 
   * @param method The CSeq method to verify absent (SipRequest.INVITE, etc.)
   * @param sequenceNumber The CSeq sequence number to verify absent
   * @param obj The MessageListener object (ie, SipCall, Subscription, etc.).
   */
  public void assertRequestNotReceived(String method, long sequenceNumber,
      MessageListener obj) {
    assertNotNull(obj);
    assertFalse(requestReceived(method, sequenceNumber, obj));
  }

  /**
   * Asserts that the given incoming or outgoing call leg was answered. Assertion failure output
   * includes the given message text.
   * 
   * @param call The incoming or outgoing call leg.
   */
  public void assertAnswered(SipCall call) {
    assertNotNull(call);
    assertTrue(call.isCallAnswered());
  }

  /**
   * Awaits that the given incoming or outgoing call leg was answered. Assertion failure output
   * includes the given message text.
   * 
   * @param call The incoming or outgoing call leg.
   */
  public void awaitAnswered(final SipCall call) {
    await().until(() -> assertAnswered(call));
  }

  public void awaitDialogReady(final ReferNotifySender ub) {
    await().until(() -> assertNotNull(ub.getDialog()));
  }

  /**
   * Asserts that the given incoming or outgoing call leg has not been answered. Assertion failure
   * output includes the given message text.
   * 
   * @param call The incoming or outgoing call leg.
   */
  public void assertNotAnswered(SipCall call) {
    assertNotNull(call);
    assertFalse(call.isCallAnswered());
  }

  /**
   * Asserts that the given SIP message contains a body. Assertion failure output includes the given
   * message text.
   * 
   * @param sipMessage the SIP message.
   */
  public void assertBodyPresent(SipMessage sipMessage) {
    assertNotNull(sipMessage);
    assertTrue(sipMessage.getContentLength() > 0);
  }

  /**
   * Asserts that the given SIP message contains no body. Assertion failure output includes the
   * given message text.
   * 
   * @param sipMessage the SIP message.
   */
  public void assertBodyNotPresent(SipMessage sipMessage) {
    assertNotNull(sipMessage);
    assertFalse(sipMessage.getContentLength() > 0);
  }

  /**
   * Asserts that the given SIP message contains a body that includes the given value. The assertion
   * fails if a body is not present in the message or is present but doesn't include the value.
   * Assertion failure output includes the given message text.
   * 
   * @param sipMessage the SIP message.
   * @param value the string value to look for in the body. An exact string match is done against
   *        the entire contents of the body. The assertion will pass if any part of the body matches
   *        the value given.
   */
  public void assertBodyContains(SipMessage sipMessage, String value) {
    assertNotNull(sipMessage);
    assertBodyPresent(sipMessage);
    String body = new String(sipMessage.getRawContent());

    if (body.contains(value)) {
      assertTrue(true);
      return;
    }

    fail();
  }

  /**
   * Asserts that the body in the given SIP message does not contain the value given, or that there
   * is no body in the message. The assertion fails if the body is present and contains the value.
   * Assertion failure output includes the given message text.
   * 
   * @param sipMessage the SIP message.
   * @param value the string value to look for in the body. An exact string match is done against
   *        the entire contents of the body. The assertion will fail if any part of the body matches
   *        the value given.
   */
  public void assertBodyNotContains(SipMessage sipMessage, String value) {
    assertNotNull(sipMessage);
    if (sipMessage.getContentLength() > 0) {
      String body = new String(sipMessage.getRawContent());

      if (body.contains(value)) {
        fail();
      }
    }

    assertTrue(true);
  }

  /**
   * Asserts that the given Subscription has not encountered any errors while processing received
   * subscription responses and received NOTIFY requests. Assertion failure output includes the
   * given message text along with the encountered error(s).
   * 
   * @param subscription the Subscription in question.
   */
  public void assertNoSubscriptionErrors(EventSubscriber subscription) {
    assertNotNull(subscription);
    assertEquals(0, subscription.getEventErrors().size());
  }

  /**
   * Awaits the an error free {@link SipStack#dispose()}.
   */
  public void awaitStackDispose(final SipStack sipStack) {
    await().until(() -> {
      try {
        sipStack.dispose();
      } catch (RuntimeException e) {
        e.printStackTrace();
        fail();
      }
    });
  }

  // Later: ContentDispositionHeader, ContentEncodingHeader,
  // ContentLanguageHeader,
  // ContentLengthHeader, ContentTypeHeader, MimeVersionHeader

  /*
   * From seeing how to remove our stuff from the failure stack:
   * 
   * catch (AssertionFailedError e) // adjust stack trace { ArrayList stack = new ArrayList();
   * StackTraceElement[] t = e.getStackTrace(); String thisclass = this.getClass().getName(); for
   * (int i = 0; i < t.length; i++) { StackTraceElement ele = t[i]; System.out.println("Element = "
   * + ele.toString()); if (thisclass.equals(ele.getClass().getName()) == true) { continue; }
   * stack.add(t[i]); }
   * 
   * StackTraceElement[] new_stack = new StackTraceElement[stack.size()];
   * e.setStackTrace((StackTraceElement[]) stack.toArray(new_stack)); throw e; }
   */

  /*
   * public class Arguments { public static void notNull(Object arg) { if(arg == null) {
   * IllegalArgumentException t = new IllegalArgumentException(); reorient(t); throw t; } } private
   * static void reorient(Throwable t) { StackTraceElement[] elems = t.getStackTrace();
   * StackTraceElement[] subElems = new StackTraceElement[elems.length-1]; System.arrayCopy(elems,
   * 1, subElems, 0, elems.length-1); t.setStackTrace(t); } }
   */

}
