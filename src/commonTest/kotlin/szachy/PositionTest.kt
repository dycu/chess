package szachy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PositionTest {

    /** Liczba wszystkich ciągów ruchów o danej długości. */
    private fun perft(position: Position, depth: Int): Long =
        if (depth == 0) 1 else position.legalMoves.sumOf { perft(position.play(it), depth - 1) }

    private fun perftOf(fen: String, depth: Int) = perft(Position.fromFen(fen), depth)

    // Oczekiwane liczby to powszechnie publikowane wartości referencyjne perft.

    @Test
    fun perftFromTheStartingPosition() {
        val start = Position.initial()
        assertEquals(20L, perft(start, 1))
        assertEquals(400L, perft(start, 2))
        assertEquals(8902L, perft(start, 3))
        assertEquals(197281L, perft(start, 4))
    }

    @Test
    fun perftWithCastlingPinsAndEnPassant() {
        val fen = "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1"
        assertEquals(48L, perftOf(fen, 1))
        assertEquals(2039L, perftOf(fen, 2))
        assertEquals(97862L, perftOf(fen, 3))
    }

    @Test
    fun perftOfRookAndPawnEndgame() {
        assertEquals(43238L, perftOf("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1", 4))
    }

    @Test
    fun perftWithPromotions() {
        assertEquals(9467L, perftOf("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1", 3))
    }

    @Test
    fun perftOfMiddlegameWithDiscoveredChecks() {
        assertEquals(62379L, perftOf("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8", 3))
    }

    private fun afterMove(fen: String, text: String): String = Position.fromFen(fen).play(move(text)).fen()

    @Test
    fun doublePawnPushRecordsEnPassantSquare() {
        assertEquals(
            "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1",
            Position.initial().play(move("e2e4")).fen(),
        )
    }

    @Test
    fun castlingMovesTheRookAndDropsRights() {
        assertEquals(
            "r3k2r/8/8/8/8/8/8/R4RK1 b kq - 1 1",
            afterMove("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1", "e1g1"),
        )
    }

    @Test
    fun queensideCastlingMovesTheRook() {
        assertEquals(
            "2kr3r/8/8/8/8/8/8/R3K2R w KQ - 1 2",
            afterMove("r3k2r/8/8/8/8/8/8/R3K2R b KQkq - 0 1", "e8c8"),
        )
    }

    @Test
    fun enPassantRemovesTheCapturedPawn() {
        assertEquals(
            "4k3/8/3P4/8/8/8/8/4K3 b - - 0 1",
            afterMove("4k3/8/8/3pP3/8/8/8/4K3 w - d6 0 1", "e5d6"),
        )
    }

    @Test
    fun promotionPlacesTheChosenPiece() {
        assertEquals("N3k3/8/8/8/8/8/8/4K3 b - - 0 1", afterMove("4k3/P7/8/8/8/8/8/4K3 w - - 0 1", "a7a8n"))
    }

    @Test
    fun pawnReachingLastRankOffersFourPromotions() {
        val promotions = Position.fromFen("4k3/P7/8/8/8/8/8/4K3 w - - 0 1").legalMoves
            .filter { it.from == sq("a7") }
            .map { it.promotion }
            .toSet()
        assertEquals(setOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT), promotions)
    }

    @Test
    fun capturingARookOnItsHomeSquareRemovesThatCastlingRight() {
        assertEquals(
            "R3k2r/8/8/8/8/8/8/4K2R b Kk - 0 1",
            afterMove("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1", "a1a8"),
        )
    }

    @Test
    fun nonPawnQuietMoveAdvancesTheFiftyMoveCounter() {
        assertEquals("4k3/8/8/8/8/8/R7/4K3 b - - 8 20", afterMove("4k3/8/8/8/8/8/8/R3K3 w - - 7 20", "a1a2"))
    }

    @Test
    fun pawnMoveResetsTheFiftyMoveCounter() {
        assertEquals("4k3/8/8/8/8/P7/8/4K3 b - - 0 20", afterMove("4k3/8/8/8/8/8/P7/4K3 w - - 7 20", "a2a3"))
    }

    @Test
    fun pinnedPieceCannotMove() {
        val moves = Position.fromFen("4k3/8/8/8/8/4r3/4N3/4K3 w - - 0 1").legalMoves
        assertTrue(moves.none { it.from == sq("e2") })
    }

    @Test
    fun cannotCastleThroughAttackedSquare() {
        assertFalse(move("e1g1") in Position.fromFen("4k3/8/8/8/8/5r2/8/4K2R w K - 0 1").legalMoves)
    }

    @Test
    fun cannotCastleOutOfCheck() {
        assertFalse(move("e1g1") in Position.fromFen("4k3/8/8/8/8/4r3/8/4K2R w K - 0 1").legalMoves)
    }

    @Test
    fun queensideCastlingIsAllowedWhenOnlyTheB1SquareIsAttacked() {
        assertTrue(move("e1c1") in Position.fromFen("4k3/8/8/8/8/1r6/8/R3K3 w Q - 0 1").legalMoves)
    }

    @Test
    fun detectsCheck() {
        assertTrue(Position.fromFen("4k3/8/8/8/8/4r3/8/4K3 w - - 0 1").inCheck)
        assertFalse(Position.fromFen("4k3/8/8/8/8/3r4/8/4K3 w - - 0 1").inCheck)
    }
}
