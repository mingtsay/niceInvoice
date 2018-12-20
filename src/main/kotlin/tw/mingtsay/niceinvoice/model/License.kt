package tw.mingtsay.niceinvoice.model

data class License(
    val name: String,
    val category: String,
    val licenses: Array<String>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as License

        if (name != other.name) return false
        if (category != other.category) return false
        if (!licenses.contentEquals(other.licenses)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + category.hashCode()
        result = 31 * result + licenses.contentHashCode()
        return result
    }
}

data class Licenses(
    val licenses: Array<License>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Licenses
        return licenses contentEquals other.licenses
    }

    override fun hashCode(): Int {
        return licenses.contentHashCode()
    }
}
