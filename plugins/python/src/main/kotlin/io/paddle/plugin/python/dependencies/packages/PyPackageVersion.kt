/**
Source: https://github.com/JetBrains/intellij-community/blob/945fa97a72f5f46e4ba69264b63ccff95535a971/python/python-psi-impl/src/com/jetbrains/python/packaging/PyPackageVersion.kt
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

/**
 * Presents normalized version of python package or requirement as described in [PEP-440][https://www.python.org/dev/peps/pep-0440/#normalization].
 *
 * Instances of this class MUST be obtained from [PyPackageVersionNormalizer.normalize].
 */
@kotlinx.serialization.Serializable
data class PyPackageVersion internal constructor(
    val epoch: String? = null,
    val release: String,
    val pre: String? = null,
    val post: String? = null,
    val dev: String? = null,
    val local: String? = null
) {

    /**
     * String representation that follows spelling described in [PEP-440][https://www.python.org/dev/peps/pep-0440/#normalization]
     */
    val presentableText: String
        get() = sequenceOf(epochPresentable(), release, pre, postPresentable(), devPresentable(), localPresentable())
            .filterNotNull()
            .joinToString(separator = "") { it }

    private fun epochPresentable() = epoch?.let { "$it!" }
    private fun postPresentable() = post?.let { ".$it" }
    private fun devPresentable() = dev?.let { ".$it" }
    private fun localPresentable() = local?.let { "+$it" }
}
