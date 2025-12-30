package io.github.sonicalgo.core.util

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.and
import kotlin.math.pow

/**
 * TOTP (Time-based One-Time Password) utility for generating 6-digit codes.
 *
 * Implements RFC 6238 using HMAC-SHA1 with 30-second time periods.
 * No external dependencies - uses only Java cryptography APIs.
 *
 * ## Base32 Secret Format
 *
 * The secret should be a Base32-encoded string as provided by authenticator setup:
 * - Characters: A-Z and 2-7 only (case-insensitive)
 * - Padding ('=') is optional and ignored
 * - Spaces are ignored for readability
 * - Example: "JBSWY3DPEHPK3PXP" or "JBSW Y3DP EHPK 3PXP"
 *
 * This is the same format used by Google Authenticator, Authy, and similar apps.
 */
object TotpUtil {

    private const val PERIOD_SECONDS = 30L
    private const val CODE_DIGITS = 6
    private const val DEFAULT_MIN_VALID_SECONDS = 5

    /**
     * Generates a TOTP code with smart timing.
     *
     * If the current TOTP has less than [minValidSeconds] remaining before expiry,
     * this method waits for the next time period to ensure the code remains valid
     * long enough for the API request to complete.
     *
     * **Note:** This method may block up to 30 seconds. For non-blocking usage,
     * use [computeTotp] directly and handle timing in your application.
     *
     * @param secret Base32-encoded TOTP secret (A-Z, 2-7; case-insensitive; spaces/padding ignored)
     * @param minValidSeconds Minimum seconds required before expiry (default: 5, max: 30)
     * @return 6-digit TOTP code as a zero-padded string
     * @throws IllegalArgumentException if secret is invalid or minValidSeconds is out of range
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    @JvmStatic
    @JvmOverloads
    @Throws(InterruptedException::class)
    fun generateTotp(secret: String, minValidSeconds: Int = DEFAULT_MIN_VALID_SECONDS): String {
        require(minValidSeconds in 1..PERIOD_SECONDS.toInt()) {
            "minValidSeconds must be between 1 and $PERIOD_SECONDS"
        }

        val currentTimeSeconds = System.currentTimeMillis() / 1000
        val secondsRemaining = PERIOD_SECONDS - (currentTimeSeconds % PERIOD_SECONDS)

        if (secondsRemaining < minValidSeconds) {
            // Wait for next TOTP period
            Thread.sleep(secondsRemaining * 1000)
        }

        return computeTotp(secret)
    }

    /**
     * Computes TOTP for the current time period without any timing logic.
     *
     * @param secret Base32-encoded TOTP secret (A-Z, 2-7; case-insensitive; spaces/padding ignored)
     * @return 6-digit TOTP code as a zero-padded string
     */
    @JvmStatic
    fun computeTotp(secret: String): String {
        val currentTimeSeconds = System.currentTimeMillis() / 1000
        val counter = currentTimeSeconds / PERIOD_SECONDS
        return computeTotpForCounter(secret, counter)
    }

    /**
     * Computes TOTP for a specific counter value.
     *
     * @param secret Base32 encoded TOTP secret
     * @param counter The time-based counter value
     * @return 6-digit TOTP code as a zero-padded string
     */
    private fun computeTotpForCounter(secret: String, counter: Long): String {
        val key = base32Decode(secret)
        val counterBytes = ByteArray(8)
        var value = counter
        for (i in 7 downTo 0) {
            counterBytes[i] = (value and 0xFF).toByte()
            value = value shr 8
        }

        val hmac = hmacSha1(key, counterBytes)
        val code = dynamicTruncate(hmac)
        val otp = code % 10.0.pow(CODE_DIGITS.toDouble()).toInt()

        return otp.toString().padStart(CODE_DIGITS, '0')
    }

    /**
     * Computes HMAC-SHA1.
     */
    private fun hmacSha1(key: ByteArray, data: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA1")
        mac.init(SecretKeySpec(key, "HmacSHA1"))
        return mac.doFinal(data)
    }

    /**
     * Dynamic truncation as per RFC 4226.
     */
    private fun dynamicTruncate(hmac: ByteArray): Int {
        val offset = (hmac[hmac.size - 1] and 0x0F).toInt()
        return ((hmac[offset].toInt() and 0x7F) shl 24) or
                ((hmac[offset + 1].toInt() and 0xFF) shl 16) or
                ((hmac[offset + 2].toInt() and 0xFF) shl 8) or
                (hmac[offset + 3].toInt() and 0xFF)
    }

    /**
     * Decodes a Base32 encoded string to bytes.
     * Handles uppercase/lowercase and ignores spaces.
     */
    private fun base32Decode(encoded: String): ByteArray {
        val base32Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
        val cleanInput = encoded.uppercase().replace(" ", "").replace("=", "")

        val output = mutableListOf<Byte>()
        var buffer = 0
        var bitsLeft = 0

        for (char in cleanInput) {
            val value = base32Chars.indexOf(char)
            if (value < 0) {
                throw IllegalArgumentException("Invalid Base32 character: $char")
            }

            buffer = (buffer shl 5) or value
            bitsLeft += 5

            if (bitsLeft >= 8) {
                bitsLeft -= 8
                output.add((buffer shr bitsLeft).toByte())
                buffer = buffer and ((1 shl bitsLeft) - 1)
            }
        }

        return output.toByteArray()
    }
}
