package szachy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TableTest {

    private val promotionFen = "4k3/P7/8/8/8/8/8/4K3 w - - 0 1"

    private fun Table.tapAll(vararg squares: String) = squares.fold(this) { table, name -> table.tap(sq(name)) }

    @Test
    fun tappingOwnPieceShowsItsMoves() {
        val table = Table().tap(sq("e2"))
        assertEquals(sq("e2"), table.selected)
        assertEquals(setOf(sq("e3"), sq("e4")), table.targets)
    }

    @Test
    fun tappingATargetPlaysTheMove() {
        val table = Table().tapAll("e2", "e4")
        assertEquals(Piece(Side.WHITE, PieceType.PAWN), table.game.position.board[4, 3])
        assertEquals(Side.BLACK, table.game.position.sideToMove)
        assertNull(table.selected)
        assertEquals(emptySet(), table.targets)
        assertEquals(move("e2e4"), table.lastMove)
    }

    @Test
    fun opponentPieceCannotBeSelected() {
        assertNull(Table().tap(sq("e7")).selected)
    }

    @Test
    fun emptySquareThatIsNotATargetClearsSelection() {
        val table = Table().tapAll("e2", "a5")
        assertNull(table.selected)
        assertEquals(Side.WHITE, table.game.position.sideToMove)
    }

    @Test
    fun tappingAnotherOwnPieceMovesTheSelection() {
        assertEquals(sq("d2"), Table().tapAll("e2", "d2").selected)
    }

    @Test
    fun tappingTheSelectedPieceAgainClearsSelection() {
        assertNull(Table().tapAll("e2", "e2").selected)
    }

    @Test
    fun capturingWorksByTappingTheEnemyPiece() {
        val table = Table().tapAll("e2", "e4", "d7", "d5", "e4", "d5")
        assertEquals(Piece(Side.WHITE, PieceType.PAWN), table.game.position.board[3, 4])
        assertEquals(Side.BLACK, table.game.position.sideToMove)
    }

    @Test
    fun promotionWaitsForAChoice() {
        val table = Table(Game.fromFen(promotionFen)).tapAll("a7", "a8")
        assertEquals(sq("a8"), table.promotionSquare)
        assertEquals(Side.WHITE, table.game.position.sideToMove)
    }

    @Test
    fun choosingAPromotionPlaysIt() {
        val table = Table(Game.fromFen(promotionFen)).tapAll("a7", "a8").choosePromotion(PieceType.KNIGHT)
        assertEquals(Piece(Side.WHITE, PieceType.KNIGHT), table.game.position.board[0, 7])
        assertNull(table.promotionSquare)
        assertEquals(move("a7a8n"), table.lastMove)
    }

    @Test
    fun tappingElsewhereCancelsThePromotion() {
        val table = Table(Game.fromFen(promotionFen)).tapAll("a7", "a8", "h4")
        assertNull(table.promotionSquare)
        assertNull(table.selected)
        assertEquals(Side.WHITE, table.game.position.sideToMove)
    }

    @Test
    fun finishedGameIgnoresTaps() {
        val finished = Table(Game.initial().playAll("f2f3", "e7e5", "g2g4", "d8h4"))
        assertNull(finished.tap(sq("a2")).selected)
    }

    @Test
    fun cornersOfTheBoardMapToTheRightSquares() {
        assertEquals(sq("a8"), squareAt(0f, 0f, 800f))
        assertEquals(sq("h1"), squareAt(799f, 799f, 800f))
        assertEquals(sq("e4"), squareAt(450f, 450f, 800f))
    }

    @Test
    fun tapOutsideTheBoardMapsToNothing() {
        assertNull(squareAt(-1f, 10f, 800f))
        assertNull(squareAt(10f, 800f, 800f))
    }

    @Test
    fun statusNamesTheWinnerAndTheSideToMove() {
        assertTrue("czarne" in statusText(Game.initial().playAll("f2f3", "e7e5", "g2g4", "d8h4")).lowercase())
        assertTrue("biał" in statusText(Game.initial()).lowercase())
        assertTrue("czarn" in statusText(Game.initial().playAll("e2e4")).lowercase())
    }

    @Test
    fun statusAnnouncesCheck() {
        assertTrue("szach" in statusText(Game.fromFen("4k3/8/8/8/8/4r3/8/4K3 w - - 0 1")).lowercase())
    }

    @Test
    fun statusDoesNotAnnounceCheckInAQuietPosition() {
        assertTrue("szach" !in statusText(Game.initial()).lowercase())
    }
}
