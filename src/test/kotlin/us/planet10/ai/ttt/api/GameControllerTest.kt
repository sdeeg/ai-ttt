package us.planet10.ai.ttt.api

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.http.HttpStatus
import us.planet10.ai.ttt.api.GameController.MoveRequest
import us.planet10.ai.ttt.domain.*
import us.planet10.ai.ttt.service.GameService
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class GameControllerTest {
    private lateinit var gameService: GameService
    private lateinit var controller: GameController

    @BeforeEach
    fun setup() {
        gameService = mock()
        controller = GameController(gameService)
    }

    @Test
    fun `createGame should return new game state`() {
        // Given
        val gameState = GameState(
            id = "test-id",
            board = GameBoard.createEmpty(),
            status = GameStatus.NEW,
            winner = null,
            lastMove = null
        )
        whenever(gameService.createGame()).thenReturn(gameState)

        // When
        val response = controller.createGame()

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals("test-id", response.body?.id)
        assertEquals("NEW", response.body?.status)
        assertNull(response.body?.winner)
        verify(gameService).createGame()
    }

    @Test
    fun `getGame should return game state for valid ID`() {
        // Given
        val gameState = GameState(
            id = "test-id",
            board = GameBoard.createEmpty(),
            status = GameStatus.IN_PROGRESS,
            winner = null,
            lastMove = null
        )
        whenever(gameService.getGame("test-id")).thenReturn(gameState)

        // When
        val response = controller.getGame("test-id")

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals("test-id", response.body?.id)
        assertEquals("IN_PROGRESS", response.body?.status)
        verify(gameService).getGame("test-id")
    }

    @Test
    fun `getGame should return 404 for invalid ID`() {
        // Given
        whenever(gameService.getGame("invalid-id"))
            .thenThrow(IllegalArgumentException("Game not found"))

        // When
        val response = controller.getGame("invalid-id")

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertNull(response.body)
        verify(gameService).getGame("invalid-id")
    }

    @Test
    fun `makeMove should update game state for valid move`() {
        // Given
        val move = MoveRequest(1, 1)
        val gameState = GameState(
            id = "test-id",
            board = GameBoard.createEmpty(),
            status = GameStatus.IN_PROGRESS,
            winner = null,
            lastMove = Move(
                player = Player.X,
                position = Position(1, 1),
                timestamp = Instant.now()
            )
        )
        whenever(gameService.makeMove("test-id", Position(1, 1))).thenReturn(gameState)

        // When
        val response = controller.makeMove("test-id", move)

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals("IN_PROGRESS", response.body?.status)
        verify(gameService).makeMove("test-id", Position(1, 1))
    }

    @Test
    fun `makeMove should return 400 for invalid move`() {
        // Given
        val move = MoveRequest(3, 3) // Out of bounds
        whenever(gameService.makeMove("test-id", Position(3, 3)))
            .thenThrow(IllegalArgumentException("Invalid move"))

        // When
        val response = controller.makeMove("test-id", move)

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertNull(response.body)
        verify(gameService).makeMove("test-id", Position(3, 3))
    }

    @Test
    fun `makeMove should return 409 for completed game`() {
        // Given
        val move = MoveRequest(1, 1)
        whenever(gameService.makeMove("test-id", Position(1, 1)))
            .thenThrow(IllegalStateException("Game is completed"))

        // When
        val response = controller.makeMove("test-id", move)

        // Then
        assertEquals(HttpStatus.CONFLICT, response.statusCode)
        assertNull(response.body)
        verify(gameService).makeMove("test-id", Position(1, 1))
    }

    @Test
    fun `makeAiMove should update game state`() {
        // Given
        val gameState = GameState(
            id = "test-id",
            board = GameBoard.createEmpty(),
            status = GameStatus.IN_PROGRESS,
            winner = null,
            lastMove = Move(
                player = Player.O,
                position = Position(1, 1),
                timestamp = Instant.now()
            )
        )
        whenever(gameService.makeAiMove("test-id")).thenReturn(gameState)

        // When
        val response = controller.makeAiMove("test-id")

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals("IN_PROGRESS", response.body?.status)
        verify(gameService).makeAiMove("test-id")
    }

    @Test
    fun `makeAiMove should return 404 for invalid game ID`() {
        // Given
        whenever(gameService.makeAiMove("invalid-id"))
            .thenThrow(IllegalArgumentException("Game not found"))

        // When
        val response = controller.makeAiMove("invalid-id")

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertNull(response.body)
        verify(gameService).makeAiMove("invalid-id")
    }

    @Test
    fun `makeAiMove should return 409 for completed game`() {
        // Given
        whenever(gameService.makeAiMove("test-id"))
            .thenThrow(IllegalStateException("Game is completed"))

        // When
        val response = controller.makeAiMove("test-id")

        // Then
        assertEquals(HttpStatus.CONFLICT, response.statusCode)
        assertNull(response.body)
        verify(gameService).makeAiMove("test-id")
    }

    @Test
    fun `abandonGame should update game status`() {
        // Given
        val gameState = GameState(
            id = "test-id",
            board = GameBoard.createEmpty(),
            status = GameStatus.ABANDONED,
            winner = null,
            lastMove = null
        )
        whenever(gameService.abandonGame("test-id")).thenReturn(gameState)

        // When
        val response = controller.abandonGame("test-id")

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals("ABANDONED", response.body?.status)
        verify(gameService).abandonGame("test-id")
    }

    @Test
    fun `abandonGame should return 404 for invalid game ID`() {
        // Given
        whenever(gameService.abandonGame("invalid-id"))
            .thenThrow(IllegalArgumentException("Game not found"))

        // When
        val response = controller.abandonGame("invalid-id")

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertNull(response.body)
        verify(gameService).abandonGame("invalid-id")
    }

    @Test
    fun `handleException should return appropriate error responses`() {
        // Test IllegalArgumentException
        val badRequestResponse = controller.handleException(
            IllegalArgumentException("Bad request")
        )
        assertEquals(HttpStatus.BAD_REQUEST, badRequestResponse.statusCode)
        assertEquals("Bad request", badRequestResponse.body?.message)

        // Test IllegalStateException
        val conflictResponse = controller.handleException(
            IllegalStateException("Conflict")
        )
        assertEquals(HttpStatus.CONFLICT, conflictResponse.statusCode)
        assertEquals("Conflict", conflictResponse.body?.message)

        // Test other exceptions
        val errorResponse = controller.handleException(
            RuntimeException("Internal error")
        )
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, errorResponse.statusCode)
        assertEquals("Internal error", errorResponse.body?.message)
    }
}
