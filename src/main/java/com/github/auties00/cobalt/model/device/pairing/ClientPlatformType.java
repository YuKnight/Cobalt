package com.github.auties00.cobalt.model.device.pairing;

import it.auties.protobuf.annotation.ProtobufEnum;
import it.auties.protobuf.annotation.ProtobufEnumIndex;

@ProtobufEnum(name = "ClientPayload.UserAgent.Platform")
public enum ClientPlatformType {
    ANDROID(0),
    IOS(1),
    WINDOWS_PHONE(2),
    BLACKBERRY(3),
    BLACKBERRYX(4),
    S40(5),
    S60(6),
    PYTHON_CLIENT(7),
    TIZEN(8),
    ENTERPRISE(9),
    ANDROID_BUSINESS(10),
    KAIOS(11),
    IOS_BUSINESS(12),
    WINDOWS(13),
    WEB(14),
    PORTAL(15),
    GREEN_ANDROID(16),
    GREEN_IPHONE(17),
    BLUE_ANDROID(18),
    BLUE_IPHONE(19),
    FBLITE_ANDROID(20),
    MLITE_ANDROID(21),
    IGLITE_ANDROID(22),
    PAGE(23),
    MACOS(24),
    OCULUS_MSG(25),
    OCULUS_CALL(26),
    MILAN(27),
    CAPI(28),
    WEAROS(29),
    ARDEVICE(30),
    VRDEVICE(31),
    BLUE_WEB(32),
    IPAD(33),
    TEST(34),
    SMART_GLASSES(35),
    BLUE_VR(36),
    AR_WRIST(37);

    ClientPlatformType(@ProtobufEnumIndex int index) {
        this.index = index;
    }

    final int index;

    public int index() {
        return this.index;
    }
}
