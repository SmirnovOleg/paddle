package io.paddle.plugin.python.dependencies.packages

enum class PyPackageVersionRelation(val operator: String) {
    LT("<"),
    LTE("<="),
    GT(">"),
    GTE(">="),
    EQ("=="),
    NE("!="),
    COMPATIBLE("~="),
    STR_EQ("===")
}

class PyPackageVersionSpecifier private constructor(val clauses: List<PyPackageVersionClause>) {
    data class PyPackageVersionClause(val relation: PyPackageVersionRelation, val version: PyPackageVersion) {
        override fun toString(): String = "$relation$version"
    }

    companion object {
        fun fromString(versionSpecifier: String): PyPackageVersionSpecifier {
            return versionSpecifier.split(",").map { it.trim() }.map { clause ->
                val relation = PyPackageVersionRelation.values().find { clause.startsWith(it.operator) }
                if (relation == null) {
                    // relation is not specified, assuming it is meant to be == (EQ)
                    val version = PyPackageVersionNormalizer.normalize(clause.trim())
                        ?: error(
                            "Failed to parse clause $clause for version specifier $versionSpecifier. " +
                                "Please, make sure you are following PEP 440."
                        )
                    PyPackageVersionClause(PyPackageVersionRelation.EQ, version)
                } else {
                    val rawVersion = clause.substringAfter(relation.operator).trim()
                    val version = PyPackageVersionNormalizer.normalize(rawVersion)
                        ?: error(
                            "Failed to parse version $rawVersion for clause $clause within version specifier $versionSpecifier. " +
                                "Please, make sure you are following PEP 440."
                        )
                    PyPackageVersionClause(relation, version)
                }
            }.let { PyPackageVersionSpecifier(it) }
        }
    }

    override fun toString(): String {
        return clauses.joinToString(", ")
    }
}
