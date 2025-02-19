package us.planet10.ai.ttt.service

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import us.planet10.ai.ttt.domain.*
import us.planet10.ai.ttt.engine.GameEngine
import java.util.concurrent.ConcurrentHashMap
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class GameServiceTest {
    private lateinit var gameEngine: GameEngine
    private lateinit var gameService: GameService

    @BeforeEach
    fun setup() {
        gameEngine = mock()
        gameService = GameService(gameEngine)
    }

    @Test
    fun `createGame should generate new game with unique ID`() {
        // Given
        val mockGameState = GameState(
            id = "test-id",
            board = GameBoard.createEmpty(),
            status = GameStatus.NEW,
            winner = null,
            lastMove = null
        )
        Mockito.`when`(gameEngine.createGame(any())).thenAnswer { 
            mockGameState.copy(id = it.arguments[0] as String)
        }

        // When
        val gameState = gameService.createGame()

        // Then
        assertNotNull(gameState)
        assertEquals(GameStatus.NEW, gameState.status)
        verify(gameEngine).createGame(any())
    }

    @Test
    fun `getGame should return game state for valid ID`() {
        // Given
        val mockGameState = GameState(
            id = "test-id",
            board = GameBoard.createEmpty(),
            status = GameStatus.NEW,
            winner = null,
            lastMove = null
        )
        Mockito.`when`(gameEngine.createGame(any())).thenAnswer { 
            mockGameState.copy(id = it.arguments[0] as String)
        }
        val game = gameService.createGame()

        // When
        val retrievedGame = gameService.getGame(game.id)

        // Then
        assertEquals(game, retrievedGame)
    }

    @Test
    fun `getGame should throw exception for invalid ID`() {
        assertThrows<IllegalArgumentException> {
            gameService.getGame("invalid-id")
        }
    }

    @Test
    fun `makeMove should update game state`() {
        // Given
        val initialState = GameState(
            id = "test-id",
            board = GameBoard.createEmpty(),
            status = GameStatus.NEW,
            winner = null,
            lastMove = null
        )
        val position = Position(1, 1)
        val updatedState = initialState.copy(status = GameStatus.IN_PROGRESS)
        
        Mockito.`when`(gameEngine.createGame(any())).thenAnswer { 
            initialState.copy(id = it.arguments[0] as String)
        }
        Mockito.`when`(gameEngine.makeMove(any(), eq(position))).thenReturn(updatedState)
        
        val game = gameService.createGame()

        // When
        val newState = gameService.makeMove(game.id, position)

        // Then
        assertEquals(GameStatus.IN_PROGRESS, newState.status)
        verify(gameEngine).makeMove(any(), eq(position))
    }

    @Test
    fun `makeMove should throw exception for invalid game ID`() {
        assertThrows<IllegalArgumentException> {
            gameService.makeMove("invalid-id", Position(0, 0))
        }
    }

    @Test
    fun `makeAiMove should generate and apply AI move`() {
        // Given
        val initialState = GameState(
            id = "test-id",
            board = GameBoard.createEmpty(),
            status = GameStatus.NEW,
            winner = null,
            lastMove = null
        )
        val aiMove = Position(1, 1)
        val updatedState = initialState.copy(status = GameStatus.IN_PROGRESS)
        
        Mockito.`when`(gameEngine.createGame(any())).thenAnswer { 
            initialState.copy(id = it.arguments[0] as String)
        }
        Mockito.`when`(gameEngine.generateAiMove(any())).thenReturn(aiMove)
        Mockito.`when`(gameEngine.makeMove(any(), eq(aiMove))).thenReturn(updatedState)
        
        val game = gameService.createGame()

        // When
        val newState = gameService.makeAiMove(game.id)

        // Then
        assertEquals(GameStatus.IN_PROGRESS, newState.status)
        verify(gameEngine).generateAiMove(any())
        verify(gameEngine).makeMove(any(), eq(aiMove))
    }

    @Test
    fun `makeAiMove should throw exception for invalid game ID`() {
        assertThrows<IllegalArgumentException> {
            gameService.makeAiMove("invalid-id")
        }
    }

    @Test
    fun `abandonGame should update game status to abandoned`() {
        // Given
        val initialState = GameState(
            id = "test-id",
            board = GameBoard.createEmpty(),
            status = GameStatus.IN_PROGRESS,
            winner = null,
            lastMove = null
        )
        Mockito.`when`(gameEngine.createGame(any())).thenAnswer { 
            initialState.copy(id = it.arguments[0] as String)
        }
        val game = gameService.createGame()

        // When
        val abandonedState = gameService.abandonGame(game.id)

        // Then
        assertEquals(GameStatus.ABANDONED, abandonedState.status)
    }

    @Test
    fun `abandonGame should throw exception for invalid game ID`() {
        assertThrows<IllegalArgumentException> {
            gameService.abandonGame("invalid-id")
        }
    }

    @Test
    fun `cleanupOldGames should remove completed and abandoned games`() {
        // Given
        val completedState = GameState(
            id = "completed-game",
            board = GameBoard.createEmpty(),
            status = GameStatus.COMPLETED,
            winner = Player.X,
            lastMove = null
        )
        val abandonedState = GameState(
            id = "abandoned-game",
            board = GameBoard.createEmpty(),
            status = GameStatus.ABANDONED,
            winner = null,
            lastMove = null
        )
        val activeState = GameState(
            id = "active-game",
            board = GameBoard.createEmpty(),
            status = GameStatus.IN_PROGRESS,
            winner = null,
            lastMove = null
        )

        Mockito.`when`(gameEngine.createGame(any())).thenAnswer { invocation ->
            when (val id = invocation.arguments[0] as String) {
                "completed-game" -> completedState.copy(id = id)
                "abandoned-game" -> abandonedState.copy(id = id)
                "active-game" -> activeState.copy(id = id)
                else -> activeState.copy(id = id)
            }
        }

        // Create games with specific IDs
        val completedGame = gameService.createGame()
        val abandonedGame = gameService.createGame()
        val activeGame = gameService.createGame()

        // Update game states to match our test scenario
        gameService.abandonGame(abandonedGame.id)
        
        // Simulate a completed game by updating the internal map
        val games = gameService::class.java.getDeclaredField("games").apply { isAccessible = true }
        val gamesMap = games.get(gameService) as ConcurrentHashMap<String, GameState>
        gamesMap[completedGame.id] = completedGame.copy(status = GameStatus.COMPLETED, winner = Player.X)

        // When
        gameService.cleanupOldGames()

        // Then
        assertThrows<IllegalArgumentException> {
            gameService.getGame(completedGame.id)
        }
        assertThrows<IllegalArgumentException> {
            gameService.getGame(abandonedGame.id)
        }
        assertNotNull(gameService.getGame(activeGame.id))
    }
}
