package szachy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BoardTest {
    private val board = Board.initial()

    @Test
    fun startingPositionHas32Pieces() {
        val count = (0..7).sumOf { file -> (0..7).count { rank -> board[file, rank] != null } }
        assertEquals(32, count)
    }

    @Test
    fun kingsAndQueensStandOnTheirSquares() {
        assertEquals(Piece(Side.WHITE, PieceType.KING), board[4, 0]) // e1
        assertEquals(Piece(Side.WHITE, PieceType.QUEEN), board[3, 0]) // d1
        assertEquals(Piece(Side.BLACK, PieceType.KING), board[4, 7]) // e8
        assertEquals(Piece(Side.BLACK, PieceType.QUEEN), board[3, 7]) // d8
    }

    @Test
    fun pawnsFillSecondAndSeventhRank() {
        for (file in 0..7) {
            assertEquals(Piece(Side.WHITE, PieceType.PAWN), board[file, 1])
            assertEquals(Piece(Side.BLACK, PieceType.PAWN), board[file, 6])
        }
    }

    @Test
    fun middleOfTheBoardIsEmpty() {
        for (file in 0..7) for (rank in 2..5) assertNull(board[file, rank])
    }
}
