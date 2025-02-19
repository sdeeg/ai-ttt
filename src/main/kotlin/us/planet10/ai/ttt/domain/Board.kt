package us.planet10.ai.ttt.domain

import java.time.Instant

enum class Player {
    X, O
}

data class Position(
    val row: Int,
    val col: Int
)

interface Cell {
    val position: Position
    val player: Player?
}

data class BoardCell(
    override val position: Position,
    override val player: Player? = null
) : Cell

interface Board {
    val cells: List<List<Cell>>
    val currentPlayer: Player
    val moveCount: Int
    
    fun makeMove(move: Move): Board
    fun isValidMove(move: Move): Boolean
    fun getWinner(): Player?
}

data class GameBoard(
    override val cells: List<List<Cell>>,
    override val currentPlayer: Player,
    override val moveCount: Int
) : Board {
    
    override fun makeMove(move: Move): Board {
        require(isValidMove(move)) { "Invalid move: $move" }
        
        val newCells = cells.map { row ->
            row.map { cell ->
                if (cell.position == move.position) {
                    BoardCell(cell.position, move.player)
                } else {
                    cell
                }
            }
        }
        
        return GameBoard(
            cells = newCells,
            currentPlayer = if (currentPlayer == Player.X) Player.O else Player.X,
            moveCount = moveCount + 1
        )
    }
    
    override fun isValidMove(move: Move): Boolean {
        if (move.position.row !in cells.indices || 
            move.position.col !in cells[0].indices) {
            return false
        }
        
        val cell = cells[move.position.row][move.position.col]
        return cell.player == null && move.player == currentPlayer
    }
    
    override fun getWinner(): Player? {
        // Check rows
        for (row in cells) {
            val first = row[0].player
            if (first != null && row.all { it.player == first }) {
                return first
            }
        }
        
        // Check columns
        for (col in cells[0].indices) {
            val first = cells[0][col].player
            if (first != null && cells.all { it[col].player == first }) {
                return first
            }
        }
        
        // Check diagonals
        val center = cells[1][1].player
        if (center != null) {
            // Main diagonal
            if (cells[0][0].player == center && cells[2][2].player == center) {
                return center
            }
            // Other diagonal
            if (cells[0][2].player == center && cells[2][0].player == center) {
                return center
            }
        }
        
        return null
    }
    
    companion object {
        fun createEmpty(size: Int = 3, startingPlayer: Player = Player.X): Board {
            val cells = List(size) { row ->
                List(size) { col ->
                    BoardCell(Position(row, col))
                }
            }
            return GameBoard(cells, startingPlayer, 0)
        }
    }
}

data class Move(
    val player: Player,
    val position: Position,
    val timestamp: Instant
)
