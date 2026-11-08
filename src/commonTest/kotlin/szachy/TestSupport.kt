package szachy

fun sq(name: String): Square = Square(name[0] - 'a', name[1] - '1')

/** Ruch w zapisie "e2e4" albo "a7a8n" (z figurą promocji na końcu). */
fun move(text: String): Move = Move(
    from = sq(text.substring(0, 2)),
    to = sq(text.substring(2, 4)),
    promotion = when (text.getOrNull(4)) {
        'q' -> PieceType.QUEEN
        'r' -> PieceType.ROOK
        'b' -> PieceType.BISHOP
        'n' -> PieceType.KNIGHT
        else -> null
    },
)

fun Game.playAll(vararg moves: String): Game = moves.fold(this) { game, text -> game.play(move(text)) }
