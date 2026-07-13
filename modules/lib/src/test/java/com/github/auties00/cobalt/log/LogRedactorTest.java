package com.github.auties00.cobalt.log;

import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.stanza.Stanza;
import com.github.auties00.cobalt.stanza.StanzaBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercises the privacy redaction layer ({@link LogRedactor}, {@link LogRedacted}, and the {@link Log} wrapper
 * factories). The tests assert the three properties the design promises -- same input yields the same token,
 * distinct inputs yield distinct tokens, and no token echoes the raw value -- plus the structural redaction of
 * {@link Jid} and {@link Stanza} and the on/off toggle.
 *
 * <p>Every test uses a fixed sample phone number ({@code 393331234567}) as the canonical secret so the "raw
 * value never appears" assertions read consistently. The salt is process-random, so the tests assert
 * within-run determinism rather than pinning exact token bytes.
 */
@DisplayName("LogRedactor")
class LogRedactorTest {
    private static final String PHONE = "393331234567";

    @AfterEach
    void reEnableRedaction() {
        LogRedactor.setEnabled(true);
    }

    @Nested
    @DisplayName("fingerprinting")
    class Fingerprinting {
        @Test
        @DisplayName("is deterministic for equal inputs and distinct for different inputs")
        void deterministicAndDistinct() {
            var first = LogRedactor.fingerprint(PHONE.getBytes());
            var again = LogRedactor.fingerprint(PHONE.getBytes());
            var other = LogRedactor.fingerprint("393339999999".getBytes());
            assertEquals(first, again, "same bytes must fingerprint identically");
            assertNotEquals(first, other, "different bytes must fingerprint differently");
        }

        @Test
        @DisplayName("never echoes the raw value")
        void neverEchoesRaw() {
            assertFalse(LogRedactor.fingerprint(PHONE.getBytes()).contains(PHONE));
        }
    }

    @Nested
    @DisplayName("Jid")
    class JidRedaction {
        @Test
        @DisplayName("hides the user but keeps server and device structure")
        void keepsRoutingHidesUser() {
            var redacted = LogRedactor.redactJid(Jid.of(PHONE + ":5@lid"));
            assertFalse(redacted.contains(PHONE), "phone/user must not appear");
            assertTrue(redacted.startsWith("#"), "user is replaced by a fingerprint token");
            assertTrue(redacted.contains("@lid"), "server is preserved");
            assertTrue(redacted.contains(":5"), "device is preserved");
        }

        @Test
        @DisplayName("renders server-only JIDs verbatim")
        void serverOnlyVerbatim() {
            assertEquals("s.whatsapp.net", LogRedactor.redactJid(Jid.userServer()));
        }

        @Test
        @DisplayName("maps distinct users to distinct tokens and the same user to the same token")
        void correlatable() {
            var a = LogRedactor.redactJid(Jid.of(PHONE + "@s.whatsapp.net"));
            var aAgain = LogRedactor.redactJid(Jid.of(PHONE + "@s.whatsapp.net"));
            var b = LogRedactor.redactJid(Jid.of("393339999999@s.whatsapp.net"));
            assertEquals(a, aAgain);
            assertNotEquals(a, b);
        }
    }

    @Nested
    @DisplayName("byte[]")
    class ByteRedaction {
        @Test
        @DisplayName("keeps the length but not the content")
        void lengthOnly() {
            var key = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
            var redacted = LogRedactor.redactBytes(key);
            assertTrue(redacted.startsWith("bytes(len=8,#"));
            assertFalse(redacted.contains("1, 2"), "array content must not appear");
        }
    }

    @Nested
    @DisplayName("wrappers")
    class Wrappers {
        @Test
        @DisplayName("mask the raw value behind a typed token")
        void maskRawValue() {
            assertFalse(Log.phone(PHONE).toString().contains(PHONE));
            assertTrue(Log.phone(PHONE).toString().startsWith("phone(#"));
            assertTrue(Log.token("abcSECRETxyz").toString().startsWith("token(len=12,#"));
            assertFalse(Log.token("abcSECRETxyz").toString().contains("SECRET"));
            assertTrue(Log.email("a@b.com").toString().startsWith("email(#"));
            assertTrue(Log.code("123456").toString().startsWith("code(#"));
            assertTrue(Log.secret("hunter2").toString().startsWith("secret(#"));
            assertFalse(Log.secret("hunter2").toString().contains("hunter2"));
        }

        @Test
        @DisplayName("fingerprint the E.164 and bare-digit phone forms identically")
        void phoneNormalisation() {
            var plus = Log.phone("+" + PHONE).toString();
            var bare = Log.phone(PHONE).toString();
            var numeric = Log.phone(393331234567L).toString();
            assertEquals(plus, bare);
            assertEquals(bare, numeric);
        }

        @Test
        @DisplayName("redact a JID carried as a String")
        void jidStringWrapper() {
            var redacted = Log.jid(PHONE + "@s.whatsapp.net").toString();
            assertFalse(redacted.contains(PHONE));
            assertTrue(redacted.contains("@s.whatsapp.net"));
        }
    }

    @Nested
    @DisplayName("Stanza")
    class StanzaRedaction {
        @Test
        @DisplayName("summarises shape without leaking attribute values or content")
        void shapeOnly() {
            var stanza = new StanzaBuilder()
                    .description("message")
                    .attribute("id", "STANZA123")
                    .attribute("from", Jid.of(PHONE + "@s.whatsapp.net"))
                    .attribute("type", "text")
                    .build();
            var redacted = LogRedactor.redactStanza(stanza);
            assertTrue(redacted.contains("stanza(message"));
            assertTrue(redacted.contains("attrs=[id, from, type]"), "attribute keys are structure");
            assertFalse(redacted.contains("STANZA123"), "attribute values must not appear");
            assertFalse(redacted.contains(PHONE), "the from JID must not appear");
        }

        @Test
        @DisplayName("marks textual content as redacted without printing it")
        void textContentLogRedacted() {
            var stanza = new StanzaBuilder()
                    .description("body")
                    .content("super secret body")
                    .build();
            var redacted = LogRedactor.redactStanza(stanza);
            assertTrue(redacted.contains("content=<redacted>"));
            assertFalse(redacted.contains("secret body"));
        }

        @Test
        @DisplayName("reports the child count for container stanzas")
        void childCount() {
            var child = new StanzaBuilder().description("item").build();
            var stanza = new StanzaBuilder().description("iq").content(child).build();
            assertTrue(LogRedactor.redactStanza(stanza).contains("children=1"));
        }
    }

    @Nested
    @DisplayName("toggle")
    class Toggle {
        @Test
        @DisplayName("passes values through raw when disabled")
        void disabledPassesThrough() {
            LogRedactor.setEnabled(false);
            assertEquals(Jid.of(PHONE + "@s.whatsapp.net"), LogRedactor.redact(Jid.of(PHONE + "@s.whatsapp.net")));
            assertTrue(Log.phone(PHONE).toString().contains(PHONE), "wrapper shows raw value when disabled");
        }
    }

    @Nested
    @DisplayName("formatMessage")
    class FormatMessage {
        @Test
        @DisplayName("redacts Jid and wrapper parameters in the rendered line")
        void redactsParameters() {
            var formatter = new Log.CobaltFormatter();
            var record = new LogRecord(Level.INFO, "sent to {0} with code {1}");
            record.setParameters(new Object[]{Jid.of(PHONE + "@s.whatsapp.net"), Log.code("654321")});
            var message = formatter.formatMessage(record);
            assertFalse(message.contains(PHONE), "JID phone must be masked");
            assertFalse(message.contains("654321"), "code must be masked");
            assertTrue(message.contains("code(#"));
        }

        @Test
        @DisplayName("leaves a non-sensitive parameter untouched")
        void keepsSafeParameters() {
            var formatter = new Log.CobaltFormatter();
            var record = new LogRecord(Level.INFO, "retry count {0}");
            record.setParameters(new Object[]{7});
            assertEquals("retry count 7", formatter.formatMessage(record));
        }
    }
}
