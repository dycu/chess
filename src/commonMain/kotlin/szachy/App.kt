package szachy

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
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
private val OnBackdrop = Color(0xFFF3ECE0)
private val ButtonFill = Color(0xFF4A4238)
private val LastMoveTint = Color(0x66E2C044)
private val SelectedTint = Color(0x9962A84B)
private val CheckTint = Color(0xB3D8392B)
private val TargetMark = Color(0x8C2F5E2A)

private val PROMOTION_CHOICES = listOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)

@Composable
fun App() {
    var table by remember { mutableStateOf(Table()) }

    Column(
        modifier = Modifier.fillMaxSize().background(Backdrop).padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.weight(1f), contentAlignment = Alignment.BottomCenter) {
            BasicText(
                text = statusText(table.game),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                style = TextStyle(
                    color = OnBackdrop,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                ),
            )
        }
        Box(Modifier.aspectRatio(1f, matchHeightConstraintsFirst = false), contentAlignment = Alignment.Center) {
            ChessBoard(table, onTap = { table = table.tap(it) }, modifier = Modifier.fillMaxSize())
            if (table.promotionSquare != null) {
                PromotionPicker(
                    side = table.game.position.sideToMove,
                    onChoose = { table = table.choosePromotion(it) },
                )
            }
        }
        Box(Modifier.weight(1f), contentAlignment = Alignment.TopCenter) {
            BasicText(
                text = "Nowa gra",
                modifier = Modifier
                    .padding(top = 20.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ButtonFill)
                    .clickable { table = Table() }
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                style = TextStyle(color = OnBackdrop, fontSize = 16.sp, fontWeight = FontWeight.Medium),
            )
        }
    }
}

@Composable
private fun PromotionPicker(side: Side, onChoose: (PieceType) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth(0.66f)
            .clip(RoundedCornerShape(10.dp))
            .background(if (side == Side.WHITE) DarkSquare else LightSquare)
            .padding(4.dp),
    ) {
        for (type in PROMOTION_CHOICES) {
            Canvas(Modifier.weight(1f).aspectRatio(1f).clickable { onChoose(type) }) {
                drawPiece(Piece(side, type), Offset.Zero, size.width)
            }
        }
    }
}

@Composable
private fun ChessBoard(table: Table, onTap: (Square) -> Unit, modifier: Modifier = Modifier) {
    val textMeasurer = rememberTextMeasurer()
    val currentOnTap by rememberUpdatedState(onTap)
    val position = table.game.position
    val targets = table.targets

    Canvas(
        modifier.pointerInput(Unit) {
            detectTapGestures { point ->
                squareAt(point.x, point.y, size.width.toFloat())?.let { currentOnTap(it) }
            }
        },
    ) {
        val squareSize = size.width / 8
        for (rank in 0..7) {
            for (file in 0..7) {
                val square = Square(file, rank)
                val piece = position.board[square]
                val topLeft = Offset(file * squareSize, (7 - rank) * squareSize)
                val isLight = (file + rank) % 2 == 1
                val cell = Size(squareSize, squareSize)

                drawRect(if (isLight) LightSquare else DarkSquare, topLeft, cell)
                if (table.lastMove?.from == square || table.lastMove?.to == square) drawRect(LastMoveTint, topLeft, cell)
                if (table.selected == square) drawRect(SelectedTint, topLeft, cell)
                if (position.inCheck && piece == Piece(position.sideToMove, PieceType.KING)) {
                    drawRect(CheckTint, topLeft, cell)
                }
                drawCoordinates(textMeasurer, file, rank, topLeft, squareSize, isLight)
                piece?.let { drawPiece(it, topLeft, squareSize) }
                if (square in targets) drawTargetMark(topLeft, squareSize, isCapture = piece != null)
            }
        }
    }
}

private fun DrawScope.drawTargetMark(topLeft: Offset, squareSize: Float, isCapture: Boolean) {
    val center = topLeft + Offset(squareSize / 2, squareSize / 2)
    if (isCapture) {
        drawCircle(TargetMark, radius = squareSize * 0.44f, center = center, style = Stroke(width = squareSize * 0.08f))
    } else {
        drawCircle(TargetMark, radius = squareSize * 0.16f, center = center)
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
