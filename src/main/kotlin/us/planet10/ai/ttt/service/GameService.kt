package us.planet10.ai.ttt.service

import org.springframework.stereotype.Service
import us.planet10.ai.ttt.domain.*
import us.planet10.ai.ttt.engine.GameEngine
import java.util.concurrent.ConcurrentHashMap

/**
 * Service layer for game operations.
 * Uses ConcurrentHashMap for thread-safe game state management.
 */
@Service
class GameService(
    private val gameEngine: GameEngine
) {
    private val games = ConcurrentHashMap<String, GameState>()

    /**
     * Creates a new game and returns its initial state.
     */
    fun createGame(): GameState {
        val gameId = generateGameId()
        val gameState = gameEngine.createGame(gameId)
        games[gameId] = gameState
        return gameState
    }

    /**
     * Generates a unique game ID.
     */
    private fun generateGameId(): String {
        return java.util.UUID.randomUUID().toString()
    }

    /**
     * Retrieves the current state of a game.
     * @throws IllegalArgumentException if game not found
     */
    fun getGame(gameId: String): GameState {
        return games[gameId] ?: throw IllegalArgumentException("Game not found: $gameId")
    }

    /**
     * Processes a player's move.
     * @throws IllegalArgumentException if game not found or move is invalid
     * @throws IllegalStateException if game is not in a valid state for moves
     */
    fun makeMove(gameId: String, position: Position): GameState {
        val gameState = getGame(gameId)
        val newState = gameEngine.makeMove(gameState, position)
        games[gameId] = newState
        return newState
    }

    /**
     * Generates and applies an AI move for the current player.
     * @throws IllegalArgumentException if game not found
     * @throws IllegalStateException if game is not in a valid state for moves
     */
    fun makeAiMove(gameId: String): GameState {
        val gameState = getGame(gameId)
        val aiMove = gameEngine.generateAiMove(gameState)
        val newState = gameEngine.makeMove(gameState, aiMove)
        games[gameId] = newState
        return newState
    }

    /**
     * Abandons a game, marking it as no longer playable.
     * @throws IllegalArgumentException if game not found
     */
    fun abandonGame(gameId: String): GameState {
        val gameState = getGame(gameId)
        val abandonedState = gameState.copy(status = GameStatus.ABANDONED)
        games[gameId] = abandonedState
        return abandonedState
    }

    /**
     * Cleans up completed or abandoned games older than the specified duration.
     */
    fun cleanupOldGames() {
        games.entries.removeIf { (_, state) ->
            state.status == GameStatus.COMPLETED || state.status == GameStatus.ABANDONED
        }
    }
}
