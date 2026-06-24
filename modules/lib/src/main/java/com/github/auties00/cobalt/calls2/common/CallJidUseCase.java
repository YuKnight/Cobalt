package com.github.auties00.cobalt.calls2.common;

import java.util.Optional;

/**
 * Enumerates the voip JID use-case codes the wa-voip engine assigns to a callable JID.
 *
 * <p>The engine stores a structural use-case code alongside every JID it builds; the code records which
 * kind of callable address the JID denotes (a user, a group, a device, a group-call session, a
 * newsletter, or a call-link extension) and is orthogonal to the {@link CallJidDomainType domain-type}
 * code, which records the WhatsApp namespace. The two fields are independent: the same use-case is reused
 * across several domains. A phone-number user, a LID user, a bot, and a hosted-LID user all carry the
 * {@link #USER} use-case and differ only by domain-type; likewise every device JID carries the
 * {@link #DEVICE} use-case regardless of which device-capable domain it lives on.
 *
 * <p>The value space is a closed set of six concrete codes: {@code 0} user, {@code 1} group, {@code 5}
 * device, {@code 6} group call, {@code 12} newsletter, and {@code 13} extension. The intervening codes
 * {@code 2,3,4,7,8,9,10,11} are unused. The numeric coincidence between {@link #DEVICE} (use-case
 * {@code 5}) and the {@code lid} domain-type code ({@code 5}) is incidental; the use-case is a structural
 * selector applied independently of the domain.
 *
 * @implNote This implementation ports the use-case argument the engine passes to
 * {@code wa_call_identifier_jid_initialize} (fn10519, {@code call_jid.cc}, the sole writer of the field)
 * in module {@code ff-tScznZ8P}. The closed set was proven exhaustive from the field's only four
 * producers: {@code wa_call_user_jid_create} (fn10522, use-case {@code 0}),
 * {@code wa_call_device_jid_create} (fn10523, use-case {@code 5}), the explicit-use-case thunk fn13840
 * forwarding its callers {@code wa_call_group_jid_create} (fn10520, {@code 1}),
 * {@code wa_call_group_call_jid_create} (fn10518, {@code 6}), and {@code wa_call_extension_jid_create}
 * (fn10517, {@code 13}), plus the inline newsletter branch of {@code wa_call_jid_from_string} (fn10513,
 * {@code 12}); a whole-corpus scan of the instruction stream found no other call site feeding the
 * initializer. The engine carries the field as a bare integer with no symbolic name table, so the
 * constant names here are Cobalt's; only the numeric values are recovered.
 */
public enum CallJidUseCase {
    /**
     * The user address use-case, code {@code 0}.
     *
     * <p>The default use-case the engine attaches to a bare user JID. It is reused for every
     * user-namespace domain ({@code s.whatsapp.net}, {@code call}, {@code lid}, {@code bot}, and
     * {@code hosted.lid}), so a phone-number user, a LID user, a bot, and a hosted-LID user all carry it.
     *
     * @implNote This implementation binds use-case {@code 0} from {@code wa_call_user_jid_create}
     * (fn10522), which passes {@code i32.const 0} as the use-case argument to
     * {@code wa_call_identifier_jid_initialize} for any domain in the {@code 0xa29} user-domain mask
     * ({@code 0,3,5,9,11}).
     */
    USER(0),

    /**
     * The group-chat address use-case, code {@code 1}.
     *
     * <p>The use-case for a {@code g.us} group-chat JID, distinct from {@link #GROUP_CALL}, which is a
     * group-call session identifier rather than the group-chat identity.
     *
     * @implNote This implementation binds use-case {@code 1} from {@code wa_call_group_jid_create}
     * (fn10520), which forwards use-case {@code 1} through the explicit-use-case thunk fn13840 to
     * {@code wa_call_identifier_jid_initialize} with domain-type {@code 1} ({@code g.us}).
     */
    GROUP(1),

    /**
     * The device address use-case, code {@code 5}.
     *
     * <p>The structural selector marking a JID as a per-device address rather than a bare account. It is
     * applied for every device-capable domain ({@code call}, {@code lid}, {@code bot}, {@code hosted.lid}).
     * The value {@code 5} coincides numerically with the {@code lid} domain-type code but is independent
     * of it.
     *
     * @implNote This implementation binds use-case {@code 5} from {@code wa_call_device_jid_create}
     * (fn10523), which passes {@code i32.const 5} as the use-case argument to
     * {@code wa_call_identifier_jid_initialize} for any domain in the {@code 0xa28} device-domain mask
     * ({@code 3,5,9,11}).
     */
    DEVICE(5),

    /**
     * The group-call session use-case, code {@code 6}.
     *
     * <p>The use-case for a group-call session identifier in the {@code call} namespace, distinct from
     * the {@link #GROUP} group-chat identity and the {@link #EXTENSION} call-link.
     *
     * @implNote This implementation binds use-case {@code 6} from {@code wa_call_group_call_jid_create}
     * (fn10518), which forwards use-case {@code 6} through the explicit-use-case thunk fn13840 to
     * {@code wa_call_identifier_jid_initialize} with domain-type {@code 3} ({@code call}).
     */
    GROUP_CALL(6),

    /**
     * The newsletter address use-case, code {@code 12}.
     *
     * <p>The use-case for a newsletter (Channel) JID in the {@code newsletter} namespace.
     *
     * @implNote This implementation binds use-case {@code 12} from the inline newsletter branch of
     * {@code wa_call_jid_from_string} (fn10513), which calls {@code wa_call_identifier_jid_initialize}
     * with use-case {@code 0xc} and domain-type {@code 8} ({@code newsletter}); this build has no
     * standalone exported newsletter-create function.
     */
    NEWSLETTER(12),

    /**
     * The call-link extension use-case, code {@code 13}.
     *
     * <p>The use-case for a call-link (extension) identifier in the {@code call} namespace.
     *
     * @implNote This implementation binds use-case {@code 13} from {@code wa_call_extension_jid_create}
     * (fn10517), which forwards use-case {@code 0xd} through the explicit-use-case thunk fn13840 to
     * {@code wa_call_identifier_jid_initialize} with domain-type {@code 3} ({@code call}).
     */
    EXTENSION(13);

    /**
     * The integer use-case code the wa-voip engine stores for this address kind.
     */
    private final int value;

    /**
     * Constructs a use-case constant bound to its engine code.
     *
     * @param value the integer use-case code the engine stores
     */
    CallJidUseCase(int value) {
        this.value = value;
    }

    /**
     * Returns the integer use-case code the wa-voip engine stores for this address kind.
     *
     * @return the engine use-case code
     */
    public int value() {
        return value;
    }

    /**
     * Returns the use-case whose {@linkplain #value() value} equals the given code.
     *
     * <p>Codes outside the closed set ({@code 0,1,5,6,12,13}) yield an empty result.
     *
     * @param value the engine use-case code to resolve
     * @return the matching use-case, or {@link Optional#empty()} if no use-case matches
     */
    public static Optional<CallJidUseCase> ofValue(int value) {
        for (var useCase : values()) {
            if (useCase.value == value) {
                return Optional.of(useCase);
            }
        }
        return Optional.empty();
    }
}
