package us.planet10.ai.ttt.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity
import us.planet10.ai.ttt.domain.*
import us.planet10.ai.ttt.service.GameService

@Tag(name = "Game API", description = "Endpoints for managing Tic Tac Toe games")
@RestController
@CrossOrigin(origins = ["http://localhost:8080"])
@RequestMapping("/api/game")
class GameController(
    private val gameService: GameService
) {
    @Schema(description = "Request object for making a move")
    data class MoveRequest(
        @field:Schema(description = "Row index (0-2)", example = "1")
        val row: Int,
        @field:Schema(description = "Column index (0-2)", example = "1")
        val col: Int
    )
    
    @Schema(description = "Response object containing game state")
    data class GameResponse(
        @field:Schema(description = "Unique game identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        val id: String,
        @field:Schema(description = "2D array representing the game board")
        val board: List<List<CellResponse>>,
        @field:Schema(description = "Current player's turn (X or O)", example = "X")
        val currentPlayer: String,
        @field:Schema(description = "Game status (NEW, IN_PROGRESS, COMPLETED, ABANDONED)", example = "IN_PROGRESS")
        val status: String,
        @field:Schema(description = "Winner of the game, if any", example = "X")
        val winner: String?,
        @field:Schema(description = "Details of the last move made")
        val lastMove: MoveResponse?
    )
    
    @Schema(description = "Response object for a cell on the board")
    data class CellResponse(
        @field:Schema(description = "Position on the board")
        val position: Position,
        @field:Schema(description = "Player occupying this cell (X, O, or null)", example = "X")
        val player: String?
    )
    
    @Schema(description = "Response object for a move")
    data class MoveResponse(
        @field:Schema(description = "Player who made the move (X or O)", example = "X")
        val player: String,
        @field:Schema(description = "Position of the move")
        val position: Position,
        @field:Schema(description = "Timestamp of when the move was made", example = "2024-02-19T10:15:30Z")
        val timestamp: String
    )
    
    data class ErrorResponse(
        val status: Int,
        val message: String,
        val timestamp: String = java.time.Instant.now().toString()
    )

    @Operation(summary = "Create a new game", description = "Creates a new Tic Tac Toe game and returns its initial state")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Game created successfully"),
        ApiResponse(responseCode = "500", description = "Internal server error", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    ])
    @PostMapping
    fun createGame(): ResponseEntity<GameResponse> {
        val state = gameService.createGame()
        return ResponseEntity.ok(state.toResponse())
    }

    @Operation(summary = "Get game state", description = "Retrieves the current state of a game by its ID")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Game state retrieved successfully"),
        ApiResponse(responseCode = "404", description = "Game not found"),
        ApiResponse(responseCode = "500", description = "Internal server error", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    ])
    @GetMapping("/{id}")
    fun getGame(@PathVariable id: String): ResponseEntity<GameResponse> {
        return try {
            val state = gameService.getGame(id)
            ResponseEntity.ok(state.toResponse())
        } catch (e: IllegalArgumentException) {
            ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(null)
        }
    }

    @Operation(summary = "Make a move", description = "Makes a move in the specified game")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Move made successfully"),
        ApiResponse(responseCode = "400", description = "Invalid move"),
        ApiResponse(responseCode = "404", description = "Game not found"),
        ApiResponse(responseCode = "409", description = "Game is already completed"),
        ApiResponse(responseCode = "500", description = "Internal server error", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    ])
    @PostMapping("/{id}/move")
    fun makeMove(
        @PathVariable id: String,
        @RequestBody move: MoveRequest
    ): ResponseEntity<GameResponse> {
        return try {
            val position = Position(move.row, move.col)
            val state = gameService.makeMove(id, position)
            ResponseEntity.ok(state.toResponse())
        } catch (e: IllegalArgumentException) {
            ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(null)
        } catch (e: IllegalStateException) {
            ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(null)
        }
    }

    @Operation(summary = "Request AI move", description = "Requests the AI to make a move in the specified game")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "AI move made successfully"),
        ApiResponse(responseCode = "404", description = "Game not found"),
        ApiResponse(responseCode = "409", description = "Game is already completed"),
        ApiResponse(responseCode = "500", description = "Internal server error", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    ])
    @PostMapping("/{id}/ai-move")
    fun makeAiMove(@PathVariable id: String): ResponseEntity<GameResponse> {
        return try {
            val state = gameService.makeAiMove(id)
            ResponseEntity.ok(state.toResponse())
        } catch (e: IllegalArgumentException) {
            ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(null)
        } catch (e: IllegalStateException) {
            ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(null)
        }
    }

    @Operation(summary = "Abandon game", description = "Marks a game as abandoned, preventing further moves")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Game abandoned successfully"),
        ApiResponse(responseCode = "404", description = "Game not found"),
        ApiResponse(responseCode = "500", description = "Internal server error", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    ])
    @PostMapping("/{id}/abandon")
    fun abandonGame(@PathVariable id: String): ResponseEntity<GameResponse> {
        return try {
            val state = gameService.abandonGame(id)
            ResponseEntity.ok(state.toResponse())
        } catch (e: IllegalArgumentException) {
            ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(null)
        }
    }

    /**
     * Global exception handler for the controller.
     */
    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        val status = when (e) {
            is IllegalArgumentException -> HttpStatus.BAD_REQUEST
            is IllegalStateException -> HttpStatus.CONFLICT
            else -> HttpStatus.INTERNAL_SERVER_ERROR
        }
        
        return ResponseEntity
            .status(status)
            .body(ErrorResponse(
                status = status.value(),
                message = e.message ?: "An error occurred"
            ))
    }

    /**
     * Converts a GameState to a GameResponse.
     */
    private fun GameState.toResponse(): GameResponse {
        return GameResponse(
            id = id,
            board = board.cells.map { row ->
                row.map { cell ->
                    CellResponse(
                        position = cell.position,
                        player = cell.player?.toString()
                    )
                }
            },
            currentPlayer = board.currentPlayer.toString(),
            status = status.toString(),
            winner = winner?.toString(),
            lastMove = lastMove?.let { move ->
                MoveResponse(
                    player = move.player.toString(),
                    position = move.position,
                    timestamp = move.timestamp.toString()
                )
            }
        )
    }
}
