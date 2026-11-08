package szachy

enum class Side {
    WHITE, BLACK;

    val opposite: Side get() = if (this == WHITE) BLACK else WHITE
}

enum class PieceType { KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN }

data class Piece(val side: Side, val type: PieceType)

/** Rozstawienie figur. `file` 0..7 to kolumny a..h, `rank` 0..7 to rzędy 1..8. */
class Board internal constructor(private val cells: Array<Piece?>) {

    operator fun get(file: Int, rank: Int): Piece? = cells[rank * 8 + file]

    operator fun get(square: Square): Piece? = get(square.file, square.rank)

    fun pieces(): List<Pair<Square, Piece>> = cells.withIndex().mapNotNull { (index, piece) ->
        piece?.let { Square(index % 8, index / 8) to it }
    }

    internal fun copyCells(): Array<Piece?> = cells.copyOf()

    companion object {
        private val BACK_RANK = listOf(
            PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
            PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK,
        )

        fun initial(): Board = Board(
            Array(64) { index ->
                val file = index % 8
                when (index / 8) {
                    0 -> Piece(Side.WHITE, BACK_RANK[file])
                    1 -> Piece(Side.WHITE, PieceType.PAWN)
                    6 -> Piece(Side.BLACK, PieceType.PAWN)
                    7 -> Piece(Side.BLACK, BACK_RANK[file])
                    else -> null
                }
            }
        )
    }
}
