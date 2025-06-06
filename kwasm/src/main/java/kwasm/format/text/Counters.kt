/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kwasm.format.text

import kwasm.ast.Identifier

data class TextModuleCounts(
    val types: Int,
    val functions: Int,
    val tables: Int,
    val memories: Int,
    val globals: Int
)

fun <T : Identifier> TextModuleCounts.incrementFor(
    identifier: T
): TextModuleCounts = when (identifier) {
    is Identifier.Type -> copy(types = types + 1)
    is Identifier.Function -> copy(functions = functions + 1)
    is Identifier.Table -> copy(tables = tables + 1)
    is Identifier.Memory -> copy(memories = memories + 1)
    is Identifier.Global -> copy(globals = globals + 1)
    else -> this
}
