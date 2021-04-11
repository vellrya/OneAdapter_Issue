package a.b.c

import com.idanatz.oneadapter.external.interfaces.Diffable

data class StickerModel(val fakeId: Long, val fakeDesc: String, val color: Int): Diffable {
    override fun areContentTheSame(other: Any): Boolean {
        return other is StickerModel && fakeId==other.fakeId && fakeDesc==other.fakeDesc && color==other.color
    }

    override val uniqueIdentifier: Long
        get() = fakeId
}

data class StickerSetModel(val fakeId: Long, val name: String): Diffable {
    override fun areContentTheSame(other: Any): Boolean {
        return other is StickerSetModel && fakeId==other.fakeId && name==other.name
    }

    override val uniqueIdentifier: Long
        get() = fakeId
}
