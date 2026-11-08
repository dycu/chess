package szachy

sealed interface GameStatus {
    val isOver: Boolean get() = this != Ongoing

    data object Ongoing : GameStatus
    data class Checkmate(val winner: Side) : GameStatus
    data object Stalemate : GameStatus
    data object FiftyMoves : GameStatus
    data object Repetition : GameStatus
    data object InsufficientMaterial : GameStatus
}

/** Pozycja razem z historią potrzebną do wykrycia powtórzeń. */
class Game private constructor(
    val position: Position,
    private val occurrences: Map<String, Int>,
) {
    val status: GameStatus by lazy {
        when {
            position.legalMoves.isEmpty() ->
                if (position.inCheck) GameStatus.Checkmate(position.sideToMove.opposite) else GameStatus.Stalemate
            hasInsufficientMaterial() -> GameStatus.InsufficientMaterial
            occurrences.getValue(position.repetitionKey) >= 3 -> GameStatus.Repetition
            position.halfMoveClock >= 100 -> GameStatus.FiftyMoves
            else -> GameStatus.Ongoing
        }
    }

    fun play(move: Move): Game {
        val next = position.play(move)
        val key = next.repetitionKey
        return Game(next, occurrences + (key to (occurrences[key] ?: 0) + 1))
    }

    private fun hasInsufficientMaterial(): Boolean {
        val others = position.board.pieces().filter { (_, piece) -> piece.type != PieceType.KING }
        if (others.any { (_, piece) -> piece.type != PieceType.BISHOP && piece.type != PieceType.KNIGHT }) return false
        if (others.size <= 1) return true
        return others.all { (_, piece) -> piece.type == PieceType.BISHOP } &&
            others.map { (square, _) -> (square.file + square.rank) % 2 }.distinct().size == 1
    }

    companion object {
        fun initial(): Game = start(Position.initial())

        fun fromFen(fen: String): Game = start(Position.fromFen(fen))

        private fun start(position: Position) = Game(position, mapOf(position.repetitionKey to 1))
    }
}
