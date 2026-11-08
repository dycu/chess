package szachy

import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.PathParser

/** Kształt figury w układzie 100x100: sylwetka i drobne linie rysowane na niej. */
class PieceShape(val body: Path, val details: Path)

private const val BASE = "M24 90 V82 Q24 78 28 78 H72 Q76 78 76 82 V90 Z"

private fun circle(x: Int, y: Int, r: Double): String =
    "M${x - r} $y a$r $r 0 1 1 ${2 * r} 0 a$r $r 0 1 1 ${-2 * r} 0 Z"

private fun shape(body: String, details: String = ""): PieceShape = PieceShape(
    body = PathParser().parsePathString("$body $BASE").toPath(),
    details = PathParser().parsePathString(details).toPath(),
)

private val PAWN = shape(
    "M50 17 A13 13 0 0 1 59 39.4 C61 41 63 42 63 45 C63 47 61 48 58 48 " +
        "C59 60 64 68 70 78 H30 C36 68 41 60 42 48 C39 48 37 47 37 45 " +
        "C37 42 39 41 41 39.4 A13 13 0 0 1 50 17 Z",
)

private val ROOK = shape(
    "M28 16 H38 V24 H45 V16 H55 V24 H62 V16 H72 V36 L64 44 V66 L72 78 H28 L36 66 V44 L28 36 Z",
    "M36 44 H64 M36 66 H64",
)

private val BISHOP = shape(
    "M50 8 A5.5 5.5 0 0 1 53.5 17.7 C62 24 68 33 66 42 C65 47 61 50 58 52 " +
        "C59 62 64 70 70 78 H30 C36 70 41 62 42 52 C39 50 35 47 34 42 " +
        "C32 33 38 24 46.5 17.7 A5.5 5.5 0 0 1 50 8 Z",
    "M50 28 V42 M44 35 H56 M42 52 H58",
)

private val KNIGHT = shape(
    "M34 78 C36 66 46 60 50 50 C44 54 36 55 30 58 C26 60 22 58 20 54 " +
        "C18 50 20 46 24 43 L38 30 C40 26 40 22 39 16 C43 18 46 20 48 23 " +
        "C50 19 53 16 57 15 C57 19 58 21 59 23 C70 30 76 46 75 60 C75 67 73 73 72 78 Z",
    circle(42, 35, 1.5),
)

private val QUEEN = shape(
    "M20 28 L33 48 L35 20 L44 46 L50 16 L56 46 L65 20 L67 48 L80 28 L70 62 " +
        "C68 68 69 72 72 78 H28 C31 72 32 68 30 62 Z " +
        circle(20, 26, 4.5) + " " + circle(35, 18, 4.5) + " " + circle(50, 13, 4.5) + " " +
        circle(65, 18, 4.5) + " " + circle(80, 26, 4.5),
    "M30 62 H70",
)

private val KING = shape(
    "M46 6 H54 V13 H61 V21 H54 V36 H46 V21 H39 V13 H46 Z " +
        "M50 34 C58 24 80 24 80 42 C80 54 70 60 68 64 C67 69 69 73 72 78 H28 " +
        "C31 73 33 69 32 64 C30 60 20 54 20 42 C20 24 42 24 50 34 Z",
    "M32 64 H68",
)

fun PieceType.shape(): PieceShape = when (this) {
    PieceType.KING -> KING
    PieceType.QUEEN -> QUEEN
    PieceType.ROOK -> ROOK
    PieceType.BISHOP -> BISHOP
    PieceType.KNIGHT -> KNIGHT
    PieceType.PAWN -> PAWN
}
