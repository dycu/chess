package szachy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GameTest {

    @Test
    fun newGameIsOngoing() {
        assertEquals(GameStatus.Ongoing, Game.initial().status)
    }

    @Test
    fun foolsMateIsCheckmateForBlack() {
        val game = Game.initial().playAll("f2f3", "e7e5", "g2g4", "d8h4")
        assertEquals(GameStatus.Checkmate(Side.BLACK), game.status)
        assertTrue(game.status.isOver)
    }

    @Test
    fun checkWithEscapeDoesNotEndTheGame() {
        val game = Game.fromFen("4k3/8/8/8/8/4r3/8/4K3 w - - 0 1")
        assertEquals(GameStatus.Ongoing, game.status)
        assertFalse(game.status.isOver)
    }

    @Test
    fun noLegalMoveWithoutCheckIsStalemate() {
        assertEquals(GameStatus.Stalemate, Game.fromFen("7k/5Q2/6K1/8/8/8/8/8 b - - 0 1").status)
    }

    @Test
    fun hundredQuietHalfMovesIsADraw() {
        val game = Game.fromFen("4k3/8/8/8/8/8/8/R3K3 w - - 99 80").playAll("a1a2")
        assertEquals(GameStatus.FiftyMoves, game.status)
    }

    @Test
    fun ninetyNineQuietHalfMovesIsNotADraw() {
        val game = Game.fromFen("4k3/8/8/8/8/8/8/R3K3 w - - 98 80").playAll("a1a2")
        assertEquals(GameStatus.Ongoing, game.status)
    }

    @Test
    fun checkmateOnTheHundredthHalfMoveBeatsTheFiftyMoveDraw() {
        val game = Game.fromFen("7k/R7/6K1/8/8/8/8/8 w - - 99 80").playAll("a7a8")
        assertEquals(GameStatus.Checkmate(Side.WHITE), game.status)
    }

    private val knightShuffle = arrayOf("g1f3", "g8f6", "f3g1", "f6g8")

    @Test
    fun secondOccurrenceOfAPositionIsNotADraw() {
        assertEquals(GameStatus.Ongoing, Game.initial().playAll(*knightShuffle).status)
    }

    @Test
    fun thirdOccurrenceOfAPositionIsADraw() {
        val game = Game.initial().playAll(*knightShuffle, *knightShuffle)
        assertEquals(GameStatus.Repetition, game.status)
    }

    @Test
    fun sameBoardWithDifferentCastlingRightsIsNotARepetition() {
        val fen = "r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1"
        // Pierwsze przejście wieżami odbiera roszady, więc pozycja startowa nie liczy się jako powtórzona.
        val shuffle = arrayOf("h1h2", "h8h7", "h2h1", "h7h8")
        assertEquals(GameStatus.Ongoing, Game.fromFen(fen).playAll(*shuffle, *shuffle).status)
    }

    @Test
    fun enPassantSquareWithoutAPossibleCaptureDoesNotBlockRepetition() {
        // Po e2e4 pole e3 jest zapisane, ale nikt nie może bić w przelocie: pozycja jest taka sama jak po powrocie.
        val game = Game.fromFen("4k1n1/8/8/8/8/8/4P3/4K1N1 w - - 0 1")
            .playAll("e2e4", "g8f6", "g1f3", "f6g8", "f3g1", "g8f6", "g1f3", "f6g8", "f3g1")
        assertEquals(GameStatus.Repetition, game.status)
    }

    @Test
    fun loneKingsAreADraw() {
        assertEquals(GameStatus.InsufficientMaterial, Game.fromFen("4k3/8/8/8/8/8/8/4K3 w - - 0 1").status)
    }

    @Test
    fun kingAndBishopAgainstKingIsADraw() {
        assertEquals(GameStatus.InsufficientMaterial, Game.fromFen("4k3/8/8/8/8/8/8/2B1K3 w - - 0 1").status)
    }

    @Test
    fun kingAndKnightAgainstKingIsADraw() {
        assertEquals(GameStatus.InsufficientMaterial, Game.fromFen("4k3/8/8/8/8/8/8/1N2K3 w - - 0 1").status)
    }

    @Test
    fun bishopsOnSameColourAreADraw() {
        // c1 i f8 to pola tego samego koloru.
        assertEquals(GameStatus.InsufficientMaterial, Game.fromFen("4kb2/8/8/8/8/8/8/2B1K3 w - - 0 1").status)
    }

    @Test
    fun bishopsOnOppositeColoursPlayOn() {
        // c1 i c8 to pola różnych kolorów.
        assertEquals(GameStatus.Ongoing, Game.fromFen("2b1k3/8/8/8/8/8/8/2B1K3 w - - 0 1").status)
    }

    @Test
    fun twoKnightsPlayOn() {
        assertEquals(GameStatus.Ongoing, Game.fromFen("4k3/8/8/8/8/8/8/1NN1K3 w - - 0 1").status)
    }

    @Test
    fun aSinglePawnPlaysOn() {
        assertEquals(GameStatus.Ongoing, Game.fromFen("4k3/8/8/8/8/8/P7/4K3 w - - 0 1").status)
    }
}
