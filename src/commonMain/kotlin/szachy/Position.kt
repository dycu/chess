package szachy

import kotlin.math.abs

data class Square(val file: Int, val rank: Int)

data class Move(val from: Square, val to: Square, val promotion: PieceType? = null)

private val STRAIGHT = listOf(1 to 0, -1 to 0, 0 to 1, 0 to -1)
private val DIAGONAL = listOf(1 to 1, 1 to -1, -1 to 1, -1 to -1)
private val ALL_DIRECTIONS = STRAIGHT + DIAGONAL
private val KNIGHT_JUMPS = listOf(1 to 2, 2 to 1, 2 to -1, 1 to -2, -1 to -2, -2 to -1, -2 to 1, -1 to 2)
private val PROMOTIONS = listOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)

private const val START_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"

private fun onBoard(file: Int, rank: Int) = file in 0..7 && rank in 0..7

/** Pełny stan partii w jednej chwili. Niezmienny: ruch tworzy nową pozycję. */
class Position private constructor(
    val board: Board,
    val sideToMove: Side,
    private val castling: String,
    private val enPassant: Square?,
    val halfMoveClock: Int,
    private val fullMoveNumber: Int,
) {
    val legalMoves: List<Move> by lazy {
        pseudoLegalMoves().filter { move ->
            val next = play(move)
            !next.isAttacked(next.kingSquare(sideToMove), sideToMove.opposite)
        }
    }

    val inCheck: Boolean get() = isAttacked(kingSquare(sideToMove), sideToMove.opposite)

    /** Zapis tego, co decyduje o powtórzeniu pozycji. */
    val repetitionKey: String by lazy {
        val capturable = enPassant != null &&
            legalMoves.any { it.to == enPassant && board[it.from]?.type == PieceType.PAWN }
        "${placement()} ${sideLetter()} ${castlingText()} ${if (capturable) squareName(enPassant) else "-"}"
    }

    fun play(move: Move): Position {
        val cells = board.copyCells()
        val piece = cells[index(move.from)] ?: error("Brak figury na polu ${move.from}")
        val isCapture = cells[index(move.to)] != null

        cells[index(move.from)] = null
        cells[index(move.to)] = move.promotion?.let { Piece(piece.side, it) } ?: piece

        if (piece.type == PieceType.PAWN && move.to == enPassant && move.from.file != move.to.file) {
            cells[index(Square(move.to.file, move.from.rank))] = null
        }
        if (piece.type == PieceType.KING && abs(move.to.file - move.from.file) == 2) {
            val rank = move.from.rank
            val rookFrom = if (move.to.file == 6) 7 else 0
            val rookTo = if (move.to.file == 6) 5 else 3
            cells[index(Square(rookTo, rank))] = cells[index(Square(rookFrom, rank))]
            cells[index(Square(rookFrom, rank))] = null
        }

        val isDoublePush = piece.type == PieceType.PAWN && abs(move.to.rank - move.from.rank) == 2
        return Position(
            board = Board(cells),
            sideToMove = sideToMove.opposite,
            castling = castlingAfter(piece, move),
            enPassant = if (isDoublePush) Square(move.from.file, (move.from.rank + move.to.rank) / 2) else null,
            halfMoveClock = if (piece.type == PieceType.PAWN || isCapture) 0 else halfMoveClock + 1,
            fullMoveNumber = fullMoveNumber + if (sideToMove == Side.BLACK) 1 else 0,
        )
    }

    fun fen(): String =
        "${placement()} ${sideLetter()} ${castlingText()} ${enPassant?.let(::squareName) ?: "-"} " +
            "$halfMoveClock $fullMoveNumber"

    private fun castlingAfter(piece: Piece, move: Move): String {
        val lost = buildSet {
            if (piece.type == PieceType.KING) addAll(if (piece.side == Side.WHITE) "KQ".asIterable() else "kq".asIterable())
            for (square in listOf(move.from, move.to)) {
                when (square) {
                    Square(0, 0) -> add('Q')
                    Square(7, 0) -> add('K')
                    Square(0, 7) -> add('q')
                    Square(7, 7) -> add('k')
                }
            }
        }
        return castling.filterNot { it in lost }
    }

    private fun pseudoLegalMoves(): List<Move> {
        val moves = ArrayList<Move>(48)
        for ((from, piece) in board.pieces()) {
            if (piece.side != sideToMove) continue
            when (piece.type) {
                PieceType.PAWN -> addPawnMoves(from, moves)
                PieceType.KNIGHT -> addSteps(from, KNIGHT_JUMPS, moves)
                PieceType.BISHOP -> addRays(from, DIAGONAL, moves)
                PieceType.ROOK -> addRays(from, STRAIGHT, moves)
                PieceType.QUEEN -> addRays(from, ALL_DIRECTIONS, moves)
                PieceType.KING -> {
                    addSteps(from, ALL_DIRECTIONS, moves)
                    addCastling(from, moves)
                }
            }
        }
        return moves
    }

    private fun addPawnMoves(from: Square, moves: MutableList<Move>) {
        val isWhite = sideToMove == Side.WHITE
        val direction = if (isWhite) 1 else -1
        val startRank = if (isWhite) 1 else 6
        val lastRank = if (isWhite) 7 else 0
        val nextRank = from.rank + direction

        fun add(to: Square) {
            if (to.rank == lastRank) PROMOTIONS.forEach { moves += Move(from, to, it) } else moves += Move(from, to)
        }

        if (board[from.file, nextRank] == null) {
            add(Square(from.file, nextRank))
            if (from.rank == startRank && board[from.file, nextRank + direction] == null) {
                add(Square(from.file, nextRank + direction))
            }
        }
        for (file in listOf(from.file - 1, from.file + 1)) {
            if (file !in 0..7) continue
            val target = Square(file, nextRank)
            val occupant = board[target]
            if (target == enPassant || (occupant != null && occupant.side != sideToMove)) add(target)
        }
    }

    private fun addSteps(from: Square, steps: List<Pair<Int, Int>>, moves: MutableList<Move>) {
        for ((df, dr) in steps) {
            val file = from.file + df
            val rank = from.rank + dr
            if (onBoard(file, rank) && board[file, rank]?.side != sideToMove) moves += Move(from, Square(file, rank))
        }
    }

    private fun addRays(from: Square, directions: List<Pair<Int, Int>>, moves: MutableList<Move>) {
        for ((df, dr) in directions) {
            var file = from.file + df
            var rank = from.rank + dr
            while (onBoard(file, rank)) {
                val occupant = board[file, rank]
                if (occupant?.side != sideToMove) moves += Move(from, Square(file, rank))
                if (occupant != null) break
                file += df
                rank += dr
            }
        }
    }

    private fun addCastling(from: Square, moves: MutableList<Move>) {
        val isWhite = sideToMove == Side.WHITE
        val rank = if (isWhite) 0 else 7
        if (from != Square(4, rank) || inCheck) return

        val enemy = sideToMove.opposite
        fun empty(vararg files: Int) = files.all { board[it, rank] == null }
        fun safe(file: Int) = !isAttacked(Square(file, rank), enemy)

        if ((if (isWhite) 'K' else 'k') in castling && empty(5, 6) && safe(5)) moves += Move(from, Square(6, rank))
        if ((if (isWhite) 'Q' else 'q') in castling && empty(1, 2, 3) && safe(3)) moves += Move(from, Square(2, rank))
    }

    private fun isAttacked(square: Square, by: Side): Boolean {
        fun hasPiece(file: Int, rank: Int, type: PieceType) =
            onBoard(file, rank) && board[file, rank] == Piece(by, type)

        fun attackedAlong(directions: List<Pair<Int, Int>>, type: PieceType) = directions.any { (df, dr) ->
            var file = square.file + df
            var rank = square.rank + dr
            while (onBoard(file, rank) && board[file, rank] == null) {
                file += df
                rank += dr
            }
            hasPiece(file, rank, type) || hasPiece(file, rank, PieceType.QUEEN)
        }

        val pawnRank = square.rank + if (by == Side.WHITE) -1 else 1
        return hasPiece(square.file - 1, pawnRank, PieceType.PAWN) ||
            hasPiece(square.file + 1, pawnRank, PieceType.PAWN) ||
            KNIGHT_JUMPS.any { (df, dr) -> hasPiece(square.file + df, square.rank + dr, PieceType.KNIGHT) } ||
            ALL_DIRECTIONS.any { (df, dr) -> hasPiece(square.file + df, square.rank + dr, PieceType.KING) } ||
            attackedAlong(STRAIGHT, PieceType.ROOK) ||
            attackedAlong(DIAGONAL, PieceType.BISHOP)
    }

    private fun kingSquare(side: Side): Square =
        board.pieces().first { (_, piece) -> piece == Piece(side, PieceType.KING) }.first

    private fun index(square: Square) = square.rank * 8 + square.file

    private fun sideLetter() = if (sideToMove == Side.WHITE) "w" else "b"

    private fun castlingText() = castling.ifEmpty { "-" }

    private fun squareName(square: Square) = "${'a' + square.file}${square.rank + 1}"

    private fun placement(): String = (7 downTo 0).joinToString("/") { rank ->
        buildString {
            var empty = 0
            for (file in 0..7) {
                val piece = board[file, rank]
                if (piece == null) {
                    empty++
                } else {
                    if (empty > 0) append(empty)
                    empty = 0
                    append(piece.fenLetter())
                }
            }
            if (empty > 0) append(empty)
        }
    }

    companion object {
        fun initial(): Position = fromFen(START_FEN)

        fun fromFen(fen: String): Position {
            val parts = fen.trim().split(" ")
            val cells = arrayOfNulls<Piece>(64)
            parts[0].split("/").forEachIndexed { row, text ->
                var file = 0
                for (char in text) {
                    if (char.isDigit()) {
                        file += char.digitToInt()
                    } else {
                        cells[(7 - row) * 8 + file] = pieceFromFen(char)
                        file++
                    }
                }
            }
            return Position(
                board = Board(cells),
                sideToMove = if (parts[1] == "w") Side.WHITE else Side.BLACK,
                castling = parts[2].replace("-", ""),
                enPassant = parts[3].takeIf { it != "-" }?.let { Square(it[0] - 'a', it[1] - '1') },
                halfMoveClock = parts[4].toInt(),
                fullMoveNumber = parts[5].toInt(),
            )
        }
    }
}

private val FEN_LETTERS = mapOf(
    PieceType.KING to 'k',
    PieceType.QUEEN to 'q',
    PieceType.ROOK to 'r',
    PieceType.BISHOP to 'b',
    PieceType.KNIGHT to 'n',
    PieceType.PAWN to 'p',
)

private fun Piece.fenLetter(): Char {
    val letter = FEN_LETTERS.getValue(type)
    return if (side == Side.WHITE) letter.uppercaseChar() else letter
}

private fun pieceFromFen(char: Char): Piece {
    val type = FEN_LETTERS.entries.first { it.value == char.lowercaseChar() }.key
    return Piece(if (char.isUpperCase()) Side.WHITE else Side.BLACK, type)
}
