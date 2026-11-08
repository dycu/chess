package szachy

/** To, co widać przy stole: partia oraz aktualne zaznaczenie gracza. */
data class Table(
    val game: Game = Game.initial(),
    val selected: Square? = null,
    val promotionSquare: Square? = null,
    val lastMove: Move? = null,
) {
    val targets: Set<Square>
        get() = selected?.let { from -> movesFrom(from).map { it.to }.toSet() } ?: emptySet()

    fun tap(square: Square): Table {
        if (game.status.isOver) return this
        if (promotionSquare != null) return copy(selected = null, promotionSquare = null)

        val moves = selected?.let { from -> movesFrom(from).filter { it.to == square } }.orEmpty()
        return when {
            moves.size == 1 -> play(moves.single())
            moves.size > 1 -> copy(promotionSquare = square)
            square != selected && game.position.board[square]?.side == game.position.sideToMove ->
                copy(selected = square)
            else -> copy(selected = null)
        }
    }

    fun choosePromotion(type: PieceType): Table {
        val from = selected ?: return this
        val to = promotionSquare ?: return this
        return play(Move(from, to, type))
    }

    private fun movesFrom(from: Square) = game.position.legalMoves.filter { it.from == from }

    private fun play(move: Move) =
        copy(game = game.play(move), selected = null, promotionSquare = null, lastMove = move)
}

/** Zamienia punkt na planszy o boku [boardSize] na pole; białe grają od dołu. */
fun squareAt(x: Float, y: Float, boardSize: Float): Square? {
    if (x < 0 || y < 0 || x >= boardSize || y >= boardSize) return null
    return Square((x / boardSize * 8).toInt(), 7 - (y / boardSize * 8).toInt())
}

fun statusText(game: Game): String {
    val position = game.position
    val toMove = if (position.sideToMove == Side.WHITE) "białe" else "czarne"
    return when (val status = game.status) {
        GameStatus.Ongoing -> if (position.inCheck) "Szach! Ruch: $toMove" else "Ruch: $toMove"
        is GameStatus.Checkmate -> "Mat! Wygrywają " + if (status.winner == Side.WHITE) "białe" else "czarne"
        GameStatus.Stalemate -> "Pat – remis"
        GameStatus.FiftyMoves -> "Remis – 50 ruchów bez bicia i ruchu piona"
        GameStatus.Repetition -> "Remis – trzykrotne powtórzenie pozycji"
        GameStatus.InsufficientMaterial -> "Remis – za mało figur, by dać mata"
    }
}
