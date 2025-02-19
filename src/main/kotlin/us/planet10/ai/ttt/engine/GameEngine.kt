package us.planet10.ai.ttt.engine

import org.springframework.stereotype.Component
import us.planet10.ai.ttt.domain.*
import java.time.Instant

/**
 * Core game engine responsible for game logic, state management, and rule enforcement.
 */
@Component
class GameEngine {
    companion object {
        private const val BOARD_SIZE = 3
        private const val MAX_DEPTH = 6
    }

    /**
     * Creates a new game with an empty board.
     * X player always starts first.
     */
    fun createGame(id: String): GameState {
        return GameState.create(id)
    }

    /**
     * Processes a move and returns the new game state.
     * Validates the move before applying it.
     * 
     * @throws IllegalArgumentException if the move is invalid
     */
    fun makeMove(gameState: GameState, position: Position): GameState {
        validateGameState(gameState)
        
        val move = Move(
            player = gameState.board.currentPlayer,
            position = position,
            timestamp = Instant.now()
        )

        if (!gameState.board.isValidMove(move)) {
            throw IllegalArgumentException("Invalid move: $move")
        }

        val newBoard = gameState.board.makeMove(move)
        val winner = newBoard.getWinner()
        val status = determineGameStatus(newBoard, winner)

        return gameState.copy(
            board = newBoard,
            status = status,
            winner = winner,
            lastMove = move
        )
    }

    /**
     * Validates that the game is in a state where moves can be made.
     * 
     * @throws IllegalStateException if the game is not in a valid state for moves
     */
    private fun validateGameState(gameState: GameState) {
        when (gameState.status) {
            GameStatus.COMPLETED -> throw IllegalStateException("Game is already completed")
            GameStatus.ABANDONED -> throw IllegalStateException("Game has been abandoned")
            else -> {} // NEW and IN_PROGRESS are valid states for moves
        }
    }

    /**
     * Determines the game status based on the board state and winner.
     */
    private fun determineGameStatus(board: Board, winner: Player?): GameStatus {
        return when {
            board.moveCount == 0 -> GameStatus.NEW
            board.moveCount == BOARD_SIZE * BOARD_SIZE || winner != null -> GameStatus.COMPLETED
            else -> GameStatus.IN_PROGRESS
        }
    }

    /**
     * Generates an AI move using the minimax algorithm with alpha-beta pruning.
     * Returns the best move for the current player.
     */
    fun generateAiMove(gameState: GameState): Position {
        validateGameState(gameState)
        
        var bestScore = Int.MIN_VALUE
        var bestMove: Position? = null
        var alpha = Int.MIN_VALUE
        val beta = Int.MAX_VALUE
        val originalPlayer = gameState.board.currentPlayer
        
        // Try each possible move
        for (row in 0 until BOARD_SIZE) {
            for (col in 0 until BOARD_SIZE) {
                val position = Position(row, col)
                val move = Move(originalPlayer, position, Instant.now())
                
                if (gameState.board.isValidMove(move)) {
                    // Make the move
                    val newBoard = gameState.board.makeMove(move)
                    // Evaluate this move with alpha-beta pruning
                    val score = minimax(newBoard, 0, false, originalPlayer, alpha, beta)
                    if (score > bestScore) {
                        bestScore = score
                        bestMove = position
                    }
                    alpha = maxOf(alpha, bestScore)
                }
            }
        }
        
        return bestMove ?: throw IllegalStateException("No valid moves available")
    }

    /**
     * Implements the minimax algorithm with alpha-beta pruning for move evaluation.
     * @param board The current board state
     * @param depth Current depth in the game tree
     * @param isMaximizing Whether this is a maximizing turn
     * @param originalPlayer The player making the initial move
     * @param alpha The alpha value for pruning
     * @param beta The beta value for pruning
     * @return The evaluated score for this position
     */
    private fun minimax(
        board: Board, 
        depth: Int, 
        isMaximizing: Boolean, 
        originalPlayer: Player,
        alpha: Int,
        beta: Int
    ): Int {
        // Check terminal states or max depth
        val winner = board.getWinner()
        when {
            winner == originalPlayer -> return 10 - depth // Win for original player
            winner != null -> return -10 + depth // Win for opponent
            board.moveCount == BOARD_SIZE * BOARD_SIZE -> return 0 // Draw
            depth == MAX_DEPTH -> return evaluatePosition(board, originalPlayer) // Depth limit
        }

        if (isMaximizing) {
            var value = Int.MIN_VALUE
            var currentAlpha = alpha
            
            for (row in 0 until BOARD_SIZE) {
                for (col in 0 until BOARD_SIZE) {
                    val move = Move(board.currentPlayer, Position(row, col), Instant.now())
                    if (board.isValidMove(move)) {
                        val newBoard = board.makeMove(move)
                        val score = minimax(newBoard, depth + 1, false, originalPlayer, currentAlpha, beta)
                        value = maxOf(value, score)
                        currentAlpha = maxOf(currentAlpha, value)
                        if (currentAlpha >= beta) {
                            return value // Beta cutoff
                        }
                    }
                }
            }
            return value
        } else {
            var value = Int.MAX_VALUE
            var currentBeta = beta
            
            for (row in 0 until BOARD_SIZE) {
                for (col in 0 until BOARD_SIZE) {
                    val move = Move(board.currentPlayer, Position(row, col), Instant.now())
                    if (board.isValidMove(move)) {
                        val newBoard = board.makeMove(move)
                        val score = minimax(newBoard, depth + 1, true, originalPlayer, alpha, currentBeta)
                        value = minOf(value, score)
                        currentBeta = minOf(currentBeta, value)
                        if (alpha >= currentBeta) {
                            return value // Alpha cutoff
                        }
                    }
                }
            }
            return value
        }
    }

    /**
     * Evaluates a non-terminal position based on piece placement and control.
     */
    private fun evaluatePosition(board: Board, player: Player): Int {
        var score = 0
        
        // Evaluate center control
        board.cells[1][1].player?.let { centerPlayer ->
            score += if (centerPlayer == player) 3 else -3
        }
        
        // Evaluate corners
        val corners = listOf(
            board.cells[0][0],
            board.cells[0][2],
            board.cells[2][0],
            board.cells[2][2]
        )
        corners.forEach { corner ->
            corner.player?.let { cornerPlayer ->
                score += if (cornerPlayer == player) 2 else -2
            }
        }
        
        return score
    }
}
