package com.github.auties00.cobalt.wam.privatestats;

import com.github.auties00.cobalt.wam.privatestats.ed25519.Ed25519HashToPoint;
import com.github.auties00.cobalt.wam.privatestats.ed25519.Ed25519Point;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Byte-identical agreement tests against vectors captured from the live
 * WhatsApp Web JavaScript bundle.
 *
 * <p>Each vector pins the output of {@code WACryptoEd25519.hashToPoint},
 * {@code WAWamPrivateStatsToken.blindToken}, and
 * {@code WAWamPrivateStatsToken.unblindToken} on a deterministic input
 * tuple. Agreement on these vectors is the strongest validation possible:
 * if any constant or formula in the Java port diverges from the JavaScript
 * reference, at least one of the three outputs will mismatch.
 *
 * <p>Vectors were captured against snapshot revision {@code 1038176432}
 * (live-runtime revision {@code 1038189736}) on 2026-04-27 via the
 * {@code mcp__whatsapp__web_live_debug_eval} tool. Re-capture if the WA Web
 * implementation changes.
 */
class Ed25519LiveBundleKatTest {
    /**
     * One captured vector. Inputs are seeded deterministically; outputs are
     * the bytes returned by the live JS bundle.
     */
    private record Vector(int index, String msg, String scalar, String sk,
                          String hashToPoint, String blinded, String pk,
                          String signed, String unblinded) {
    }

    /**
     * The eight vectors captured from the live bundle. Hex-encoded for
     * readability; converted to bytes via {@link #fromHex} before use.
     */
    private static final Vector[] VECTORS = {
            new Vector(0,
                    "00112233445566778899aabbccddeeff102132435465768798a9bacbdcedfe0f",
                    "65768798a9bacbdcedfe0f2031425364758697a8b9cadbecfd0e1f3041526374",
                    "cadbecfd0e1f30415263748596a7b8c9daebfc0d1e2f405162738495a6b7c8d9",
                    "817e1065cd465effe439bd52de34bd1a290b190b338addb997c39355d70a693e",
                    "7832328b56552bc2e8e11d499a6a878dd3458ec9efefb67fb7f5c19b28f33247",
                    "fc551cd18fe80e2d4eae3d2bdec50f4891bd0b5f2afdc0d321af62c4c7bcabd6",
                    "d84e03af76eecd5819ab79d41471afd0a33af91dff632fd3bd42a6f8761ee649",
                    "db1982a9e35533565d9a73abb82514d41b36291ae4afa1fa3e0072b062360bf3"),
            new Vector(1,
                    "25364758697a8b9cadbecfe0f102132435465768798a9bacbdcedff001122334",
                    "8a9bacbdcedff00112233445566778899aabbccddeef00112233445566778899",
                    "ef00112233445566778899aabbccddeeff102132435465768798a9bacbdcedfe",
                    "d5f2c0f717ea52162d06f1a24ca6430616e5fbd6ded66ca967628d2605ccaaed",
                    "add3c7eb7974a0fd1f13d1c9aba52f1ff36310b8f28c372c2665d6e72ba88091",
                    "c33fe612e668a3ad86d9de096b09b2fc77c9109011fc39b14676bf00f8b6d3b9",
                    "7bf9a7fe66285eb5a42d6006bf07fefc925318b3564f66aef9770f54dfa8312a",
                    "9d49bbade38645fbb8d08bfc77f079b33249112c6b666917726b21de093c1286"),
            new Vector(2,
                    "4a5b6c7d8e9fb0c1d2e3f405162738495a6b7c8d9eafc0d1e2f3041526374859",
                    "afc0d1e2f30415263748596a7b8c9daebfd0e1f2031425364758697a8b9cadbe",
                    "1425364758697a8b9cadbecfe0f102132435465768798a9bacbdcedff0011223",
                    "e937d8d50a190e794c78e1a780b685d701362d7bb9417d9b270206227a2adfa5",
                    "61db6b8aeb9f887158c6a7291507e3df2a71a0956447177b2af90033cfcc2c11",
                    "0c7751f335e21648b8b06a4e2ada2bd4519b48ba690a5179c47c354c50920406",
                    "5587447c61ba8046fa50993b74be7ac42cad190dcba80fdbe321425c477e7d85",
                    "eca0a43ac364a61f8b62906801ed5de31a19d684b503541a5bb71850dd89b691"),
            new Vector(3,
                    "6f8091a2b3c4d5e6f708192a3b4c5d6e7f90a1b2c3d4e5f60718293a4b5c6d7e",
                    "d4e5f60718293a4b5c6d7e8fa0b1c2d3e4f5061728394a5b6c7d8e9fb0c1d2e3",
                    "394a5b6c7d8e9fb0c1d2e3f405162738495a6b7c8d9eafc0d1e2f30415263748",
                    "7b3cefe74efc46b22c52d9e62d15c5a6f4cc9a7091f4d97bce8f2ef8bb5a07ef",
                    "5d61059c4c41983ae54a2d00e2b0a59e222682c4ad387b8e712a67d778bdd551",
                    "df3b01ca89c46a2c02b565d382b3a79248b9a751926be488457f82204b7105a9",
                    "ead28f8e2db252d94030292360f7b1c90041b7b40b8108f87d85f4dbeb926743",
                    "99daf1455ddc3736215fe27e4cb348ea5ff1c46fb4a1b917075377448a41fabc"),
            new Vector(4,
                    "94a5b6c7d8e9fa0b1c2d3e4f60718293a4b5c6d7e8f90a1b2c3d4e5f708192a3",
                    "f90a1b2c3d4e5f708192a3b4c5d6e7f8091a2b3c4d5e6f8091a2b3c4d5e6f708",
                    "5e6f8091a2b3c4d5e6f708192a3b4c5d6e7f90a1b2c3d4e5f60718293a4b5c6d",
                    "313dc2687a23547a65b171acb9c85630cd6af8ca300bac4fd72c295645304596",
                    "c873ccc11c2d567ee68c25aea8266902f999dbd978c33d532b4f15094f52048a",
                    "30051c48d703814920e0643c558a50207708086abf5a59f79ac5fc49bc44d64e",
                    "7b27f6b0f54335e97ff7432c8219fd4c5c3036d45a7638d98d91771e572b2a4d",
                    "0987d7e728647f665a4fc1647a313d9b2eef45284b577341245494504efeed6b"),
            new Vector(5,
                    "b9cadbecfd0e1f30415263748596a7b8c9daebfc0d1e2f405162738495a6b7c8",
                    "1e2f405162738495a6b7c8d9eafb0c1d2e3f5061728394a5b6c7d8e9fa0b1c2d",
                    "8394a5b6c7d8e9fa0b1c2d3e4f60718293a4b5c6d7e8f90a1b2c3d4e5f708192",
                    "5c57d1fc551486c0f2ce7a84ff03124745105b77b0845226f75662694c583560",
                    "048acda4aa0786f9b6f1bfc4bd692884ae1ad32137fb7e0207d06b216b2c36eb",
                    "16dd768ffd9656f9657205bfc2656566553964e9df1c6dae105266f0e24d8257",
                    "41a6d4874010ea71c390193830fc4135a75c857adb421515d867a84153f29585",
                    "02c09483fd32868e859f7c844d03ac693e729e2b44158c0da360fbc2f5459199"),
            new Vector(6,
                    "deef00112233445566778899aabbccddeeff102132435465768798a9bacbdced",
                    "435465768798a9bacbdcedfe0f2031425364758697a8b9cadbecfd0e1f304152",
                    "a8b9cadbecfd0e1f30415263748596a7b8c9daebfc0d1e2f405162738495a6b7",
                    "88b38e252297ddaae90416a6a877aadc14b4f868930ef146c5494804b12b1f0f",
                    "c1b8129c5f8b72b50c7a050e0dc2387626d80f85df63289cd3039635c3825672",
                    "53732d02ea5c0f93c650f9a7dfebc1552df58fb4d5bce75fd52e7dc6ec18e55e",
                    "d6e2b1af7a3a7ad636df84fe27443b78960f4cbd850a68070a82dc69b0a53739",
                    "495f5380e666a9e96867e482dd5ca908e0ae423afd709297900516510885ba56"),
            new Vector(7,
                    "031425364758697a8b9cadbecfe0f102132435465768798a9bacbdcedff00112",
                    "68798a9bacbdcedff00112233445566778899aabbccddeef0011223344556677",
                    "cddeef00112233445566778899aabbccddeeff102132435465768798a9bacbdc",
                    "853765e8166b01b871a2cf9d0bfa033131b474c98b3f4befd31ad3edbb378b8c",
                    "8a6285c0f6be4f2b0949bde6951ac33af583e046d182d1aff0ed55f5533b0a84",
                    "e5bd8ad17e06dc74de3e976873db1fd27e230600dace4abf042d40cd70b10a85",
                    "693de7866beb607b40f5102594dc401a3b1866a5b01d70f531dfe43fc3c753a7",
                    "48ca3f3eff319b8358021ad625f8e12268e8c4a771a2584db297c74e8e0393e2")
    };

    /**
     * Asserts {@link Ed25519HashToPoint#compute} produces the byte-identical
     * output of the live JS {@code WACryptoEd25519.hashToPoint} for every
     * vector.
     */
    @Test
    void hashToPointMatchesLiveBundle() {
        for (var v : VECTORS) {
            var msg = fromHex(v.msg());
            var expected = fromHex(v.hashToPoint());
            var p = Ed25519HashToPoint.compute(msg);
            var actual = new byte[32];
            Ed25519Point.pack(actual, p);
            assertArrayEquals(expected, actual,
                    "hashToPoint mismatch on vector " + v.index());
        }
    }

    /**
     * Asserts {@link WamPrivateStatsTokenBlinder#blind} produces the
     * byte-identical output of the live JS
     * {@code WAWamPrivateStatsToken.blindToken} for every vector.
     */
    @Test
    void blindMatchesLiveBundle() {
        for (var v : VECTORS) {
            var msg = fromHex(v.msg());
            var scalar = fromHex(v.scalar());
            var expected = fromHex(v.blinded());
            var actual = WamPrivateStatsTokenBlinder.blind(msg, scalar);
            assertArrayEquals(expected, actual,
                    "blind mismatch on vector " + v.index());
        }
    }

    /**
     * Asserts {@link WamPrivateStatsTokenBlinder#unblind} produces the
     * byte-identical output of the live JS
     * {@code WAWamPrivateStatsToken.unblindToken} for every vector.
     */
    @Test
    void unblindMatchesLiveBundle() {
        for (var v : VECTORS) {
            var signed = fromHex(v.signed());
            var scalar = fromHex(v.scalar());
            var pk = fromHex(v.pk());
            var expected = fromHex(v.unblinded());
            var actual = WamPrivateStatsTokenBlinder.unblind(signed, scalar, pk);
            assertArrayEquals(expected, actual,
                    "unblind mismatch on vector " + v.index());
        }
    }

    /**
     * Sanity assertion that all vectors have the documented 32-byte length
     * (catches transcription typos in {@link #VECTORS}).
     */
    @Test
    void vectorByteLengthsAreCorrect() {
        for (var v : VECTORS) {
            assertEquals(32, fromHex(v.msg()).length, "msg length on vector " + v.index());
            assertEquals(32, fromHex(v.scalar()).length, "scalar length on vector " + v.index());
            assertEquals(32, fromHex(v.sk()).length, "sk length on vector " + v.index());
            assertEquals(32, fromHex(v.hashToPoint()).length, "hashToPoint length on vector " + v.index());
            assertEquals(32, fromHex(v.blinded()).length, "blinded length on vector " + v.index());
            assertEquals(32, fromHex(v.pk()).length, "pk length on vector " + v.index());
            assertEquals(32, fromHex(v.signed()).length, "signed length on vector " + v.index());
            assertEquals(32, fromHex(v.unblinded()).length, "unblinded length on vector " + v.index());
        }
    }

    private static byte[] fromHex(String hex) {
        var out = new byte[hex.length() / 2];
        for (var i = 0; i < out.length; i++) {
            out[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return out;
    }
}
