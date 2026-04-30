package com.github.auties00.cobalt.client;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import it.auties.qr.QrTerminal;

import java.awt.*;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.google.zxing.client.j2se.MatrixToImageWriter.writeToPath;
import static java.lang.System.Logger.Level.INFO;
import static java.nio.file.Files.createTempFile;

/**
 * A pluggable strategy for completing the authentication ceremony that
 * links or registers a WhatsApp client.
 *
 * <p>The two sub-hierarchies address the two supported flavours of
 * authentication:
 * <ul>
 *   <li>{@link Web} handles the companion-device linking ceremony used by
 *       {@link WhatsAppClientType#WEB} clients, which can complete either
 *       by scanning a QR code on the primary device or by entering a
 *       pairing code;</li>
 *   <li>{@link Mobile} handles the registration ceremony used by
 *       {@link WhatsAppClientType#MOBILE} clients, which starts with an
 *       SMS, voice, or in-app WhatsApp verification code request.</li>
 * </ul>
 *
 * <p>Handlers are wired into a {@link WhatsAppClient} via the builder
 * ({@link WhatsAppClientBuilder.Options.Web#unregistered(WhatsAppClientVerificationHandler.Web.QrCode)}
 * and the Mobile {@code register} variants).
 *
 * @see WhatsAppClientBuilder
 */
public sealed interface WhatsAppClientVerificationHandler {
    /**
     * A verification handler for WhatsApp Web companion-device linking.
     *
     * <p>Implementations receive the value that the primary device must
     * authorise: either the payload encoded in a QR code that the user
     * scans on their phone, or a short pairing code that the user types
     * into the Linked Devices screen.
     */
    sealed interface Web extends WhatsAppClientVerificationHandler {
        /**
         * Receives the verification value produced by the Cobalt client
         * and surfaces it to the user.
         *
         * <p>The value is either a QR code payload (for
         * {@link QrCode} handlers) or a short pairing code (for
         * {@link PairingCode} handlers).
         *
         * @param value the verification value produced by the client
         */
        void handle(String value);

        /**
         * A verification handler that renders the QR code produced by
         * Cobalt during the companion-linking flow.
         *
         * <p>The handler receives the raw QR payload as a string; the
         * static factory methods provide common renderers (terminal,
         * temporary file, desktop viewer) so callers can pick a behaviour
         * without writing boilerplate.
         */
        @FunctionalInterface
        non-sealed interface QrCode extends Web {
            /**
             * Returns a handler that renders the QR code as ASCII art on
             * standard output.
             *
             * @apiNote Terminals that do not support UTF block-drawing
             *          characters render the output as garbled symbols.
             * @return the terminal-rendering handler
             */
            static QrCode toTerminal() {
                return qr -> {
                    var matrix = createMatrix(qr, 10, 0);
                    System.out.println(QrTerminal.toString(matrix, true));
                };
            }

            /**
             * Encodes a QR payload into a {@link BitMatrix} suitable for
             * rendering.
             *
             * @param qr     the payload to encode
             * @param size   the side length, in pixels, of the rendered
             *               square
             * @param margin the white margin around the rendered code, in
             *               modules
             * @return the encoded bit matrix
             * @throws UnsupportedOperationException if the payload cannot
             *                                       be encoded as a QR
             *                                       code
             */
            static BitMatrix createMatrix(String qr, int size, int margin) {
                try {
                    var writer = new MultiFormatWriter();
                    return writer.encode(qr, BarcodeFormat.QR_CODE, size, size, Map.of(EncodeHintType.MARGIN, margin, EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L));
                } catch (WriterException exception) {
                    throw new UnsupportedOperationException("Cannot create QR code", exception);
                }
            }

            /**
             * Returns a handler that writes the QR code to a temporary
             * JPEG file and forwards the file path to the supplied
             * consumer.
             *
             * @param fileConsumer the consumer that receives the path of
             *                     the rendered file
             * @return the file-rendering handler
             * @throws UncheckedIOException if the temporary file cannot
             *                              be created
             */
            static QrCode toFile(QrCode.ToFile fileConsumer) {
                try {
                    var file = createTempFile("qr", ".jpg");
                    return toFile(file, fileConsumer);
                } catch (IOException exception) {
                    throw new UncheckedIOException("Cannot create temp file for QR handler", exception);
                }
            }

            /**
             * Returns a handler that writes the QR code to the supplied
             * path and forwards it to the supplied consumer.
             *
             * @param path         the destination path where the QR code
             *                     image is saved
             * @param fileConsumer the consumer that receives the path of
             *                     the rendered file
             * @return the file-rendering handler
             */
            static QrCode toFile(Path path, QrCode.ToFile fileConsumer) {
                return qr -> {
                    try {
                        var matrix = createMatrix(qr, 500, 5);
                        writeToPath(matrix, "jpg", path);
                        fileConsumer.accept(path);
                    } catch (IOException exception) {
                        throw new UncheckedIOException("Cannot save QR code to file", exception);
                    }
                };
            }

            /**
             * A consumer that reacts to a file path where a QR code has
             * been rendered.
             *
             * <p>Callers combine a rendering path supplier (typically a
             * {@link Path} returned by
             * {@link java.nio.file.Files#createTempFile(String, String, java.nio.file.attribute.FileAttribute[])})
             * with a {@code ToFile} consumer to decide what to do with the
             * resulting image: ignore it, log its location, or open it in
             * a desktop viewer.
             */
            interface ToFile extends Consumer<Path> {
                /**
                 * Returns a consumer that ignores the rendered file path
                 * and takes no action.
                 *
                 * @return the no-op consumer
                 */
                static QrCode.ToFile discard() {
                    return ignored -> {};
                }

                /**
                 * Returns a consumer that logs the rendered file path
                 * through the system logger.
                 *
                 * @return the logging consumer
                 */
                static QrCode.ToFile toTerminal() {
                    return path -> System.getLogger(QrCode.class.getName())
                            .log(INFO, "Saved QR code at %s".formatted(path));
                }

                /**
                 * Returns a consumer that opens the rendered file with
                 * the default desktop image viewer.
                 *
                 * @return the desktop-opening consumer
                 * @throws RuntimeException if the file cannot be opened
                 *                          via {@link Desktop}
                 */
                static QrCode.ToFile toDesktop() {
                    return path -> {
                        try {
                            if (!Desktop.isDesktopSupported()) {
                                return;
                            }
                            Desktop.getDesktop().open(path.toFile());
                        } catch (Throwable throwable) {
                            throw new RuntimeException("Cannot open file with desktop", throwable);
                        }
                    };
                }
            }
        }

        /**
         * A verification handler that surfaces the short pairing code
         * produced by Cobalt during the companion-linking flow.
         *
         * <p>Pairing codes are typed into the Linked Devices screen on the
         * primary device instead of scanning a QR. The handler receives
         * the code as a plain string and is responsible for presenting it
         * to the user.
         */
        @FunctionalInterface
        non-sealed interface PairingCode extends Web {
            /**
             * Returns a handler that prints the pairing code on standard
             * output.
             *
             * @return the terminal-printing handler
             */
            static PairingCode toTerminal() {
                return System.out::println;
            }
        }
    }

    /**
     * A verification handler for the WhatsApp mobile registration flow.
     *
     * <p>Mobile registration requires the user to receive a one-time code
     * on the phone number being registered and to feed it back into the
     * client. Implementations expose two decisions: which delivery channel
     * to request (SMS, voice call, in-app WhatsApp, or server-chosen) and
     * how to obtain the code once it has been delivered.
     */
    non-sealed interface Mobile extends WhatsAppClientVerificationHandler {
        /**
         * Returns the preferred delivery channel for the verification
         * code.
         *
         * <p>Supported values mirror the WhatsApp server-side method
         * identifiers: {@code sms}, {@code voice}, and {@code wa_old}.
         * Returning {@link Optional#empty()} lets the server pick a
         * default channel.
         *
         * @return the preferred delivery method, or empty to defer the
         *         choice to the server
         */
        Optional<String> requestMethod();

        /**
         * Returns the verification code supplied by the user.
         *
         * <p>Implementations typically block on user input (e.g., reading
         * a console line) and return the code once it has been entered.
         *
         * @return the verification code
         */
        String verificationCode();

        /**
         * Solves a server-issued challenge (image or audio CAPTCHA) and
         * returns the user's answer.
         *
         * <p>Called by the registration code when {@code /v2/code} or
         * {@code /v2/exist} returns an {@code image_blob} or
         * {@code audio_blob} payload, which happens whenever the server
         * decides the client is in the low-trust lane (typically because
         * no Play Integrity / App Attest token was submitted). The
         * default implementation returns {@link Optional#empty()}, which
         * the registration treats as "caller cannot solve challenges" and
         * raises a registration failure.
         *
         * @param imagePng the PNG image challenge bytes, or {@code null}
         *                 if the server did not include one
         * @param audioOgg the Ogg-encoded audio challenge bytes, or
         *                 {@code null} if the server did not include one
         * @return the user-supplied answer, or empty to abort
         */
        default Optional<String> solveCaptcha(byte[] imagePng, byte[] audioOgg) {
            return Optional.empty();
        }

        /**
         * Returns the two-factor authentication PIN the user has
         * configured on the account being registered.
         *
         * <p>Called by the registration code when {@code /v2/register}
         * returns a {@code 2fa_required} reason. The default
         * implementation returns {@link Optional#empty()}, which the
         * registration treats as "caller cannot supply a PIN" and raises
         * a registration failure.
         *
         * @return the PIN, or empty to abort
         */
        default Optional<String> twoFactorPin() {
            return Optional.empty();
        }

        /**
         * Returns a verification handler that defers the choice of
         * delivery channel to the WhatsApp server and reads the
         * verification code from the supplied supplier.
         *
         * @param supplier the supplier that produces the verification
         *                 code once the user has received it
         * @return the verification handler
         * @throws NullPointerException if {@code supplier} is
         *                              {@code null}
         */
        static Mobile none(Supplier<String> supplier) {
            Objects.requireNonNull(supplier, "supplier cannot be null");
            return new Mobile() {
                @Override
                public Optional<String> requestMethod() {
                    return Optional.empty();
                }

                @Override
                public String verificationCode() {
                    var value = supplier.get();
                    if(value == null) {
                        throw new IllegalArgumentException("Cannot send verification code: no value");
                    }
                    return value;
                }
            };
        }

        /**
         * Returns a verification handler that requests SMS delivery and
         * reads the verification code from the supplied supplier.
         *
         * @param supplier the supplier that produces the verification
         *                 code once the user has received it
         * @return the verification handler
         * @throws NullPointerException if {@code supplier} is
         *                              {@code null}
         */
        static Mobile sms(Supplier<String> supplier) {
            Objects.requireNonNull(supplier, "supplier cannot be null");
            return new Mobile() {
                @Override
                public Optional<String> requestMethod() {
                    return Optional.of("sms");
                }

                @Override
                public String verificationCode() {
                    var value = supplier.get();
                    if(value == null) {
                        throw new IllegalArgumentException("Cannot send verification code: no value");
                    }
                    return value;
                }
            };
        }

        /**
         * Returns a verification handler that requests voice-call
         * delivery and reads the verification code from the supplied
         * supplier.
         *
         * @param supplier the supplier that produces the verification
         *                 code once the user has received it
         * @return the verification handler
         * @throws NullPointerException if {@code supplier} is
         *                              {@code null}
         */
        static Mobile call(Supplier<String> supplier) {
            Objects.requireNonNull(supplier, "supplier cannot be null");
            return new Mobile() {
                @Override
                public Optional<String> requestMethod() {
                    return Optional.of("voice");
                }

                @Override
                public String verificationCode() {
                    var value = supplier.get();
                    if(value == null) {
                        throw new IllegalArgumentException("Cannot send verification code: no value");
                    }
                    return value;
                }
            };
        }

        /**
         * Returns a verification handler that requests in-app WhatsApp
         * delivery and reads the verification code from the supplied
         * supplier.
         *
         * @param supplier the supplier that produces the verification
         *                 code once the user has received it
         * @return the verification handler
         * @throws NullPointerException if {@code supplier} is
         *                              {@code null}
         */
        static Mobile whatsapp(Supplier<String> supplier) {
            Objects.requireNonNull(supplier, "supplier cannot be null");
            return new Mobile() {
                @Override
                public Optional<String> requestMethod() {
                    return Optional.of("wa_old");
                }

                @Override
                public String verificationCode() {
                    var value = supplier.get();
                    if(value == null) {
                        throw new IllegalArgumentException("Cannot send verification code: no value");
                    }
                    return value;
                }
            };
        }
    }
}