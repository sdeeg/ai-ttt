package us.planet10.ai.ttt.engine

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import us.planet10.ai.ttt.domain.*
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GameEngineTest {
    private val engine = GameEngine()

    @Test
    fun `createGame should initialize empty board with X as first player`() {
        val gameState = engine.createGame("test-game")
        
        assertEquals(GameStatus.NEW, gameState.status)
        assertEquals(Player.X, gameState.board.currentPlayer)
        assertEquals(0, gameState.board.moveCount)
        assertNull(gameState.winner)
        assertNull(gameState.lastMove)
        
        // Verify all cells are empty
        gameState.board.cells.forEach { row ->
            row.forEach { cell ->
                assertNull(cell.player)
            }
        }
    }

    @Test
    fun `makeMove should place X in empty cell and switch to O`() {
        val gameState = engine.createGame("test-game")
        val position = Position(1, 1)
        
        val newState = engine.makeMove(gameState, position)
        
        assertEquals(Player.X, newState.board.cells[1][1].player)
        assertEquals(Player.O, newState.board.currentPlayer)
        assertEquals(1, newState.board.moveCount)
        assertEquals(GameStatus.IN_PROGRESS, newState.status)
    }

    @Test
    fun `makeMove should throw exception for occupied cell`() {
        var gameState = engine.createGame("test-game")
        val position = Position(1, 1)
        
        gameState = engine.makeMove(gameState, position)
        
        assertThrows<IllegalArgumentException> {
            engine.makeMove(gameState, position)
        }
    }

    @Test
    fun `makeMove should throw exception for out of bounds position`() {
        val gameState = engine.createGame("test-game")
        
        assertThrows<IllegalArgumentException> {
            engine.makeMove(gameState, Position(3, 0))
        }
        assertThrows<IllegalArgumentException> {
            engine.makeMove(gameState, Position(0, 3))
        }
        assertThrows<IllegalArgumentException> {
            engine.makeMove(gameState, Position(-1, 0))
        }
    }

    @Test
    fun `makeMove should detect horizontal win`() {
        var gameState = engine.createGame("test-game")
        
        // X plays row 0
        gameState = engine.makeMove(gameState, Position(0, 0))
        // O plays row 1
        gameState = engine.makeMove(gameState, Position(1, 0))
        // X plays row 0
        gameState = engine.makeMove(gameState, Position(0, 1))
        // O plays row 1
        gameState = engine.makeMove(gameState, Position(1, 1))
        // X plays row 0 to win
        gameState = engine.makeMove(gameState, Position(0, 2))
        
        assertEquals(GameStatus.COMPLETED, gameState.status)
        assertEquals(Player.X, gameState.winner)
    }

    @Test
    fun `makeMove should detect vertical win`() {
        var gameState = engine.createGame("test-game")
        
        // X plays col 0
        gameState = engine.makeMove(gameState, Position(0, 0))
        // O plays col 1
        gameState = engine.makeMove(gameState, Position(0, 1))
        // X plays col 0
        gameState = engine.makeMove(gameState, Position(1, 0))
        // O plays col 1
        gameState = engine.makeMove(gameState, Position(1, 1))
        // X plays col 0 to win
        gameState = engine.makeMove(gameState, Position(2, 0))
        
        assertEquals(GameStatus.COMPLETED, gameState.status)
        assertEquals(Player.X, gameState.winner)
    }

    @Test
    fun `makeMove should detect diagonal win`() {
        var gameState = engine.createGame("test-game")
        
        // X plays diagonal
        gameState = engine.makeMove(gameState, Position(0, 0))
        // O plays elsewhere
        gameState = engine.makeMove(gameState, Position(0, 1))
        // X plays diagonal
        gameState = engine.makeMove(gameState, Position(1, 1))
        // O plays elsewhere
        gameState = engine.makeMove(gameState, Position(0, 2))
        // X plays diagonal to win
        gameState = engine.makeMove(gameState, Position(2, 2))
        
        assertEquals(GameStatus.COMPLETED, gameState.status)
        assertEquals(Player.X, gameState.winner)
    }

    /*
        SMD - Cline/Claude got stuck here trying to create the appropriate moves to
        generate the board it printed out.  It didn't seem to understand that each
        Position in the moves list alternates between X and O.
     */
    @Test
    fun `makeMove should detect draw`() {
        var gameState = engine.createGame("test-game")
        
        // Fill board without winner
        // X O X
        // X O O
        // O X X
        val moves = listOf(
            Position(0, 0), // X
            Position(0, 1), // O
            Position(0, 2), // X
            Position(1, 1), // O
            Position(1, 0), // X
            Position(1, 2), // O
            Position(2, 1), // X
            Position(2, 0), // O
            Position(2, 2)  // X
        )
        
        // Make moves one at a time and verify game state
        for (move in moves) {
            gameState = engine.makeMove(gameState, move)
            if (gameState.status == GameStatus.COMPLETED) {
                break
            }
        }
        assertEquals(GameStatus.COMPLETED, gameState.status)
        assertNull(gameState.winner)
        assertEquals(9, gameState.board.moveCount)
    }

    @Test
    fun `makeMove should throw exception for completed game`() {
        var gameState = engine.createGame("test-game")
        
        // Create winning condition
        gameState = engine.makeMove(gameState, Position(0, 0))
        gameState = engine.makeMove(gameState, Position(1, 0))
        gameState = engine.makeMove(gameState, Position(0, 1))
        gameState = engine.makeMove(gameState, Position(1, 1))
        gameState = engine.makeMove(gameState, Position(0, 2))
        
        assertThrows<IllegalStateException> {
            engine.makeMove(gameState, Position(2, 2))
        }
    }

    @Test
    fun `generateAiMove should block opponent winning move`() {
        var gameState = engine.createGame("test-game")
        
        // X plays center
        gameState = engine.makeMove(gameState, Position(1, 1))
        // O plays top-left
        gameState = engine.makeMove(gameState, Position(0, 0))
        // X plays bottom-right
        gameState = engine.makeMove(gameState, Position(2, 2))
        
        // At this point, O should block X's win by playing (0, 2)
        val aiMove = engine.generateAiMove(gameState)
        assertEquals(Position(0, 2), aiMove)
    }

    @Test
    fun `generateAiMove should make winning move when available`() {
        var gameState = engine.createGame("test-game")
        
        // Create a board where O can win
        // O X O
        // X O X
        // - - O
        val moves = listOf(
            Position(0, 0), // X plays top-left
            Position(0, 2), // O plays top-right
            Position(1, 0), // X plays middle-left
            Position(1, 1), // O plays center
            Position(1, 2), // X plays middle-right
            Position(2, 2)  // O plays bottom-right
        )
        
        for (move in moves) {
            gameState = engine.makeMove(gameState, move)
        }
        
        // O should play (2, 0) to win
        val aiMove = engine.generateAiMove(gameState)
        assertEquals(Position(2, 0), aiMove)
    }

    @Test
    fun `generateAiMove should make valid move on empty board`() {
        val gameState = engine.createGame("test-game")
        
        val aiMove = engine.generateAiMove(gameState)
        
        // Verify move is within bounds
        assertTrue(aiMove.row in 0..2)
        assertTrue(aiMove.col in 0..2)
        
        // Verify move is valid
        val newState = engine.makeMove(gameState, aiMove)
        assertEquals(GameStatus.IN_PROGRESS, newState.status)
    }

    @Test
    fun `generateAiMove should throw exception for completed game`() {
        var gameState = engine.createGame("test-game")
        
        // Create winning condition
        gameState = engine.makeMove(gameState, Position(0, 0))
        gameState = engine.makeMove(gameState, Position(1, 0))
        gameState = engine.makeMove(gameState, Position(0, 1))
        gameState = engine.makeMove(gameState, Position(1, 1))
        gameState = engine.makeMove(gameState, Position(0, 2))
        
        assertThrows<IllegalStateException> {
            engine.generateAiMove(gameState)
        }
    }
}
