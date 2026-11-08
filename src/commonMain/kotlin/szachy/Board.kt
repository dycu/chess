package szachy

enum class Side { WHITE, BLACK }

enum class PieceType { KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN }

data class Piece(val side: Side, val type: PieceType)

/** Pozycja na szachownicy. `file` 0..7 to kolumny a..h, `rank` 0..7 to rzędy 1..8. */
class Board private constructor(private val squares: List<Piece?>) {

    operator fun get(file: Int, rank: Int): Piece? = squares[rank * 8 + file]

    companion object {
        private val BACK_RANK = listOf(
            PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
            PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK,
        )

        fun initial(): Board = Board(
            List(64) { index ->
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
