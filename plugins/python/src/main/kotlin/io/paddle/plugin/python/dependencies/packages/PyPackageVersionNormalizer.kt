/**
Source: https://github.com/JetBrains/intellij-community/blob/945fa97a72f5f46e4ba69264b63ccff95535a971/python/python-psi-impl/src/com/jetbrains/python/packaging/PyPackageVersionNormalizer.java
Author: Semyon Proshev

Copyright 2000-2018 JetBrains s.r.o.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package io.paddle.plugin.python.dependencies.packages

import java.math.BigInteger
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Normalizes requirement version.
 *
 *
 * Based on
 * [https://www.python.org/dev/peps/pep-0440/#normalization](https://www.python.org/dev/peps/pep-0440/#normalization)
 * and
 * [https://www.python.org/dev/peps/pep-0440/#summary-of-permitted-suffixes-and-relative-ordering](https://www.python.org/dev/peps/pep-0440/#summary-of-permitted-suffixes-and-relative-ordering).
 */
object PyPackageVersionNormalizer {
    private const val EPOCH_GROUP = "epoch"
    private const val RELEASE_GROUP = "release"
    private const val PRE_RELEASE_TYPE_GROUP = "pretype"
    private const val PRE_RELEASE_NUMBER_GROUP = "prenumber"
    private const val POST_RELEASE_TYPE_GROUP = "posttype"
    private const val POST_RELEASE_NUMBER_GROUP = "postnumber"
    private const val IMPLICIT_POST_RELEASE_NUMBER_GROUP = "implicitpostnumber"
    private const val DEV_RELEASE_TYPE_GROUP = "devtype"
    private const val DEV_RELEASE_NUMBER_GROUP = "devnumber"
    private const val LOCAL_VERSION_GROUP = "local"
    private const val SEP_REGEXP = "([\\.\\-_])?"
    private const val EPOCH_REGEXP = "(?<$EPOCH_GROUP>\\d+!)?"
    private const val RELEASE_REGEXP = "(?<$RELEASE_GROUP>(\\d+(\\.\\d+)*)|(\\d+\\.(\\d+\\.)*\\*))"
    private const val PRE_RELEASE_REGEXP = "(" +
        SEP_REGEXP +
        "(?<" + PRE_RELEASE_TYPE_GROUP + ">a|alpha|b|beta|rc|c|pre|preview)" +
        "(" + SEP_REGEXP + "(?<" + PRE_RELEASE_NUMBER_GROUP + ">\\d+))?" +
        ")?"
    private const val POST_RELEASE_REGEXP = "(" +
        "(" + SEP_REGEXP + "(?<" + POST_RELEASE_TYPE_GROUP + ">post|rev|r)(" + SEP_REGEXP + "(?<" + POST_RELEASE_NUMBER_GROUP + ">\\d+))?)" +
        "|" +
        "(-(?<" + IMPLICIT_POST_RELEASE_NUMBER_GROUP + ">\\d+))" +
        ")?"
    private const val DEV_RELEASE_REGEXP = "(" +
        SEP_REGEXP + "(?<" + DEV_RELEASE_TYPE_GROUP + ">dev)(?<" + DEV_RELEASE_NUMBER_GROUP + ">\\d+)?" +
        ")?"
    private const val LOCAL_VERSION_REGEXP =
        "(?<$LOCAL_VERSION_GROUP>\\+[a-z0-9]([a-z0-9\\._-]*[a-z0-9])?)?"
    private val VERSION = Pattern.compile(
        "^" +
            "v?" +
            EPOCH_REGEXP +
            RELEASE_REGEXP +
            PRE_RELEASE_REGEXP +
            POST_RELEASE_REGEXP +
            DEV_RELEASE_REGEXP +
            LOCAL_VERSION_REGEXP +
            "$",
        Pattern.CASE_INSENSITIVE
    )

    fun normalize(version: String): PyPackageVersion? {
        val matcher: Matcher = VERSION.matcher(version)
        return if (matcher.matches()) {
            PyPackageVersion(
                normalizeEpoch(matcher),
                normalizeRelease(matcher),
                normalizePre(matcher),
                normalizePost(matcher),
                normalizeDev(matcher),
                normalizeLocal(matcher)
            )
        } else null
    }

    private fun normalizeEpoch(matcher: Matcher): String? {
        val epoch: String? = matcher.group(EPOCH_GROUP)
        return epoch?.substring(0, epoch.length - 1)?.let { normalizeNumber(it) }
    }

    private fun normalizeRelease(matcher: Matcher): String {
        return matcher.group(RELEASE_GROUP).split(".").joinToString(".") { releasePart ->
            if (releasePart == "*") "*" else normalizeNumber(releasePart)
        }
    }

    private fun normalizePre(matcher: Matcher): String? {
        val preReleaseType: String? = matcher.group(PRE_RELEASE_TYPE_GROUP)
        val preReleaseNumber: String? = matcher.group(PRE_RELEASE_NUMBER_GROUP)
        val normalizedPreReleaseNumber = preReleaseNumber?.let { normalizeNumber(it) } ?: "0"
        return preReleaseType?.let { normalizePreReleaseType(it) + normalizedPreReleaseNumber }
    }

    private fun normalizePost(matcher: Matcher): String? {
        val postReleaseType: String? = matcher.group(POST_RELEASE_TYPE_GROUP)
        if (postReleaseType != null) {
            val postReleaseNumber: String? = matcher.group(POST_RELEASE_NUMBER_GROUP)
            val normalizedPostReleaseNumber = postReleaseNumber?.let { normalizeNumber(it) } ?: "0"
            return "post" + normalizeNumber(normalizedPostReleaseNumber)
        }

        val implicitPostReleaseNumber: String? = matcher.group(IMPLICIT_POST_RELEASE_NUMBER_GROUP)
        return implicitPostReleaseNumber?.let { "post" + normalizeNumber(it) }
    }

    private fun normalizeDev(matcher: Matcher): String? {
        return matcher.group(DEV_RELEASE_TYPE_GROUP)?.run {
            val devReleaseNumber: String? = matcher.group(DEV_RELEASE_NUMBER_GROUP)
            val normalizedDevReleaseNumber = devReleaseNumber?.let { normalizeNumber(it) } ?: "0"
            return "dev$normalizedDevReleaseNumber"
        }
    }

    private fun normalizeLocal(matcher: Matcher): String? {
        val localVersion: String? = matcher.group(LOCAL_VERSION_GROUP)
        return localVersion?.substring(1)?.replace("[-_]".toRegex(), ".")
    }

    private fun normalizeNumber(number: String): String {
        return BigInteger(number).toString()
    }

    private fun normalizePreReleaseType(preReleaseType: String): String {
        return when {
            preReleaseType.equals("a", ignoreCase = true) || preReleaseType.equals("alpha", ignoreCase = true) -> "a"
            preReleaseType.equals("b", ignoreCase = true) || preReleaseType.equals("beta", ignoreCase = true) -> "b"
            else -> "rc"
        }
    }
}
