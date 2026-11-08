package szachy

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Backdrop = Color(0xFF26231F)
private val LightSquare = Color(0xFFEEDFC4)
private val DarkSquare = Color(0xFF9C6B47)
private val WhitePieceFill = Color(0xFFFBF7EF)
private val WhitePieceLine = Color(0xFF2B2520)
private val BlackPieceFill = Color(0xFF2B2520)
private val BlackPieceLine = Color(0xFF0F0D0B)
private val BlackPieceDetail = Color(0xFFD9CDBB)

@Composable
fun App() {
    Box(
        modifier = Modifier.fillMaxSize().background(Backdrop),
        contentAlignment = Alignment.Center,
    ) {
        ChessBoard(
            board = remember { Board.initial() },
            modifier = Modifier.padding(12.dp).aspectRatio(1f),
        )
    }
}

@Composable
fun ChessBoard(board: Board, modifier: Modifier = Modifier) {
    val textMeasurer = rememberTextMeasurer()
    Canvas(modifier) {
        val squareSize = size.width / 8
        for (rank in 0..7) {
            for (file in 0..7) {
                val topLeft = Offset(file * squareSize, (7 - rank) * squareSize)
                val isLight = (file + rank) % 2 == 1
                drawRect(
                    color = if (isLight) LightSquare else DarkSquare,
                    topLeft = topLeft,
                    size = Size(squareSize, squareSize),
                )
                board[file, rank]?.let { drawPiece(it, topLeft, squareSize) }
                drawCoordinates(textMeasurer, file, rank, topLeft, squareSize, isLight)
            }
        }
    }
}

private fun DrawScope.drawPiece(piece: Piece, topLeft: Offset, squareSize: Float) {
    val shape = piece.type.shape()
    val isWhite = piece.side == Side.WHITE
    val outline = Stroke(width = 3f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val detailLine = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)

    translate(topLeft.x, topLeft.y) {
        scale(squareSize / 100f, squareSize / 100f, pivot = Offset.Zero) {
            drawPath(shape.body, if (isWhite) WhitePieceFill else BlackPieceFill, style = Fill)
            drawPath(shape.body, if (isWhite) WhitePieceLine else BlackPieceLine, style = outline)
            drawPath(shape.details, if (isWhite) WhitePieceLine else BlackPieceDetail, style = detailLine)
        }
    }
}

private fun DrawScope.drawCoordinates(
    textMeasurer: TextMeasurer,
    file: Int,
    rank: Int,
    topLeft: Offset,
    squareSize: Float,
    isLight: Boolean,
) {
    val style = TextStyle(
        color = if (isLight) DarkSquare else LightSquare,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
    )
    val margin = squareSize * 0.05f
    if (file == 0) {
        drawText(textMeasurer, "${rank + 1}", topLeft + Offset(margin, margin / 2), style)
    }
    if (rank == 0) {
        val label = textMeasurer.measure("${'a' + file}", style)
        drawText(
            label,
            topLeft = topLeft + Offset(
                squareSize - label.size.width - margin,
                squareSize - label.size.height - margin / 2,
            ),
        )
    }
}
